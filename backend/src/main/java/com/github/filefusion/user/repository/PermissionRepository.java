package com.github.filefusion.user.repository;

import com.github.filefusion.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PermissionRepository
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    /**
     * findAllByBasicsOrderByNameAsc
     *
     * @param basics basics
     * @return permission list
     */
    List<Permission> findAllByBasicsOrderByNameAsc(Boolean basics);

    /**
     * findAllByIdInAndBasics
     *
     * @param ids    ids
     * @param basics basics
     * @return permission list
     */
    List<Permission> findAllByIdInAndBasics(List<String> ids, Boolean basics);

    /**
     * findAllByUserId
     *
     * @param userId user id
     * @return permission list
     */
    @Query("FROM permission p WHERE p.id IN (SELECT rp.permissionId FROM role_permission rp WHERE rp.roleId IN (SELECT ur.roleId FROM user_role ur WHERE ur.userId = ?1)) OR p.basics = true")
    List<Permission> findAllByUserId(String userId);

    /**
     * findAllByRoleId
     *
     * @param roleId role id
     * @return permission list
     */
    @Query("FROM permission p WHERE p.id IN (SELECT rp.permissionId FROM role_permission rp WHERE rp.roleId = ?1) OR p.basics = true")
    List<Permission> findAllByRoleId(String roleId);

}
