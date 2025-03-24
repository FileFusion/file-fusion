package com.github.filefusion.constant;

import org.springframework.http.MediaType;

/**
 * FileAttribute
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class FileAttribute {

    public static final String PARENT_ROOT = "root";
    public static final String RECYCLE_BIN_ROOT = "recycle_bin";
    public static final String SEPARATOR = "/";
    public static final String DOWNLOAD_ZIP_NAME = "download.zip";
    public static final String DOWNLOAD_THUMBNAIL_NAME = "thumbnail.webp";
    public static final String THUMBNAIL_FILE_MIME_TYPE = MimeType.WEBP.value.toString();
    public static final String THUMBNAIL_FILE_SUFFIX = ".webp";

    public enum MimeType {
        FOLDER(MediaType.parseMediaType("custom/folder")),
        ZIP(MediaType.parseMediaType("application/zip")),
        WEBP(MediaType.parseMediaType("image/webp"));

        private final MediaType value;

        MimeType(MediaType value) {
            this.value = value;
        }

        public MediaType value() {
            return this.value;
        }
    }

}
