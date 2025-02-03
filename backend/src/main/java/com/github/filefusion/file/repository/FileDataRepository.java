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
     * deleteAllByPathOrPathLike
     *
     * @param path     file path
     * @param pathLike path like
     */
    void deleteAllByPathOrPathLike(String path, String pathLike);

    /**
     * existsByPath
     *
     * @param path path
     * @return exists
     */
    Boolean existsByPath(String path);

    /**
     * findAllByPathIn
     *
     * @param pathList path list
     * @return file list
     */
    List<FileData> findAllByPathIn(List<String> pathList);

    /**
     * findFirstByPath
     *
     * @param path path
     * @return file
     */
    FileData findFirstByPath(String path);

}
