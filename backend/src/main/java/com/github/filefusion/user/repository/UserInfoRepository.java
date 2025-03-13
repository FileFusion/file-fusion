package com.github.filefusion.user.repository;

import com.github.filefusion.user.entity.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * UserInfoRepository
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {

    /**
     * findByUsername
     *
     * @param username username
     * @return user
     */
    UserInfo findByUsername(String username);

    /**
     * findAllByIdNotInOrderByUsernameAsc
     *
     * @param ids user ids
     * @return user list
     */
    List<UserInfo> findAllByIdNotInOrderByUsernameAsc(List<String> ids);

    /**
     * findAllByIdInOrderByUsernameAsc
     *
     * @param orgId  org id
     * @param search search
     * @param page   page
     * @return user list
     */
    @Query("FROM user_info u WHERE u.id IN (SELECT ou.userId FROM org_user ou WHERE ou.orgId = ?1) AND (u.username LIKE %?2% OR u.name LIKE %?2% OR u.email LIKE %?2% OR CONCAT(u.areaCode, u.phone) LIKE %?2%) ORDER BY u.username ASC")
    Page<UserInfo> findAllByIdInOrderByUsernameAsc(String orgId, String search, Pageable page);

    /**
     * findAllOrderBySort
     *
     * @param search search
     * @param page   page
     * @return user list
     */
    @Query("FROM user_info u WHERE u.username LIKE %?1% OR u.name LIKE %?1% OR u.email LIKE %?1% OR CONCAT(u.areaCode, u.phone) LIKE %?1%")
    Page<UserInfo> findAllOrderBySort(String search, Pageable page);

    /**
     * existsByUsername
     *
     * @param username username
     * @return exists
     */
    boolean existsByUsername(String username);

}
