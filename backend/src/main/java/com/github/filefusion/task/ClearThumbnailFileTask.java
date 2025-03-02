package com.github.filefusion.task;

import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.file.service.FileDataService;
import com.github.filefusion.util.DistributedLock;
import com.github.filefusion.util.file.ThumbnailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
    private final FileDataService fileDataService;

    @Autowired
    public ClearThumbnailFileTask(@Value("${task.lock-timeout}") Duration taskLockTimeout,
                                  DistributedLock distributedLock,
                                  ThumbnailUtil thumbnailUtil,
                                  FileDataService fileDataService) {
        this.taskLockTimeout = taskLockTimeout;
        this.distributedLock = distributedLock;
        this.thumbnailUtil = thumbnailUtil;
        this.fileDataService = fileDataService;
    }

    /**
     * Because the format of the files to be previewed can be adjusted at any time,
     * So need to clean up expired preview images regularly.
     */
    @Scheduled(cron = "${task.clear-thumbnail-file}")
    public void clearThumbnailFileTask() {
        distributedLock.tryLock(RedisAttribute.LockType.task, "clearThumbnailFileTask", () -> {
            try (Stream<Path> baseDirStream = Files.list(thumbnailUtil.getBaseDir())) {
                List<String> pathList = new ArrayList<>(BATCH_FILE_SIZE);
                baseDirStream.filter(path -> {
                    String fileName = path.getFileName().toString();
                    return !fileName.equals(FileAttribute.THUMBNAIL_FILE_TYPE)
                            && fileName.endsWith(FileAttribute.THUMBNAIL_FILE_TYPE);
                }).map(path -> {
                    String fileName = path.getFileName().toString();
                    return fileName.substring(0, fileName.length() - FileAttribute.THUMBNAIL_FILE_TYPE.length());
                }).forEach(path -> {
                    pathList.add(path);
                    if (pathList.size() >= BATCH_FILE_SIZE) {
                        fileDataService.clearThumbnailFile(new ArrayList<>(pathList));
                        pathList.clear();
                    }
                });
                if (!pathList.isEmpty()) {
                    fileDataService.clearThumbnailFile(new ArrayList<>(pathList));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, taskLockTimeout);
    }

}
