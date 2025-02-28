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
    public static final String DOWNLOAD_ZIP_NAME = "download.zip";
    public static final String THUMBNAIL_FILE_TYPE = ".png";

    public enum Type {
        FILE,
        FOLDER
    }

    public enum MimeType {
        FOLDER(MediaType.parseMediaType("custom/folder")),
        ZIP(MediaType.parseMediaType("application/zip"));

        private final MediaType value;

        MimeType(MediaType value) {
            this.value = value;
        }

        public MediaType value() {
            return this.value;
        }
    }

}
