package com.github.filefusion.file.controller;

import com.github.filefusion.constant.FileSeparator;
import com.github.filefusion.constant.SorterOrder;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.NewFolderModel;
import com.github.filefusion.file.service.FileDataService;
import com.github.filefusion.util.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

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
            path = CurrentUser.get().getId() + FileSeparator.VALUE;
        } else {
            path = CurrentUser.get().getId() + FileSeparator.VALUE + path;
        }
        return fileDataService.get(PageRequest.of(page - 1, pageSize, sorterOrder.order(), sorter), path, name);
    }


    /**
     * batch delete file
     *
     * @param filePathList file path list
     */
    @PostMapping("_batch_delete")
    @PreAuthorize("hasAuthority('personal_file:delete')")
    public void batchDelete(@RequestBody List<String> filePathList) {
        fileDataService.batchDelete(CurrentUser.get().getId(), filePathList);
    }

    /**
     * new folder
     *
     * @param newFolder folder info
     */
    @PostMapping("_new_folder")
    @PreAuthorize("hasAuthority('personal_file:add')")
    public void newFolder(@RequestBody NewFolderModel newFolder) {
        String filePath = newFolder.getPath();
        if (!StringUtils.hasLength(filePath)) {
            filePath = CurrentUser.get().getId() + FileSeparator.VALUE;
        } else {
            filePath = CurrentUser.get().getId() + FileSeparator.VALUE + filePath;
        }
        String folderName = newFolder.getName();
        fileDataService.newFolder(filePath, folderName);
    }

}
