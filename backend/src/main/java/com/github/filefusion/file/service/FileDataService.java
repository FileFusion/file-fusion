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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final DistributedLock distributedLock;
    private final SystemFile systemFile;

    @Autowired
    public FileDataService(FileDataRepository fileDataRepository,
                           DistributedLock distributedLock,
                           SystemFile systemFile) {
        this.fileDataRepository = fileDataRepository;
        this.distributedLock = distributedLock;
        this.systemFile = systemFile;
    }

    private static List<Path> getHierarchyPath(String path) {
        Path rootPath = Paths.get(path).normalize();
        List<Path> hierarchyPathList = new ArrayList<>(rootPath.getNameCount() - 1);
        Path currentPath = rootPath.getName(0);
        for (int i = 1; i < rootPath.getNameCount(); i++) {
            currentPath = Paths.get(currentPath.resolve(rootPath.getName(i)).toString());
            hierarchyPathList.add(currentPath);
        }
        return hierarchyPathList;
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
    public void batchDelete(List<String> pathList) {
        List<String> allPathList = pathList.stream()
                .flatMap(path -> fileDataRepository.findAllByPathOrPathLike(path, path + FileAttribute.SEPARATOR + "%").stream())
                .map(FileData::getPath).distinct().toList();
        distributedLock.tryMultiLock(allPathList, () -> {
            fileDataRepository.deleteAllByPathIn(allPathList);
            systemFile.delete(allPathList);
        });
    }

    public void createFolder(final String path, Long lastModified, final boolean allowExists) {
        final Date lastModifiedDate = new Date(lastModified);
        final List<Path> hierarchyPathList = getHierarchyPath(path);
        final List<String> sortedPathList = hierarchyPathList.stream().map(Path::toString).toList();
        distributedLock.tryMultiLock(sortedPathList, () -> {
            if (!allowExists && fileDataRepository.existsByPath(path)) {
                throw new HttpException(I18n.get("folderExits"));
            }
            List<FileData> existsFileDataList = fileDataRepository.findAllByPathIn(sortedPathList);
            Map<String, FileData> existsFileDataMap = existsFileDataList.stream().collect(Collectors.toMap(FileData::getPath, fileData -> fileData));
            List<FileData> fileDataList = new ArrayList<>(hierarchyPathList.size());
            for (Path hierarchyPath : hierarchyPathList) {
                String folderPath = hierarchyPath.toString();
                FileData fileData = existsFileDataMap.get(folderPath);
                if (fileData == null) {
                    fileData = new FileData();
                }
                fileData.setPath(folderPath);
                fileData.setName(hierarchyPath.getFileName().toString());
                fileData.setType(FileAttribute.Type.FOLDER);
                fileData.setMimeType(FileAttribute.FOLDER_MIME_TYPE);
                fileData.setSize(0L);
                fileData.setEncrypted(false);
                fileData.setFileLastModifiedDate(lastModifiedDate);
                fileDataList.add(fileData);
            }
            systemFile.createFolder(path);
            fileDataRepository.saveAll(fileDataList);
        });
    }

    public void upload(final MultipartFile file, final String name,
                       String path, final String type, final Long lastModified) {
        createFolder(path, lastModified, true);

        final String filePath = path + FileAttribute.SEPARATOR + name;
        distributedLock.tryLock(filePath, () -> {
            FileData fileData = fileDataRepository.findFirstByPath(filePath);
            if (fileData == null) {
                fileData = new FileData();
            }
            fileData.setPath(filePath);
            fileData.setName(name);
            fileData.setType(FileAttribute.Type.FILE);
            fileData.setMimeType(type);
            fileData.setSize(file.getSize());
            fileData.setEncrypted(false);
            fileData.setFileLastModifiedDate(new Date(lastModified));
            systemFile.upload(file, filePath);
            fileDataRepository.save(fileData);
        });
    }

}
