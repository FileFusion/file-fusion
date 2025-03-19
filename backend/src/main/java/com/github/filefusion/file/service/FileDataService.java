package com.github.filefusion.file.service;

import com.github.filefusion.common.FileProperties;
import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.constant.SysConfigKey;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.FileHashUsageCountModel;
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

    private static void hashFormatCheck(String hash) {
        if (!StringUtils.hasLength(hash)) {
            throw new HttpException(I18n.get("fileHashEmpty"));
        }
        if (hash.length() != 64 || !hash.matches("^[a-zA-Z0-9]+$")) {
            throw new HttpException(I18n.get("fileHashFormatError"));
        }
    }

    private static void nameFormatCheck(String name) {
        if (!StringUtils.hasLength(name)) {
            throw new HttpException(I18n.get("fileNameEmpty"));
        }
        if (name.length() > 255 || name.equals(".") || name.equals("..")
                || !name.matches("[^\\\\/:*?\"<>|]+")) {
            throw new HttpException(I18n.get("fileNameFormatError"));
        }
    }

    private static void pathFormatCheck(String path) {
        if (!StringUtils.hasLength(path)) {
            throw new HttpException(I18n.get("filePathEmpty"));
        }
        if (path.length() > 4096 || path.contains(".") || path.contains("..") || path.startsWith("/")) {
            throw new HttpException(I18n.get("filePathFormatError"));
        }
        String[] parts = path.split(Pattern.quote(FileAttribute.SEPARATOR));
        for (String part : parts) {
            nameFormatCheck(part);
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
        List<FileData> childrenList = findAllChildren(file.getId());
        LinkedList<String> lockKeyList = childrenList.stream()
                .map(child -> userId + RedisAttribute.SEPARATOR + child.getPath())
                .collect(Collectors.toCollection(LinkedList::new));
        lockKeyList.addFirst(userId + RedisAttribute.SEPARATOR + file.getPath());
        distributedLock.tryMultiLock(RedisAttribute.LockType.file, lockKeyList, () -> {
            SysConfig config = sysConfigService.get(SysConfigKey.RECYCLE_BIN);
            if (Boolean.parseBoolean(config.getConfigValue())) {
                batchRecycle(file, childrenList);
            } else {
                batchDelete(file, childrenList);
            }
        }, fileProperties.getLockTimeout());
    }

    private void batchRecycle(FileData file, List<FileData> childrenList) {
        LocalDateTime deletedDate = LocalDateTime.now();
        file.setParentId(FileAttribute.RECYCLE_BIN_ROOT);
        List<FileData> allFiles = Stream.concat(Stream.of(file), childrenList.stream())
                .parallel().peek(f -> {
                    f.setDeleted(true);
                    f.setDeletedDate(deletedDate);
                }).toList();
        fileDataRepository.saveAll(allFiles);
    }

    private void batchDelete(FileData file, List<FileData> childrenList) {
        List<FileData> allFiles = new ArrayList<>(childrenList.size() + 1);
        allFiles.add(file);
        allFiles.addAll(childrenList);
        fileDataRepository.deleteAllInBatch(allFiles);
        List<String> hashList = allFiles.stream().map(FileData::getHashValue)
                .filter(StringUtils::hasLength).distinct().toList();
        if (!hashList.isEmpty()) {
            Map<String, Long> hashCounts = fileDataRepository.countByHashValueList(hashList)
                    .stream().collect(Collectors.toMap(
                            FileHashUsageCountModel::getHashValue,
                            FileHashUsageCountModel::getCount
                    ));
            List<Path> deletePaths = hashList.parallelStream()
                    .filter(hash -> hashCounts.getOrDefault(hash, 0L) <= 1)
                    .map(hash -> FileUtil.getHashPath(fileProperties.getDir(), hash)).toList();
            try {
                FileUtil.delete(deletePaths);
            } catch (FileUtil.FileDeletionFailedException e) {
                throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileDeleteFailed"));
            }
        }
    }

    public void createFolder(String userId, String parentId, String name) {
        nameFormatCheck(name);
        parentId = StringUtils.hasLength(parentId) ? parentId : FileAttribute.PARENT_ROOT;
        if (fileDataRepository.existsByUserIdAndParentIdAndName(userId, parentId, name)) {
            throw new HttpException(I18n.get("fileExits", name));
        }
        createHierarchicalFolders(userId, parentId, name, LocalDateTime.now());
    }

    private FileData createHierarchicalFolders(String userId, String parentId, String folder, LocalDateTime lastModifiedDate) {
        AtomicReference<String> currentParentId = new AtomicReference<>(parentId);
        StringBuilder parentPath;
        if (FileAttribute.PARENT_ROOT.equals(parentId)) {
            parentPath = new StringBuilder();
        } else {
            parentPath = new StringBuilder(fileDataRepository.findFirstByUserIdAndId(userId, parentId)
                    .map(FileData::getPath)
                    .orElseThrow(() -> new HttpException(I18n.get("fileNotExist"))));
        }

        String[] pathSegments = folder.split(Pattern.quote(FileAttribute.SEPARATOR));
        List<String> pathList = new ArrayList<>(pathSegments.length);
        for (String pathSegment : pathSegments) {
            if (!StringUtils.hasLength(pathSegment)) {
                continue;
            }
            if (!parentPath.isEmpty()) {
                parentPath.append(FileAttribute.SEPARATOR);
            }
            parentPath.append(pathSegment);
            pathList.add(parentPath.toString());
        }

        AtomicReference<FileData> lastCreatedFile = new AtomicReference<>();
        distributedLock.tryMultiLock(RedisAttribute.LockType.file,
                pathList.stream().map(rp -> userId + RedisAttribute.SEPARATOR + rp).toList(), () -> {
                    Map<String, FileData> existsFileMap = fileDataRepository.findAllByUserIdAndPathIn(userId, pathList)
                            .stream().collect(Collectors.toMap(FileData::getPath, Function.identity()));
                    for (String path : pathList) {
                        FileData file = existsFileMap.get(path);
                        if (file == null) {
                            file = new FileData();
                            file.setUserId(userId);
                            file.setParentId(currentParentId.get());
                            file.setName(Paths.get(path).getFileName().toString());
                            file.setPath(path);
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

    public boolean uploadChunkMerge(String userId, String parentId, String parentPath, String name, String hashValue,
                                    String mimeType, Long size, LocalDateTime lastModified, boolean fastUpload) {
        nameFormatCheck(name);
        hashFormatCheck(hashValue);
        parentId = StringUtils.hasLength(parentId) ? parentId : FileAttribute.PARENT_ROOT;
        LocalDateTime lastModifiedDate = lastModified == null ? LocalDateTime.now() : lastModified;
        String pId;
        String path;
        if (StringUtils.hasLength(parentPath)) {
            pathFormatCheck(parentPath);
            FileData parentFile = createHierarchicalFolders(userId, parentId, parentPath, lastModifiedDate);
            pId = parentFile.getId();
            path = parentFile.getPath() + FileAttribute.SEPARATOR + name;
        } else {
            if (!FileAttribute.PARENT_ROOT.equals(parentId) && !fileDataRepository.existsByUserIdAndId(userId, parentId)) {
                throw new HttpException(I18n.get("fileNotExist"));
            }
            pId = parentId;
            path = name;
        }
        AtomicBoolean uploadStatus = new AtomicBoolean(false);
        distributedLock.tryMultiLock(RedisAttribute.LockType.file, List.of(hashValue, userId + RedisAttribute.SEPARATOR + path), () -> {
            if (fileDataRepository.existsByUserIdAndParentIdAndName(userId, pId, name)) {
                throw new HttpException(I18n.get("fileExits", name));
            }
            try {
                uploadStatus.set(chunkMerge(hashValue, fastUpload));
            } catch (IOException e) {
                throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileUploadFailed"));
            }
            if (uploadStatus.get()) {
                FileData file = new FileData();
                file.setUserId(userId);
                file.setParentId(pId);
                file.setName(name);
                file.setPath(path);
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

    private boolean chunkMerge(String hashValue, boolean fastUpload) throws IOException {
        Path chunkDirPath = FileUtil.getHashPath(fileProperties.getTmpDir(), hashValue);
        Path filePath = FileUtil.getHashPath(fileProperties.getDir(), hashValue);
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
        hashFormatCheck(chunkHashValue);
        hashFormatCheck(hashValue);
        Path chunkDirPath = FileUtil.getHashPath(fileProperties.getTmpDir(), hashValue);
        Path chunkPath = chunkDirPath.resolve(String.valueOf(chunkIndex));
        distributedLock.tryLock(RedisAttribute.LockType.file, hashValue + RedisAttribute.SEPARATOR + chunkIndex, () -> {
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

    @Transactional(rollbackFor = HttpException.class)
    public void rename(String userId, String id, String name) {
        nameFormatCheck(name);

        FileData file = fileDataRepository.findFirstByUserIdAndId(userId, id)
                .orElseThrow(() -> new HttpException(I18n.get("fileNotExist")));
        file.setName(name);
        String sourcePath = file.getPath();
        Path parentPath = Paths.get(sourcePath).getParent();
        String targetPath;
        if (parentPath == null) {
            targetPath = name;
        } else {
            targetPath = parentPath.resolve(name).toString();
        }
        file.setPath(targetPath);

        List<FileData> childrenList = findAllChildren(file.getId());
        for (FileData children : childrenList) {
            String childrenPath = children.getPath().substring(sourcePath.length());
            children.setPath(targetPath + childrenPath);
        }
        childrenList.add(file);

        fileDataRepository.saveAll(childrenList);
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
        RList<String> downloadIdList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + downloadId);
        downloadIdList.addAll(allList.stream().map(FileData::getId).toList());
        downloadIdList.expire(fileProperties.getDownloadLinkTimeout());
        return downloadId;
    }

    public ResponseEntity<StreamingResponseBody> download(String downloadId) {
        RList<String> idList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + downloadId);
        if (CollectionUtils.isEmpty(idList)) {
            throw new HttpException(I18n.get("downloadLinkExpired"));
        }
        List<FileData> fileList = fileDataRepository.findAllById(idList);
        if (fileList.isEmpty()) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        FileData file = fileList.getFirst();
        if (fileList.size() == 1 && !FileAttribute.MimeType.FOLDER.value().toString().equals(file.getMimeType())) {
            return DownloadUtil.download(file.getName(), file.getMimeType(),
                    FileUtil.getHashPath(fileProperties.getDir(), file.getHashValue()));
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
                    FileUtil.getHashPath(fileProperties.getDir(), file.getHashValue()), start, end);
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
                    ThumbnailUtil.generateThumbnail(mimeType,
                            FileUtil.getHashPath(fileProperties.getDir(), file.getHashValue()),
                            FileUtil.getHashPath(fileProperties.getThumbnailDir(), file.getHashValue(), FileAttribute.THUMBNAIL_FILE_SUFFIX),
                            fileProperties.getThumbnailImageMimeType(), fileProperties.getThumbnailVideoMimeType(),
                            fileProperties.getThumbnailGenerateTimeout()));
        } catch (ThumbnailUtil.FileNotSupportThumbnailException e) {
            throw new HttpException(I18n.get("fileNotSupportThumbnail"));
        } catch (ThumbnailUtil.ThumbnailGenerationFailedException | IOException e) {
            throw new HttpException(I18n.get("thumbnailGenerationFailed"));
        }
    }

}
