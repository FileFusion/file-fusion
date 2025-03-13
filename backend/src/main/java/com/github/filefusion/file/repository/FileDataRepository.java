package com.github.filefusion.file.repository;

import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.FileHashUsageCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FileDataRepository
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Repository
public interface FileDataRepository extends JpaRepository<FileData, String> {

    /**
     * findAllByUserIdAndParentIdAndDeletedFalse
     *
     * @param userId   user id
     * @param parentId parent id
     * @param page     page
     * @return file list
     */
    Page<FileData> findAllByUserIdAndParentIdAndDeletedFalse(String userId, String parentId, Pageable page);

    /**
     * findFirstByUserIdAndId
     *
     * @param userId user id
     * @param id     id
     * @return file
     */
    Optional<FileData> findFirstByUserIdAndId(String userId, String id);

    /**
     * findAllByParentIdIn
     *
     * @param parentIdList parent id list
     * @return file list
     */
    List<FileData> findAllByParentIdIn(List<String> parentIdList);

    /**
     * findAllByUserIdAndRelativePathIn
     *
     * @param userId           user id
     * @param relativePathList relative path list
     * @return file
     */
    List<FileData> findAllByUserIdAndRelativePathIn(String userId, List<String> relativePathList);

    /**
     * existsByUserIdAndParentIdAndName
     *
     * @param userId   user id
     * @param parentId parent id
     * @param name     name
     * @return exists
     */
    boolean existsByUserIdAndParentIdAndName(String userId, String parentId, String name);

    /**
     * findAllByUserIdAndIdIn
     *
     * @param userId user id
     * @param idList id list
     * @return file list
     */
    List<FileData> findAllByUserIdAndIdIn(String userId, List<String> idList);

    /**
     * countByHashValueList
     *
     * @param hashList hash list
     * @return hash count list
     */
    @Query("SELECT new com.github.filefusion.file.model.FileHashUsageCount(f.hashValue, f.mimeType, COUNT(f.id)) FROM file_data f WHERE f.hashValue IN :hashList GROUP BY f.hashValue, f.mimeType")
    List<FileHashUsageCount> countByHashValueList(@Param("hashList") List<String> hashList);

    /**
     * findAllByDeletedTrueAndDeletedDateBefore
     *
     * @param deletedDate deleted date before
     * @return file list
     */
    List<FileData> findAllByDeletedTrueAndDeletedDateBefore(LocalDateTime deletedDate);

}
