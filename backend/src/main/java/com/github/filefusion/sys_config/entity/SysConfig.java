package com.github.filefusion.sys_config.entity;

import com.github.filefusion.common.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * SysConfig
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Entity(name = "sys_config")
@FieldNameConstants
public class SysConfig extends BaseEntity {

    /**
     * config key
     */
    private String configKey;

    /**
     * config value
     */
    private String configValue;

    /**
     * description
     */
    private String description;

}
