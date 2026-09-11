package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.util.Set;
import java.util.UUID;

public record ThirdPartyFiscalProfile(
        UUID thirdPartyId,
        String taxRegime,
        Set<String> taxResponsibilities,
        String municipalityCode,
        Set<String> ciiuCodes,
        String personType,
        String taxResidency,
        String incomeTaxStatus,
        Set<String> selfWithholdingScopes,
        String fiscalEvidenceReference,
        boolean active) {

    public ThirdPartyFiscalProfile {
        taxResponsibilities = taxResponsibilities == null ? Set.of() : Set.copyOf(taxResponsibilities);
        ciiuCodes = ciiuCodes == null ? Set.of() : Set.copyOf(ciiuCodes);
        personType = personType == null ? "UNKNOWN" : personType;
        taxResidency = taxResidency == null ? "UNKNOWN" : taxResidency;
        incomeTaxStatus = incomeTaxStatus == null ? "UNKNOWN" : incomeTaxStatus;
        selfWithholdingScopes = selfWithholdingScopes == null ? Set.of() : Set.copyOf(selfWithholdingScopes);
    }

    public ThirdPartyFiscalProfile(UUID thirdPartyId, String taxRegime, Set<String> taxResponsibilities,
            String municipalityCode, String ciiuCode, boolean active) {
        this(thirdPartyId, taxRegime, taxResponsibilities, municipalityCode,
                ciiuCode == null || ciiuCode.isBlank() ? Set.of() : Set.of(ciiuCode),
                "UNKNOWN", "UNKNOWN", "UNKNOWN", Set.of(), null, active);
    }

    public ThirdPartyFiscalProfile(UUID thirdPartyId, String taxRegime, Set<String> taxResponsibilities,
            String municipalityCode, Set<String> ciiuCodes, boolean active) {
        this(thirdPartyId, taxRegime, taxResponsibilities, municipalityCode, ciiuCodes,
                "UNKNOWN", "UNKNOWN", "UNKNOWN", Set.of(), null, active);
    }

    public String ciiuCode() {
        return ciiuCodes.stream().sorted().findFirst().orElse(null);
    }

    public boolean hasCiiu(String code) {
        return code != null && ciiuCodes.contains(code);
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
