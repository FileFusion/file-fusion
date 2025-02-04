package com.github.filefusion.file.service;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.repository.FileDataRepository;
import com.github.filefusion.util.DistributedLock;
import com.github.filefusion.util.I18n;
import com.github.filefusion.util.SystemFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FileDataService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Service
public class FileDataService {

    private final FileDataRepository fileDataRepository;
    private final DistributedLock distributedLock;

    @Autowired
    public FileDataService(FileDataRepository fileDataRepository,
                           DistributedLock distributedLock) {
        this.fileDataRepository = fileDataRepository;
        this.distributedLock = distributedLock;
    }

    private static Map<String, String> pathToPathTree(String path) {
        Map<String, String> pathTree = new LinkedHashMap<>();
        String[] splitPaths = path.split(FileAttribute.SEPARATOR);
        StringBuilder splitPath = new StringBuilder();
        for (int i = 0; i < splitPaths.length - 1; i++) {
            splitPath.append(splitPaths[i]);
            String name = splitPaths[i + 1];
            pathTree.put(splitPath + FileAttribute.SEPARATOR + name, name);
            splitPath.append(FileAttribute.SEPARATOR);
        }
        return pathTree;
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

    public void batchDelete(List<String> pathList) {
        for (final String path : pathList) {
            final List<FileData> fileDataList = fileDataRepository.findAllByPathOrPathLike(path, path + FileAttribute.SEPARATOR + "%");
            List<String> fileDataPathList = fileDataList.stream().map(FileData::getPath).distinct().toList();
            distributedLock.tryMultiLock(fileDataPathList, () -> {
                fileDataRepository.deleteAll(fileDataList);
                SystemFile.delete(path);
            });
        }
    }

    public void createFolder(String path, Long lastModified, boolean allowExist) {
        if (!allowExist && fileDataRepository.existsByPath(path)) {
            throw new HttpException(I18n.get("folderExits"));
        }

        Map<String, String> pathTree = pathToPathTree(path);
        final Date lastModifiedDate = new Date(lastModified);
        for (final String folderPath : pathTree.keySet()) {
            final String name = pathTree.get(folderPath);
            distributedLock.tryLock(folderPath, () -> {
                FileData fileData = fileDataRepository.findFirstByPath(folderPath);
                if (fileData == null) {
                    fileData = new FileData();
                    fileData.setPath(folderPath);
                    fileData.setName(name);
                    fileData.setType(FileAttribute.Type.FOLDER);
                    fileData.setSize(0L);
                    fileData.setEncrypted(false);
                }
                fileData.setFileLastModifiedDate(lastModifiedDate);
                SystemFile.createFolder(folderPath);
                fileDataRepository.save(fileData);
            });
        }
    }

    public void upload(final MultipartFile file, final String name,
                       String path, final String type, final Long lastModified) {
        createFolder(path, lastModified, true);

        final String filePath = path + FileAttribute.SEPARATOR + name;
        distributedLock.tryLock(filePath, () -> {
            FileData fileData = fileDataRepository.findFirstByPath(filePath);
            if (fileData == null) {
                fileData = new FileData();
                fileData.setPath(filePath);
                fileData.setName(name);
                fileData.setType(FileAttribute.Type.FILE);
                fileData.setEncrypted(false);
            }
            fileData.setMimeType(type);
            fileData.setSize(file.getSize());
            fileData.setFileLastModifiedDate(new Date(lastModified));
            SystemFile.upload(file, filePath);
            fileDataRepository.save(fileData);
        });
    }

}
