package com.github.filefusion.license.entity;

import com.github.filefusion.common.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * License
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Entity(name = "license")
@FieldNameConstants
public class License extends BaseEntity {

    /**
     * authorized to
     */
    private String authorizedTo;

    /**
     * edition
     */
    private String edition;

    /**
     * start date
     */
    private LocalDateTime startDate;

    /**
     * end date
     */
    private LocalDateTime endDate;

}
