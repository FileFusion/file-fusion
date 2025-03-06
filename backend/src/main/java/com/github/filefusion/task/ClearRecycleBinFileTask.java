package com.github.filefusion.task;

import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.constant.SysConfigKey;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.repository.FileDataRepository;
import com.github.filefusion.file.service.FileDataService;
import com.github.filefusion.sys_config.entity.SysConfig;
import com.github.filefusion.sys_config.service.SysConfigService;
import com.github.filefusion.util.DistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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
    private final SysConfigService sysConfigService;
    private final FileDataRepository fileDataRepository;
    private final FileDataService fileDataService;

    @Autowired
    public ClearRecycleBinFileTask(@Value("${task.lock-timeout}") Duration taskLockTimeout,
                                   DistributedLock distributedLock,
                                   SysConfigService sysConfigService,
                                   FileDataRepository fileDataRepository,
                                   FileDataService fileDataService) {
        this.taskLockTimeout = taskLockTimeout;
        this.distributedLock = distributedLock;
        this.sysConfigService = sysConfigService;
        this.fileDataRepository = fileDataRepository;
        this.fileDataService = fileDataService;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void clearRecycleBinFileTask() {
        distributedLock.tryLock(RedisAttribute.LockType.task, "clearRecycleBinFileTask", () -> {
            SysConfig recycleBinConfig = sysConfigService.get(SysConfigKey.RECYCLE_BIN);
            SysConfig recycleBinRetentionDaysConfig = sysConfigService.get(SysConfigKey.RECYCLE_BIN_RETENTION_DAYS);
            boolean recycleBin = Boolean.parseBoolean(recycleBinConfig.getConfigValue());
            int recycleBinRetentionDays = Integer.parseInt(recycleBinRetentionDaysConfig.getConfigValue());
            if (!recycleBin || recycleBinRetentionDays <= 0) {
                return;
            }
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(recycleBinRetentionDays)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
            List<FileData> fileList = fileDataRepository.findAllByDeletedTrueAndDeletedDateBefore(cutoffDate);
//            fileDataService.batchDeleteFromRecycleBin(fileList);
        }, taskLockTimeout);
    }

}
