package com.github.filefusion.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * I18n
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public class I18n {

    private static MessageSource MESSAGE_SOURCE;

    @Autowired
    public I18n(MessageSource messageSource) {
        MESSAGE_SOURCE = messageSource;
    }

    public static String get(String message) {
        return get(message, new Object[]{});
    }

    public static String get(String message, Object[] args) {
        if (args == null) {
            args = new Object[]{};
        }
        return MESSAGE_SOURCE.getMessage(message, args, LocaleContextHolder.getLocale());
    }

}
