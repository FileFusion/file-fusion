package com.github.filefusion.user.repository;

import com.github.filefusion.user.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

/**
 * RolePermissionRepository
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {

    /**
     * deleteAllByRoleId
     *
     * @param roleId role id
     */
    @Modifying
    void deleteAllByRoleId(String roleId);

}
