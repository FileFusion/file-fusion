package com.github.filefusion.user.repository;

import com.github.filefusion.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * UserRoleRepository
 *
 * @author 13712
 * @since 2022/4/1
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {

    /**
     * deleteAllByRoleId
     *
     * @param roleId role id
     */
    void deleteAllByRoleId(String roleId);

    /**
     * deleteAllByUserId
     *
     * @param userId user id
     */
    void deleteAllByUserId(String userId);

    /**
     * deleteAllByUserIdIn
     *
     * @param userIds user ids
     */
    void deleteAllByUserIdIn(List<String> userIds);

    /**
     * findAllByUserIdIn
     *
     * @param userIds user ids
     * @return user role list
     */
    List<UserRole> findAllByUserIdIn(List<String> userIds);

}
