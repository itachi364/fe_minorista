package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record WithholdingRule(
        UUID id,
        UUID companyId,
        String ruleSetVersion,
        FiscalOperationType operationType,
        String conceptCode,
        WithholdingType withholdingType,
        BigDecimal baseMinAmount,
        BigDecimal rate,
        boolean requiresCompanyWithholdingAgent,
        boolean requiresCompanyVatResponsible,
        String requiredThirdPartyTaxRegime,
        String requiredThirdPartyResponsibility,
        String municipalityCode,
        String ciiuCode,
        LocalDate validFrom,
        LocalDate validTo,
        int priority,
        boolean active) {

    public WithholdingRule {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(ruleSetVersion, "ruleSetVersion is required");
        Objects.requireNonNull(operationType, "operationType is required");
        Objects.requireNonNull(withholdingType, "withholdingType is required");
        baseMinAmount = baseMinAmount == null ? BigDecimal.ZERO : baseMinAmount;
        Objects.requireNonNull(rate, "rate is required");
        Objects.requireNonNull(validFrom, "validFrom is required");
        if (rate.signum() < 0) {
            throw new IllegalArgumentException("withholding rule rate cannot be negative");
        }
        if (validTo != null && validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("withholding rule validTo cannot be before validFrom");
        }
    }

    public boolean appliesTo(FiscalOperationType operationType, String conceptCode, LocalDate operationDate,
            CompanyTaxProfile companyProfile, ThirdPartyFiscalProfile thirdPartyProfile) {
        return active
                && this.operationType == operationType
                && matchesConcept(conceptCode)
                && matchesDate(operationDate)
                && matchesCompany(companyProfile)
                && matchesThirdParty(thirdPartyProfile);
    }

    private boolean matchesConcept(String requestedConceptCode) {
        return conceptCode == null || conceptCode.isBlank() || "ANY".equals(conceptCode)
                || Objects.equals(conceptCode, requestedConceptCode);
    }

    private boolean matchesDate(LocalDate operationDate) {
        return !operationDate.isBefore(validFrom) && (validTo == null || !operationDate.isAfter(validTo));
    }

    private boolean matchesCompany(CompanyTaxProfile companyProfile) {
        if (requiresCompanyWithholdingAgent && !companyProfile.withholdingAgent()) {
            return false;
        }
        if (requiresCompanyVatResponsible && !companyProfile.vatResponsible()) {
            return false;
        }
        return true;
    }

    private boolean matchesThirdParty(ThirdPartyFiscalProfile thirdPartyProfile) {
        if (requiredThirdPartyTaxRegime != null
                && !Objects.equals(requiredThirdPartyTaxRegime, thirdPartyProfile.taxRegime())) {
            return false;
        }
        if (requiredThirdPartyResponsibility != null
                && !thirdPartyProfile.hasResponsibility(requiredThirdPartyResponsibility)) {
            return false;
        }
        if (municipalityCode != null && !Objects.equals(municipalityCode, thirdPartyProfile.municipalityCode())) {
            return false;
        }
        return ciiuCode == null || Objects.equals(ciiuCode, thirdPartyProfile.ciiuCode());
    }
}
