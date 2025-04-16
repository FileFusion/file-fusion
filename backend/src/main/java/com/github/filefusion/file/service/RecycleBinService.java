package com.github.filefusion.file.service;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.repository.FileDataRepository;
import com.github.filefusion.util.I18n;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * RecycleBinService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Slf4j
@Service
public class RecycleBinService {

    private final FileDataRepository fileDataRepository;
    private final FileDataService fileDataService;

    @Autowired
    public RecycleBinService(FileDataRepository fileDataRepository,
                             FileDataService fileDataService) {
        this.fileDataRepository = fileDataRepository;
        this.fileDataService = fileDataService;
    }

    @Transactional(rollbackFor = HttpException.class)
    public void restore(String userId, String sourceId, String targetId) {
        FileData fileData = fileDataRepository.findFirstByUserIdAndIdAndDeletedTrue(userId, sourceId)
                .orElseThrow(() -> new HttpException(I18n.get("fileNotExist")));
        if (!StringUtils.hasLength(targetId)) {
            Path parent = Paths.get(fileData.getPath()).getParent();
            if (parent == null) {
                targetId = FileAttribute.PARENT_ROOT;
            } else {
                FileData parentFile = fileDataRepository.findFirstByUserIdAndPathAndDeletedFalse(userId, parent.toString())
                        .orElseThrow(() -> new HttpException(I18n.get("originalPathNoExists")));
                targetId = parentFile.getId();
            }
        }
        fileDataService.move(userId, sourceId, targetId, null);
    }

    @Transactional(rollbackFor = HttpException.class)
    public void deleteAll(String userId) {
        List<FileData> fileDataList = fileDataRepository.findAllByUserIdAndParentIdAndDeletedTrue(userId, FileAttribute.RECYCLE_BIN_ROOT);
        List<FileData> allFileList = new ArrayList<>(fileDataList);
        for (FileData fileData : fileDataList) {
            allFileList.addAll(fileDataService.findAllChildren(fileData.getId()));
        }
        fileDataService.batchDelete(allFileList);
    }

}
