package com.github.filefusion.util;

import com.github.filefusion.common.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SystemFile
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public class SystemFile {

    private final Path baseDir;

    @Autowired
    public SystemFile(@Value("${file.dir}") String fileDir) {
        this.baseDir = Paths.get(fileDir).normalize().toAbsolutePath();
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
                    throw new HttpException(I18n.get("folderCreationFailed"));
                }
            }
        } catch (IOException e) {
            throw new HttpException(I18n.get("folderCreationFailed"));
        }
    }

    public String upload(MultipartFile file, String path) {
        Path targetPath = resolveSafePath(path);
        if (Files.exists(targetPath)) {
            delete(targetPath);
        }
        try (HashingInputStream hashingInputStream = new HashingInputStream(file.getInputStream())) {
            Files.copy(hashingInputStream, targetPath);
            return hashingInputStream.getHashString();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new HttpException(I18n.get("fileUploadFailed"));
        }
    }

    public void delete(List<String> pathList) {
        final AtomicBoolean success = new AtomicBoolean(true);
        pathList.forEach(path -> {
            try {
                delete(resolveSafePath(path));
            } catch (Exception e) {
                success.set(false);
            }
        });
        if (!success.get()) {
            throw new HttpException(I18n.get("fileDeletionFailed"));
        }
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

    private void delete(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new HttpException(I18n.get("fileDeletionFailed"));
        }
    }

}
