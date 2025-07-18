package com.github.filefusion.file.controller;

import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.SorterOrder;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.MoveFileModel;
import com.github.filefusion.file.service.FileDataService;
import com.github.filefusion.file.service.RecycleBinService;
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
    private final RecycleBinService recycleBinService;

    @Autowired
    public RecycleBinController(FileDataService fileDataService,
                                RecycleBinService recycleBinService) {
        this.fileDataService = fileDataService;
        this.recycleBinService = recycleBinService;
    }

    /**
     * get recycle bin file list - paged
     *
     * @param page        page
     * @param pageSize    page size
     * @param name        name
     * @param sorter      sorter
     * @param sorterOrder sorter order
     * @return recycle bin file list
     */
    @GetMapping("/{page}/{pageSize}")
    @PreAuthorize("hasAuthority('recycle_bin_file:read')")
    public Page<FileData> get(@PathVariable Integer page, @PathVariable Integer pageSize,
                              @RequestParam(required = false) String name,
                              @RequestParam(required = false) String sorter,
                              @RequestParam(required = false) SorterOrder sorterOrder) {
        if (!StringUtils.hasLength(sorter)) {
            sorter = FileData.Fields.name;
        }
        if (sorterOrder == null) {
            sorterOrder = SorterOrder.ascend;
        }
        return fileDataService.get(PageRequest.of(page - 1, pageSize, sorterOrder.order(), sorter),
                CurrentUser.getId(), FileAttribute.RECYCLE_BIN_ROOT, name, true);
    }

    /**
     * restore file
     *
     * @param restoreFileModel restore file
     */
    @PutMapping("/_restore")
    @PreAuthorize("hasAuthority('recycle_bin_file:restore')")
    public void restore(@RequestBody MoveFileModel restoreFileModel) {
        recycleBinService.restore(CurrentUser.getId(), restoreFileModel.getSourceId(), restoreFileModel.getTargetId());
    }

    /**
     * delete recycle bin file
     *
     * @param id id
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('recycle_bin_file:delete')")
    public void delete(@PathVariable String id) {
        fileDataService.recycleOrDelete(CurrentUser.getId(), id, false);
    }

    /**
     * delete all recycle bin file
     */
    @DeleteMapping("/all")
    @PreAuthorize("hasAuthority('recycle_bin_file:delete')")
    public void deleteAll() {
        recycleBinService.deleteAll(CurrentUser.getId());
    }

}
