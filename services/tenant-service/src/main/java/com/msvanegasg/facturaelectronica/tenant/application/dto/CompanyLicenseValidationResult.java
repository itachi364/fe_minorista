package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicenseStatus;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseAction;

public record CompanyLicenseValidationResult(
        UUID companyId,
        LicenseAction action,
        boolean allowed,
        CompanyLicenseStatus status,
        String reasonCode,
        String message) {
}
