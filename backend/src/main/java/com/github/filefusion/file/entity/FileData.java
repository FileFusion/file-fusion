package com.github.filefusion.file.entity;

import com.github.filefusion.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

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
     * user id
     */
    private String userId;

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
     * hash value
     */
    private String hashValue;

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
     * file last modified date
     */
    private LocalDateTime fileLastModifiedDate;

    /**
     * deleted
     */
    private Boolean deleted;

    /**
     * deleted date
     */
    private LocalDateTime deletedDate;

    /**
     * has thumbnail
     */
    @Transient
    private Boolean hasThumbnail;

    /**
     * can play
     */
    @Transient
    private Boolean canPlay;

}
