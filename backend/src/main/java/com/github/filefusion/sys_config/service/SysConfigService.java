package com.github.filefusion.sys_config.service;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.SysConfigKey;
import com.github.filefusion.file.service.RecycleBinService;
import com.github.filefusion.sys_config.entity.SysConfig;
import com.github.filefusion.sys_config.repository.SysConfigRepository;
import com.github.filefusion.util.I18n;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final RecycleBinService recycleBinService;

    @Autowired
    public SysConfigService(SysConfigRepository sysConfigRepository,
                            RecycleBinService recycleBinService) {
        this.sysConfigRepository = sysConfigRepository;
        this.recycleBinService = recycleBinService;
    }

    public SysConfig get(SysConfigKey configKey) {
        return sysConfigRepository.findFirstByConfigKey(configKey.name()).orElseThrow();
    }

    @Transactional(rollbackFor = HttpException.class)
    public SysConfig update(SysConfig sysConfig) {
        if (!StringUtils.hasLength(sysConfig.getConfigKey())) {
            throw new HttpException(I18n.get("configKeyNotExist"));
        }
        if (!StringUtils.hasLength(sysConfig.getConfigValue())) {
            throw new HttpException(I18n.get("configValueCannotNull"));
        }
        SysConfigKey sysConfigKey = SysConfigKey.valueOf(sysConfig.getConfigKey());
        SysConfig oldSysConfig = get(sysConfigKey);
        oldSysConfig.setConfigValue(sysConfig.getConfigValue());
        if (SysConfigKey.RECYCLE_BIN.equals(sysConfigKey) && !"true".equals(sysConfig.getConfigValue())) {
            recycleBinService.deleteAll();
        }
        return sysConfigRepository.save(oldSysConfig);
    }

}
