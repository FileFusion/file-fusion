package com.github.filefusion.task;

import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.util.DistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * ClearRecycleBinFileTask
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public class ClearRecycleBinFileTask {

    private final Duration taskLockTimeout;
    private final DistributedLock distributedLock;

    @Autowired
    public ClearRecycleBinFileTask(@Value("${task.lock-timeout}") Duration taskLockTimeout,
                                   DistributedLock distributedLock) {
        this.taskLockTimeout = taskLockTimeout;
        this.distributedLock = distributedLock;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void clearRecycleBinFileTask() {
        distributedLock.tryLock(RedisAttribute.LockType.task, "clearRecycleBinFileTask", () -> {
            // todo clearRecycleBinFile
        }, taskLockTimeout);
    }

}
