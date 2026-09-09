package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicenseStatus;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseAction;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseModule;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseFeature;

public record CompanyLicenseValidationResult(
        UUID companyId,
        LicenseAction action,
        LicenseModule module,
        LicenseFeature feature,
        boolean allowed,
        CompanyLicenseStatus status,
        String planCode,
        Integer maxUsers,
        Integer maxMonthlyDocuments,
        String reasonCode,
        String message) {
}
