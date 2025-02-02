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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Page<FileData> get(PageRequest page, String path, String name) {
        path = path + "%";
        if (StringUtils.hasLength(name)) {
            name = "%" + name + "%";
        } else {
            name = "%";
        }
        return fileDataRepository.findAllByPathLikeAndPathNotLikeAndNameLike(path,
                path + FileAttribute.SEPARATOR + "%",
                name, page);
    }

    @Transactional(rollbackFor = HttpException.class)
    public void batchDelete(List<String> filePathList) {
        for (String filePath : filePathList) {
            SystemFile.delete(filePath);
            fileDataRepository.deleteAllByPathOrPathLike(filePath, filePath + FileAttribute.SEPARATOR + "%");
        }
    }

    @Transactional(rollbackFor = HttpException.class)
    public void createFolder(String[] folderPaths, String[] folderNames, Date createTime, boolean allowExist) {
        List<String> fullFolderPathList = new ArrayList<>(folderPaths.length);
        for (int i = 0; i < folderPaths.length; i++) {
            fullFolderPathList.add(folderPaths[i] + FileAttribute.SEPARATOR + folderNames[i]);
        }
        List<FileData> existsFileDataList = fileDataRepository.findAllByPathIn(fullFolderPathList);
        if (!allowExist && !CollectionUtils.isEmpty(existsFileDataList)) {
            throw new HttpException(I18n.get("folderExits"));
        }

        Map<String, FileData> existsFileDataMap = existsFileDataList.stream().collect(Collectors.toMap(FileData::getPath, fileData -> fileData));
        List<FileData> fileDataList = new ArrayList<>(fullFolderPathList.size());
        for (int i = 0; i < fullFolderPathList.size(); i++) {
            String path = fullFolderPathList.get(i);
            FileData fileData;
            if (existsFileDataMap.containsKey(path)) {
                fileData = existsFileDataMap.get(path);
                fileData.setFileLastModifiedDate(createTime);
            } else {
                fileData = new FileData();
                fileData.setPath(path);
                fileData.setName(folderNames[i]);
                fileData.setType(FileAttribute.Type.FOLDER);
                fileData.setSize(0L);
                fileData.setEncrypted(false);
                fileData.setFileCreatedDate(createTime);
                fileData.setFileLastModifiedDate(createTime);
            }
            fileDataList.add(fileData);
        }

        SystemFile.createFolder(fullFolderPathList);
        fileDataRepository.saveAll(fileDataList);
    }

    @Transactional(rollbackFor = HttpException.class)
    public void upload(MultipartFile[] files, String[] paths) {
        Date currentTime = new Date();

        List<String> folderPaths = new ArrayList<>();
        List<String> folderNames = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            String[] splitPaths = paths[i].split(FileAttribute.SEPARATOR);
            StringBuilder splitPath = new StringBuilder();
            for (int j = 0; j < splitPaths.length - 1; j++) {
                splitPath.append(splitPaths[j]);
                folderPaths.add(splitPath.toString());
                folderNames.add(splitPaths[j + 1]);
                splitPath.append(FileAttribute.SEPARATOR);
            }
            splitPath.append(files[i].getOriginalFilename());
            filePaths.add(splitPath.toString());
        }
        createFolder(folderPaths.toArray(new String[0]), folderNames.toArray(new String[0]), currentTime, true);

        List<FileData> existsFileDataList = fileDataRepository.findAllByPathIn(filePaths);
        Map<String, FileData> existsFileDataMap = existsFileDataList.stream().collect(Collectors.toMap(FileData::getPath, fileData -> fileData));

        List<FileData> fileDataList = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String filePath = paths[i] + FileAttribute.SEPARATOR + file.getOriginalFilename();
            FileData fileData;
            if (existsFileDataMap.containsKey(filePath)) {
                fileData = existsFileDataMap.get(filePath);
                fileData.setFileLastModifiedDate(currentTime);
            } else {
                fileData = new FileData();
                fileData.setPath(filePath);
                fileData.setName(file.getOriginalFilename());
                fileData.setType(FileAttribute.Type.FILE);
                fileData.setSize(file.getSize());
                fileData.setEncrypted(false);
                fileData.setFileCreatedDate(currentTime);
                fileData.setFileLastModifiedDate(currentTime);
            }
            fileDataList.add(fileData);
        }

        SystemFile.upload(files, paths);
        fileDataRepository.saveAll(fileDataList);
    }

}
