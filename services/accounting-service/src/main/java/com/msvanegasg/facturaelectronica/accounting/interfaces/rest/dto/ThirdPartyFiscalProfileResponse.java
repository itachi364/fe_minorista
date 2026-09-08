package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.util.Set;
import java.util.UUID;

public record ThirdPartyFiscalProfileResponse(
        UUID thirdPartyId,
        String taxRegime,
        Set<String> taxResponsibilities,
        String municipalityCode,
        String ciiuCode,
        Set<String> ciiuCodes,
        boolean active) {
}
