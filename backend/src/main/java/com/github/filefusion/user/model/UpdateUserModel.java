package com.github.filefusion.user.model;

import lombok.Data;

import java.io.Serializable;

/**
 * UpdateUserModel
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
public class UpdateUserModel implements Serializable {

    /**
     *
     */
    private String id;

    /**
     * username
     */
    private String username;

    /**
     * name
     */
    private String name;

    /**
     * email
     */
    private String email;

    /**
     * area code
     */
    private String areaCode;

    /**
     * phone
     */
    private String phone;

}
