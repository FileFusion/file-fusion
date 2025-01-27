package com.github.filefusion.file.entity;

import com.github.filefusion.common.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Data;

import java.util.Date;

/**
 * FileData
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Entity(name = "file_data")
public class FileData extends BaseEntity {

    /**
     * path
     */
    private String path;

    /**
     * name
     */
    private String name;

    /**
     * mime type
     */
    private String mimeType;

    /**
     * size
     */
    private Long size;

    /**
     * encrypted
     */
    private Boolean encrypted;

    /**
     * file created date
     */
    private Date fileCreatedDate;

    /**
     * file last modified date
     */
    private Date fileLastModifiedDate;

}
