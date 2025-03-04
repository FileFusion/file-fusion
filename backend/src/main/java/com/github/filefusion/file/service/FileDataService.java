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
import com.github.filefusion.util.file.RecycleBinUtil;
import com.github.filefusion.util.file.ThumbnailUtil;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    private final RecycleBinUtil recycleBinUtil;
    private final ThumbnailUtil thumbnailUtil;

    @Autowired
    public FileDataService(@Value("${file.lock-timeout}") Duration fileLockTimeout,
                           @Value("${file.download-link-timeout}") Duration fileDownloadLinkTimeout,
                           RedissonClient redissonClient,
                           FileDataRepository fileDataRepository,
                           DistributedLock distributedLock,
                           SysConfigService sysConfigService,
                           FileUtil fileUtil,
                           RecycleBinUtil recycleBinUtil,
                           ThumbnailUtil thumbnailUtil) {
        this.fileLockTimeout = fileLockTimeout;
        this.fileDownloadLinkTimeout = fileDownloadLinkTimeout;
        this.redissonClient = redissonClient;
        this.fileDataRepository = fileDataRepository;
        this.distributedLock = distributedLock;
        this.sysConfigService = sysConfigService;
        this.fileUtil = fileUtil;
        this.recycleBinUtil = recycleBinUtil;
        this.thumbnailUtil = thumbnailUtil;
    }

    private List<Path> getHierarchyPathList(String path) {
        Path rootPath = Paths.get(path).normalize();
        return IntStream.range(1, rootPath.getNameCount())
                .mapToObj(i -> rootPath.subpath(0, i + 1))
                .toList();
    }

    private String dismissUserPath(String path) {
        return path.substring(path.indexOf(FileAttribute.SEPARATOR) + 1);
    }

    public String formatUserPath(String userId, String path) {
        fileUtil.createUserFolder(userId);
        if (!StringUtils.hasLength(path)) {
            path = userId;
        } else {
            path = userId + FileAttribute.SEPARATOR + path;
        }
        return path;
    }

    public void verifyUserAuthorize(String userId, String... pathList) {
        if (pathList == null || pathList.length == 0) {
            throw new HttpException(I18n.get("operationFileSelectCheck"));
        }
        String userPath = userId + FileAttribute.SEPARATOR;
        for (String path : pathList) {
            if (!StringUtils.startsWithIgnoreCase(path, userPath)) {
                throw new HttpException(I18n.get("noOperationPermission"));
            }
        }
    }

    public Page<FileData> get(PageRequest page, String path, boolean deleted, String name) {
        path = path + "%";
        if (StringUtils.hasLength(name)) {
            name = "%" + name + "%";
        } else {
            name = "%";
        }
        Page<FileData> fileDataPage = fileDataRepository.findAllByPathLikeAndPathNotLikeAndDeletedAndNameLike(
                path, path + FileAttribute.SEPARATOR + "%", deleted, name, page);
        fileDataPage.getContent().forEach(fileData ->
                fileData.setHasThumbnail(thumbnailUtil.hasThumbnail(fileData.getMimeType()))
        );
        return fileDataPage;
    }

    public void batchRecycleOrDelete(List<String> pathList) {
        SysConfig config = sysConfigService.get(SysConfigKey.RECYCLE_BIN);
        if (Boolean.parseBoolean(config.getConfigValue())) {
            batchRecycle(pathList);
        } else {
            batchDelete(pathList);
        }
    }

    private void batchRecycle(List<String> pathList) {
        List<FileData> parentList = fileDataRepository.findAllByPathInAndDeletedFalse(pathList);
        if (parentList.isEmpty()) {
            return;
        }
        Map<String, List<FileData>> childMap = pathList.stream().collect(Collectors.toMap(
                path -> path,
                path -> fileDataRepository.findAllByPathLikeAndDeletedFalse(path + FileAttribute.SEPARATOR + "%")
        ));
        List<String> allPathList = Stream.concat(
                parentList.stream().map(FileData::getPath),
                childMap.values().stream().flatMap(List::stream).map(FileData::getPath)
        ).toList();
        List<FileData> recycleInfoList = recycleBinUtil.setRecycleInfo(parentList, childMap);
        distributedLock.tryMultiLock(RedisAttribute.LockType.file, allPathList, () -> {
            fileDataRepository.saveAll(recycleInfoList);
            PathUtil.move(parentList.stream().collect(Collectors.toMap(
                    file -> PathUtil.resolvePath(fileUtil.getBaseDir(), file.getPath(), true),
                    file -> PathUtil.resolvePath(recycleBinUtil.getBaseDir(), file.getRecyclePath(), false)
            )));
        }, fileLockTimeout);
    }

    private void batchDelete(List<String> pathList) {
        Map<String, FileData> allMap = pathList.stream()
                .flatMap(path -> fileDataRepository.findAllByPathOrPathLike(path, path + FileAttribute.SEPARATOR + "%").stream())
                .collect(Collectors.toMap(FileData::getPath, Function.identity()));
        if (allMap.isEmpty()) {
            return;
        }
        Set<String> allPathList = allMap.keySet();
        List<String> allHashList = allMap.values().stream().map(FileData::getHashValue)
                .filter(StringUtils::hasLength).distinct().toList();
        distributedLock.tryMultiLock(RedisAttribute.LockType.file, allPathList, () -> {
            fileDataRepository.deleteAllByPathIn(allPathList);
            PathUtil.delete(pathList.stream()
                    .map(path -> PathUtil.resolveSafePath(fileUtil.getBaseDir(), path, true))
                    .toList());
            if (!allHashList.isEmpty()) {
                thumbnailUtil.clearThumbnail(allHashList);
            }
        }, fileLockTimeout);
    }

    public void createFolder(String path, LocalDateTime lastModifiedDate, boolean allowExists) {
        List<Path> hierarchyPathList = getHierarchyPathList(path);
        List<String> sortedPathList = hierarchyPathList.stream().map(Path::toString).toList();
        distributedLock.tryMultiLock(RedisAttribute.LockType.file, sortedPathList, () -> {
            if (!allowExists && fileDataRepository.existsByPathAndDeletedFalse(path)) {
                throw new HttpException(I18n.get("fileExits", dismissUserPath(path)));
            }
            List<FileData> existsFileList = fileDataRepository.findAllByPathInAndDeletedFalse(sortedPathList);
            existsFileList.forEach(file -> {
                if (!FileAttribute.Type.FOLDER.equals(file.getType())) {
                    throw new HttpException(I18n.get("fileExits", dismissUserPath(file.getPath())));
                }
            });
            Map<String, FileData> existsFileMap = existsFileList.stream().collect(Collectors.toMap(FileData::getPath, fileData -> fileData));
            List<FileData> fileList = new ArrayList<>(hierarchyPathList.size());
            for (Path hierarchyPath : hierarchyPathList) {
                String folderPath = hierarchyPath.toString();
                String folderName = hierarchyPath.getFileName().toString();
                FileData file = existsFileMap.get(folderPath);
                if (file == null) {
                    file = new FileData();
                }
                file.setPath(folderPath);
                file.setName(folderName);
                file.setType(FileAttribute.Type.FOLDER);
                file.setMimeType(FileAttribute.MimeType.FOLDER.value().toString());
                file.setSize(0L);
                file.setEncrypted(false);
                file.setFileLastModifiedDate(lastModifiedDate);
                file.setDeleted(false);
                fileList.add(file);
            }
            fileDataRepository.saveAll(fileList);
            fileUtil.createFolder(path);
        }, fileLockTimeout);
    }

    public void upload(MultipartFile multipartFile, String name,
                       String path, String type, LocalDateTime lastModifiedDate) {
        createFolder(path, lastModifiedDate, true);

        String filePath = path + FileAttribute.SEPARATOR + name;
        distributedLock.tryLock(RedisAttribute.LockType.file, filePath, () -> {
            if (fileDataRepository.existsByPathAndDeletedFalse(filePath)) {
                throw new HttpException(I18n.get("fileExits", dismissUserPath(filePath)));
            }
            FileData file = new FileData();
            file.setPath(filePath);
            file.setName(name);
            file.setType(FileAttribute.Type.FILE);
            file.setMimeType(type);
            file.setSize(multipartFile.getSize());
            file.setEncrypted(false);
            file.setFileLastModifiedDate(lastModifiedDate);
            file.setDeleted(false);
            file.setHashValue(fileUtil.upload(multipartFile, filePath));
            try {
                fileDataRepository.save(file);
            } catch (Exception e) {
                PathUtil.delete(PathUtil.resolveSafePath(fileUtil.getBaseDir(), filePath, false));
                throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileUploadFailed"));
            }
        }, fileLockTimeout);
    }

    public void rename(String path, String originalName, String targetName) {
        if (!StringUtils.hasLength(originalName)) {
            throw new HttpException(I18n.get("renameFileSelectCheck"));
        }
        String originalPath = path + FileAttribute.SEPARATOR + originalName;
        FileData originalFile = fileDataRepository.findFirstByPathAndDeletedFalse(originalPath);
        if (originalFile == null) {
            throw new HttpException(I18n.get("fileNotExist"));
        }

        if (!StringUtils.hasLength(targetName)) {
            throw new HttpException(I18n.get("fileNameEmpty"));
        }
        String targetPath = path + FileAttribute.SEPARATOR + targetName;

        String originalPathFolder = originalPath + FileAttribute.SEPARATOR;
        String targetPathFolder = targetPath + FileAttribute.SEPARATOR;

        List<FileData> originalChildList = fileDataRepository.findAllByPathLikeAndDeletedFalse(originalPathFolder + "%");
        List<String> lockPathList = originalChildList.stream().map(FileData::getPath).collect(Collectors.toList());
        originalChildList.forEach(file -> {
            String filePath = file.getPath();
            filePath = filePath.substring(originalPathFolder.length(), filePath.length() - 1);
            file.setPath(targetPathFolder + filePath);
        });
        lockPathList.addAll(originalChildList.stream().map(FileData::getPath).toList());
        lockPathList.add(originalPath);
        lockPathList.add(targetPath);

        originalFile.setPath(targetPath);
        originalFile.setName(targetName);
        originalChildList.add(originalFile);

        distributedLock.tryMultiLock(RedisAttribute.LockType.file, lockPathList, () -> {
            if (fileDataRepository.existsByPathAndDeletedFalse(targetPath)) {
                throw new HttpException(I18n.get("fileNameAlreadyExists"));
            }
            fileDataRepository.saveAll(originalChildList);
            PathUtil.move(
                    PathUtil.resolvePath(fileUtil.getBaseDir(), originalPath, true),
                    PathUtil.resolveSafePath(fileUtil.getBaseDir(), targetPath, false)
            );
        }, fileLockTimeout);
    }

    public SubmitDownloadFilesResponse submitDownload(List<String> pathList) {
        String downloadId = ULID.randomULID();
        RList<String> pathRList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + RedisAttribute.SEPARATOR + downloadId);
        pathRList.addAll(pathList);
        pathRList.expire(fileDownloadLinkTimeout);
        return new SubmitDownloadFilesResponse(downloadId);
    }

    public ResponseEntity<StreamingResponseBody> download(String downloadId) {
        RList<String> pathList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + RedisAttribute.SEPARATOR + downloadId);
        if (CollectionUtils.isEmpty(pathList)) {
            throw new HttpException(I18n.get("downloadLinkExpired"));
        }
        List<Path> safePathList = PathUtil.resolveSafePath(fileUtil.getBaseDir(), pathList, true);
        Path pathFirst = safePathList.getFirst();
        if (safePathList.size() == 1 && Files.isRegularFile(pathFirst)) {
            return fileUtil.download(pathFirst);
        } else {
            return fileUtil.downloadZip(safePathList);
        }
    }

    public ResponseEntity<StreamingResponseBody> downloadChunked(String path, String range) {
        Path safePath = PathUtil.resolveSafePath(fileUtil.getBaseDir(), path, true);
        String[] ranges = range.replace("bytes=", "").split("-");
        long start = 0;
        long end = -1;
        if (ranges.length > 0) {
            start = Long.parseLong(ranges[0]);
        }
        if (ranges.length > 1) {
            end = Long.parseLong(ranges[1]);
        }
        return fileUtil.downloadChunked(safePath, start, end);
    }

    public ResponseEntity<StreamingResponseBody> thumbnailFile(String path) {
        FileData file = fileDataRepository.findFirstByPath(path);
        if (file == null) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        String mimeType = file.getMimeType();
        String fileHash = file.getHashValue();
        if (!StringUtils.hasLength(fileHash) || !thumbnailUtil.hasThumbnail(mimeType)) {
            throw new HttpException(I18n.get("fileNotSupportThumbnail"));
        }
        Path originalPath = PathUtil.resolvePath(
                file.getDeleted() ? recycleBinUtil.getBaseDir() : fileUtil.getBaseDir(),
                file.getDeleted() ? file.getRecyclePath() : file.getPath(), true);
        return fileUtil.download(thumbnailUtil.generateThumbnail(originalPath, mimeType, fileHash));
    }

}
