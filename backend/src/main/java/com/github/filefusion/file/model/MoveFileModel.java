package com.github.filefusion.file.model;

import lombok.Data;

import java.io.Serializable;

/**
 * MoveFileModel
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
public class MoveFileModel implements Serializable {

    /**
     * source id
     */
    private String sourceId;


    /**
     * target id
     */
    private String targetId;

}
