package com.github.filefusion.file.controller;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.SorterOrder;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.CreateFolderModel;
import com.github.filefusion.file.service.FileDataService;
import com.github.filefusion.util.CurrentUser;
import com.github.filefusion.util.I18n;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
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
     * @param filePathList file path list
     */
    @PostMapping("/_batch_delete")
    @PreAuthorize("hasAuthority('personal_file:delete')")
    public void batchDelete(@RequestBody List<String> filePathList) {
        if (CollectionUtils.isEmpty(filePathList)) {
            return;
        }
        String userPath = CurrentUser.get().getId() + FileAttribute.SEPARATOR;
        for (String filePath : filePathList) {
            if (!StringUtils.startsWithIgnoreCase(filePath, userPath)) {
                throw new HttpException(I18n.get("noOperationPermission"));
            }
        }
        fileDataService.batchDelete(filePathList);
    }

    /**
     * create folder
     *
     * @param createFolder folder info
     */
    @PostMapping("/_create_folder")
    @PreAuthorize("hasAuthority('personal_file:add')")
    public void createFolder(@RequestBody CreateFolderModel createFolder) {
        String folderPath = createFolder.getPath();
        if (!StringUtils.hasLength(folderPath)) {
            folderPath = CurrentUser.get().getId();
        } else {
            folderPath = CurrentUser.get().getId() + FileAttribute.SEPARATOR + folderPath;
        }
        fileDataService.createFolder(new String[]{folderPath}, new String[]{createFolder.getName()}, new Date(), false);
    }

    /**
     * upload files
     *
     * @param files files
     * @param paths paths
     */
    @PostMapping("/_upload")
    public void upload(@RequestParam("files") MultipartFile[] files,
                       @RequestParam("paths") String[] paths) {
        String userPath = CurrentUser.get().getId();
        for (int i = 0; i < paths.length; i++) {
            if (!StringUtils.hasLength(paths[i])) {
                paths[i] = userPath;
            } else {
                paths[i] = userPath + FileAttribute.SEPARATOR + paths[i];
            }
        }
        fileDataService.upload(files, paths);
    }

}
