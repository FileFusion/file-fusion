package com.github.filefusion.util.file;

import com.github.filefusion.util.EncryptUtil;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.digest.Blake3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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
@Slf4j
public final class FileUtil {

    private static final int BUFFER_SIZE = 4 * 1024 * 1024;

    public static void transferTo(Path path, WritableByteChannel outChannel) throws IOException {
        transferTo(path, outChannel, 0, Long.MAX_VALUE);
    }

    public static void transferTo(Path path, WritableByteChannel outChannel, long start, long end) throws IOException {
        try (FileChannel inChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            long total = inChannel.size();
            start = Math.min(Math.max(start, 0), total);
            end = Math.min(end, total);
            if (start >= end) {
                log.error("File read index exceeded");
                return;
            }
            while (start < end) {
                long transferred = inChannel.transferTo(start, end - start, outChannel);
                if (transferred == 0) {
                    long currentPosition = inChannel.position();
                    if (currentPosition >= total) {
                        break;
                    }
                    log.error("File read index exceeded");
                    throw new IOException();
                }
                start += transferred;
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static Path getHashPath(Path dir, String hash, String... extension) {
        String path = Paths.get(hash.substring(0, 2), hash.substring(2, 4), hash).toString();
        if (extension.length > 0) {
            return dir.resolve(path + extension[0]);
        } else {
            return dir.resolve(path);
        }
    }

    public static String calculateHash(Path path) {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            Blake3.Blake3_256 digest = new Blake3.Blake3_256();
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (channel.read(buffer) != -1) {
                buffer.flip();
                digest.update(buffer.array(), buffer.position(), buffer.remaining());
                buffer.clear();
            }
            return EncryptUtil.bytesToHex(digest.digest());
        } catch (IOException ignored) {
            return null;
        }
    }

    public static void chunkMerge(Path chunkDirPath, Path targetPath) throws IOException {
        try {
            Files.createDirectories(targetPath.getParent());
            try (Stream<Path> chunkPathStream = Files.list(chunkDirPath);
                 FileChannel outChannel = FileChannel.open(targetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                List<Path> chunkList = chunkPathStream
                        .sorted(Comparator.comparing(p -> Integer.parseInt(p.getFileName().toString())))
                        .toList();
                for (Path chunk : chunkList) {
                    transferTo(chunk, outChannel);
                }
            }
            delete(chunkDirPath);
        } catch (IOException e) {
            delete(targetPath);
            throw new IOException(e);
        }
    }

    public static void delete(List<Path> pathList) throws FileDeletionFailedException {
        AtomicBoolean success = new AtomicBoolean(true);
        pathList.forEach(path -> {
            try {
                delete(path);
            } catch (Exception e) {
                log.error("Error deleting file", e);
                success.set(false);
            }
        });
        if (!success.get()) {
            throw new FileDeletionFailedException();
        }
    }

    public static void delete(Path path) throws FileDeletionFailedException {
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
                        log.error("Error deleting file", e);
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
                        log.error("Error deleting file", e);
                        success.set(false);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            log.error("Error deleting file", e);
            success.set(false);
        }
        if (!success.get()) {
            throw new FileDeletionFailedException();
        }
    }

    public static class FileDeletionFailedException extends IOException {
    }

}
