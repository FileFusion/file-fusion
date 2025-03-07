package com.github.filefusion.file.service;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.constant.SysConfigKey;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.SubmitDownloadFilesResponse;
import com.github.filefusion.file.repository.FileDataRepository;
import com.github.filefusion.sys_config.entity.SysConfig;
import com.github.filefusion.sys_config.service.SysConfigService;
import com.github.filefusion.util.DistributedLock;
import com.github.filefusion.util.I18n;
import com.github.filefusion.util.ULID;
import com.github.filefusion.util.file.FileUtil;
import com.github.filefusion.util.file.PathUtil;
import com.github.filefusion.util.file.ThumbnailUtil;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * FileDataService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Service
public class FileDataService {

    private final Duration fileLockTimeout;
    private final Duration fileDownloadLinkTimeout;
    private final RedissonClient redissonClient;
    private final FileDataRepository fileDataRepository;
    private final DistributedLock distributedLock;
    private final SysConfigService sysConfigService;
    private final FileUtil fileUtil;
    private final ThumbnailUtil thumbnailUtil;

    @Autowired
    public FileDataService(@Value("${file.lock-timeout}") Duration fileLockTimeout,
                           @Value("${file.download-link-timeout}") Duration fileDownloadLinkTimeout,
                           RedissonClient redissonClient,
                           FileDataRepository fileDataRepository,
                           DistributedLock distributedLock,
                           SysConfigService sysConfigService,
                           FileUtil fileUtil,
                           ThumbnailUtil thumbnailUtil) {
        this.fileLockTimeout = fileLockTimeout;
        this.fileDownloadLinkTimeout = fileDownloadLinkTimeout;
        this.redissonClient = redissonClient;
        this.fileDataRepository = fileDataRepository;
        this.distributedLock = distributedLock;
        this.sysConfigService = sysConfigService;
        this.fileUtil = fileUtil;
        this.thumbnailUtil = thumbnailUtil;
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
                fileData.setHasThumbnail(thumbnailUtil.hasThumbnail(fileData.getMimeType()))
        );
        return fileDataPage;
    }

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
        }, fileLockTimeout);
        return id.toString();
    }

    public void upload(String userId, MultipartFile multipartFile, String parentId, String name, String path,
                       String hashValue, String mimeType, Long size, LocalDateTime lastModified) {
        if (!StringUtils.hasLength(name)) {
            throw new HttpException(I18n.get("fileNameEmpty"));
        }
        if (!StringUtils.hasLength(hashValue)) {
            throw new HttpException(I18n.get("fileHashEmpty"));
        }
        LocalDateTime lastModifiedDate = lastModified == null ? LocalDateTime.now() : lastModified;
        String filePath = PathUtil.hashToPath(hashValue).toString();
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
            file.setPath(filePath);
            file.setHashValue(hashValue);
            file.setMimeType(mimeType);
            file.setSize(size);
            file.setEncrypted(false);
            file.setFileLastModifiedDate(lastModifiedDate);
            file.setDeleted(false);
            fileDataRepository.save(file);
            fileUtil.upload(multipartFile, PathUtil.resolvePath(fileUtil.getBaseDir(), file.getPath()));
        }, fileLockTimeout);
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

    public SubmitDownloadFilesResponse submitDownload(String userId, List<String> idList) {
        List<FileData> fileList = fileDataRepository.findAllByUserIdAndIdIn(userId, idList);
        if (fileList.isEmpty() || fileList.size() != idList.size()) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        List<FileData> allList = new ArrayList<>(fileList);
        fileList.forEach(file -> allList.addAll(findAllChildren(file.getId())));
        String downloadId = ULID.randomULID();
        RList<String> idRList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + RedisAttribute.SEPARATOR + downloadId);
        idRList.addAll(allList.stream().map(FileData::getId).toList());
        idRList.expire(fileDownloadLinkTimeout);
        return new SubmitDownloadFilesResponse(downloadId);
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
            return fileUtil.download(file.getName(), file.getMimeType(),
                    PathUtil.resolvePath(fileUtil.getBaseDir(), file.getPath()));
        }
        return fileUtil.downloadZip(fileList);
    }

    public ResponseEntity<StreamingResponseBody> downloadChunked(String userId, String id, String range) {
        FileData file = fileDataRepository.findFirstByUserIdAndId(userId, id);
        if (file == null) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        String[] ranges = range.replace("bytes=", "").split("-");
        long start = 0;
        long end = -1;
        if (ranges.length > 0) {
            start = Long.parseLong(ranges[0]);
        }
        if (ranges.length > 1) {
            end = Long.parseLong(ranges[1]);
        }
        return fileUtil.downloadChunked(file.getName(), file.getMimeType(),
                PathUtil.resolvePath(fileUtil.getBaseDir(), file.getPath()), start, end);
    }

    public ResponseEntity<StreamingResponseBody> thumbnail(String userId, String id) {
        FileData file = fileDataRepository.findFirstByUserIdAndId(userId, id);
        if (file == null) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        String mimeType = file.getMimeType();
        if (!thumbnailUtil.hasThumbnail(mimeType)) {
            throw new HttpException(I18n.get("fileNotSupportThumbnail"));
        }
        return fileUtil.download(FileAttribute.DOWNLOAD_THUMBNAIL_NAME, FileAttribute.THUMBNAIL_FILE_MIME_TYPE,
                thumbnailUtil.generateThumbnail(
                        PathUtil.resolvePath(fileUtil.getBaseDir(), file.getPath()), file.getPath(), mimeType));
    }

}
