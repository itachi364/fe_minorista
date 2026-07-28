package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicenseStatus;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseAction;

public record CompanyLicenseValidationResponse(
        UUID companyId,
        LicenseAction action,
        boolean allowed,
        CompanyLicenseStatus status,
        String reasonCode,
        String message) {
}
