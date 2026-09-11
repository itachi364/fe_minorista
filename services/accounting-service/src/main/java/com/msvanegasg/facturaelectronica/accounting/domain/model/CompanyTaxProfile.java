package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.util.Set;

public record CompanyTaxProfile(
        String taxRegime,
        Set<String> rutResponsibilities,
        boolean vatResponsible,
        boolean withholdingAgent,
        boolean largeTaxpayer,
        boolean selfWithholding,
        boolean simpleRegime,
        String icaMunicipalityCode,
        Set<String> ciiuCodes,
        boolean vatWithholdingAgent,
        boolean icaWithholdingAgent,
        String taxResidency,
        String incomeTaxStatus,
        Set<String> selfWithholdingScopes,
        String fiscalEvidenceReference) {

    public CompanyTaxProfile {
        rutResponsibilities = rutResponsibilities == null ? Set.of() : Set.copyOf(rutResponsibilities);
        ciiuCodes = ciiuCodes == null ? Set.of() : Set.copyOf(ciiuCodes);
        taxResidency = taxResidency == null ? "UNKNOWN" : taxResidency;
        incomeTaxStatus = incomeTaxStatus == null ? "UNKNOWN" : incomeTaxStatus;
        selfWithholdingScopes = selfWithholdingScopes == null ? Set.of() : Set.copyOf(selfWithholdingScopes);
    }

    public CompanyTaxProfile(String taxRegime, Set<String> rutResponsibilities, boolean vatResponsible,
            boolean withholdingAgent, boolean largeTaxpayer, boolean selfWithholding, boolean simpleRegime,
            String icaMunicipalityCode, Set<String> ciiuCodes) {
        this(taxRegime, rutResponsibilities, vatResponsible, withholdingAgent, largeTaxpayer, selfWithholding,
                simpleRegime, icaMunicipalityCode, ciiuCodes,
                rutResponsibilities != null && rutResponsibilities.contains("O-23"), false,
                "UNKNOWN", "UNKNOWN", Set.of(), null);
    }

    public CompanyTaxProfile(String taxRegime, Set<String> rutResponsibilities, boolean vatResponsible,
            boolean withholdingAgent, boolean largeTaxpayer, boolean selfWithholding, boolean simpleRegime,
            String icaMunicipalityCode, Set<String> ciiuCodes, boolean vatWithholdingAgent,
            boolean icaWithholdingAgent) {
        this(taxRegime, rutResponsibilities, vatResponsible, withholdingAgent, largeTaxpayer, selfWithholding,
                simpleRegime, icaMunicipalityCode, ciiuCodes, vatWithholdingAgent, icaWithholdingAgent,
                "UNKNOWN", "UNKNOWN", Set.of(), null);
    }
}
