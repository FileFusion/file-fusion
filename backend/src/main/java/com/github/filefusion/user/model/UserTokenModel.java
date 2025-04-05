package com.github.filefusion.user.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * UserTokenModel
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
public class UserTokenModel implements Serializable {

    /**
     * user agent
     */
    private String userAgent;

    /**
     * client ip
     */
    private String clientIp;

    /**
     * issued at
     */
    private Date issuedAt;

}
