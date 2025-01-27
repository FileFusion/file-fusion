package com.github.filefusion.util;

/**
 * ULID
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class ULID {

    private static final de.huxhorn.sulky.ulid.ULID ULID = new de.huxhorn.sulky.ulid.ULID();

    public static String randomULID() {
        return ULID.nextULID();
    }

}
