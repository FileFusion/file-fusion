package com.github.filefusion.file.repository;

import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.FileHashUsageCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
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
     * findAllByPathLikeAndPathNotLikeAndDeletedAndNameLike
     *
     * @param path        path
     * @param excludePath exclude path
     * @param deleted     deleted
     * @param name        name
     * @param page        page
     * @return file list
     */
    Page<FileData> findAllByPathLikeAndPathNotLikeAndDeletedAndNameLike(String path, String excludePath, boolean deleted, String name, PageRequest page);

    /**
     * findAllByPathOrPathLike
     *
     * @param path     path
     * @param pathLike path like
     * @return file list
     */
    List<FileData> findAllByPathOrPathLike(String path, String pathLike);

    /**
     * findAllByPathLikeAndDeletedFalse
     *
     * @param pathLike path like
     * @return file list
     */
    List<FileData> findAllByPathLikeAndDeletedFalse(String pathLike);

    /**
     * deleteAllByPathIn
     *
     * @param pathList path list
     */
    void deleteAllByPathIn(Collection<String> pathList);

    /**
     * existsByPathAndDeletedFalse
     *
     * @param path path
     * @return exists
     */
    Boolean existsByPathAndDeletedFalse(String path);

    /**
     * findAllByPathInAndDeletedFalse
     *
     * @param pathList path list
     * @return file list
     */
    List<FileData> findAllByPathInAndDeletedFalse(List<String> pathList);

    /**
     * findFirstByPath
     *
     * @param path path
     * @return file
     */
    FileData findFirstByPath(String path);

    /**
     * findFirstByPathAndDeletedFalse
     *
     * @param path path
     * @return file
     */
    FileData findFirstByPathAndDeletedFalse(String path);

    /**
     * countByHashValueList
     *
     * @param hashList hash list
     * @return hash count
     */
    @Query("SELECT new com.github.filefusion.file.model.FileHashUsageCount(f.hashValue, f.mimeType, COUNT(f.id)) FROM file_data f WHERE f.hashValue IN :hashList GROUP BY f.hashValue, f.mimeType")
    List<FileHashUsageCount> countByHashValueList(@Param("hashList") Collection<String> hashList);

    /**
     * findAllByDeletedTrueAndDeletedDateBefore
     *
     * @param deletedDate deleted date before
     * @return file list
     */
    List<FileData> findAllByDeletedTrueAndDeletedDateBefore(LocalDateTime deletedDate);

}
