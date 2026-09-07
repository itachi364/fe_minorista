package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.util.Set;
import java.util.UUID;

public record ThirdPartyFiscalProfileCommand(
        UUID thirdPartyId,
        String taxRegime,
        Set<String> taxResponsibilities,
        String municipalityCode,
        String ciiuCode,
        boolean active) {
}
