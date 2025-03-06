package com.github.filefusion.common;

import com.github.filefusion.config.ULIDGeneratorType;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * BaseEntity
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    /**
     * id
     */
    @Id
    @ULIDGeneratorType.ULIDGenerator
    private String id;

    /**
     * created by
     */
    @CreatedBy
    private String createdBy;

    /**
     * created date
     */
    @CreatedDate
    private LocalDateTime createdDate;

    /**
     * last modified by
     */
    @LastModifiedBy
    private String lastModifiedBy;

    /**
     * last modified date
     */
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

}
