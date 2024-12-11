package com.github.filefusion.user.repository;

import com.github.filefusion.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * RoleRepository
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    /**
     * existsByNameAndIdNot
     *
     * @param name name
     * @param id   role id
     * @return exists
     */
    Boolean existsByNameAndIdNot(String name, String id);

    /**
     * existsByName
     *
     * @param name name
     * @return exists
     */
    Boolean existsByName(String name);

}
