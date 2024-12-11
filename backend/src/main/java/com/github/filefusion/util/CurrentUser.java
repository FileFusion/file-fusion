package com.github.filefusion.util;

import com.github.filefusion.user.entity.UserInfo;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * CurrentUser
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class CurrentUser {

    public static UserInfo get() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
