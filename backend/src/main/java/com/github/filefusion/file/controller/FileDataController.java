package com.github.filefusion.file.controller;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.SorterOrder;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.RenameFileModel;
import com.github.filefusion.file.model.SubmitDownloadFilesResponse;
import com.github.filefusion.file.service.FileDataService;
import com.github.filefusion.util.CurrentUser;
import com.github.filefusion.util.I18n;
import com.github.filefusion.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDateTime;
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
            sorter = FileData.Fields.name;
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
     * get recycle bin file list - paged
     *
     * @param page        page
     * @param pageSize    page size
     * @param path        path
     * @param name        name
     * @param sorter      sorter
     * @param sorterOrder sorter order
     * @return recycle bin file list
     */
    @GetMapping("/recycle_bin/{page}/{pageSize}")
    @PreAuthorize("hasAuthority('recycle_bin_file:read')")
    public Page<FileData> getRecycleBin(@PathVariable Integer page, @PathVariable Integer pageSize,
                                        @RequestParam(required = false) String path,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) String recycleId,
                                        @RequestParam(required = false) String sorter,
                                        @RequestParam(required = false) SorterOrder sorterOrder) {
        if (!StringUtils.hasLength(sorter)) {
            sorter = FileData.Fields.name;
        }
        if (sorterOrder == null) {
            sorterOrder = SorterOrder.ascend;
        }
        if (!StringUtils.hasLength(path)) {
            path = CurrentUser.get().getId() + FileAttribute.SEPARATOR + "%" + FileAttribute.SEPARATOR;
        } else {
            if (!StringUtils.hasLength(recycleId)) {
                throw new HttpException(I18n.get("recycleIdNotExist"));
            }
            path = CurrentUser.get().getId() + FileAttribute.SEPARATOR + recycleId + FileAttribute.SEPARATOR + path + FileAttribute.SEPARATOR;
        }
        return fileDataService.getFromRecycleBin(PageRequest.of(page - 1, pageSize, sorterOrder.order(), sorter), path, name);

    }

    /**
     * batch delete file
     *
     * @param pathList path list
     */
    @PostMapping("/_batch_delete")
    @PreAuthorize("hasAuthority('personal_file:delete')")
    public void batchDelete(@RequestBody List<String> pathList) {
        fileDataService.verifyUserAuthorize(CurrentUser.get().getId(), pathList.toArray(new String[0]));
        fileDataService.batchRecycleOrDelete(pathList);
    }

    /**
     * create folder
     *
     * @param fileData path
     */
    @PostMapping("/_create_folder")
    @PreAuthorize("hasAuthority('personal_file:upload')")
    public void createFolder(@RequestBody FileData fileData) {
        String path = fileDataService.formatUserPath(CurrentUser.get().getId(), fileData.getPath());
        fileDataService.createFolder(path, LocalDateTime.now(), false);
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
    @PreAuthorize("hasAuthority('personal_file:upload')")
    public void upload(@RequestParam("file") MultipartFile file,
                       @RequestParam("name") String name,
                       @RequestParam("path") String path,
                       @RequestParam("type") String type,
                       @RequestParam("lastModified") Long lastModified) {
        path = fileDataService.formatUserPath(CurrentUser.get().getId(), path);
        fileDataService.upload(file, name, path, type, TimeUtil.fromMillis(lastModified));
    }

    /**
     * rename file
     *
     * @param renameFileModel rename file info
     */
    @PostMapping("/_rename")
    @PreAuthorize("hasAuthority('personal_file:edit')")
    public void rename(@RequestBody RenameFileModel renameFileModel) {
        String path = fileDataService.formatUserPath(CurrentUser.get().getId(), renameFileModel.getPath());
        fileDataService.rename(path, renameFileModel.getOriginalName(), renameFileModel.getTargetName());
    }

    /**
     * submit download file list
     *
     * @param pathList path list
     * @return download id
     */
    @PostMapping("/_submit_download")
    @PreAuthorize("hasAnyAuthority('personal_file:download','personal_file:preview','recycle_bin_file:preview')")
    public SubmitDownloadFilesResponse submitDownloadFiles(@RequestBody List<String> pathList) {
        fileDataService.verifyUserAuthorize(CurrentUser.get().getId(), pathList.toArray(new String[0]));
        return fileDataService.submitDownload(pathList);
    }

    /**
     * download file list
     *
     * @param downloadId download id
     * @return file list
     */
    @GetMapping("/_download/{downloadId}")
    public ResponseEntity<StreamingResponseBody> downloadFiles(@PathVariable String downloadId) {
        return fileDataService.download(downloadId);
    }

    /**
     * download chunked
     *
     * @param fileData path
     * @param range    chunked range
     * @return file chunked
     */
    @PostMapping("/_download_chunked")
    @PreAuthorize("hasAnyAuthority('personal_file:download','personal_file:preview','recycle_bin_file:preview')")
    public ResponseEntity<StreamingResponseBody> downloadChunked(@RequestBody FileData fileData,
                                                                 @RequestHeader String range) {
        fileDataService.verifyUserAuthorize(CurrentUser.get().getId(), fileData.getPath());
        return fileDataService.downloadChunked(fileData.getPath(), range);
    }

    /**
     * thumbnail file
     *
     * @param fileData path
     * @return file thumbnail
     */
    @PostMapping("/_thumbnail")
    @PreAuthorize("hasAnyAuthority('personal_file:read','recycle_bin_file:read')")
    public ResponseEntity<StreamingResponseBody> thumbnailFile(@RequestBody FileData fileData) {
        fileDataService.verifyUserAuthorize(CurrentUser.get().getId(), fileData.getPath());
        return fileDataService.thumbnailFile(fileData.getPath());
    }

}
