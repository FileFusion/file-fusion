package com.github.filefusion.file.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * UploadFileModel
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
public class UploadFileModel implements Serializable {

    /**
     * file
     */
    private MultipartFile file;

    /**
     * parent id
     */
    private String parentId;

    /**
     * name
     */
    private String name;

    /**
     * path
     */
    private String path;

    /**
     * md5 value
     */
    private String md5Value;


    /**
     * mime type
     */
    private String mimeType;

    /**
     * file last modified date
     */
    private Long fileLastModifiedDate;

}
