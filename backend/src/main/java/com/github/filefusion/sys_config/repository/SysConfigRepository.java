package com.github.filefusion.sys_config.repository;

import com.github.filefusion.sys_config.entity.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * SysConfigRepository
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Repository
public interface SysConfigRepository extends JpaRepository<SysConfig, String> {

    /**
     * findFirstByConfigKey
     *
     * @param configKey config key
     * @return sys config
     */
    Optional<SysConfig> findFirstByConfigKey(String configKey);

}
