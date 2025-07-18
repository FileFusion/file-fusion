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
     * id
     */
    private String id;


    /**
     * name
     */
    private String name;

}
