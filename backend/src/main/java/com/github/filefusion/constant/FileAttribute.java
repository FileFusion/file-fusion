package com.github.filefusion.constant;

/**
 * FileAttribute
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class FileAttribute {

    public static final String SEPARATOR = "/";
    public static final String FOLDER_MIME_TYPE = "custom/folder";
    public static final String DOWNLOAD_ZIP_NAME = "download.zip";
    public static final String THUMBNAIL_FILE_TYPE = ".png";

    public enum Type {
        FILE,
        FOLDER
    }

}
