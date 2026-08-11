package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicense;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicenseStatus;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseModule;

public record CompanyLicenseResult(
        UUID id,
        UUID companyId,
        String planCode,
        CompanyLicenseStatus status,
        LocalDate validFrom,
        LocalDate validTo,
        Integer maxUsers,
        Integer maxMonthlyDocuments,
        Set<LicenseModule> enabledModules,
        Instant createdAt,
        Instant updatedAt) {

    public static CompanyLicenseResult from(CompanyLicense license) {
        return new CompanyLicenseResult(
                license.id(),
                license.companyId(),
                license.planCode(),
                license.status(),
                license.validFrom(),
                license.validTo(),
                license.maxUsers(),
                license.maxMonthlyDocuments(),
                license.enabledModules(),
                license.createdAt(),
                license.updatedAt());
    }
}
