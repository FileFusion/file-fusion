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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static String getHashPath(String hash) {
        if (!StringUtils.hasLength(hash)) {
            throw new HttpException(I18n.get("fileHashEmpty"));
        }
        if (hash.length() != 64 || !hash.matches("^[a-zA-Z0-9]+$")) {
            throw new HttpException(I18n.get("fileHashFormatError"));
        }
        return Paths.get(hash.substring(0, 2), hash.substring(2, 4), hash).toString();
    }

    private static void nameFormatCheck(String name) {
        if (!StringUtils.hasLength(name)) {
            throw new HttpException(I18n.get("fileNameEmpty"));
        }
        if (name.contains("//") || name.startsWith("/")) {
            throw new HttpException(I18n.get("fileNameFormatError"));
        }
    }

    private List<FileData> findAllChildren(String id) {
        List<FileData> children = new ArrayList<>();
        Queue<String> queue = new ArrayDeque<>();
        queue.add(id);
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<String> currentLevelParentIds = new ArrayList<>(levelSize);
            for (int i = 0; i < levelSize; i++) {
                currentLevelParentIds.add(queue.poll());
            }
            List<FileData> currentChildren = fileDataRepository.findAllByParentIdIn(currentLevelParentIds);
            children.addAll(currentChildren);
            currentChildren.stream()
                    .filter(child -> FileAttribute.MimeType.FOLDER.value().toString().equals(child.getMimeType()))
                    .forEach(child -> queue.add(child.getId()));
        }
        return children;
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
        FileData file = fileDataRepository.findFirstByUserIdAndId(userId, id)
                .orElseThrow(() -> new HttpException(I18n.get("fileNotExist")));
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

    public void createFolder(String userId, String parentId, String name) {
        nameFormatCheck(name);
        if (fileDataRepository.existsByUserIdAndParentIdAndName(userId, parentId, name)) {
            throw new HttpException(I18n.get("fileExits", name));
        }
        createHierarchicalFolders(userId, parentId, name, LocalDateTime.now());
    }

    private FileData createHierarchicalFolders(String userId, String parentId, String path, LocalDateTime lastModifiedDate) {
        AtomicReference<String> currentParentId = new AtomicReference<>();
        StringBuilder parentPath;
        if (!StringUtils.hasLength(parentId)) {
            currentParentId.set(FileAttribute.PARENT_ROOT);
            parentPath = new StringBuilder();
        } else {
            currentParentId.set(parentId);
            parentPath = new StringBuilder(fileDataRepository.findFirstByUserIdAndId(userId, parentId)
                    .map(FileData::getRelativePath)
                    .orElseThrow(() -> new HttpException(I18n.get("fileNotExist"))));
        }

        String[] pathSegments = path.split(Pattern.quote(File.separator));
        List<String> relativePathList = new ArrayList<>(pathSegments.length);
        for (String pathSegment : pathSegments) {
            if (!StringUtils.hasLength(pathSegment)) {
                continue;
            }
            if (!parentPath.isEmpty()) {
                parentPath.append(File.separator);
            }
            parentPath.append(pathSegment);
            relativePathList.add(parentPath.toString());
        }

        AtomicReference<FileData> lastCreatedFile = new AtomicReference<>();
        distributedLock.tryMultiLock(RedisAttribute.LockType.file,
                relativePathList.stream().map(rp -> userId + rp).toList(), () -> {
                    Map<String, FileData> existsFileMap = fileDataRepository.findAllByUserIdAndRelativePathIn(userId, relativePathList)
                            .stream().collect(Collectors.toMap(FileData::getRelativePath, Function.identity()));
                    for (String relativePath : relativePathList) {
                        FileData file = existsFileMap.get(relativePath);
                        if (file == null) {
                            file = new FileData();
                            file.setUserId(userId);
                            file.setParentId(currentParentId.get());
                            file.setName(Paths.get(relativePath).getFileName().toString());
                            file.setRelativePath(relativePath);
                            file.setMimeType(FileAttribute.MimeType.FOLDER.value().toString());
                            file.setSize(0L);
                            file.setEncrypted(false);
                            file.setFileLastModifiedDate(lastModifiedDate);
                            file.setDeleted(false);
                            fileDataRepository.save(file);
                        }
                        currentParentId.set(file.getId());
                        lastCreatedFile.set(file);
                    }
                }, fileProperties.getLockTimeout());
        return lastCreatedFile.get();
    }

    public boolean uploadChunkMerge(String userId, String parentId, String name, String path, String hashValue,
                                    String mimeType, Long size, LocalDateTime lastModified, boolean fastUpload) {
        nameFormatCheck(name);
        String hashPath = getHashPath(hashValue);
        LocalDateTime lastModifiedDate = lastModified == null ? LocalDateTime.now() : lastModified;
        String pId;
        String relativePath;
        if (StringUtils.hasLength(path)) {
            nameFormatCheck(path);
            FileData parentFile = createHierarchicalFolders(userId, parentId, path, lastModifiedDate);
            pId = parentFile.getId();
            relativePath = parentFile.getRelativePath() + File.separator + name;
        } else {
            pId = FileAttribute.PARENT_ROOT;
            relativePath = name;
        }
        AtomicBoolean uploadStatus = new AtomicBoolean(false);
        distributedLock.tryMultiLock(RedisAttribute.LockType.file, List.of(hashValue, userId + relativePath), () -> {
            if (fileDataRepository.existsByUserIdAndParentIdAndName(userId, pId, name)) {
                throw new HttpException(I18n.get("fileExits", name));
            }
            try {
                uploadStatus.set(chunkMerge(hashPath, hashValue, fastUpload));
            } catch (IOException e) {
                throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileUploadFailed"));
            }
            if (uploadStatus.get()) {
                FileData file = new FileData();
                file.setUserId(userId);
                file.setParentId(pId);
                file.setName(name);
                file.setRelativePath(relativePath);
                file.setPath(hashPath);
                file.setHashValue(hashValue);
                file.setMimeType(mimeType);
                file.setSize(size);
                file.setEncrypted(false);
                file.setFileLastModifiedDate(lastModifiedDate);
                file.setDeleted(false);
                fileDataRepository.save(file);
            }
        }, fileProperties.getLockTimeout());
        return uploadStatus.get();
    }

    private boolean chunkMerge(String hashPath, String hashValue, boolean fastUpload) throws IOException {
        Path chunkDirPath = fileProperties.getTmpDir().resolve(hashPath);
        Path filePath = fileProperties.getDir().resolve(hashPath);
        if (hashValue.equals(FileUtil.calculateHash(filePath))) {
            return true;
        }
        if (fastUpload) {
            return false;
        }
        try {
            FileUtil.chunkMerge(chunkDirPath, filePath);
        } catch (IOException e) {
            return false;
        }
        if (hashValue.equals(FileUtil.calculateHash(filePath))) {
            return true;
        } else {
            FileUtil.delete(filePath);
        }
        return false;
    }

    public void uploadChunk(MultipartFile file, Integer chunkIndex, String chunkHashValue, String hashValue) {
        Path chunkDirPath = fileProperties.getTmpDir().resolve(getHashPath(hashValue));
        Path chunkPath = chunkDirPath.resolve(String.valueOf(chunkIndex));
        distributedLock.tryLock(RedisAttribute.LockType.file, hashValue + chunkIndex, () -> {
            try {
                if (Files.exists(chunkPath)) {
                    if (chunkHashValue.equals(FileUtil.calculateHash(chunkPath))) {
                        return;
                    } else {
                        FileUtil.delete(chunkPath);
                    }
                }
                Files.createDirectories(chunkPath.getParent());
                try {
                    file.transferTo(chunkPath);
                    if (!chunkHashValue.equals(FileUtil.calculateHash(chunkPath))) {
                        throw new IOException();
                    }
                } catch (IOException e) {
                    FileUtil.delete(chunkPath);
                    throw new IOException(e);
                }
            } catch (IOException e) {
                throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileUploadFailed"));
            }
        }, fileProperties.getLockTimeout());
    }

    public void rename(String userId, String id, String name) {
        if (!StringUtils.hasLength(name)) {
            throw new HttpException(I18n.get("fileNameEmpty"));
        }
        FileData file = fileDataRepository.findFirstByUserIdAndId(userId, id)
                .orElseThrow(() -> new HttpException(I18n.get("fileNotExist")));
        file.setName(name);
        fileDataRepository.save(file);
    }

    public String submitDownload(String userId, List<String> idList) {
        List<FileData> fileList = fileDataRepository.findAllByUserIdAndIdIn(userId, idList);
        if (fileList.isEmpty() || fileList.size() != idList.size()) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        List<FileData> allList = fileList.stream()
                .flatMap(file -> Stream.concat(Stream.of(file), findAllChildren(file.getId()).stream()))
                .toList();
        String downloadId = ULID.randomULID();
        RList<String> downloadIdList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + RedisAttribute.SEPARATOR + downloadId);
        downloadIdList.addAll(allList.stream().map(FileData::getId).toList());
        downloadIdList.expire(fileProperties.getDownloadLinkTimeout());
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
        FileData file = fileDataRepository.findFirstByUserIdAndId(userId, id)
                .orElseThrow(() -> new HttpException(I18n.get("fileNotExist")));
        String[] ranges = range.replace("bytes=", "").split("-");
        long start = 0L;
        long end = Long.MAX_VALUE;
        if (ranges.length > 0) {
            start = Long.parseLong(ranges[0]);
        }
        if (ranges.length > 1) {
            end = Long.parseLong(ranges[1]);
        }
        try {
            return DownloadUtil.downloadChunked(file.getName(), file.getMimeType(),
                    fileProperties.getDir().resolve(file.getPath()), start, end);
        } catch (IOException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileDownloadFailed"));
        }
    }

    public ResponseEntity<StreamingResponseBody> thumbnail(String userId, String id) {
        FileData file = fileDataRepository.findFirstByUserIdAndId(userId, id)
                .orElseThrow(() -> new HttpException(I18n.get("fileNotExist")));
        String mimeType = file.getMimeType();
        if (!ThumbnailUtil.hasThumbnail(mimeType, fileProperties.getThumbnailImageMimeType(),
                fileProperties.getThumbnailVideoMimeType())) {
            throw new HttpException(I18n.get("fileNotSupportThumbnail"));
        }
        try {
            return DownloadUtil.download(FileAttribute.DOWNLOAD_THUMBNAIL_NAME, FileAttribute.THUMBNAIL_FILE_MIME_TYPE,
                    ThumbnailUtil.generateThumbnail(fileProperties.getThumbnailDir(),
                            fileProperties.getDir().resolve(file.getPath()), file.getPath(), mimeType,
                            fileProperties.getThumbnailImageMimeType(), fileProperties.getThumbnailVideoMimeType(),
                            fileProperties.getThumbnailGenerateTimeout()));
        } catch (ThumbnailUtil.FileNotSupportThumbnailException e) {
            throw new HttpException(I18n.get("fileNotSupportThumbnail"));
        } catch (ThumbnailUtil.ThumbnailGenerationFailedException | IOException e) {
            throw new HttpException(I18n.get("thumbnailGenerationFailed"));
        }
    }

}
