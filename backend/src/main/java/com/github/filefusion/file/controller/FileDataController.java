package com.github.filefusion.file.controller;

import com.github.filefusion.constant.SorterOrder;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.CreateFolderModel;
import com.github.filefusion.file.model.RenameFileModel;
import com.github.filefusion.file.model.SubmitDownloadFilesResponse;
import com.github.filefusion.file.model.UploadFileModel;
import com.github.filefusion.file.service.FileDataService;
import com.github.filefusion.util.CurrentUser;
import com.github.filefusion.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
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
     * @param parentId    parent id
     * @param sorter      sorter
     * @param sorterOrder sorter order
     * @return file list
     */
    @GetMapping("/{page}/{pageSize}")
    @PreAuthorize("hasAuthority('personal_file:read')")
    public Page<FileData> get(@PathVariable Integer page, @PathVariable Integer pageSize,
                              @RequestParam(required = false) String parentId,
                              @RequestParam(required = false) String sorter,
                              @RequestParam(required = false) SorterOrder sorterOrder) {
        if (!StringUtils.hasLength(sorter)) {
            sorter = FileData.Fields.name;
        }
        if (sorterOrder == null) {
            sorterOrder = SorterOrder.ascend;
        }
        return fileDataService.get(PageRequest.of(page - 1, pageSize, sorterOrder.order(), sorter),
                CurrentUser.get().getId(), parentId);
    }

    /**
     * delete file
     *
     * @param id id
     */
    @DeleteMapping
    @PreAuthorize("hasAuthority('personal_file:delete')")
    public void delete(@RequestParam String id) {
        fileDataService.recycleOrDelete(CurrentUser.get().getId(), id);
    }

    /**
     * create folder
     *
     * @param createFolderModel create folder info
     */
    @PostMapping("/_create_folder")
    @PreAuthorize("hasAuthority('personal_file:upload')")
    public void createFolder(@RequestBody CreateFolderModel createFolderModel) {
        fileDataService.createFolder(CurrentUser.get().getId(), createFolderModel.getParentId(),
                createFolderModel.getName(), LocalDateTime.now(), false);
    }

    /**
     * upload file
     *
     * @param uploadFileModel upload file info
     */
    @PostMapping("/_upload")
    @PreAuthorize("hasAuthority('personal_file:upload')")
    public void upload(@RequestParam UploadFileModel uploadFileModel) {
        fileDataService.upload(CurrentUser.get().getId(), uploadFileModel.getFile(), uploadFileModel.getParentId(),
                uploadFileModel.getName(), uploadFileModel.getPath(), uploadFileModel.getMd5Value(),
                uploadFileModel.getMimeType(), TimeUtil.fromMillis(uploadFileModel.getFileLastModifiedDate()));
    }

    /**
     * rename file
     *
     * @param renameFileModel rename file info
     */
    @PutMapping("/_rename")
    @PreAuthorize("hasAuthority('personal_file:edit')")
    public void rename(@RequestBody RenameFileModel renameFileModel) {
        fileDataService.rename(CurrentUser.get().getId(), renameFileModel.getId(), renameFileModel.getName());
    }

    /**
     * submit download file list
     *
     * @param idList id list
     * @return download id
     */
    @PostMapping("/_submit_download")
    @PreAuthorize("hasAnyAuthority('personal_file:download','personal_file:preview')")
    public SubmitDownloadFilesResponse submitDownload(@RequestBody List<String> idList) {
        return fileDataService.submitDownload(CurrentUser.get().getId(), idList);
    }

    /**
     * download file list
     *
     * @param downloadId download id
     * @return file list
     */
    @GetMapping("/_download")
    public ResponseEntity<StreamingResponseBody> download(@RequestParam String downloadId) {
        return fileDataService.download(downloadId);
    }

    /**
     * download chunked
     *
     * @param id    id
     * @param range chunked range
     * @return file chunked
     */
    @GetMapping("/_download_chunked/{id}")
    @PreAuthorize("hasAnyAuthority('personal_file:download','personal_file:preview')")
    public ResponseEntity<StreamingResponseBody> downloadChunked(@PathVariable String id, @RequestHeader String range) {
        return fileDataService.downloadChunked(CurrentUser.get().getId(), id, range);
    }

    /**
     * thumbnail file
     *
     * @param id id
     * @return file thumbnail
     */
    @GetMapping("/_thumbnail/{id}")
    @PreAuthorize("hasAnyAuthority('personal_file:read','recycle_bin_file:read')")
    public ResponseEntity<StreamingResponseBody> thumbnail(@PathVariable String id) {
        return fileDataService.thumbnail(CurrentUser.get().getId(), id);
    }

}
