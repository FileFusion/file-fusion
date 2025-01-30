package com.github.filefusion.file.model;

import lombok.Data;

import java.io.Serializable;

/**
 * CreateFolderModel
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
public class CreateFolderModel implements Serializable {

    /**
     * path
     */
    private String path;

    /**
     * name
     */
    private String name;

}
