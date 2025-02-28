package com.github.filefusion.sys_config.controller;

import com.github.filefusion.constant.SysConfigKey;
import com.github.filefusion.sys_config.entity.SysConfig;
import com.github.filefusion.sys_config.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * SysConfigController
 *
 * @author hackyo
 * @since 2022/4/1
 */
@RestController
@RequestMapping("/sys-config")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @Autowired
    public SysConfigController(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    /**
     * get sys config
     *
     * @return sys config
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('sys_config:recycle_bin_read')")
    public SysConfig get(@RequestParam SysConfigKey configKey) {
        return sysConfigService.get(configKey);
    }

    /**
     * update sys config
     *
     * @param sysConfig sys config
     * @return sys config
     */
    @PutMapping
    @PreAuthorize("hasAnyAuthority('sys_config:recycle_bin_edit')")
    public SysConfig update(@RequestBody SysConfig sysConfig) {
        return sysConfigService.update(sysConfig);
    }

}
