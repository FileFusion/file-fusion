package com.github.filefusion.file.controller;

import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.SorterOrder;
import com.github.filefusion.constant.VideoAttribute;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.CreateFolderModel;
import com.github.filefusion.file.model.MoveFileModel;
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
     * @param name        name
     * @param sorter      sorter
     * @param sorterOrder sorter order
     * @return file list
     */
    @GetMapping("/{page}/{pageSize}")
    @PreAuthorize("hasAuthority('personal_file:read')")
    public Page<FileData> get(@PathVariable Integer page, @PathVariable Integer pageSize,
                              @RequestParam(required = false) String parentId,
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
                CurrentUser.getId(), parentId, name, false);
    }

    /**
     * delete file
     *
     * @param id id
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('personal_file:delete')")
    public void delete(@PathVariable String id) {
        fileDataService.recycleOrDelete(CurrentUser.getId(), id);
    }

    /**
     * get folder list
     *
     * @param parentId parent id
     * @return folder list
     */
    @GetMapping("/folder")
    @PreAuthorize("hasAuthority('personal_file:read')")
    public List<FileData> getFolderList(@RequestParam(required = false) String parentId) {
        return fileDataService.getFolderList(CurrentUser.getId(), parentId);
    }

    /**
     * create folder
     *
     * @param createFolderModel create folder info
     */
    @PostMapping("/folder")
    @PreAuthorize("hasAuthority('personal_file:upload')")
    public void createFolder(@RequestBody CreateFolderModel createFolderModel) {
        fileDataService.createFolder(CurrentUser.getId(), createFolderModel.getParentId(), createFolderModel.getName());
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
     * @param path                 path
     * @param name                 name
     * @param hashValue            hash value
     * @param mimeType             mime type
     * @param size                 size
     * @param fileLastModifiedDate file last modified date
     * @param fastUpload           fast upload
     */
    @PostMapping("/_upload_chunk_merge")
    @PreAuthorize("hasAuthority('personal_file:upload')")
    public boolean uploadChunkMerge(@RequestParam(required = false) String parentId,
                                    @RequestParam(required = false) String path,
                                    @RequestParam String name, @RequestParam String hashValue,
                                    @RequestParam(required = false) String mimeType,
                                    @RequestParam Long size, @RequestParam(required = false) Long fileLastModifiedDate,
                                    @RequestParam(required = false) Boolean fastUpload) {
        return fileDataService.uploadChunkMerge(CurrentUser.getId(), parentId, path, name, hashValue,
                mimeType, size, TimeUtil.fromMillis(fileLastModifiedDate), Boolean.TRUE.equals(fastUpload));
    }

    /**
     * rename file
     *
     * @param renameFileModel rename file info
     */
    @PutMapping("/_rename")
    @PreAuthorize("hasAuthority('personal_file:rename')")
    public void rename(@RequestBody RenameFileModel renameFileModel) {
        fileDataService.rename(CurrentUser.getId(), renameFileModel.getId(), renameFileModel.getName());
    }

    /**
     * move file
     *
     * @param moveFileModel move file info
     */
    @PutMapping("/_move")
    @PreAuthorize("hasAuthority('personal_file:move')")
    public void move(@RequestBody MoveFileModel moveFileModel) {
        fileDataService.move(CurrentUser.getId(), moveFileModel.getSourceId(), moveFileModel.getTargetId(), null);
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
        return fileDataService.submitDownload(CurrentUser.getId(), idList);
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
     * download file chunked
     *
     * @param downloadId  download id
     * @param range       file chunked range
     * @param ignoredName file name
     * @return file chunked
     */
    @GetMapping("/_download_chunked/{downloadId}/{name}")
    public ResponseEntity<StreamingResponseBody> downloadChunked(@PathVariable String downloadId, @RequestHeader String range,
                                                                 @PathVariable("name") String ignoredName) {
        return fileDataService.downloadChunked(downloadId, range);
    }

    /**
     * thumbnail file
     *
     * @param id id
     * @return file thumbnail
     */
    @GetMapping("/{id}/" + FileAttribute.DOWNLOAD_THUMBNAIL_NAME)
    @PreAuthorize("hasAnyAuthority('personal_file:read','recycle_bin_file:read')")
    public ResponseEntity<StreamingResponseBody> thumbnail(@PathVariable String id) {
        return fileDataService.thumbnail(CurrentUser.getId(), id);
    }

    /**
     * get video file master playlist
     *
     * @param id id
     * @return master playlist
     */
    @GetMapping("/{id}/" + VideoAttribute.MASTER_PLAYLIST_NAME)
    @PreAuthorize("hasAnyAuthority('personal_file:read')")
    public ResponseEntity<StreamingResponseBody> getMasterPlaylist(@PathVariable String id) {
        return fileDataService.getMasterPlaylist(CurrentUser.getId(), id);
    }

    /**
     * get video file media playlist
     *
     * @param id                id
     * @param ignoredResolution resolution
     * @return media playlist
     */
    @GetMapping("/{id}/{resolution}/" + VideoAttribute.MEDIA_PLAYLIST_NAME)
    @PreAuthorize("hasAnyAuthority('personal_file:read')")
    public ResponseEntity<StreamingResponseBody> getMediaPlaylist(
            @PathVariable String id, @PathVariable("resolution") String ignoredResolution) {
        return fileDataService.getMediaPlaylist(CurrentUser.getId(), id);
    }

    /**
     * get video file media segment
     *
     * @param id         id
     * @param resolution resolution
     * @param segment    segment
     * @return media segment
     */
    @GetMapping("/{id}/{resolution}/{segment}/" + VideoAttribute.MEDIA_SEGMENT_NAME)
    @PreAuthorize("hasAnyAuthority('personal_file:read')")
    public ResponseEntity<StreamingResponseBody> getMediaSegment(
            @PathVariable String id, @PathVariable String resolution, @PathVariable Integer segment) {
        return fileDataService.getMediaSegment(CurrentUser.getId(), id, resolution, segment);
    }

}
