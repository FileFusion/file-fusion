package com.github.filefusion.user.model;

import lombok.Data;

import java.io.Serializable;

/**
 * UpdateUserPasswordModel
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
public class UpdateUserPasswordModel implements Serializable {

    /**
     * original password
     */
    private String originalPassword;

    /**
     * new password
     */
    private String newPassword;

}
