package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicenseStatus;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseModule;

public record CompanyLicenseResponse(
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
}
