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
    }

    public ThirdPartyFiscalProfileCommand(UUID thirdPartyId, String taxRegime, Set<String> taxResponsibilities,
            String municipalityCode, String ciiuCode, boolean active) {
        this(thirdPartyId, taxRegime, taxResponsibilities, municipalityCode, ciiuCode, null, active);
    }
}
