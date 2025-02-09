package com.github.filefusion.file.model;

import lombok.Data;

import java.io.Serializable;

/**
 * RenameFileModel
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
public class RenameFileModel implements Serializable {

    /**
     * path
     */
    private String path;

    /**
     * original name
     */
    private String originalName;

    /**
     * target name
     */
    private String targetName;

}
