package com.github.filefusion.task;

import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.util.DistributedLock;
import com.github.filefusion.util.file.ThumbnailUtil;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * ClearThumbnailFileTask
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public class ClearThumbnailFileTask {

    private static final int BATCH_FILE_SIZE = 1000;

    private final Duration taskLockTimeout;
    private final DistributedLock distributedLock;
    private final ThumbnailUtil thumbnailUtil;

    @Autowired
    public ClearThumbnailFileTask(@Value("${task.lock-timeout}") Duration taskLockTimeout,
                                  DistributedLock distributedLock,
                                  ThumbnailUtil thumbnailUtil) {
        this.taskLockTimeout = taskLockTimeout;
        this.distributedLock = distributedLock;
        this.thumbnailUtil = thumbnailUtil;
    }

    @Scheduled(cron = "${task.clear-thumbnail-file}")
    public void clearThumbnailFileTask() {
        distributedLock.tryLock(RedisAttribute.LockType.task, "clearThumbnailFileTask", () -> {
            try {
                Map<String, Path> thumbnailMap = new HashMap<>();
                Files.walkFileTree(thumbnailUtil.getBaseDir(), new SimpleFileVisitor<>() {
                    @Override
                    @Nonnull
                    public FileVisitResult visitFile(Path file, @Nonnull BasicFileAttributes attrs) {
                        try {
                            String fileName = file.getFileName().toString();
                            if (!FileAttribute.THUMBNAIL_FILE_SUFFIX.equals(fileName)
                                    && fileName.endsWith(FileAttribute.THUMBNAIL_FILE_SUFFIX)) {
                                String md5 = fileName.substring(0, fileName.length() - FileAttribute.THUMBNAIL_FILE_SUFFIX.length());
                                thumbnailMap.put(md5, file);
                                if (thumbnailMap.size() >= BATCH_FILE_SIZE) {
                                    thumbnailUtil.clearThumbnail(new HashMap<>(thumbnailMap));
                                    thumbnailMap.clear();
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                if (!thumbnailMap.isEmpty()) {
                    thumbnailUtil.clearThumbnail(thumbnailMap);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, taskLockTimeout);
    }

}
