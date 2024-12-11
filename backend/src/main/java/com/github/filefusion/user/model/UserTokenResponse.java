package com.github.filefusion.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * UserTokenResponse
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTokenResponse implements Serializable {

    /**
     * token
     */
    private String token;

}
