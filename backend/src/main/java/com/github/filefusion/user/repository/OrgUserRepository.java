package com.github.filefusion.user.repository;

import com.github.filefusion.user.entity.OrgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OrgUserRepository
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Repository
public interface OrgUserRepository extends JpaRepository<OrgUser, String> {

    /**
     * existsByOrgId
     *
     * @param orgId org id
     * @return exists
     */
    boolean existsByOrgId(String orgId);

    /**
     * findAllByOrgIdIn
     *
     * @param orgId org ids
     * @return org user list
     */
    List<OrgUser> findAllByOrgIdIn(List<String> orgId);

    /**
     * deleteAllByUserId
     *
     * @param userId user id
     */
    @Modifying
    void deleteAllByUserId(String userId);

}
