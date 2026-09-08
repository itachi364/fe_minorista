package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.util.Set;
import java.util.UUID;

public record ThirdPartyFiscalProfileRequest(
        UUID thirdPartyId,
        String taxRegime,
        Set<String> taxResponsibilities,
        String municipalityCode,
        String ciiuCode,
        Set<String> ciiuCodes,
        boolean active) {

    public ThirdPartyFiscalProfileRequest(UUID thirdPartyId, String taxRegime, Set<String> taxResponsibilities,
            String municipalityCode, String ciiuCode, boolean active) {
        this(thirdPartyId, taxRegime, taxResponsibilities, municipalityCode, ciiuCode, null, active);
    }
}
