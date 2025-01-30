package com.github.filefusion.file.repository;

import com.github.filefusion.file.entity.FileData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FileDataRepository
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Repository
public interface FileDataRepository extends JpaRepository<FileData, String> {

    /**
     * findAllByPathLikeAndNameLike
     *
     * @param path path
     * @param name name
     * @param page page
     * @return file list
     */
    Page<FileData> findAllByPathLikeAndNameLike(String path, String name, PageRequest page);

    /**
     * deleteAllByPathIn
     *
     * @param filePathList file path list
     */
    void deleteAllByPathIn(List<String> filePathList);

}
