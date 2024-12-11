package com.github.filefusion.user.repository;

import com.github.filefusion.user.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * RolePermissionRepository
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {

    /**
     * findAllByRoleId
     *
     * @param roleId role id
     * @return role permission list
     */
    List<RolePermission> findAllByRoleId(String roleId);

    /**
     * deleteAllByRoleId
     *
     * @param roleId role id
     */
    void deleteAllByRoleId(String roleId);

}
