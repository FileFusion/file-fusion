package com.github.filefusion.user.entity;

import com.github.filefusion.common.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * Role
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Entity(name = "role")
@FieldNameConstants
public class Role extends BaseEntity {

    /**
     * name
     */
    private String name;

    /**
     * name
     */
    private String description;

    /**
     * is system role
     */
    private Boolean systemRole;

}
