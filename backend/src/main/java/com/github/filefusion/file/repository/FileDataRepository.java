package com.github.filefusion.file.repository;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.FileHashUsageCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
     * findAllByPathLikeAndPathNotLikeAndNameLikeAndDeletedFalse
     *
     * @param path        path
     * @param excludePath exclude path
     * @param name        name
     * @param page        page
     * @return file list
     */
    Page<FileData> findAllByPathLikeAndPathNotLikeAndNameLikeAndDeletedFalse(String path, String excludePath, String name, Pageable page);

    /**
     * findAllByRecyclePathLikeAndRecyclePathNotLikeAndNameLikeAndDeletedTrue
     *
     * @param recyclePath        recycle path
     * @param excludeRecyclePath exclude recycle path
     * @param name               name
     * @param page               page
     * @return file list
     */
    Page<FileData> findAllByRecyclePathLikeAndRecyclePathNotLikeAndNameLikeAndDeletedTrue(String recyclePath, String excludeRecyclePath, String name, Pageable page);

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
    @Modifying
    @Transactional(rollbackFor = HttpException.class)
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
