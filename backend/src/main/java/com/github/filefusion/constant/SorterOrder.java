package com.github.filefusion.constant;

import org.springframework.data.domain.Sort;

/**
 * SorterOrder
 *
 * @author hackyo
 * @since 2022/4/1
 */
public enum SorterOrder {

    /**
     * asc
     */
    ascend,

    /**
     * desc
     */
    descend;

    public Sort.Direction order() {
        return switch (this) {
            case ascend -> Sort.Direction.ASC;
            case descend -> Sort.Direction.DESC;
        };
    }

}
