package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.time.LocalDate;
import java.util.Set;

import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseModule;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseFeature;

public record CompanyLicenseCommand(
        String planCode,
        LocalDate validFrom,
        LocalDate validTo,
        Integer maxUsers,
        Integer maxMonthlyDocuments,
        Set<LicenseModule> enabledModules,
        Set<LicenseFeature> enabledFeatures) {
}
