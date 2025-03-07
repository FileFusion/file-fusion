package com.github.filefusion.file.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * SubmitDownloadFilesResponse
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@AllArgsConstructor
public class SubmitDownloadFilesResponse implements Serializable {

    /**
     * download id
     */
    private String downloadId;

}
