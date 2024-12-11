package com.github.filefusion.common;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * BaseResult
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
public class BaseResult implements Serializable {

    /**
     * status success
     */
    private boolean success = true;

    /**
     * status code
     */
    private int code = HttpStatus.OK.value();

    /**
     * message
     */
    private String message = "success";

    /**
     * timestamp
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * data
     */
    private Object data = null;

    public static BaseResult ok(Object data) {
        BaseResult result = new BaseResult();
        result.setData(data);
        return result;
    }

    public static BaseResult fail(HttpStatus code, String message) {
        BaseResult result = new BaseResult();
        result.setSuccess(code.is2xxSuccessful());
        result.setCode(code.value());
        result.setMessage(message);
        return result;
    }

}
