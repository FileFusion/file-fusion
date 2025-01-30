package com.github.filefusion.util;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

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
        File folder = new File(FILE_DIR + FileAttribute.SEPARATOR + folderPath);
        if (folder.exists()) {
            throw new HttpException(I18n.get("folderExits"));
        }
        if (!folder.mkdirs()) {
            throw new HttpException(I18n.get("folderCreationFailed"));
        }
    }

}
