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
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
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

    public static void createFolder(List<String> folderPathList) {
        for (String folderPath : folderPathList) {
            Path path = Paths.get(FILE_DIR + FileAttribute.SEPARATOR + folderPath).toAbsolutePath().normalize();
            if (Files.exists(path)) {
                continue;
            }
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new HttpException(I18n.get("folderCreationFailed", e.getMessage()));
            }
        }
    }

    public static void upload(MultipartFile file, String name, String path) {
        String filePath = FILE_DIR + FileAttribute.SEPARATOR + path + FileAttribute.SEPARATOR + name;
        Path p = Paths.get(filePath).toAbsolutePath().normalize();
        try {
            Files.copy(file.getInputStream(), p, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new HttpException(I18n.get("fileUploadFailed", e.getMessage()));
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
