package com.github.filefusion.util.file;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.util.I18n;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * PathUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class PathUtil {

    public static MediaType getFileMediaType(Path path) {
        try {
            return MediaType.parseMediaType(Files.probeContentType(path));
        } catch (IllegalArgumentException | IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    public static Path resolvePath(Path baseDir, String path, boolean exists) {
        Path resolvedPath = baseDir.resolve(path);
        if (exists && !Files.exists(resolvedPath)) {
            throw new HttpException(I18n.get("noOperationPermission"));
        }
        return resolvedPath;
    }

    public static List<Path> resolvePath(Path baseDir, List<String> pathList, boolean exists) {
        return pathList.stream()
                .map(path -> resolvePath(baseDir, path, exists))
                .toList();
    }

    public static Path resolveSafePath(Path baseDir, String path, boolean exists) {
        if (!StringUtils.hasLength(path) || path.contains("..") || path.contains("//") || path.startsWith("/")) {
            throw new HttpException(I18n.get("noOperationPermission"));
        }
        Path resolvedPath = baseDir.resolve(path).normalize();
        if (!resolvedPath.startsWith(baseDir) || (exists && !Files.exists(resolvedPath))) {
            throw new HttpException(I18n.get("noOperationPermission"));
        }
        return resolvedPath;
    }

    public static List<Path> resolveSafePath(Path baseDir, List<String> pathList, boolean exists) {
        return pathList.stream()
                .map(path -> resolveSafePath(baseDir, path, exists))
                .toList();
    }

    public static void move(Map<Path, Path> originalTargetMap) {
        AtomicBoolean success = new AtomicBoolean(true);
        originalTargetMap.forEach((originalPath, targetPath) -> {
            try {
                move(originalPath, targetPath);
            } catch (Exception e) {
                success.set(false);
            }
        });
        if (!success.get()) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileMoveFailed"));
        }
    }

    public static void move(Path originalPath, Path targetPath) {
        try {
            Files.createDirectories(targetPath.getParent());
            Files.move(originalPath, targetPath, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileMoveFailed"));
        }
    }

    public static void delete(List<Path> pathList) {
        AtomicBoolean success = new AtomicBoolean(true);
        pathList.forEach(path -> {
            try {
                delete(path);
            } catch (Exception e) {
                success.set(false);
            }
        });
        if (!success.get()) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileDeletionFailed"));
        }
    }

    public static void delete(Path path) {
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

}
