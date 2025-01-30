package com.github.filefusion.util;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * SystemFile
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public class SystemFile {

    private static String FILE_DIR;

    @Autowired
    public SystemFile(@Value("${file.dir}") String fileDir) {
        FILE_DIR = fileDir;
    }

    public static void createFolder(String folderPath) {
        folderPath = FILE_DIR + FileAttribute.SEPARATOR + folderPath;
        Path path = Paths.get(folderPath).toAbsolutePath().normalize();
        if (Files.exists(path)) {
            throw new HttpException(I18n.get("folderExits"));
        }
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new HttpException(I18n.get("folderCreationFailed", e.getMessage()));
        }
    }

    public static void delete(String filePath) {
        filePath = FILE_DIR + FileAttribute.SEPARATOR + filePath;
        Path targetPath = Paths.get(filePath).toAbsolutePath().normalize();
        if (!Files.exists(targetPath)) {
            return;
        }
        try (Stream<Path> pathStream = Files.walk(targetPath)) {
            pathStream
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new HttpException(I18n.get("fileDeletionFailed", e.getMessage()));
                        }
                    });
        } catch (IOException e) {
            throw new HttpException(I18n.get("fileDeletionFailed", e.getMessage()));
        }
    }

}
