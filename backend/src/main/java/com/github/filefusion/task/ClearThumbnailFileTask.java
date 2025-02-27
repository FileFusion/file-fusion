package com.github.filefusion.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ClearThumbnailFileTask
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public class ClearThumbnailFileTask {

    @Scheduled(cron = "${task.clear-thumbnail-file}")
    public void clearThumbnailFileTask() {
        // todo clearThumbnailFile
    }

}
