package com.github.filefusion.file.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * FileHashUsageCount
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@AllArgsConstructor
public class FileHashUsageCount implements Serializable {

    /**
     * hash
     */
    private String hashValue;

    /**
     * mime type
     */
    private String mimeType;

    /**
     * count
     */
    private Long count;

}
