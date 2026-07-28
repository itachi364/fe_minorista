package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicense;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicenseStatus;

public record CompanyLicenseResult(
        UUID id,
        UUID companyId,
        String planCode,
        CompanyLicenseStatus status,
        LocalDate validFrom,
        LocalDate validTo,
        Integer maxUsers,
        Integer maxMonthlyDocuments,
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
                license.createdAt(),
                license.updatedAt());
    }
}
