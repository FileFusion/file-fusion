package com.github.filefusion.license.controller;

import com.github.filefusion.license.entity.License;
import com.github.filefusion.license.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LicenseController
 *
 * @author hackyo
 * @since 2022/4/1
 */
@RestController
@RequestMapping("/license")
public class LicenseController {

    private final LicenseService licenseService;

    @Autowired
    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    /**
     * get current license
     *
     * @return license
     */
    @GetMapping("/current")
    @PreAuthorize("hasAuthority('license:read')")
    public License getCurrentLicense() {
        return licenseService.getCurrentLicense();
    }

}
