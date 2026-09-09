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
        if (hasCode(rutResponsibilities, "48") && hasCode(rutResponsibilities, "49")) {
            throw new IllegalArgumentException("RUT responsibilities 48 and 49 are mutually exclusive");
        }
        if (rutResponsibilities.contains("R-99-PN") && rutResponsibilities.size() > 1) {
            throw new IllegalArgumentException("RUT responsibility R-99-PN cannot be combined with other responsibilities");
        }
        if (hasCode(rutResponsibilities, "47") && !"SIMPLE".equals(taxRegime)) {
            throw new IllegalArgumentException("RUT responsibility 47 requires SIMPLE tax regime");
        }

        vatResponsible = hasCode(rutResponsibilities, "48");
        withholdingAgent = hasCode(rutResponsibilities, "07");
        vatWithholdingAgent = hasCode(rutResponsibilities, "23");
        largeTaxpayer = hasCode(rutResponsibilities, "13");
        selfWithholding = hasCode(rutResponsibilities, "15") || hasCode(rutResponsibilities, "59");
        simpleRegime = "SIMPLE".equals(taxRegime) || hasCode(rutResponsibilities, "47");
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

    private static boolean hasCode(Set<String> values, String expected) {
        return values.stream().anyMatch(value -> value.equals(expected) || value.equals("O-" + expected));
    }
}
