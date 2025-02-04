package com.github.filefusion.util;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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

    public static void createFolder(String path) {
        String folderPath = FILE_DIR + FileAttribute.SEPARATOR + path;
        Path targetPath = Paths.get(folderPath).toAbsolutePath().normalize();
        if (Files.exists(targetPath)) {
            return;
        }
        try {
            Files.createDirectories(targetPath);
        } catch (IOException e) {
            throw new HttpException(I18n.get("folderCreationFailed", e.getMessage()));
        }
    }

    public static void upload(MultipartFile file, String path) {
        String filePath = FILE_DIR + FileAttribute.SEPARATOR + path;
        Path targetPath = Paths.get(filePath).toAbsolutePath().normalize();
        try {
            if (Files.exists(targetPath)) {
                delete(path);
            }
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException e) {
            throw new HttpException(I18n.get("fileUploadFailed", e.getMessage()));
        }
    }

    public static void delete(String path) {
        String filePath = FILE_DIR + FileAttribute.SEPARATOR + path;
        Path targetPath = Paths.get(filePath).toAbsolutePath().normalize();
        if (!Files.exists(targetPath)) {
            return;
        }
        try (Stream<Path> pathStream = Files.walk(targetPath)) {
            pathStream
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new HttpException(I18n.get("fileDeletionFailed", e.getMessage()));
                        }
                    });
        } catch (IOException e) {
            throw new HttpException(I18n.get("fileDeletionFailed", e.getMessage()));
        }
    }

}
