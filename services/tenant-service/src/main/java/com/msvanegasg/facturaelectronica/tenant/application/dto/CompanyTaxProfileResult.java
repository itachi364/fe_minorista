package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyTaxProfile;

public record CompanyTaxProfileResult(
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

    public static CompanyTaxProfileResult from(CompanyTaxProfile profile) {
        return new CompanyTaxProfileResult(profile.companyId(), profile.companySize(),
                profile.financialReportingGroup(), profile.taxRegime(), profile.rutResponsibilities(),
                profile.vatResponsible(), profile.withholdingAgent(), profile.vatWithholdingAgent(),
                profile.icaWithholdingAgent(), profile.largeTaxpayer(), profile.selfWithholding(),
                profile.simpleRegime(), profile.icaMunicipalityCode(), profile.ciiuCodes(), profile.taxResidency(),
                profile.incomeTaxStatus(), profile.selfWithholdingScopes(), profile.fiscalEvidenceReference(),
                profile.updatedBy(), profile.updatedAt());
    }
}
