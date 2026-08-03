package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CreateCompanyCommand;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyRequest;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyResponse;

public final class CompanyRestMapper {

    private CompanyRestMapper() {
    }

    public static CreateCompanyCommand toCommand(CompanyRequest request) {
        return new CreateCompanyCommand(
                request.legalName(),
                request.tradeName(),
                request.identificationTypeCode(),
                request.identificationNumber(),
                request.verificationDigit(),
                request.email());
    }

    public static CompanyResponse toResponse(CompanyResult result) {
        return new CompanyResponse(
                result.id(),
                result.legalName(),
                result.tradeName(),
                result.identificationTypeCode(),
                result.identificationNumber(),
                result.verificationDigit(),
                result.email(),
                result.status(),
                result.createdAt(),
                result.updatedAt());
    }
}
