package com.github.filefusion.util.file;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.util.EncryptUtil;
import com.github.filefusion.util.I18n;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * FileUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class FileUtil {

    public static String getHashPath(String hash) {
        if (!StringUtils.hasLength(hash) || hash.length() != 32 || !hash.matches("^[a-zA-Z0-9]+$")) {
            throw new HttpException(I18n.get("filePathFormatError"));
        }
        return Paths.get(hash.substring(0, 2), hash.substring(2, 4), hash).toString();
    }

    public static String calculateMd5(Path path) {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            MessageDigest md = MessageDigest.getInstance(EncryptUtil.MD5);
            long fileSize = channel.size();
            long position = 0;
            while (position < fileSize) {
                long chunkSize = Math.min(fileSize - position, Integer.MAX_VALUE);
                md.update(channel.map(FileChannel.MapMode.READ_ONLY, position, chunkSize));
                position += chunkSize;
            }
            return EncryptUtil.bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("getFileHashFailed"));
        }
    }

    public static void upload(MultipartFile file, Path path) {
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(path.getParent());
            Files.copy(in, path);
        } catch (IOException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileUploadFailed"));
        }
    }

    public static void merge(Path chunkDirPath, Path targetPath) {
        try (FileChannel outputChannel = FileChannel.open(targetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            for (int i = 0; ; i++) {
                Path chunk = chunkDirPath.resolve(String.valueOf(i));
                if (!Files.isRegularFile(chunk)) {
                    break;
                }
                try (FileChannel inputChannel = FileChannel.open(chunk, StandardOpenOption.READ)) {
                    inputChannel.transferTo(outputChannel.size(), inputChannel.size(), outputChannel);
                }
            }
        } catch (IOException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileUploadFailed"));
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
                @Nonnull
                public FileVisitResult visitFile(Path file, @Nonnull BasicFileAttributes attrs) {
                    try {
                        Files.deleteIfExists(file);
                    } catch (IOException e) {
                        success.set(false);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                @Nonnull
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
