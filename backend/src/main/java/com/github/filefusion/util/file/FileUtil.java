package com.github.filefusion.util.file;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.util.I18n;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * FileUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
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

    private MediaType getFileMediaType(Path path) {
        try {
            return MediaType.parseMediaType(Files.probeContentType(path));
        } catch (IllegalArgumentException | IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private Path resolveSafePath(String path) {
        if (!StringUtils.hasLength(path) || path.contains("..") || path.contains("//") || path.startsWith("/")) {
            throw new HttpException(I18n.get("noOperationPermission"));
        }
        Path resolvedPath = baseDir.resolve(path).normalize();
        if (!resolvedPath.startsWith(baseDir)) {
            throw new HttpException(I18n.get("noOperationPermission"));
        }
        return resolvedPath;
    }

    public List<Path> validatePaths(List<String> pathList) {
        return pathList.stream()
                .map(this::resolveSafePath)
                .peek(path -> {
                    if (!Files.exists(path)) {
                        throw new HttpException(I18n.get("noOperationPermission"));
                    }
                })
                .toList();
    }

    public Path validatePath(String path) {
        Path targetPath = resolveSafePath(path);
        if (!Files.exists(targetPath)) {
            throw new HttpException(I18n.get("noOperationPermission"));
        }
        return targetPath;
    }

    public void createFolder(String path) {
        Path targetPath = resolveSafePath(path);
        try {
            Files.createDirectories(targetPath);
        } catch (FileAlreadyExistsException e) {
            if (!Files.isDirectory(targetPath)) {
                delete(targetPath);
                try {
                    Files.createDirectories(targetPath);
                } catch (IOException e1) {
                    throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("folderCreationFailed"));
                }
            }
        } catch (IOException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("folderCreationFailed"));
        }
    }

    public String upload(MultipartFile file, String path) {
        Path targetPath = resolveSafePath(path);
        delete(targetPath);
        try (HashingInputStream in = new HashingInputStream(file.getInputStream())) {
            Files.copy(in, targetPath);
            return in.getHashString();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileUploadFailed"));
        }
    }

    public void move(String original, String target) {
        Path originalPath = validatePath(original);
        Path targetPath = resolveSafePath(target);
        delete(targetPath);
        try {
            Path targetParentPath = targetPath.getParent();
            if (targetParentPath != null) {
                Files.createDirectories(targetParentPath);
            }
            Files.move(originalPath, targetPath, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileMoveFailed"));
        }
    }

    public void deleteSafe(Collection<String> pathList) {
        deleteAll(pathList, this::resolveSafePath);
    }

    public void delete(Collection<Path> pathList) {
        deleteAll(pathList, Function.identity());
    }

    private <T> void deleteAll(Collection<T> items, Function<T, Path> pathResolver) {
        AtomicBoolean success = new AtomicBoolean(true);
        items.forEach(item -> {
            try {
                delete(pathResolver.apply(item));
            } catch (Exception e) {
                success.set(false);
            }
        });
        if (!success.get()) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileDeletionFailed"));
        }
    }

    public void delete(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        AtomicBoolean success = new AtomicBoolean(true);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        Files.deleteIfExists(file);
                    } catch (IOException e) {
                        success.set(false);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    try {
                        Files.deleteIfExists(dir);
                    } catch (IOException e) {
                        success.set(false);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            success.set(false);
        }
        if (!success.get()) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileDeletionFailed"));
        }
    }

    public ResponseEntity<StreamingResponseBody> download(Path path) {
        return download(path.getFileName().toString(),
                getFileMediaType(path),
                HttpStatus.OK,
                out -> Files.copy(path, out),
                new HttpHeaders());
    }

    public ResponseEntity<StreamingResponseBody> download(Path path, long start, long end) {
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
        return download(path.getFileName().toString(),
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

    public ResponseEntity<StreamingResponseBody> download(List<Path> pathList) {
        return download(FileAttribute.DOWNLOAD_ZIP_NAME,
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

    private ResponseEntity<StreamingResponseBody> download(String filename,
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
