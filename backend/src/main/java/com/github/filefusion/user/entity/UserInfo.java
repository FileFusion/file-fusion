package com.github.filefusion.user.entity;

import com.github.filefusion.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserInfo
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Entity(name = "user_info")
@FieldNameConstants
public class UserInfo extends BaseEntity {

    /**
     * username
     */
    private String username;

    /**
     * password
     */
    private String password;

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

    /**
     * earliest credentials
     */
    private LocalDateTime earliestCredentials;

    /**
     * is systemd user
     */
    private Boolean systemdUser;

    /**
     * non expired
     */
    private Boolean nonExpired;

    /**
     * non locked
     */
    private Boolean nonLocked;

    /**
     * credentials non expired
     */
    private Boolean credentialsNonExpired;

    /**
     * enabled
     */
    private Boolean enabled;

    /**
     * role ids
     */
    @Transient
    private List<String> roleIds;

    /**
     * permission ids
     */
    @Transient
    private List<String> permissionIds;

}
