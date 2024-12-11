package com.github.filefusion.user.service;

import com.github.filefusion.user.entity.Permission;
import com.github.filefusion.user.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PermissionService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Autowired
    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> get(Boolean basics) {
        if (basics == null) {
            return permissionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        } else {
            return permissionRepository.findAllByBasicsOrderByNameAsc(basics);
        }
    }

}
