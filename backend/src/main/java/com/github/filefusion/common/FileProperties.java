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
    private Duration thumbnailGenerateTimeout;
    private Path dir;
    private Path tmpDir;
    private Path recycleBinDir;
    private Path thumbnailDir;
    private List<String> thumbnailImageMimeType;
    private List<String> thumbnailVideoMimeType;

    public FileProperties() {
        try {
            if (!Files.exists(this.dir)) {
                Files.createDirectories(this.dir);
            }
            if (!Files.exists(this.tmpDir)) {
                Files.createDirectories(this.tmpDir);
            }
            if (!Files.exists(this.recycleBinDir)) {
                Files.createDirectories(this.recycleBinDir);
            }
            if (!Files.exists(this.thumbnailDir)) {
                Files.createDirectories(this.thumbnailDir);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
