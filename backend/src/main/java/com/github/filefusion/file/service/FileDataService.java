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
    public void createFolder(String path, Long lastModified, boolean allowExist) {
        if (!allowExist && fileDataRepository.existsByPath(path)) {
            throw new HttpException(I18n.get("folderExits"));
        }

        List<String> pathList = new ArrayList<>();
        List<String> nameList = new ArrayList<>();
        String[] splitPaths = path.split(FileAttribute.SEPARATOR);
        StringBuilder splitPath = new StringBuilder();
        for (int i = 0; i < splitPaths.length - 1; i++) {
            splitPath.append(splitPaths[i]);
            pathList.add(splitPath.toString());
            nameList.add(splitPaths[i + 1]);
            splitPath.append(FileAttribute.SEPARATOR);
        }
        List<String> fullPathList = new ArrayList<>(pathList.size());
        for (int i = 0; i < pathList.size(); i++) {
            fullPathList.add(pathList.get(i) + FileAttribute.SEPARATOR + nameList.get(i));
        }
        List<FileData> existsFileDataList = fileDataRepository.findAllByPathIn(fullPathList);
        Map<String, FileData> existsFileDataMap = existsFileDataList.stream().collect(Collectors.toMap(FileData::getPath, fileData -> fileData));

        Date lastModifiedDate = new Date(lastModified);
        List<FileData> fileDataList = new ArrayList<>(fullPathList.size());
        for (int i = 0; i < fullPathList.size(); i++) {
            String p = fullPathList.get(i);
            FileData fileData;
            if (existsFileDataMap.containsKey(p)) {
                fileData = existsFileDataMap.get(p);
            } else {
                fileData = new FileData();
                fileData.setPath(p);
                fileData.setName(nameList.get(i));
                fileData.setType(FileAttribute.Type.FOLDER);
                fileData.setSize(0L);
                fileData.setEncrypted(false);
            }
            fileData.setFileLastModifiedDate(lastModifiedDate);
            fileDataList.add(fileData);
        }

        fileDataRepository.saveAll(fileDataList);
        SystemFile.createFolder(fullPathList);
    }

    @Transactional(rollbackFor = HttpException.class)
    public void upload(MultipartFile file, String name, String path, String type, Long lastModified) {
        createFolder(path, lastModified, true);

        String filePath = path + FileAttribute.SEPARATOR + name;
        FileData existsFileData = fileDataRepository.findFirstByPath(filePath);

        FileData fileData;
        if (existsFileData != null) {
            fileData = existsFileData;
        } else {
            fileData = new FileData();
            fileData.setPath(filePath);
            fileData.setName(name);
            fileData.setType(FileAttribute.Type.FILE);
            fileData.setEncrypted(false);
        }
        fileData.setMimeType(type);
        fileData.setSize(file.getSize());
        fileData.setFileLastModifiedDate(new Date(lastModified));

        fileDataRepository.save(fileData);
        SystemFile.upload(file, name, path);
    }

}
