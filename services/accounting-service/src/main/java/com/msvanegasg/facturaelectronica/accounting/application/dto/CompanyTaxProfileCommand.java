package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.util.Set;

public record CompanyTaxProfileCommand(
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

    public CompanyTaxProfileCommand(String taxRegime, Set<String> rutResponsibilities, boolean vatResponsible,
            boolean withholdingAgent, boolean largeTaxpayer, boolean selfWithholding, boolean simpleRegime,
            String icaMunicipalityCode, Set<String> ciiuCodes) {
        this(taxRegime, rutResponsibilities, vatResponsible, withholdingAgent, largeTaxpayer, selfWithholding,
                simpleRegime, icaMunicipalityCode, ciiuCodes,
                rutResponsibilities != null && rutResponsibilities.contains("O-23"), false,
                "UNKNOWN", "UNKNOWN", Set.of(), null);
    }

    public CompanyTaxProfileCommand(String taxRegime, Set<String> rutResponsibilities, boolean vatResponsible,
            boolean withholdingAgent, boolean largeTaxpayer, boolean selfWithholding, boolean simpleRegime,
            String icaMunicipalityCode, Set<String> ciiuCodes, boolean vatWithholdingAgent,
            boolean icaWithholdingAgent) {
        this(taxRegime, rutResponsibilities, vatResponsible, withholdingAgent, largeTaxpayer, selfWithholding,
                simpleRegime, icaMunicipalityCode, ciiuCodes, vatWithholdingAgent, icaWithholdingAgent,
                "UNKNOWN", "UNKNOWN", Set.of(), null);
    }
}
