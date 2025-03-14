package com.github.filefusion.util;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * CurrentUser
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class CurrentUser {

    public static String getId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
