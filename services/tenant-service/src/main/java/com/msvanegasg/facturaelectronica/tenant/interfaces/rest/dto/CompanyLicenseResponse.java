package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicenseStatus;

public record CompanyLicenseResponse(
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
}
