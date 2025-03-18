package com.github.filefusion.constant;

/**
 * RedisAttribute
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class RedisAttribute {

    public static final String SEPARATOR = ":";
    public static final String CACHE_PREFIX = "cache:";
    public static final String TOKEN_PREFIX = "token:";
    public static final String LOCK_PREFIX = "lock:";
    public static final String DOWNLOAD_ID_PREFIX = "download:";

    public enum LockType {
        task,
        file,
        cache
    }

}
