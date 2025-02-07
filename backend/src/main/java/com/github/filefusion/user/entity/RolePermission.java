package com.github.filefusion.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * RolePermission
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Entity(name = "role_permission")
@IdClass(RolePermission.class)
@FieldNameConstants
public class RolePermission implements Serializable {

    /**
     * role id
     */
    @Id
    private String roleId;

    /**
     * permission id
     */
    @Id
    private String permissionId;

}
