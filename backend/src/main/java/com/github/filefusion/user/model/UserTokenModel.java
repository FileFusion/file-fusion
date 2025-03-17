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

    private String userAgent;
    private String clientIp;
    private Date issuedAt;

}
