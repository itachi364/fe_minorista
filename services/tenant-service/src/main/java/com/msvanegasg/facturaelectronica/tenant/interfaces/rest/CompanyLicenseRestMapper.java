package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseValidationResult;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyLicenseRequest;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyLicenseResponse;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyLicenseValidationResponse;

public final class CompanyLicenseRestMapper {

    private CompanyLicenseRestMapper() {
    }

    public static CompanyLicenseCommand toCommand(CompanyLicenseRequest request) {
        return new CompanyLicenseCommand(
                request.planCode(),
                request.validFrom(),
                request.validTo(),
                request.maxUsers(),
                request.maxMonthlyDocuments());
    }

    public static CompanyLicenseResponse toResponse(CompanyLicenseResult result) {
        return new CompanyLicenseResponse(
                result.id(),
                result.companyId(),
                result.planCode(),
                result.status(),
                result.validFrom(),
                result.validTo(),
                result.maxUsers(),
                result.maxMonthlyDocuments(),
                result.createdAt(),
                result.updatedAt());
    }

    public static CompanyLicenseValidationResponse toResponse(CompanyLicenseValidationResult result) {
        return new CompanyLicenseValidationResponse(
                result.companyId(),
                result.action(),
                result.allowed(),
                result.status(),
                result.reasonCode(),
                result.message());
    }
}
