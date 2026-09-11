package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.util.Set;
import java.util.UUID;

public record CompanyTaxProfileCommand(
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
        UUID updatedBy) {

    public CompanyTaxProfileCommand(String companySize, String financialReportingGroup, String taxRegime,
            Set<String> rutResponsibilities, boolean vatResponsible, boolean withholdingAgent,
            boolean vatWithholdingAgent, boolean icaWithholdingAgent, boolean largeTaxpayer,
            boolean selfWithholding, boolean simpleRegime, String icaMunicipalityCode, Set<String> ciiuCodes,
            UUID updatedBy) {
        this(companySize, financialReportingGroup, taxRegime, rutResponsibilities, vatResponsible, withholdingAgent,
                vatWithholdingAgent, icaWithholdingAgent, largeTaxpayer, selfWithholding, simpleRegime,
                icaMunicipalityCode, ciiuCodes, "UNKNOWN", "UNKNOWN", Set.of(), null, updatedBy);
    }
}
