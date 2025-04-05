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
import com.github.filefusion.util.file.MediaUtil;
import com.github.filefusion.util.file.ThumbnailUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
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
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
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
@Slf4j
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
            List<FileData> currentChildren = fileDataRepository.findAllByParentIdInAndDeletedFalse(currentLevelParentIds);
            children.addAll(currentChildren);
            currentChildren.stream()
                    .filter(child -> FileAttribute.MimeType.FOLDER.value().toString().equals(child.getMimeType()))
                    .forEach(child -> queue.add(child.getId()));
        }
        return children;
    }

    public Page<FileData> get(PageRequest page, String userId, String parentId, String name, boolean deleted) {
        if (!StringUtils.hasLength(parentId)) {
            parentId = FileAttribute.PARENT_ROOT;
        }
        if (StringUtils.hasLength(name)) {
            name = "%" + name + "%";
        } else {
            name = "%";
        }
        Page<FileData> fileDataPage = fileDataRepository.findAllByUserIdAndParentIdAndNameLikeAndDeleted(
                userId, parentId, name, deleted, page);
        fileDataPage.getContent().forEach(fileData -> {
                    fileData.setHasThumbnail(ThumbnailUtil.hasThumbnail(fileData.getMimeType(),
                            fileProperties.getThumbnailImageMimeType(),
                            fileProperties.getThumbnailVideoMimeType())
                    );
                    fileData.setCanPlay(MediaUtil.isDashSupported(fileData.getMimeType(),
                            fileProperties.getVideoPlayMimeType())
                    );
                }
        );
        return fileDataPage;
    }

    public List<FileData> getAllParent(String userId, String id) {
        FileData fileData = fileDataRepository.findFirstByUserIdAndIdAndDeletedFalse(userId, id)
                .orElseThrow(() -> new HttpException(I18n.get("fileNotExist")));
        List<FileData> parentList = new ArrayList<>();
        parentList.add(fileData);
        while (!FileAttribute.PARENT_ROOT.equals(fileData.getParentId())) {
            fileData = fileDataRepository.findFirstByUserIdAndIdAndDeletedFalse(userId, fileData.getParentId())
                    .orElseThrow(() -> new HttpException(I18n.get("fileNotExist")));
            parentList.add(fileData);
        }
        return parentList;
    }

    @Transactional(rollbackFor = HttpException.class)
    public void recycleOrDelete(String userId, String id) {
        FileData file = fileDataRepository.findFirstByUserIdAndIdAndDeletedFalse(userId, id)
                .orElseThrow(() -> new HttpException(I18n.get("fileNotExist")));
        List<FileData> childrenList = findAllChildren(file.getId());

        List<String> lockKeyList = new ArrayList<>(List.of(
                userId + RedisAttribute.SEPARATOR + file.getPath()
        ));
        childrenList.stream()
                .map(child -> userId + RedisAttribute.SEPARATOR + child.getPath())
                .forEach(lockKeyList::add);

        SysConfig config = sysConfigService.get(SysConfigKey.RECYCLE_BIN);
        if (Boolean.parseBoolean(config.getConfigValue())) {
            distributedLock.tryMultiLock(RedisAttribute.LockType.file, lockKeyList,
                    () -> batchRecycle(file, childrenList), fileProperties.getLockTimeout());
        } else {
            lockKeyList.add(file.getHashValue());
            childrenList.stream().map(FileData::getHashValue).forEach(lockKeyList::add);
            distributedLock.tryMultiLock(RedisAttribute.LockType.file, lockKeyList,
                    () -> batchDelete(file, childrenList), fileProperties.getLockTimeout());
        }
    }

    private void batchRecycle(FileData file, List<FileData> childrenList) {
        LocalDateTime deletedDate = LocalDateTime.now();
        file.setParentId(FileAttribute.RECYCLE_BIN_ROOT);
        List<FileData> allFiles = Stream.concat(Stream.of(file), childrenList.stream()).peek(f -> {
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
        if (hashList.isEmpty()) {
            return;
        }
        Map<String, Long> hashCounts = fileDataRepository.countByHashValueList(hashList)
                .stream().collect(Collectors.toMap(
                        FileHashUsageCountModel::getHashValue,
                        FileHashUsageCountModel::getCount
                ));
        List<Path> deletePaths = hashList.stream()
                .filter(hash -> hashCounts.getOrDefault(hash, 0L) == 0)
                .map(hash -> FileUtil.getHashPath(fileProperties.getDir(), hash)).toList();
        try {
            FileUtil.delete(deletePaths);
        } catch (FileUtil.FileDeletionFailedException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileDeleteFailed"));
        }
    }

    public List<FileData> getFolderList(String userId, String parentId) {
        if (!StringUtils.hasLength(parentId)) {
            parentId = FileAttribute.PARENT_ROOT;
        }
        return fileDataRepository.findAllByUserIdAndParentIdAndMimeTypeAndDeletedFalse(
                userId, parentId, FileAttribute.MimeType.FOLDER.value().toString());
    }

    public void createFolder(String userId, String parentId, String name) {
        nameFormatCheck(name);
        parentId = StringUtils.hasLength(parentId) ? parentId : FileAttribute.PARENT_ROOT;
        if (fileDataRepository.existsByUserIdAndParentIdAndNameAndDeletedFalse(userId, parentId, name)) {
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
            parentPath = new StringBuilder(fileDataRepository.findFirstByUserIdAndIdAndDeletedFalse(userId, parentId)
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
                    Map<String, FileData> existsFileMap = fileDataRepository.findAllByUserIdAndPathInAndDeletedFalse(userId, pathList)
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
            if (!FileAttribute.PARENT_ROOT.equals(parentId) && !fileDataRepository.existsByUserIdAndIdAndDeletedFalse(userId, parentId)) {
                throw new HttpException(I18n.get("fileNotExist"));
            }
            pId = parentId;
            path = name;
        }
        AtomicBoolean uploadStatus = new AtomicBoolean(false);
        distributedLock.tryMultiLock(RedisAttribute.LockType.file, List.of(hashValue, userId + RedisAttribute.SEPARATOR + path), () -> {
            if (fileDataRepository.existsByUserIdAndParentIdAndNameAndDeletedFalse(userId, pId, name)) {
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

                RBlockingDeque<FileData> queue = redissonClient.getBlockingDeque(RedisAttribute.EVENT_PREFIX + RedisAttribute.EventType.file_upload_success);
                queue.offerFirst(file);
            }
        }, fileProperties.getLockTimeout());
        return uploadStatus.get();
    }

    private boolean chunkMerge(String hashValue, boolean fastUpload) throws IOException {
        Path chunkDirPath = FileUtil.getHashPath(fileProperties.getUploadDir(), hashValue);
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
        Path chunkDirPath = FileUtil.getHashPath(fileProperties.getUploadDir(), hashValue);
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
                        log.error("Error uploading chunk");
                        throw new IOException();
                    }
                } catch (IOException e) {
                    log.error("Error uploading chunk", e);
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
        move(userId, id, null, name);
    }

    @Transactional(rollbackFor = HttpException.class)
    public void move(String userId, String sourceId, String targetId, String name) {
        if (sourceId.equals(targetId)) {
            throw new HttpException(I18n.get("fileCannotMoveItself"));
        }
        FileData sourceFile = fileDataRepository.findFirstByUserIdAndIdAndDeletedFalse(userId, sourceId)
                .orElseThrow(() -> new HttpException(I18n.get("fileNotExist")));
        if (!StringUtils.hasLength(targetId)) {
            targetId = sourceFile.getParentId();
        }
        if (!StringUtils.hasLength(name)) {
            name = sourceFile.getName();
        }
        if (fileDataRepository.existsByUserIdAndParentIdAndNameAndDeletedFalse(userId, targetId, name)) {
            throw new HttpException(I18n.get("fileExits", sourceFile.getName()));
        }
        String sourcePath = sourceFile.getPath();
        String targetPath;
        if (FileAttribute.PARENT_ROOT.equals(targetId)) {
            targetPath = name;
        } else {
            FileData targetFile = fileDataRepository.findFirstByUserIdAndIdAndDeletedFalse(userId, targetId)
                    .orElseThrow(() -> new HttpException(I18n.get("fileNotExist")));
            targetPath = Paths.get(targetFile.getPath()).resolve(name).toString();
        }
        List<FileData> childrenList = findAllChildren(sourceFile.getId());
        for (FileData children : childrenList) {
            children.setPath(targetPath + children.getPath().substring(sourcePath.length()));
        }
        sourceFile.setParentId(targetId);
        sourceFile.setName(name);
        sourceFile.setPath(targetPath);
        childrenList.add(sourceFile);
        fileDataRepository.saveAll(childrenList);
    }

    public String submitDownload(String userId, List<String> idList) {
        List<FileData> fileList = fileDataRepository.findAllByUserIdAndIdInAndDeletedFalse(userId, idList);
        if (fileList.isEmpty() || fileList.size() != idList.size()) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        fileList = fileList.stream()
                .flatMap(file -> Stream.concat(Stream.of(file), findAllChildren(file.getId()).stream()))
                .toList();
        String downloadId = ULID.randomULID();
        RList<FileData> downloadFileList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + downloadId);
        downloadFileList.addAll(fileList);
        downloadFileList.expire(fileProperties.getDownloadLinkTimeout());
        return downloadId;
    }

    public String submitPreviewVideo(String userId, String id) {
        if (Boolean.FALSE.equals(fileProperties.getVideoPlay())) {
            throw new HttpException(I18n.get("videoPlayNotEnabled"));
        }
        FileData file = fileDataRepository.findFirstByUserIdAndIdAndDeletedFalse(userId, id)
                .orElseThrow(() -> new HttpException(I18n.get("fileNotExist")));
        if (!MediaUtil.isDashSupported(file.getMimeType(), fileProperties.getVideoPlayMimeType())) {
            throw new HttpException(I18n.get("fileNotSupportPlay"));
        }
        Path statusFile = FileUtil.getHashPath(fileProperties.getVideoPlayDir(), file.getHashValue())
                .resolve(MediaUtil.STATUS_FILE_NAME);
        if (!Files.exists(statusFile)) {
            throw new HttpException(I18n.get("videoGeneratedFailed"));
        }
        MediaUtil.GenerationStatus generationStatus;
        try {
            generationStatus = MediaUtil.GenerationStatus.valueOf(Files.readString(statusFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new HttpException(I18n.get("videoGeneratedFailed"));
        }
        if (MediaUtil.GenerationStatus.WAITING.equals(generationStatus)) {
            throw new HttpException(I18n.get("videoBeingGenerated"));
        } else if (MediaUtil.GenerationStatus.SUCCESS.equals(generationStatus)) {
            return submitDownload(userId, List.of(id));
        }
        throw new HttpException(I18n.get("videoGeneratedFailed"));
    }

    public ResponseEntity<StreamingResponseBody> download(String downloadId) {
        RList<FileData> fileList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + downloadId);
        if (CollectionUtils.isEmpty(fileList)) {
            throw new HttpException(I18n.get("downloadLinkExpired"));
        }
        FileData file = fileList.getFirst();
        if (fileList.size() == 1 && !FileAttribute.MimeType.FOLDER.value().toString().equals(file.getMimeType())) {
            return DownloadUtil.download(file.getName(), file.getMimeType(),
                    FileUtil.getHashPath(fileProperties.getDir(), file.getHashValue()));
        }
        return DownloadUtil.downloadZip(fileProperties.getDir(), fileList);
    }

    public ResponseEntity<StreamingResponseBody> downloadChunked(String downloadId, String range) {
        RList<FileData> fileList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + downloadId);
        if (CollectionUtils.isEmpty(fileList)) {
            throw new HttpException(I18n.get("downloadLinkExpired"));
        }
        FileData file = fileList.getFirst();
        if (fileList.size() != 1 || FileAttribute.MimeType.FOLDER.value().toString().equals(file.getMimeType())) {
            throw new HttpException(I18n.get("SegmentedDownloadOnlySupportSingle"));
        }
        return downloadChunked(FileUtil.getHashPath(fileProperties.getDir(), file.getHashValue()),
                file.getName(), file.getMimeType(), range);
    }

    private ResponseEntity<StreamingResponseBody> downloadChunked(Path path, String name, String mimeType, String range) {
        String[] ranges;
        if (StringUtils.hasLength(range)) {
            ranges = range.replace("bytes=", "").split("-");
        } else {
            ranges = new String[]{};
        }
        long start = 0L;
        long end = Long.MAX_VALUE;
        if (ranges.length > 0) {
            start = Long.parseLong(ranges[0]);
        }
        if (ranges.length > 1) {
            end = Long.parseLong(ranges[1]);
        }
        try {
            long size = Files.size(path);
            start = Math.max(start, 0);
            end = Math.min(end, size - 1);
            if (start > end) {
                throw new HttpException(I18n.get("fileDownloadFailed"));
            }
            return DownloadUtil.downloadChunked(name, mimeType, path, start, end, size);
        } catch (IOException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileDownloadFailed"));
        }
    }

    public ResponseEntity<StreamingResponseBody> playVideo(String downloadId, String fileName, String range) {
        if (Boolean.FALSE.equals(fileProperties.getVideoPlay())) {
            throw new HttpException(I18n.get("videoPlayNotEnabled"));
        }
        MimeType mimeType = MediaUtil.getDashFileMimeType(fileName);
        if (mimeType == null) {
            throw new HttpException(I18n.get("fileNotSupportPlay"));
        }
        RList<FileData> fileList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + downloadId);
        if (CollectionUtils.isEmpty(fileList)) {
            throw new HttpException(I18n.get("playLinkExpired"));
        }
        FileData file = fileList.getFirst();
        if (fileList.size() != 1 || !MediaUtil.isDashSupported(file.getMimeType(), fileProperties.getVideoPlayMimeType())) {
            throw new HttpException(I18n.get("fileNotSupportPlay"));
        }
        return downloadChunked(FileUtil.getHashPath(fileProperties.getVideoPlayDir(), file.getHashValue()).resolve(fileName),
                fileName, mimeType.toString(), range);
    }

    public ResponseEntity<StreamingResponseBody> thumbnail(String userId, String id) {
        FileData file = fileDataRepository.findFirstByUserIdAndId(userId, id)
                .orElseThrow(() -> new HttpException(I18n.get("fileNotExist")));
        String mimeType = file.getMimeType();
        if (!ThumbnailUtil.hasThumbnail(mimeType, fileProperties.getThumbnailImageMimeType(),
                fileProperties.getThumbnailVideoMimeType())) {
            throw new HttpException(I18n.get("fileNotSupportThumbnail"));
        }
        AtomicReference<Path> thumbnailPath = new AtomicReference<>(FileUtil.getHashPath(fileProperties.getThumbnailDir(),
                file.getHashValue(), FileAttribute.THUMBNAIL_FILE_SUFFIX));
        if (!Files.isRegularFile(thumbnailPath.get())) {
            distributedLock.tryLock(RedisAttribute.LockType.file, RedisAttribute.GENERATE_THUMBNAIL + file.getHashValue(), () -> {
                try {
                    thumbnailPath.set(ThumbnailUtil.generateThumbnail(mimeType,
                            FileUtil.getHashPath(fileProperties.getDir(), file.getHashValue()), thumbnailPath.get(),
                            fileProperties.getThumbnailImageMimeType(), fileProperties.getThumbnailVideoMimeType(),
                            fileProperties.getThumbnailGenerateTimeout()));
                } catch (ThumbnailUtil.FileNotSupportThumbnailException e) {
                    throw new HttpException(I18n.get("fileNotSupportThumbnail"));
                } catch (ThumbnailUtil.ThumbnailGenerationFailedException | IOException | ExecutionException |
                         InterruptedException e) {
                    throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("thumbnailGenerationFailed"));
                }
            }, null);
        }
        return DownloadUtil.download(FileAttribute.DOWNLOAD_THUMBNAIL_NAME, FileAttribute.MimeType.WEBP.value().toString(), thumbnailPath.get());
    }

}
