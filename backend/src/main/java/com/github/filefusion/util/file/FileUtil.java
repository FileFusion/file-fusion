package com.github.filefusion.util.file;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.util.I18n;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * FileUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Getter
@Component
public class FileUtil {

    private final Path baseDir;

    @Autowired
    public FileUtil(@Value("${file.dir}") String fileDir) {
        this.baseDir = Paths.get(fileDir).normalize().toAbsolutePath();
        if (!Files.exists(this.baseDir)) {
            try {
                Files.createDirectories(this.baseDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private MediaType getFileMediaType(Path path) {
        try {
            return MediaType.parseMediaType(Files.probeContentType(path));
        } catch (IllegalArgumentException | IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
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

    public void createUserFolder(String userId) {
        Path userPath = PathUtil.resolvePath(baseDir, userId, false);
        try {
            Files.createDirectories(userPath);
        } catch (IOException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("folderCreationFailed"));
        }
    }

    public void createFolder(String path) {
        Path targetPath = PathUtil.resolveSafePath(baseDir, path, false);
        try {
            Files.createDirectories(targetPath);
        } catch (IOException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("folderCreationFailed"));
        }
    }

    public String upload(MultipartFile file, String path) {
        try (HashingInputStream in = new HashingInputStream(file.getInputStream())) {
            Files.copy(in, PathUtil.resolveSafePath(baseDir, path, false));
            return in.getHashString();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileUploadFailed"));
        }
    }

    public ResponseEntity<StreamingResponseBody> download(Path path) {
        return downloadResponse(path.getFileName().toString(),
                getFileMediaType(path),
                HttpStatus.OK,
                out -> Files.copy(path, out),
                new HttpHeaders());
    }

    public ResponseEntity<StreamingResponseBody> downloadChunked(Path path, long start, long end) {
        long size;
        try {
            size = Files.size(path);
        } catch (IOException e) {
            throw new HttpException(I18n.get("getFileSizeFailed"));
        }
        long endReal = end == -1 ? size - 1 : end;
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.add(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", start, endReal, size));
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(endReal - start + 1));
        return downloadResponse(path.getFileName().toString(),
                getFileMediaType(path),
                HttpStatus.PARTIAL_CONTENT,
                out -> {
                    try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
                         WritableByteChannel outChannel = Channels.newChannel(out)) {
                        fileChannel.transferTo(start, endReal - start + 1, outChannel);
                    }
                },
                headers);
    }

    public ResponseEntity<StreamingResponseBody> downloadZip(List<Path> pathList) {
        return downloadResponse(FileAttribute.DOWNLOAD_ZIP_NAME,
                FileAttribute.MimeType.ZIP.value(),
                HttpStatus.OK,
                out -> {
                    try (ZipOutputStream zos = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
                        zos.setLevel(Deflater.BEST_SPEED);
                        for (Path path : pathList) {
                            addToZip(zos, path, "");
                        }
                    }
                },
                new HttpHeaders());
    }

    private ResponseEntity<StreamingResponseBody> downloadResponse(String filename,
                                                                   MediaType mediaType,
                                                                   HttpStatus status,
                                                                   StreamingResponseBody body,
                                                                   HttpHeaders headers) {
        String contentDisposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build().toString();
        return ResponseEntity.status(status)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .headers(headers)
                .contentType(mediaType)
                .body(body);
    }

}
