package com.github.filefusion.file.controller;

import com.github.filefusion.constant.SorterOrder;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.CreateFolderModel;
import com.github.filefusion.file.model.RenameFileModel;
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
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('personal_file:delete')")
    public void delete(@PathVariable String id) {
        fileDataService.recycleOrDelete(CurrentUser.get().getId(), id);
    }

    /**
     * create folder
     *
     * @param createFolderModel create folder info
     */
    @PostMapping("/_create_folder")
    @PreAuthorize("hasAuthority('personal_file:upload')")
    public String createFolder(@RequestBody CreateFolderModel createFolderModel) {
        return fileDataService.createFolder(CurrentUser.get().getId(), createFolderModel.getParentId(),
                createFolderModel.getName(), LocalDateTime.now(), false);
    }

    /**
     * upload file chunk
     *
     * @param file           file chunk
     * @param chunkIndex     chunk index
     * @param chunkHashValue chunk hash value
     * @param hashValue      hash value
     */
    @PostMapping("/_upload_chunk")
    @PreAuthorize("hasAuthority('personal_file:upload')")
    public void uploadChunk(@RequestParam MultipartFile file, @RequestParam Integer chunkIndex,
                            @RequestParam String chunkHashValue, @RequestParam String hashValue) {
        fileDataService.uploadChunk(file, chunkIndex, chunkHashValue, hashValue);
    }

    /**
     * upload chunk merge
     *
     * @param parentId             parent id
     * @param name                 name
     * @param path                 path
     * @param hashValue            hash value
     * @param mimeType             mime type
     * @param size                 size
     * @param fileLastModifiedDate file last modified date
     */
    @PostMapping("/_upload_chunk_merge")
    @PreAuthorize("hasAuthority('personal_file:upload')")
    public boolean uploadChunkMerge(@RequestParam(required = false) String parentId,
                                    @RequestParam String name, @RequestParam(required = false) String path,
                                    @RequestParam String hashValue, @RequestParam(required = false) String mimeType,
                                    @RequestParam Long size, @RequestParam(required = false) Long fileLastModifiedDate,
                                    @RequestParam(required = false) Boolean fastUpload) {
        return fileDataService.uploadChunkMerge(CurrentUser.get().getId(), parentId, name, path, hashValue,
                mimeType, size, TimeUtil.fromMillis(fileLastModifiedDate), Boolean.TRUE.equals(fastUpload));
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
    public String submitDownload(@RequestBody List<String> idList) {
        return fileDataService.submitDownload(CurrentUser.get().getId(), idList);
    }

    /**
     * download file list
     *
     * @param downloadId download id
     * @return file list
     */
    @GetMapping("/_download/{downloadId}")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable String downloadId) {
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
