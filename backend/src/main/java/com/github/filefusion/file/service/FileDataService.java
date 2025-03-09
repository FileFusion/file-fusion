package com.github.filefusion.file.service;

import com.github.filefusion.common.FileProperties;
import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.constant.SysConfigKey;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.repository.FileDataRepository;
import com.github.filefusion.sys_config.entity.SysConfig;
import com.github.filefusion.sys_config.service.SysConfigService;
import com.github.filefusion.util.DistributedLock;
import com.github.filefusion.util.I18n;
import com.github.filefusion.util.ULID;
import com.github.filefusion.util.file.DownloadUtil;
import com.github.filefusion.util.file.FileUtil;
import com.github.filefusion.util.file.ThumbnailUtil;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * FileDataService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Service
public class FileDataService {

    private final RedissonClient redissonClient;
    private final DistributedLock distributedLock;
    private final FileProperties fileProperties;
    private final FileDataRepository fileDataRepository;
    private final SysConfigService sysConfigService;

    @Autowired
    public FileDataService(RedissonClient redissonClient,
                           DistributedLock distributedLock,
                           FileProperties fileProperties,
                           FileDataRepository fileDataRepository,
                           SysConfigService sysConfigService) {
        this.redissonClient = redissonClient;
        this.distributedLock = distributedLock;
        this.fileProperties = fileProperties;
        this.fileDataRepository = fileDataRepository;
        this.sysConfigService = sysConfigService;
    }

    private List<FileData> findAllChildren(String id) {
        List<FileData> children = new ArrayList<>();
        findAllChildren(id, children);
        return children;
    }

    private void findAllChildren(String parentId, List<FileData> children) {
        List<FileData> currentChildren = fileDataRepository.findAllByParentId(parentId);
        if (!currentChildren.isEmpty()) {
            children.addAll(currentChildren);
            currentChildren.forEach(child -> {
                if (FileAttribute.MimeType.FOLDER.value().toString().equals(child.getMimeType())) {
                    findAllChildren(child.getId(), children);
                }
            });
        }
    }

    private List<String> getHierarchyPathList(String path) {
        Path rootPath = Paths.get(path).normalize();
        List<String> hierarchyPathList = new ArrayList<>(rootPath.getNameCount());
        for (int i = 0; i < rootPath.getNameCount(); i++) {
            hierarchyPathList.add(rootPath.getName(i).toString());
        }
        return hierarchyPathList;
    }

    public Page<FileData> get(PageRequest page, String userId, String parentId) {
        if (!StringUtils.hasLength(parentId)) {
            parentId = FileAttribute.PARENT_ROOT;
        }
        Page<FileData> fileDataPage = fileDataRepository.findAllByUserIdAndParentIdAndDeletedFalse(
                userId, parentId, page);
        fileDataPage.getContent().forEach(fileData ->
                fileData.setHasThumbnail(ThumbnailUtil.hasThumbnail(fileData.getMimeType(),
                        fileProperties.getThumbnailImageMimeType(),
                        fileProperties.getThumbnailVideoMimeType())
                )
        );
        return fileDataPage;
    }

    @Transactional(rollbackFor = HttpException.class)
    public void recycleOrDelete(String userId, String id) {
        FileData file = fileDataRepository.findFirstByUserIdAndId(userId, id);
        if (file == null) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        List<FileData> allList = findAllChildren(file.getId());
        allList.addFirst(file);
        SysConfig config = sysConfigService.get(SysConfigKey.RECYCLE_BIN);
        if (Boolean.parseBoolean(config.getConfigValue())) {
            batchRecycle(allList);
        } else {
            batchDelete(allList);
        }
    }

    private void batchRecycle(List<FileData> fileList) {
        LocalDateTime deletedDate = LocalDateTime.now();
        fileList.forEach(file -> {
            file.setDeleted(true);
            file.setDeletedDate(deletedDate);
        });
        fileDataRepository.saveAll(fileList);
    }

    private void batchDelete(List<FileData> fileList) {
        fileDataRepository.deleteAll(fileList);
    }

    public String createFolder(String userId, String parentId, String name, LocalDateTime lastModifiedDate, boolean allowExists) {
        if (!StringUtils.hasLength(name)) {
            throw new HttpException(I18n.get("fileNameEmpty"));
        }
        StringBuffer id = new StringBuffer();
        String pId = !StringUtils.hasLength(parentId) ? FileAttribute.PARENT_ROOT : parentId;
        distributedLock.tryLock(RedisAttribute.LockType.file, userId + pId + name, () -> {
            FileData file = fileDataRepository.findFirstByUserIdAndParentIdAndName(userId, pId, name);
            if (!allowExists && file != null) {
                throw new HttpException(I18n.get("fileExits", name));
            }
            if (file != null) {
                id.append(file.getId());
                return;
            }
            file = new FileData();
            file.setUserId(userId);
            file.setParentId(pId);
            file.setName(name);
            file.setMimeType(FileAttribute.MimeType.FOLDER.value().toString());
            file.setSize(0L);
            file.setEncrypted(false);
            file.setFileLastModifiedDate(lastModifiedDate);
            file.setDeleted(false);
            fileDataRepository.save(file);
            id.append(file.getId());
        }, fileProperties.getLockTimeout());
        return id.toString();
    }

    public void uploadChunk(MultipartFile file, Integer chunkIndex, String chunkHashValue, String hashValue) {
        Path chunkDirPath = fileProperties.getTmpDir().resolve(FileUtil.getHashPath(hashValue));
        Path chunkPath = chunkDirPath.resolve(String.valueOf(chunkIndex));
        distributedLock.tryLock(RedisAttribute.LockType.file, hashValue + chunkIndex, () -> {
            if (Files.exists(chunkPath)) {
                if (chunkHashValue.equals(FileUtil.calculateMd5(chunkPath))) {
                    return;
                } else {
                    FileUtil.delete(chunkPath);
                }
            }
            FileUtil.upload(file, chunkPath);
            if (!chunkHashValue.equals(FileUtil.calculateMd5(chunkPath))) {
                FileUtil.delete(chunkPath);
                throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileUploadFailed"));
            }
        }, fileProperties.getLockTimeout());
    }

    @Transactional(rollbackFor = HttpException.class)
    public boolean uploadChunkMerge(String userId, String parentId, String name, String path, String hashValue,
                                    String mimeType, Long size, LocalDateTime lastModified, boolean fastUpload) {
        if (!StringUtils.hasLength(name)) {
            throw new HttpException(I18n.get("fileNameEmpty"));
        }
        if (!StringUtils.hasLength(hashValue)) {
            throw new HttpException(I18n.get("fileHashEmpty"));
        }
        if (StringUtils.hasLength(path) && (path.contains("..") || path.contains("//") || path.startsWith("/"))) {
            throw new HttpException(I18n.get("fileHashFormatError"));
        }
        String hashPath = FileUtil.getHashPath(hashValue);
        LocalDateTime lastModifiedDate = lastModified == null ? LocalDateTime.now() : lastModified;
        String pId = !StringUtils.hasLength(parentId) ? FileAttribute.PARENT_ROOT : parentId;
        distributedLock.tryLock(RedisAttribute.LockType.file, userId + pId + path + name, () -> {
            String fileParentId = pId;
            if (StringUtils.hasLength(path)) {
                List<String> hierarchyPathList = getHierarchyPathList(path);
                for (String hierarchyPath : hierarchyPathList) {
                    fileParentId = createFolder(userId, fileParentId, hierarchyPath, lastModifiedDate, true);
                }
            }
            if (fileDataRepository.existsByUserIdAndParentIdAndName(userId, fileParentId, name)) {
                throw new HttpException(I18n.get("fileExits", name));
            }
            FileData file = new FileData();
            file.setUserId(userId);
            file.setParentId(fileParentId);
            file.setName(name);
            file.setPath(hashPath);
            file.setHashValue(hashValue);
            file.setMimeType(mimeType);
            file.setSize(size);
            file.setEncrypted(false);
            file.setFileLastModifiedDate(lastModifiedDate);
            file.setDeleted(false);
            fileDataRepository.save(file);
        }, fileProperties.getLockTimeout());

        return chunkMerge(hashPath, hashValue, fastUpload);
    }

    private boolean chunkMerge(String hashPath, String hashValue, boolean fastUpload) {
        Path chunkDirPath = fileProperties.getTmpDir().resolve(hashPath);
        Path filePath = fileProperties.getDir().resolve(hashPath);
        AtomicBoolean uploadStatus = new AtomicBoolean(false);
        distributedLock.tryLock(RedisAttribute.LockType.file, hashValue, () -> {
            if (Files.exists(filePath)) {
                if (hashValue.equals(FileUtil.calculateMd5(filePath))) {
                    uploadStatus.set(true);
                    return;
                } else {
                    FileUtil.delete(filePath);
                }
            }
            if (fastUpload) {
                return;
            }
            FileUtil.chunkMerge(chunkDirPath, filePath);
            if (!hashValue.equals(FileUtil.calculateMd5(filePath))) {
                FileUtil.delete(filePath);
                throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileUploadFailed"));
            }
            uploadStatus.set(true);
        }, fileProperties.getLockTimeout());
        if (!uploadStatus.get()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return uploadStatus.get();
    }

    public void rename(String userId, String id, String name) {
        if (!StringUtils.hasLength(name)) {
            throw new HttpException(I18n.get("fileNameEmpty"));
        }
        FileData file = fileDataRepository.findFirstByUserIdAndId(userId, id);
        if (file == null) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        file.setName(name);
        fileDataRepository.save(file);
    }

    public String submitDownload(String userId, List<String> idList) {
        List<FileData> fileList = fileDataRepository.findAllByUserIdAndIdIn(userId, idList);
        if (fileList.isEmpty() || fileList.size() != idList.size()) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        List<FileData> allList = new ArrayList<>(fileList);
        fileList.forEach(file -> allList.addAll(findAllChildren(file.getId())));
        String downloadId = ULID.randomULID();
        RList<String> idRList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + RedisAttribute.SEPARATOR + downloadId);
        idRList.addAll(allList.stream().map(FileData::getId).toList());
        idRList.expire(fileProperties.getDownloadLinkTimeout());
        return downloadId;
    }

    public ResponseEntity<StreamingResponseBody> download(String downloadId) {
        RList<String> idList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + RedisAttribute.SEPARATOR + downloadId);
        if (CollectionUtils.isEmpty(idList)) {
            throw new HttpException(I18n.get("downloadLinkExpired"));
        }
        List<FileData> fileList = fileDataRepository.findAllById(idList);
        if (fileList.isEmpty()) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        FileData file = fileList.getFirst();
        if (fileList.size() == 1 && !FileAttribute.MimeType.FOLDER.value().toString().equals(file.getMimeType())) {
            return DownloadUtil.download(file.getName(), file.getMimeType(), fileProperties.getDir().resolve(file.getPath()));
        }
        return DownloadUtil.downloadZip(fileProperties.getDir(), fileList);
    }

    public ResponseEntity<StreamingResponseBody> downloadChunked(String userId, String id, String range) {
        FileData file = fileDataRepository.findFirstByUserIdAndId(userId, id);
        if (file == null) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        String[] ranges = range.replace("bytes=", "").split("-");
        long start = 0L;
        long end = Long.MAX_VALUE;
        if (ranges.length > 0) {
            start = Long.parseLong(ranges[0]);
        }
        if (ranges.length > 1) {
            end = Long.parseLong(ranges[1]);
        }
        return DownloadUtil.downloadChunked(file.getName(), file.getMimeType(),
                fileProperties.getDir().resolve(file.getPath()), start, end);
    }

    public ResponseEntity<StreamingResponseBody> thumbnail(String userId, String id) {
        FileData file = fileDataRepository.findFirstByUserIdAndId(userId, id);
        if (file == null) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        String mimeType = file.getMimeType();
        if (!ThumbnailUtil.hasThumbnail(mimeType, fileProperties.getThumbnailImageMimeType(),
                fileProperties.getThumbnailVideoMimeType())) {
            throw new HttpException(I18n.get("fileNotSupportThumbnail"));
        }
        return DownloadUtil.download(FileAttribute.DOWNLOAD_THUMBNAIL_NAME, FileAttribute.THUMBNAIL_FILE_MIME_TYPE,
                ThumbnailUtil.generateThumbnail(fileProperties.getThumbnailDir(),
                        fileProperties.getDir().resolve(file.getPath()), file.getPath(), mimeType,
                        fileProperties.getThumbnailImageMimeType(), fileProperties.getThumbnailVideoMimeType(),
                        fileProperties.getThumbnailGenerateTimeout()));
    }

}
