package com.github.filefusion.file.repository;

import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.FileHashUsageCountModel;
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
     * @param deleted  deleted
     * @param name     name
     * @param page     page
     * @return file list
     */
    Page<FileData> findAllByUserIdAndParentIdAndNameLikeAndDeleted(String userId, String parentId, String name, boolean deleted, Pageable page);

    /**
     * findAllByUserIdAndParentIdAndMimeTypeAndDeletedFalse
     *
     * @param userId   user id
     * @param parentId parent id
     * @param mimeType mime type
     * @return file list
     */
    List<FileData> findAllByUserIdAndParentIdAndMimeTypeAndDeletedFalse(String userId, String parentId, String mimeType);

    /**
     * findAllByUserIdAndParentIdAndDeletedTrue
     *
     * @param userId   user id
     * @param parentId parent id
     * @return file list
     */
    List<FileData> findAllByUserIdAndParentIdAndDeletedTrue(String userId, String parentId);

    /**
     * findFirstByUserIdAndId
     *
     * @param userId user id
     * @param id     id
     * @return file
     */
    Optional<FileData> findFirstByUserIdAndId(String userId, String id);

    /**
     * findFirstByUserIdAndIdAndDeletedFalse
     *
     * @param userId user id
     * @param id     id
     * @return file
     */
    Optional<FileData> findFirstByUserIdAndIdAndDeletedFalse(String userId, String id);

    /**
     * findFirstByUserIdAndIdAndDeletedTrue
     *
     * @param userId user id
     * @param id     id
     * @return file
     */
    Optional<FileData> findFirstByUserIdAndIdAndDeletedTrue(String userId, String id);

    /**
     * existsByUserIdAndIdAndDeletedFalse
     *
     * @param userId user id
     * @param id     id
     * @return exists
     */
    boolean existsByUserIdAndIdAndDeletedFalse(String userId, String id);

    /**
     * findAllByParentIdIn
     *
     * @param parentIdList parent id list
     * @return file list
     */
    List<FileData> findAllByParentIdIn(List<String> parentIdList);

    /**
     * findAllByUserIdAndPathInAndDeletedFalse
     *
     * @param userId   user id
     * @param pathList path list
     * @return file list
     */
    List<FileData> findAllByUserIdAndPathInAndDeletedFalse(String userId, List<String> pathList);

    /**
     * findFirstByUserIdAndPathAndDeletedFalse
     *
     * @param userId user id
     * @param path   path
     * @return file
     */
    Optional<FileData> findFirstByUserIdAndPathAndDeletedFalse(String userId, String path);

    /**
     * existsByUserIdAndParentIdAndNameAndDeletedFalse
     *
     * @param userId   user id
     * @param parentId parent id
     * @param name     name
     * @return exists
     */
    boolean existsByUserIdAndParentIdAndNameAndDeletedFalse(String userId, String parentId, String name);

    /**
     * findAllByUserIdAndIdInAndDeletedFalse
     *
     * @param userId user id
     * @param idList id list
     * @return file list
     */
    List<FileData> findAllByUserIdAndIdInAndDeletedFalse(String userId, List<String> idList);

    /**
     * countByHashValueList
     *
     * @param hashList hash list
     * @return hash count list
     */
    @Query("SELECT new com.github.filefusion.file.model.FileHashUsageCountModel(f.hashValue, f.mimeType, COUNT(f.id)) FROM file_data f WHERE f.hashValue IN :hashList GROUP BY f.hashValue, f.mimeType")
    List<FileHashUsageCountModel> countByHashValueList(@Param("hashList") List<String> hashList);

    /**
     * findAllByDeletedTrueAndDeletedDateBefore
     *
     * @param deletedDate deleted date before
     * @return file list
     */
    List<FileData> findAllByDeletedTrueAndDeletedDateBefore(LocalDateTime deletedDate);

}
