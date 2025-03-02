package com.github.filefusion.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * RecycleBinUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public final class RecycleBinUtil {

    private final Path baseDir;

    @Autowired
    public RecycleBinUtil(@Value("${recycle-bin.dir}") String recycleBinDir) {
        this.baseDir = Paths.get(recycleBinDir).normalize().toAbsolutePath();
        if (!Files.exists(this.baseDir)) {
            try {
                Files.createDirectories(this.baseDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
