package com.msvanegasg.facturaelectronica.tenant.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CompanyTaxProfile(
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
        UUID updatedBy,
        Instant updatedAt) {

    public CompanyTaxProfile {
        companySize = normalize(companySize);
        financialReportingGroup = normalize(financialReportingGroup);
        taxRegime = normalize(taxRegime);
        rutResponsibilities = clean(rutResponsibilities);
        icaMunicipalityCode = normalize(icaMunicipalityCode);
        ciiuCodes = clean(ciiuCodes);
        if (companyId == null || taxRegime == null || updatedAt == null) {
            throw new IllegalArgumentException("Company, tax regime and update date are required");
        }
        if (simpleRegime && !"SIMPLE".equals(taxRegime)) {
            throw new IllegalArgumentException("Simple regime flag requires SIMPLE tax regime");
        }
    }

    private static Set<String> clean(Set<String> values) {
        if (values == null) {
            return Set.of();
        }
        return values.stream().map(CompanyTaxProfile::normalize).filter(value -> value != null)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
