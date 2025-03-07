package com.github.filefusion.file.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * FileMd5UsageCount
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@AllArgsConstructor
public class FileMd5UsageCount implements Serializable {

    /**
     * md5
     */
    private String md5Value;

    /**
     * mime type
     */
    private String mimeType;

    /**
     * count
     */
    private Long count;

}
