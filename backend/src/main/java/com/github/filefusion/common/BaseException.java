package com.github.filefusion.common;

import java.io.Serializable;

/**
 * BaseException
 *
 * @author hackyo
 * @since 2022/4/1
 */
public class BaseException extends RuntimeException implements Serializable {

    public BaseException() {
        super();
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

}
