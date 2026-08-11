package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicenseStatus;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseAction;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseModule;

public record CompanyLicenseValidationResponse(
        UUID companyId,
        LicenseAction action,
        LicenseModule module,
        boolean allowed,
        CompanyLicenseStatus status,
        Integer maxUsers,
        Integer maxMonthlyDocuments,
        String reasonCode,
        String message) {
}
