package com.github.filefusion.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * TimeUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class TimeUtil {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    public static LocalDateTime fromMillis(long milliseconds, ZoneId... zone) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), zone.length > 0 ? zone[0] : DEFAULT_ZONE);
    }

}
