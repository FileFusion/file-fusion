package com.github.filefusion.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * OrgUser
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Entity(name = "org_user")
@IdClass(OrgUser.class)
@FieldNameConstants
public class OrgUser implements Serializable {

    /**
     * org id
     */
    @Id
    private String orgId;

    /**
     * user id
     */
    @Id
    private String userId;

}
