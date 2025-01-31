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
     * findAllByPathLikeAndPathNotLikeAndNameLike
     *
     * @param path        path
     * @param excludePath exclude path
     * @param name        name
     * @param page        page
     * @return file list
     */
    Page<FileData> findAllByPathLikeAndPathNotLikeAndNameLike(String path, String excludePath, String name, PageRequest page);

    /**
     * deleteAllByPathIn
     *
     * @param filePathList file path list
     */
    void deleteAllByPathIn(List<String> filePathList);

    /**
     * existsByPath
     *
     * @param path path
     * @return exists
     */
    Boolean existsByPath(String path);

}
