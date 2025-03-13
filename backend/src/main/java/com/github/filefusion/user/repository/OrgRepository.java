package com.github.filefusion.user.repository;

import com.github.filefusion.user.entity.Org;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OrgRepository
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Repository
public interface OrgRepository extends JpaRepository<Org, String> {

    /**
     * existsByNameAndParentIdAndIdNot
     *
     * @param name     name
     * @param parentId parent id
     * @param id       org id
     * @return exists
     */
    boolean existsByNameAndParentIdAndIdNot(String name, String parentId, String id);

    /**
     * existsByNameAndParentId
     *
     * @param name     name
     * @param parentId parent id
     * @return exists
     */
    boolean existsByNameAndParentId(String name, String parentId);

    /**
     * existsByParentId
     *
     * @param parentId parent id
     * @return exists
     */
    boolean existsByParentId(String parentId);

    /**
     * findAllByParentId
     *
     * @param parentId parent id
     * @return org list
     */
    List<Org> findAllByParentId(String parentId);

}
