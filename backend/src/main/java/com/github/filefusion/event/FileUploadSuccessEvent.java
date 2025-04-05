package com.github.filefusion.event;

import com.github.filefusion.common.FileProperties;
import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.constant.VideoAttribute;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.util.file.FileUtil;
import com.github.filefusion.util.file.MediaUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * FileUploadSuccessEvent
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Slf4j
@Component
public class FileUploadSuccessEvent {

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    private final FileProperties fileProperties;
    private final RBlockingDeque<FileData> queue;

    @Autowired
    public FileUploadSuccessEvent(RedissonClient redissonClient, FileProperties fileProperties) {
        this.fileProperties = fileProperties;
        this.queue = redissonClient.getBlockingDeque(RedisAttribute.EVENT_PREFIX + RedisAttribute.EventType.file_upload_success);
    }

    @PostConstruct
    public void startListening() {
        if (!RUNNING.compareAndSet(false, true)) {
            log.warn("File upload success event is already running");
            return;
        }
        log.info("Starting file upload success event listener");
        EXECUTOR.execute(() -> {
            while (RUNNING.get()) {
                try {
                    FileData file = queue.takeLast();
                    generateMediaDash(file.getHashValue(), file.getMimeType());
                } catch (InterruptedException e) {
                    log.error("File upload success event listener interrupted");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("File upload success event listener failed", e);
                }
            }
        });
    }

    @PreDestroy
    public void stopListening() {
        RUNNING.set(false);
        EXECUTOR.shutdownNow();
        log.info("Stopping file upload success event listener");
    }

    private void generateMediaDash(String hashValue, String mimeType) {
        try {
            if (Boolean.TRUE.equals(fileProperties.getVideoPlay()) && MediaUtil.supportGenerateDash(mimeType, fileProperties.getVideoPlayMimeType())) {
                log.info("Generating dash file {}", hashValue);
                MediaUtil.generateMediaDash(FileUtil.getHashPath(fileProperties.getDir(), hashValue),
                        FileUtil.getHashPath(fileProperties.getVideoPlayDir(), hashValue).resolve(VideoAttribute.MEDIA_MANIFEST_NAME),
                        fileProperties.getVideoGenerateTimeout());
                log.info("Dash file generated {}", hashValue);
            }
        } catch (Exception e) {
            log.error("Error while generating media dash", e);
        }
    }

}
