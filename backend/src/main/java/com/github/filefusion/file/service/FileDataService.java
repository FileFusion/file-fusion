package com.github.filefusion.file.service;

import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.repository.FileDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
        name = "%" + name + "%";
        return fileDataRepository.findAllByPathLikeAndNameLike(path, name, page);
    }

}
