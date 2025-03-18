package com.github.filefusion.user.repository;

import com.github.filefusion.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    @Modifying
    void deleteAllByRoleId(String roleId);

    /**
     * deleteAllByUserId
     *
     * @param userId user id
     */
    @Modifying
    void deleteAllByUserId(String userId);

    /**
     * findAllByUserIdIn
     *
     * @param userIds user ids
     * @return user role list
     */
    List<UserRole> findAllByUserIdIn(List<String> userIds);

    /**
     * findAllByRoleId
     *
     * @param roleId role id
     * @return user role list
     */
    List<UserRole> findAllByRoleId(String roleId);

}
