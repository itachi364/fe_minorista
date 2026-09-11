package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;

public record CompanyTaxProfileRequest(
        String companySize,
        String financialReportingGroup,
        @NotBlank String taxRegime,
        Set<String> rutResponsibilities,
        boolean vatResponsible,
        boolean withholdingAgent,
        boolean vatWithholdingAgent,
        boolean icaWithholdingAgent,
        boolean largeTaxpayer,
        boolean selfWithholding,
        boolean simpleRegime,
        String icaMunicipalityCode,
        Set<String> ciiuCodes,
        String taxResidency,
        String incomeTaxStatus,
        Set<String> selfWithholdingScopes,
        String fiscalEvidenceReference) {
}
