package com.github.filefusion.task;

import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.util.DistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * ClearThumbnailFileTask
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public class ClearThumbnailFileTask {

    private final Duration taskLockTimeout;
    private final DistributedLock distributedLock;

    @Autowired
    public ClearThumbnailFileTask(@Value("${task.lock-timeout}") Duration taskLockTimeout,
                                  DistributedLock distributedLock) {
        this.taskLockTimeout = taskLockTimeout;
        this.distributedLock = distributedLock;
    }

    @Scheduled(cron = "${task.clear-thumbnail-file}")
    public void clearThumbnailFileTask() {
        distributedLock.tryLock(RedisAttribute.LockType.task, "clearThumbnailFileTask", () -> {
            // todo clearThumbnailFile
        }, taskLockTimeout);
    }

}
