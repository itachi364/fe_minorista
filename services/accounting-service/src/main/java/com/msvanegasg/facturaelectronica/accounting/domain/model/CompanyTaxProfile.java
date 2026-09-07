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
        Set<String> ciiuCodes) {

    public CompanyTaxProfile {
        rutResponsibilities = rutResponsibilities == null ? Set.of() : Set.copyOf(rutResponsibilities);
        ciiuCodes = ciiuCodes == null ? Set.of() : Set.copyOf(ciiuCodes);
    }
}
