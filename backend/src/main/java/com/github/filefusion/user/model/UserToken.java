package com.github.filefusion.user.model;

import com.github.filefusion.util.EncryptUtil;
import com.github.filefusion.util.Json;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * UserToken
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserToken implements Serializable {

    private final static String TOKEN_HEADER = "Bearer ";

    private String userId;
    private Date createdDate;
    private String random;

    public static UserToken decoder(String token) {
        String userTokenJson = EncryptUtil.aesDecoder(token.substring(TOKEN_HEADER.length()));
        return Json.parseObject(userTokenJson, UserToken.class);
    }

    public static String encoder(String userId) {
        UserToken userToken = new UserToken(userId, new Date(), UUID.randomUUID().toString());
        return TOKEN_HEADER + EncryptUtil.aesEncoder(Json.toJsonString(userToken));
    }

}
