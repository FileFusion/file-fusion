package com.github.filefusion.user.controller;

import com.github.filefusion.user.entity.Role;
import com.github.filefusion.user.model.RoleModel;
import com.github.filefusion.user.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RoleController
 *
 * @author hackyo
 * @since 2022/4/1
 */
@RestController
@RequestMapping("/role")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * get role list
     *
     * @return role list
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('role:read', 'user_management:read')")
    public List<Role> get() {
        return roleService.get();
    }

    /**
     * update role
     *
     * @param roleModel role model
     * @return role
     */
    @PutMapping
    @PreAuthorize("hasAuthority('role:edit')")
    public Role update(@RequestBody RoleModel roleModel) {
        return roleService.update(roleModel.getRole(), roleModel.getPermissions());
    }

    /**
     * add role
     *
     * @param roleModel role model
     * @return role
     */
    @PostMapping
    @PreAuthorize("hasAuthority('role:add')")
    public Role add(@RequestBody RoleModel roleModel) {
        return roleService.add(roleModel.getRole(), roleModel.getPermissions());
    }

    /**
     * delete role
     *
     * @param roleId role id
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:delete')")
    public void delete(@PathVariable String roleId) {
        roleService.delete(roleId);
    }

    /**
     * get role permission list
     *
     * @param roleId role id
     * @return permission list
     */
    @GetMapping("/{roleId}/permission")
    @PreAuthorize("hasAuthority('role:read')")
    public List<String> getRolePermission(@PathVariable String roleId) {
        return roleService.getRolePermission(roleId);
    }

}
