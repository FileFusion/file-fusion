package com.github.filefusion.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ClearRecycleBinFileTask
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public class ClearRecycleBinFileTask {

    @Scheduled(cron = "0 0 * * * ?")
    public void clearRecycleBinFileTask() {
        // todo clearRecycleBinFile
    }

}
