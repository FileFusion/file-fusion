package com.github.filefusion.file.service;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.constant.SysConfigKey;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.FileHashUsageCount;
import com.github.filefusion.file.model.SubmitDownloadFilesResponse;
import com.github.filefusion.file.repository.FileDataRepository;
import com.github.filefusion.sys_config.entity.SysConfig;
import com.github.filefusion.sys_config.service.SysConfigService;
import com.github.filefusion.util.DistributedLock;
import com.github.filefusion.util.EncryptUtil;
import com.github.filefusion.util.I18n;
import com.github.filefusion.util.ULID;
import com.github.filefusion.util.file.FileUtil;
import com.github.filefusion.util.file.RecycleBinUtil;
import com.github.filefusion.util.file.ThumbnailUtil;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
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

    public String formatUserPath(String userId, String path) {
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

    public Page<FileData> get(PageRequest page, String path, String name) {
        path = path + "%";
        if (StringUtils.hasLength(name)) {
            name = "%" + name + "%";
        } else {
            name = "%";
        }
        Page<FileData> fileDataPage = fileDataRepository.findAllByPathLikeAndPathNotLikeAndNameLike(path,
                path + FileAttribute.SEPARATOR + "%", name, page);
        fileDataPage.getContent().forEach(fileData ->
                fileData.setHasThumbnail(thumbnailUtil.hasThumbnail(fileData.getMimeType()))
        );
        return fileDataPage;
    }

    @Transactional(rollbackFor = HttpException.class)
    public void batchRecycleOrDelete(List<String> pathList) {
        SysConfig sysConfig = sysConfigService.get(SysConfigKey.RECYCLE_BIN);
        if (Boolean.parseBoolean(sysConfig.getConfigValue())) {
            batchRecycle(pathList);
        } else {
            batchDelete(pathList);
        }
    }

    private void batchRecycle(List<String> pathList) {
        List<FileData> allFileList = fileDataRepository.findAllByPathIn(pathList);
        if (allFileList.isEmpty()) {
            return;
        }
        Map<String, List<FileData>> allChildFileMap = pathList.stream().collect(Collectors.toMap(
                path -> path,
                path -> fileDataRepository.findAllByPathLike(path + FileAttribute.SEPARATOR + "%")
        ));
        List<String> allFilePathList = Stream.concat(
                allFileList.stream().map(FileData::getPath),
                allChildFileMap.values().stream().flatMap(Collection::stream).map(FileData::getPath)
        ).toList();
        distributedLock.tryMultiLock(RedisAttribute.LockType.file, allFilePathList, () -> {
            fileDataRepository.saveAll(recycleBinUtil.setRecycleInfo(allFileList, allChildFileMap));
            recycleBinUtil.recycle(allFileList);
        }, fileLockTimeout);
    }

    private void batchDelete(List<String> pathList) {
        Map<String, FileData> allFileMap = pathList.stream()
                .flatMap(path -> fileDataRepository.findAllByPathOrPathLike(path, path + FileAttribute.SEPARATOR + "%").stream())
                .collect(Collectors.toMap(FileData::getPath, Function.identity(), (existing, replacement) -> existing));
        if (allFileMap.isEmpty()) {
            return;
        }
        Set<String> allPathList = allFileMap.keySet();
        Set<String> allHashList = allFileMap.values().stream().map(FileData::getHashValue)
                .filter(StringUtils::hasLength).collect(Collectors.toSet());
        distributedLock.tryMultiLock(RedisAttribute.LockType.file, allPathList, () -> {
            fileDataRepository.deleteAllByPathIn(allPathList);
            fileUtil.deleteSafe(allPathList);

            if (allHashList.isEmpty()) {
                return;
            }
            clearThumbnailFile(allHashList);
        }, fileLockTimeout);
    }

    public void clearThumbnailFile(Collection<String> needClearHashList) {
        Map<String, FileHashUsageCount> hashUsageCountMap = fileDataRepository.countByHashValueList(needClearHashList)
                .stream().collect(Collectors.toMap(FileHashUsageCount::getHashValue, Function.identity()));
        List<String> thumbnailImageMimeType = thumbnailUtil.getThumbnailImageMimeType();
        List<String> thumbnailVideoMimeType = thumbnailUtil.getThumbnailVideoMimeType();
        List<String> hashToDeleteList = needClearHashList.stream()
                .filter(hash -> {
                    FileHashUsageCount hashUsageCount = hashUsageCountMap.get(hash);
                    return hashUsageCount == null
                            || hashUsageCount.getCount() == null || hashUsageCount.getCount() == 0L
                            || !StringUtils.hasLength(hashUsageCount.getMimeType())
                            || (!thumbnailImageMimeType.contains(hashUsageCount.getMimeType())
                            && !thumbnailVideoMimeType.contains(hashUsageCount.getMimeType()));
                }).toList();
        thumbnailUtil.deleteThumbnail(hashToDeleteList);
    }

    public void createFolder(String path, LocalDateTime lastModifiedDate, boolean allowExists) {
        List<Path> hierarchyPathList = getHierarchyPathList(path);
        List<String> sortedPathList = hierarchyPathList.stream().map(Path::toString).toList();
        distributedLock.tryMultiLock(RedisAttribute.LockType.file, sortedPathList, () -> {
            if (!allowExists && fileDataRepository.existsByPath(path)) {
                throw new HttpException(I18n.get("folderExits"));
            }
            List<FileData> existsFileDataList = fileDataRepository.findAllByPathIn(sortedPathList);
            Map<String, FileData> existsFileDataMap = existsFileDataList.stream().collect(Collectors.toMap(FileData::getPath, fileData -> fileData));
            List<FileData> fileDataList = new ArrayList<>(hierarchyPathList.size());
            for (Path hierarchyPath : hierarchyPathList) {
                String folderPath = hierarchyPath.toString();
                String folderName = hierarchyPath.getFileName().toString();
                FileData fileData = existsFileDataMap.get(folderPath);
                if (fileData == null) {
                    fileData = new FileData();
                }
                fileData.setPath(folderPath);
                fileData.setName(folderName);
                fileData.setType(FileAttribute.Type.FOLDER);
                fileData.setMimeType(FileAttribute.MimeType.FOLDER.value().toString());
                fileData.setSize(0L);
                fileData.setEncrypted(false);
                fileData.setHashValue(EncryptUtil.sha256(folderName));
                fileData.setFileLastModifiedDate(lastModifiedDate);
                fileData.setDeleted(false);
                fileDataList.add(fileData);
            }
            fileUtil.createFolder(path);
            fileDataRepository.saveAll(fileDataList);
        }, fileLockTimeout);
    }

    public void upload(MultipartFile file, String name,
                       String path, String type, LocalDateTime lastModifiedDate) {
        createFolder(path, lastModifiedDate, true);

        String filePath = path + FileAttribute.SEPARATOR + name;
        distributedLock.tryLock(RedisAttribute.LockType.file, filePath, () -> {
            FileData fileData = fileDataRepository.findFirstByPath(filePath);
            if (fileData == null) {
                fileData = new FileData();
            }
            fileData.setPath(filePath);
            fileData.setName(name);
            fileData.setType(FileAttribute.Type.FILE);
            fileData.setMimeType(type);
            fileData.setSize(file.getSize());
            fileData.setEncrypted(false);
            fileData.setFileLastModifiedDate(lastModifiedDate);
            fileData.setDeleted(false);
            fileData.setHashValue(fileUtil.upload(file, filePath));
            fileDataRepository.save(fileData);
        }, fileLockTimeout);
    }

    @Transactional(rollbackFor = HttpException.class)
    public void rename(String path, String originalName, String targetName) {
        if (!StringUtils.hasLength(originalName)) {
            throw new HttpException(I18n.get("renameFileSelectCheck"));
        }
        String originalPath = path + FileAttribute.SEPARATOR + originalName;
        FileData originalFile = fileDataRepository.findFirstByPath(originalPath);
        if (originalFile == null) {
            throw new HttpException(I18n.get("fileNotExist"));
        }

        if (!StringUtils.hasLength(targetName)) {
            throw new HttpException(I18n.get("fileNameEmpty"));
        }
        String targetPath = path + FileAttribute.SEPARATOR + targetName;
        if (fileDataRepository.existsByPath(targetPath)) {
            throw new HttpException(I18n.get("fileNameAlreadyExists"));
        }

        String originalPathFolder = originalPath + FileAttribute.SEPARATOR;
        String targetPathFolder = targetPath + FileAttribute.SEPARATOR;
        List<FileData> originalFileList = fileDataRepository.findAllByPathLike(originalPathFolder + "%");

        List<String> allPathList = originalFileList.stream().map(FileData::getPath).collect(Collectors.toList());
        allPathList.add(originalPath);
        allPathList.add(targetPath);
        distributedLock.tryMultiLock(RedisAttribute.LockType.file, allPathList, () -> {
            originalFile.setPath(targetPath);
            originalFile.setName(targetName);
            for (FileData fileData : originalFileList) {
                String filePath = fileData.getPath();
                filePath = filePath.substring(originalPathFolder.length(), filePath.length() - 1);
                fileData.setPath(targetPathFolder + filePath);
            }
            originalFileList.add(originalFile);

            fileDataRepository.saveAll(originalFileList);
            fileUtil.move(originalPath, targetPath);
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
        List<Path> safePathList = fileUtil.validatePaths(pathList);
        Path pathFirst = safePathList.getFirst();
        if (safePathList.size() == 1 && Files.isRegularFile(pathFirst)) {
            return fileUtil.download(pathFirst);
        } else {
            return fileUtil.download(safePathList);
        }
    }

    public ResponseEntity<StreamingResponseBody> downloadChunked(String path, String range) {
        Path safePath = fileUtil.validatePath(path);
        String[] ranges = range.replace("bytes=", "").split("-");
        long start = 0;
        long end = -1;
        if (ranges.length > 0) {
            start = Long.parseLong(ranges[0]);
        }
        if (ranges.length > 1) {
            end = Long.parseLong(ranges[1]);
        }
        return fileUtil.download(safePath, start, end);
    }

    public ResponseEntity<StreamingResponseBody> thumbnailFile(String path) {
        FileData fileData = fileDataRepository.findFirstByPath(path);
        if (fileData == null) {
            throw new HttpException(I18n.get("fileNotExist"));
        }
        String mimeType = fileData.getMimeType();
        String fileHash = fileData.getHashValue();
        if (!StringUtils.hasLength(fileHash) || !thumbnailUtil.hasThumbnail(mimeType)) {
            throw new HttpException(I18n.get("fileNotSupportThumbnail"));
        }
        return fileUtil.download(thumbnailUtil.generateThumbnail(path, mimeType, fileHash));
    }

}
