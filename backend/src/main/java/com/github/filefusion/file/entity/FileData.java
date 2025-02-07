package com.github.filefusion.file.entity;

import com.github.filefusion.common.BaseEntity;
import com.github.filefusion.constant.FileAttribute;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.Date;

/**
 * FileData
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Entity(name = "file_data")
@FieldNameConstants
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
     * type
     */
    private FileAttribute.Type type;

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
     * hash value
     */
    private String hashValue;

    /**
     * file last modified date
     */
    private Date fileLastModifiedDate;

}
