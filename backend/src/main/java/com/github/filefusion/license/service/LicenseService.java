package com.github.filefusion.license.service;

import com.github.filefusion.license.entity.License;
import com.github.filefusion.license.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * LicenseService
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;

    @Autowired
    public LicenseService(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    public License getCurrentLicense() {
        License license = licenseRepository.findFirstByStartDateNotNullOrderByStartDateDesc();
        if (license != null) {
            return license;
        }
        return licenseRepository.findFirstByAuthorizedTo("Personal");
    }

}
