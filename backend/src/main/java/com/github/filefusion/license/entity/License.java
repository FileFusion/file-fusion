package com.github.filefusion.license.entity;

import com.github.filefusion.common.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Data;

import java.util.Date;

/**
 * License
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Entity(name = "license")
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
    private Date startDate;

    /**
     * end date
     */
    private Date endDate;

}
