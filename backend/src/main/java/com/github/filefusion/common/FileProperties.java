package com.github.filefusion.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * FileProperties
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Component
@ConfigurationProperties("file")
public class FileProperties {

    private Duration lockTimeout;
    private Duration downloadLinkTimeout;
    private Duration videoGenerateTimeout;
    private Duration thumbnailGenerateTimeout;
    private Path dir;
    private Path tmpDir;
    private Path videoDir;
    private Path thumbnailDir;
    private List<String> thumbnailImageMimeType;
    private List<String> thumbnailVideoMimeType;

    public void setDir(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        this.dir = dir;
    }

    public void setTmpDir(Path tmpDir) throws IOException {
        if (!Files.exists(tmpDir)) {
            Files.createDirectories(tmpDir);
        }
        this.tmpDir = tmpDir;
    }

    public void setVideoDir(Path videoDir) throws IOException {
        if (!Files.exists(videoDir)) {
            Files.createDirectories(videoDir);
        }
        this.videoDir = videoDir;
    }

    public void setThumbnailDir(Path thumbnailDir) throws IOException {
        if (!Files.exists(thumbnailDir)) {
            Files.createDirectories(thumbnailDir);
        }
        this.thumbnailDir = thumbnailDir;
    }
}
