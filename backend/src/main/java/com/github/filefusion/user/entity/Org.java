package com.github.filefusion.user.entity;

import com.github.filefusion.common.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Data;

/**
 * Org
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Entity(name = "org")
public class Org extends BaseEntity {

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

}
