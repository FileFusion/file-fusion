package com.github.filefusion.user.entity;

import com.github.filefusion.common.BaseEntity;
import com.github.filefusion.util.I18n;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;

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
public class UserInfo extends BaseEntity implements UserDetails {

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
     * roles
     */
    @Transient
    private List<Role> roles;

    /**
     * permissions
     */
    @Transient
    private List<Permission> permissions;

    @Override
    public boolean isAccountNonExpired() {
        return this.nonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.nonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    @Transient
    public List<Permission> getAuthorities() {
        return this.permissions;
    }

    public void verifyUser() throws AccountStatusException {
        if (!this.isAccountNonExpired()) {
            throw new AccountExpiredException(I18n.get("userExpired"));
        }
        if (!this.isAccountNonLocked()) {
            throw new LockedException(I18n.get("userLocked"));
        }
        if (!this.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException(I18n.get("userPasswordExpired"));
        }
        if (!this.isEnabled()) {
            throw new DisabledException(I18n.get("userDisabled"));
        }
    }

}
