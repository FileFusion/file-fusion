package com.github.filefusion.sys_config.service;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.SysConfigKey;
import com.github.filefusion.sys_config.entity.SysConfig;
import com.github.filefusion.sys_config.repository.SysConfigRepository;
import com.github.filefusion.util.I18n;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * SysConfigService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Service
public class SysConfigService {

    private final SysConfigRepository sysConfigRepository;

    @Autowired
    public SysConfigService(SysConfigRepository sysConfigRepository) {
        this.sysConfigRepository = sysConfigRepository;
    }

    public SysConfig get(SysConfigKey configKey) {
        return sysConfigRepository.findFirstByConfigKey(configKey.name()).orElseThrow();
    }

    public SysConfig update(SysConfig sysConfig) {
        if (!StringUtils.hasLength(sysConfig.getConfigKey())) {
            throw new HttpException(I18n.get("configKeyNotExist"));
        }
        if (!StringUtils.hasLength(sysConfig.getConfigValue())) {
            throw new HttpException(I18n.get("configValueCannotNull"));
        }
        SysConfigKey sysConfigKey;
        try {
            sysConfigKey = SysConfigKey.valueOf(sysConfig.getConfigKey());
        } catch (IllegalArgumentException e) {
            throw new HttpException(I18n.get("configKeyNotExist"));
        }
        SysConfig oldSysConfig = get(sysConfigKey);
        oldSysConfig.setConfigValue(sysConfig.getConfigValue());
        return sysConfigRepository.save(oldSysConfig);
    }

}
