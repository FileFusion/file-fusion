package com.github.filefusion.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;

import java.io.Serializable;

/**
 * UserRole
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Entity(name = "user_role")
@IdClass(UserRole.class)
public class UserRole implements Serializable {

    /**
     * user id
     */
    @Id
    private String userId;

    /**
     * role id
     */
    @Id
    private String roleId;

}
