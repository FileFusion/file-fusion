package com.github.filefusion.util.file;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.util.EncryptUtil;
import com.github.filefusion.util.I18n;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * FileUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class FileUtil {

    public static void transferTo(Path path, WritableByteChannel outChannel) throws IOException {
        transferTo(path, outChannel, true);
    }

    public static void transferTo(Path path, WritableByteChannel outChannel, boolean closeOut) throws IOException {
        transferTo(path, outChannel, closeOut, 0, Long.MAX_VALUE);
    }

    public static void transferTo(Path path, WritableByteChannel outChannel, boolean closeOut, long start, long end) throws IOException {
        try (FileChannel inChannel = FileChannel.open(path, StandardOpenOption.READ);
             AutoCloseable ignored = closeOut ? outChannel : null) {
            long total = inChannel.size();
            start = Math.min(Math.max(start, 0), total);
            end = Math.min(end, total);
            if (start >= end) {
                return;
            }
            while (start < end) {
                long transferred = inChannel.transferTo(start, end - start, outChannel);
                if (transferred == 0) {
                    long currentPosition = inChannel.position();
                    if (currentPosition >= total) {
                        break;
                    }
                    throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileDownloadFailed"));
                }
                start += transferred;
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static String getHashPath(String hash) {
        if (!StringUtils.hasLength(hash) || hash.length() != 32 || !hash.matches("^[a-zA-Z0-9]+$")) {
            throw new HttpException(I18n.get("filePathFormatError"));
        }
        return Paths.get(hash.substring(0, 2), hash.substring(2, 4), hash).toString();
    }

    public static String calculateHash(Path path) {
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
        try {
            Files.createDirectories(path.getParent());
            file.transferTo(path);
        } catch (IOException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("fileUploadFailed"));
        }
    }

    public static void chunkMerge(Path chunkDirPath, Path targetPath) {
        try {
            Files.createDirectories(targetPath.getParent());
            try (Stream<Path> chunkPathStream = Files.list(chunkDirPath);
                 FileChannel outChannel = FileChannel.open(targetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                List<Path> chunkList = chunkPathStream
                        .sorted(Comparator.comparing(p -> Integer.parseInt(p.getFileName().toString())))
                        .toList();
                for (Path chunk : chunkList) {
                    transferTo(chunk, outChannel, false);
                }
            }
            delete(chunkDirPath);
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
