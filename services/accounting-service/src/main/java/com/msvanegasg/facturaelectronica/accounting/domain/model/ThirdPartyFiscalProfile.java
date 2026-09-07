package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.util.Set;
import java.util.UUID;

public record ThirdPartyFiscalProfile(
        UUID thirdPartyId,
        String taxRegime,
        Set<String> taxResponsibilities,
        String municipalityCode,
        String ciiuCode,
        boolean active) {

    public ThirdPartyFiscalProfile {
        taxResponsibilities = taxResponsibilities == null ? Set.of() : Set.copyOf(taxResponsibilities);
    }

    public boolean hasResponsibility(String code) {
        return code != null && taxResponsibilities.contains(code);
    }

    public boolean isSimpleRegime() {
        return "SIMPLE".equals(taxRegime) || hasResponsibility("O-47");
    }

    public boolean isNoResponsibleOrNotApplicable() {
        return "NO_RESPONSABLE_IVA".equals(taxRegime) || hasResponsibility("R-99-PN");
    }
}
