package com.github.filefusion.file.service;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.SubmitDownloadFilesResponse;
import com.github.filefusion.file.repository.FileDataRepository;
import com.github.filefusion.util.*;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletResponse;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * FileDataService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Service
public class FileDataService {

    private final FileDataRepository fileDataRepository;
    private final DistributedLock distributedLock;
    private final SystemFile systemFile;
    private final RedissonClient redissonClient;
    private final Duration lockTimeout;

    @Autowired
    public FileDataService(FileDataRepository fileDataRepository,
                           DistributedLock distributedLock,
                           SystemFile systemFile,
                           RedissonClient redissonClient,
                           @Value("${server.servlet.session.timeout}") Duration timeout) {
        this.fileDataRepository = fileDataRepository;
        this.distributedLock = distributedLock;
        this.systemFile = systemFile;
        this.redissonClient = redissonClient;
        this.lockTimeout = timeout;
    }

    private List<Path> getHierarchyPathList(String path) {
        Path rootPath = Paths.get(path).normalize();
        return IntStream.range(1, rootPath.getNameCount())
                .mapToObj(i -> rootPath.subpath(0, i + 1))
                .toList();
    }

    public Page<FileData> get(PageRequest page, String path, String name) {
        path = path + "%";
        if (StringUtils.hasLength(name)) {
            name = "%" + name + "%";
        } else {
            name = "%";
        }
        return fileDataRepository.findAllByPathLikeAndPathNotLikeAndNameLike(path,
                path + FileAttribute.SEPARATOR + "%",
                name, page);
    }

    @Transactional(rollbackFor = HttpException.class)
    public void batchDelete(List<String> pathList) {
        List<String> allPathList = pathList.stream()
                .flatMap(path -> fileDataRepository.findAllByPathOrPathLike(path, path + FileAttribute.SEPARATOR + "%").stream())
                .map(FileData::getPath).distinct().toList();
        distributedLock.tryMultiLock(allPathList, () -> {
            fileDataRepository.deleteAllByPathIn(allPathList);
            systemFile.delete(allPathList);
        });
    }

    public void createFolder(final String path, Long lastModified, final boolean allowExists) {
        final Date lastModifiedDate = new Date(lastModified);
        final List<Path> hierarchyPathList = getHierarchyPathList(path);
        final List<String> sortedPathList = hierarchyPathList.stream().map(Path::toString).toList();
        distributedLock.tryMultiLock(sortedPathList, () -> {
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
                fileData.setMimeType(FileAttribute.FOLDER_MIME_TYPE);
                fileData.setSize(0L);
                fileData.setEncrypted(false);
                fileData.setHashValue(EncryptUtil.sha256(folderName));
                fileData.setFileLastModifiedDate(lastModifiedDate);
                fileDataList.add(fileData);
            }
            systemFile.createFolder(path);
            fileDataRepository.saveAll(fileDataList);
        });
    }

    public void upload(final MultipartFile file, final String name,
                       String path, final String type, final Long lastModified) {
        createFolder(path, lastModified, true);

        final String filePath = path + FileAttribute.SEPARATOR + name;
        distributedLock.tryLock(filePath, () -> {
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
            fileData.setFileLastModifiedDate(new Date(lastModified));
            fileData.setHashValue(systemFile.upload(file, filePath));
            fileDataRepository.save(fileData);
        });
    }

    public void rename(String path, String originalName, String targetName) {
        if (!StringUtils.hasLength(originalName)) {
            throw new HttpException(I18n.get("renameFileSelectCheck"));
        }
        if (!StringUtils.hasLength(targetName)) {
            throw new HttpException(I18n.get("fileNameEmpty"));
        }
        String originalPath = path + FileAttribute.SEPARATOR + originalName;
        FileData originalFile = fileDataRepository.findFirstByPath(originalPath);
        if (originalFile == null) {
            throw new HttpException(I18n.get("originalFileNotExist"));
        }
        String targetPath = path + FileAttribute.SEPARATOR + targetName;
        if (fileDataRepository.existsByPath(targetPath)) {
            throw new HttpException(I18n.get("fileNameAlreadyExists"));
        }
        systemFile.move(originalPath, targetPath);
    }

    public SubmitDownloadFilesResponse submitDownload(List<String> pathList) {
        String downloadId = ULID.randomULID();
        RList<String> pathRList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + RedisAttribute.SEPARATOR + downloadId);
        pathRList.addAll(pathList);
        pathRList.expire(lockTimeout);
        return new SubmitDownloadFilesResponse(downloadId);
    }

    public ResponseEntity<StreamingResponseBody> download(String downloadId, HttpServletResponse response) {
        RList<String> pathList = redissonClient.getList(RedisAttribute.DOWNLOAD_ID_PREFIX + RedisAttribute.SEPARATOR + downloadId);
        if (CollectionUtils.isEmpty(pathList)) {
            throw new HttpException(I18n.get("downloadLinkExpired"));
        }
        List<Path> safePathList = systemFile.validatePaths(pathList);
        pathList.delete();
        Path pathFirst = safePathList.getFirst();

        String encodedFilename;
        MediaType contentType;
        StreamingResponseBody streamingResponseBody;
        if (safePathList.size() == 1 && !Files.isDirectory(pathFirst)) {
            encodedFilename = ContentDisposition.attachment()
                    .filename(pathFirst.getFileName().toString(), StandardCharsets.UTF_8)
                    .build().toString();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, encodedFilename);
            contentType = MediaType.APPLICATION_OCTET_STREAM;
            streamingResponseBody = out -> Files.copy(pathFirst, out);
        } else {
            encodedFilename = ContentDisposition.attachment()
                    .filename(FileAttribute.DOWNLOAD_ZIP_NAME, StandardCharsets.UTF_8)
                    .build().toString();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, encodedFilename);
            contentType = MediaType.parseMediaType(FileAttribute.ZIP_MEDIA_TYPE);
            streamingResponseBody = new ZipStreamingResponseBody(safePathList);
        }
        return ResponseEntity.ok()
                .contentType(contentType)
                .body(streamingResponseBody);
    }

    private record ZipStreamingResponseBody(List<Path> pathList) implements StreamingResponseBody {
        @Override
        public void writeTo(@Nonnull OutputStream out) throws IOException {
            try (ZipOutputStream zos = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
                zos.setLevel(Deflater.BEST_SPEED);
                for (Path path : pathList) {
                    addToZip(zos, path, "");
                }
            }
        }

        private void addToZip(ZipOutputStream zos, Path path, String parent) throws IOException {
            String entryName = parent + path.getFileName();
            if (Files.isDirectory(path)) {
                entryName += FileAttribute.SEPARATOR;
                zos.putNextEntry(new ZipEntry(entryName));
                zos.closeEntry();
                try (DirectoryStream<Path> children = Files.newDirectoryStream(path)) {
                    for (Path child : children) {
                        addToZip(zos, child, entryName);
                    }
                }
            } else {
                zos.putNextEntry(new ZipEntry(entryName));
                Files.copy(path, zos);
                zos.closeEntry();
            }
        }
    }

}
