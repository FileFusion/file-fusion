package com.github.filefusion.user.entity;

import com.github.filefusion.common.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

/**
 * Permission
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Entity(name = "permission")
public class Permission extends BaseEntity implements GrantedAuthority {

    /**
     * parent id
     */
    private String parentId;

    /**
     * name
     */
    private String name;

    /**
     * description
     */
    private String description;

    /**
     * is basics
     */
    private Boolean basics;

    @Override
    public String getAuthority() {
        return this.getId();
    }

}
