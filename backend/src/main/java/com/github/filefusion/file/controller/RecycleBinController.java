package com.github.filefusion.file.controller;

import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.SorterOrder;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.service.FileDataService;
import com.github.filefusion.util.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * RecycleBinController
 *
 * @author hackyo
 * @since 2022/4/1
 */
@RestController
@RequestMapping("/recycle_bin")
public class RecycleBinController {

    private final FileDataService fileDataService;

    @Autowired
    public RecycleBinController(FileDataService fileDataService) {
        this.fileDataService = fileDataService;
    }

    /**
     * get file list - paged
     *
     * @param page        page
     * @param pageSize    page size
     * @param name        name
     * @param sorter      sorter
     * @param sorterOrder sorter order
     * @return file list
     */
    @GetMapping("/{page}/{pageSize}")
    @PreAuthorize("hasAuthority('recycle_bin_file:read')")
    public Page<FileData> getRecycleBin(@PathVariable Integer page, @PathVariable Integer pageSize,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) String sorter,
                                        @RequestParam(required = false) SorterOrder sorterOrder) {
        if (!StringUtils.hasLength(sorter)) {
            sorter = FileData.Fields.name;
        }
        if (sorterOrder == null) {
            sorterOrder = SorterOrder.ascend;
        }
        String path = CurrentUser.get().getId() + FileAttribute.SEPARATOR + "%" + FileAttribute.SEPARATOR;
        return fileDataService.getFromRecycleBin(PageRequest.of(page - 1, pageSize, sorterOrder.order(), sorter), path, name);
    }

}
