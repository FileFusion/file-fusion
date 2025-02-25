package com.github.filefusion.constant;

import org.springframework.http.MediaType;

/**
 * FileAttribute
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class FileAttribute {

    public static final String SEPARATOR = "/";
    public static final MediaType FOLDER_MIME_TYPE = MediaType.parseMediaType("custom/folder");
    public static final String DOWNLOAD_ZIP_NAME = "download.zip";
    public static final MediaType ZIP_MIME_TYPE = MediaType.parseMediaType("application/zip");
    public static final String THUMBNAIL_FILE_TYPE = ".png";

    public enum Type {
        FILE,
        FOLDER
    }

}
