package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyTaxProfileCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CreateCompanyCommand;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyRequest;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyResponse;

public final class CompanyRestMapper {

    private CompanyRestMapper() {
    }

    public static CreateCompanyCommand toCommand(CompanyRequest request, UUID updatedBy) {
        return new CreateCompanyCommand(
                request.legalName(),
                request.tradeName(),
                request.identificationTypeCode(),
                request.identificationNumber(),
                request.verificationDigit(),
                request.email(),
                toTaxProfileCommand(request.taxProfile(), updatedBy));
    }

    private static CompanyTaxProfileCommand toTaxProfileCommand(
            com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyTaxProfileRequest request,
            UUID updatedBy) {
        return new CompanyTaxProfileCommand(request.companySize(), request.financialReportingGroup(),
                request.taxRegime(), request.rutResponsibilities(), request.vatResponsible(),
                request.withholdingAgent(), request.vatWithholdingAgent(), request.icaWithholdingAgent(),
                request.largeTaxpayer(), request.selfWithholding(), request.simpleRegime(),
                request.icaMunicipalityCode(), request.ciiuCodes(), updatedBy);
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
