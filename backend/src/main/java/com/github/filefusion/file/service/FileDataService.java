package com.github.filefusion.file.service;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.repository.FileDataRepository;
import com.github.filefusion.util.I18n;
import com.github.filefusion.util.SystemFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * FileDataService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Service
public class FileDataService {

    private final FileDataRepository fileDataRepository;

    @Autowired
    public FileDataService(FileDataRepository fileDataRepository) {
        this.fileDataRepository = fileDataRepository;
    }

    private void checkUserPermission(String userId, String filePath) {
        String userPath = userId + FileAttribute.SEPARATOR;
        if (!StringUtils.startsWithIgnoreCase(filePath, userPath)) {
            throw new HttpException(I18n.get("noOperationPermission"));
        }
    }

    public Page<FileData> get(PageRequest page, String path, String name) {
        path = path + "%";
        name = "%" + name + "%";
        return fileDataRepository.findAllByPathLikeAndNameLike(path, name, page);
    }

    public void batchDelete(String userId, List<String> filePathList) {
        if (CollectionUtils.isEmpty(filePathList)) {
            return;
        }
        for (String filePath : filePathList) {
            checkUserPermission(userId, filePath);
        }
        // todo 删除磁盘文件
        fileDataRepository.deleteAllByPathIn(filePathList);
    }

    @Transactional(rollbackFor = HttpException.class)
    public void createFolder(String filePath, String folderName) {
        String path = filePath + folderName;
        if (fileDataRepository.existsByPath(path)) {
            throw new HttpException(I18n.get("folderExits"));
        }
        Date currentTime = new Date();
        FileData fileData = new FileData();
        fileData.setPath(path);
        fileData.setName(folderName);
        fileData.setType(FileAttribute.Type.FOLDER);
        fileData.setSize(0L);
        fileData.setEncrypted(false);
        fileData.setFileCreatedDate(currentTime);
        fileData.setFileLastModifiedDate(currentTime);
        fileDataRepository.save(fileData);
        SystemFile.createFolder(path);
    }

}
