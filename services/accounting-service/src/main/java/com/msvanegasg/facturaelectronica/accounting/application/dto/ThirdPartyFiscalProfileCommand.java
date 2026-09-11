package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.util.Set;
import java.util.UUID;

public record ThirdPartyFiscalProfileCommand(
        UUID thirdPartyId,
        String taxRegime,
        Set<String> taxResponsibilities,
        String municipalityCode,
        String ciiuCode,
        Set<String> ciiuCodes,
        String personType,
        String taxResidency,
        String incomeTaxStatus,
        Set<String> selfWithholdingScopes,
        String fiscalEvidenceReference,
        boolean active) {

    public ThirdPartyFiscalProfileCommand {
        java.util.LinkedHashSet<String> merged = new java.util.LinkedHashSet<>();
        if (ciiuCodes != null) {
            merged.addAll(ciiuCodes);
        }
        if (ciiuCode != null && !ciiuCode.isBlank()) {
            merged.add(ciiuCode);
        }
        ciiuCodes = Set.copyOf(merged);
        personType = personType == null ? "UNKNOWN" : personType;
        taxResidency = taxResidency == null ? "UNKNOWN" : taxResidency;
        incomeTaxStatus = incomeTaxStatus == null ? "UNKNOWN" : incomeTaxStatus;
        selfWithholdingScopes = selfWithholdingScopes == null ? Set.of() : Set.copyOf(selfWithholdingScopes);
    }

    public ThirdPartyFiscalProfileCommand(UUID thirdPartyId, String taxRegime, Set<String> taxResponsibilities,
            String municipalityCode, String ciiuCode, boolean active) {
        this(thirdPartyId, taxRegime, taxResponsibilities, municipalityCode, ciiuCode, null,
                "UNKNOWN", "UNKNOWN", "UNKNOWN", Set.of(), null, active);
    }

    public ThirdPartyFiscalProfileCommand(UUID thirdPartyId, String taxRegime, Set<String> taxResponsibilities,
            String municipalityCode, String ciiuCode, Set<String> ciiuCodes, boolean active) {
        this(thirdPartyId, taxRegime, taxResponsibilities, municipalityCode, ciiuCode, ciiuCodes,
                "UNKNOWN", "UNKNOWN", "UNKNOWN", Set.of(), null, active);
    }
}
