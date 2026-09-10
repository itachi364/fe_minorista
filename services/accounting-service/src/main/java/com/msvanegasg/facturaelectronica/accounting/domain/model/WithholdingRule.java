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
        boolean active,
        FiscalThresholdUnit thresholdUnit,
        BigDecimal thresholdValue,
        FiscalThresholdOperator thresholdOperator,
        FiscalCalculationBase calculationBase,
        FiscalThresholdTreatment thresholdTreatment,
        WithholdingDecision decision,
        boolean requiresCompanyVatWithholdingAgent,
        boolean requiresCompanyIcaWithholdingAgent,
        String legalReference,
        String sourceUrl,
        int specificity,
        boolean published,
        UUID targetThirdPartyId,
        String evidenceReference) {

    public WithholdingRule {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(ruleSetVersion, "ruleSetVersion is required");
        Objects.requireNonNull(operationType, "operationType is required");
        Objects.requireNonNull(withholdingType, "withholdingType is required");
        baseMinAmount = baseMinAmount == null ? BigDecimal.ZERO : baseMinAmount;
        Objects.requireNonNull(rate, "rate is required");
        Objects.requireNonNull(validFrom, "validFrom is required");
        thresholdUnit = thresholdUnit == null ? FiscalThresholdUnit.COP : thresholdUnit;
        thresholdValue = thresholdValue == null ? baseMinAmount : thresholdValue;
        thresholdOperator = thresholdOperator == null ? FiscalThresholdOperator.GTE : thresholdOperator;
        calculationBase = calculationBase == null ? defaultCalculationBase(withholdingType) : calculationBase;
        thresholdTreatment = thresholdTreatment == null ? FiscalThresholdTreatment.FULL_AMOUNT : thresholdTreatment;
        decision = decision == null ? WithholdingDecision.APPLIED : decision;
        if (rate.signum() < 0) {
            throw new IllegalArgumentException("withholding rule rate cannot be negative");
        }
        if (validTo != null && validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("withholding rule validTo cannot be before validFrom");
        }
    }

    public WithholdingRule(UUID id, UUID companyId, String ruleSetVersion, FiscalOperationType operationType,
            String conceptCode, WithholdingType withholdingType, BigDecimal baseMinAmount, BigDecimal rate,
            boolean requiresCompanyWithholdingAgent, boolean requiresCompanyVatResponsible,
            String requiredThirdPartyTaxRegime, String requiredThirdPartyResponsibility, String municipalityCode,
            String ciiuCode, LocalDate validFrom, LocalDate validTo, int priority, boolean active) {
        this(id, companyId, ruleSetVersion, operationType, conceptCode, withholdingType, baseMinAmount, rate,
                requiresCompanyWithholdingAgent, requiresCompanyVatResponsible, requiredThirdPartyTaxRegime,
                requiredThirdPartyResponsibility, municipalityCode, ciiuCode, validFrom, validTo, priority, active,
                FiscalThresholdUnit.COP, baseMinAmount, FiscalThresholdOperator.GTE,
                defaultCalculationBase(withholdingType), FiscalThresholdTreatment.FULL_AMOUNT,
                WithholdingDecision.APPLIED, false, false, null, null, 0, true, null, null);
    }

    public boolean appliesTo(FiscalOperationType operationType, String conceptCode, LocalDate operationDate,
            CompanyTaxProfile companyProfile, ThirdPartyFiscalProfile thirdPartyProfile) {
        return appliesTo(operationType, conceptCode, operationDate, companyProfile, thirdPartyProfile,
                thirdPartyProfile.municipalityCode());
    }

    public boolean appliesTo(FiscalOperationType operationType, String conceptCode, LocalDate operationDate,
            CompanyTaxProfile companyProfile, ThirdPartyFiscalProfile thirdPartyProfile,
            String operationMunicipalityCode) {
        return active && published
                && this.operationType == operationType
                && matchesConcept(conceptCode)
                && matchesDate(operationDate)
                && matchesCompany(companyProfile)
                && matchesThirdParty(thirdPartyProfile, operationMunicipalityCode);
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
        if (requiresCompanyVatWithholdingAgent && !companyProfile.vatWithholdingAgent()) {
            return false;
        }
        if (requiresCompanyIcaWithholdingAgent && !companyProfile.icaWithholdingAgent()) {
            return false;
        }
        if (withholdingType == WithholdingType.AUTORETENCION && ciiuCode != null
                && !companyProfile.ciiuCodes().contains(ciiuCode)) {
            return false;
        }
        return true;
    }

    private boolean matchesThirdParty(ThirdPartyFiscalProfile thirdPartyProfile, String operationMunicipalityCode) {
        if (targetThirdPartyId != null && !Objects.equals(targetThirdPartyId, thirdPartyProfile.thirdPartyId())) {
            return false;
        }
        if (requiredThirdPartyTaxRegime != null
                && !Objects.equals(requiredThirdPartyTaxRegime, thirdPartyProfile.taxRegime())) {
            return false;
        }
        if (requiredThirdPartyResponsibility != null
                && !thirdPartyProfile.hasResponsibility(requiredThirdPartyResponsibility)) {
            return false;
        }
        if (municipalityCode != null && !Objects.equals(municipalityCode, operationMunicipalityCode)) {
            return false;
        }
        return ciiuCode == null || withholdingType == WithholdingType.AUTORETENCION
                || thirdPartyProfile.hasCiiu(ciiuCode);
    }

    public boolean thresholdReached(BigDecimal base, BigDecimal uvtValue) {
        BigDecimal threshold = thresholdInCop(uvtValue);
        int comparison = base.compareTo(threshold);
        return thresholdOperator == FiscalThresholdOperator.GT ? comparison > 0 : comparison >= 0;
    }

    public BigDecimal taxableAmount(BigDecimal base, BigDecimal uvtValue) {
        if (thresholdTreatment == FiscalThresholdTreatment.EXCESS) {
            return base.subtract(thresholdInCop(uvtValue)).max(BigDecimal.ZERO);
        }
        return base;
    }

    public int effectiveSpecificity() {
        int calculated = specificity;
        calculated += companyId == null ? 0 : 32;
        calculated += targetThirdPartyId == null ? 0 : 64;
        calculated += municipalityCode == null ? 0 : 16;
        calculated += ciiuCode == null ? 0 : 8;
        calculated += requiredThirdPartyResponsibility == null ? 0 : 4;
        calculated += requiredThirdPartyTaxRegime == null ? 0 : 2;
        calculated += conceptCode == null || "ANY".equals(conceptCode) ? 0 : 1;
        return calculated;
    }

    private BigDecimal thresholdInCop(BigDecimal uvtValue) {
        if (thresholdUnit == FiscalThresholdUnit.UVT) {
            return thresholdValue.multiply(Objects.requireNonNull(uvtValue, "UVT value is required"));
        }
        return thresholdValue;
    }

    private static FiscalCalculationBase defaultCalculationBase(WithholdingType type) {
        return type == WithholdingType.RETEIVA ? FiscalCalculationBase.VAT_AMOUNT
                : type == WithholdingType.AUTORETENCION ? FiscalCalculationBase.COMPANY_INCOME
                        : FiscalCalculationBase.TAXABLE_BASE;
    }
}
