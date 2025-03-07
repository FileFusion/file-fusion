package com.github.filefusion.file.repository;

import com.github.filefusion.file.entity.FileData;
import com.github.filefusion.file.model.FileMd5UsageCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
    FileData findFirstByUserIdAndId(String userId, String id);

    /**
     * findAllByParentId
     *
     * @param parentId parent id
     * @return file list
     */
    List<FileData> findAllByParentId(String parentId);

    /**
     * findFirstByUserIdAndParentIdAndName
     *
     * @param userId   user id
     * @param parentId parent id
     * @param name     name
     * @return file
     */
    FileData findFirstByUserIdAndParentIdAndName(String userId, String parentId, String name);

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
     * countByMd5ValueList
     *
     * @param md5List md5 list
     * @return md5 count list
     */
    @Query("SELECT new com.github.filefusion.file.model.FileMd5UsageCount(f.md5Value, f.mimeType, COUNT(f.id)) FROM file_data f WHERE f.md5Value IN :md5List GROUP BY f.md5Value, f.mimeType")
    List<FileMd5UsageCount> countByMd5ValueList(@Param("hashList") List<String> md5List);

    /**
     * findAllByDeletedTrueAndDeletedDateBefore
     *
     * @param deletedDate deleted date before
     * @return file list
     */
    List<FileData> findAllByDeletedTrueAndDeletedDateBefore(LocalDateTime deletedDate);

}
