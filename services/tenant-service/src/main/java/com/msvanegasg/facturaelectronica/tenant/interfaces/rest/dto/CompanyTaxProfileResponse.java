package com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CompanyTaxProfileResponse(
        UUID companyId,
        String companySize,
        String financialReportingGroup,
        String taxRegime,
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
        String fiscalEvidenceReference,
        UUID updatedBy,
        Instant updatedAt) {
}
