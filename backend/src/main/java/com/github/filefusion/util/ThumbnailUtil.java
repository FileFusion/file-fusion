package com.github.filefusion.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

/**
 * ThumbnailUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public final class ThumbnailUtil {

    private final Path baseDir;
    private final Duration thumbnailGenerateTimeout;
    private final List<String> thumbnailMimeType;

    @Autowired
    public ThumbnailUtil(@Value("${thumbnail.dir}") String thumbnailDir,
                         @Value("${thumbnail.generate-timeout}") Duration thumbnailGenerateTimeout,
                         @Value("${thumbnail.mime-type}") List<String> thumbnailMimeType) {
        this.baseDir = Paths.get(thumbnailDir).normalize().toAbsolutePath();
        this.thumbnailGenerateTimeout = thumbnailGenerateTimeout;
        this.thumbnailMimeType = thumbnailMimeType;
        System.out.println("aaa");
    }

}
