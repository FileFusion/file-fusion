package com.github.filefusion.file.controller;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.SorterOrder;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.service.FileDataService;
import com.github.filefusion.user.entity.UserInfo;
import com.github.filefusion.util.CurrentUser;
import com.github.filefusion.util.I18n;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

/**
 * FileDataController
 *
 * @author hackyo
 * @since 2022/4/1
 */
@RestController
@RequestMapping("/file_data")
public class FileDataController {

    private final FileDataService fileDataService;

    @Autowired
    public FileDataController(FileDataService fileDataService) {
        this.fileDataService = fileDataService;
    }

    /**
     * get file list - paged
     *
     * @param page        page
     * @param pageSize    page size
     * @param path        path
     * @param name        name
     * @param sorter      sorter
     * @param sorterOrder sorter order
     * @return file list
     */
    @GetMapping("/{page}/{pageSize}")
    @PreAuthorize("hasAuthority('personal_file:read')")
    public Page<FileData> get(@PathVariable Integer page, @PathVariable Integer pageSize,
                              @RequestParam(required = false) String path,
                              @RequestParam(required = false) String name,
                              @RequestParam(required = false) String sorter,
                              @RequestParam(required = false) SorterOrder sorterOrder) {
        if (!StringUtils.hasLength(sorter)) {
            sorter = "name";
        }
        if (sorterOrder == null) {
            sorterOrder = SorterOrder.ascend;
        }
        if (!StringUtils.hasLength(path)) {
            path = CurrentUser.get().getId() + FileAttribute.SEPARATOR;
        } else {
            path = CurrentUser.get().getId() + FileAttribute.SEPARATOR + path + FileAttribute.SEPARATOR;
        }
        return fileDataService.get(PageRequest.of(page - 1, pageSize, sorterOrder.order(), sorter), path, name);
    }


    /**
     * batch delete file
     *
     * @param pathList path list
     */
    @PostMapping("/_batch_delete")
    @PreAuthorize("hasAuthority('personal_file:delete')")
    public void batchDelete(@RequestBody List<String> pathList) {
        if (CollectionUtils.isEmpty(pathList)) {
            return;
        }
        String userPath = CurrentUser.get().getId() + FileAttribute.SEPARATOR;
        for (String path : pathList) {
            if (!StringUtils.startsWithIgnoreCase(path, userPath)) {
                throw new HttpException(I18n.get("noOperationPermission"));
            }
        }
        fileDataService.batchDelete(pathList);
    }

    /**
     * create folder
     *
     * @param fileData path
     */
    @PostMapping("/_create_folder")
    @PreAuthorize("hasAuthority('personal_file:add')")
    public void createFolder(@RequestBody FileData fileData) {
        String path = fileData.getPath();
        if (!StringUtils.hasLength(path)) {
            path = CurrentUser.get().getId();
        } else {
            path = CurrentUser.get().getId() + FileAttribute.SEPARATOR + path;
        }
        fileDataService.createFolder(path, System.currentTimeMillis(), false);
    }

    /**
     * upload file
     *
     * @param file         file
     * @param name         name
     * @param path         path
     * @param type         mime type
     * @param lastModified last modified
     */
    @PostMapping("/_upload")
    public void upload(@RequestParam("file") MultipartFile file,
                       @RequestParam("name") String name,
                       @RequestParam("path") String path,
                       @RequestParam("type") String type,
                       @RequestParam("lastModified") Long lastModified) {
        if (!StringUtils.hasLength(path)) {
            path = CurrentUser.get().getId();
        } else {
            path = CurrentUser.get().getId() + FileAttribute.SEPARATOR + path;
        }
        fileDataService.upload(file, name, path, type, lastModified);
    }

    /**
     * download file
     *
     * @param pathList path list
     * @return file list
     */
    @GetMapping(value = "/_download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> downloadFiles(@RequestParam("paths") List<String> pathList,
                                                               HttpServletResponse response) {
        if (CollectionUtils.isEmpty(pathList)) {
            return null;
        }
        UserInfo userInfo = CurrentUser.get();
        String userPath = userInfo.getId() + FileAttribute.SEPARATOR;
        for (String path : pathList) {
            if (!StringUtils.startsWithIgnoreCase(path, userPath)) {
                throw new HttpException(I18n.get("noOperationPermission"));
            }
        }
        return fileDataService.download(userInfo.getUsername(), pathList, response);
    }

}
