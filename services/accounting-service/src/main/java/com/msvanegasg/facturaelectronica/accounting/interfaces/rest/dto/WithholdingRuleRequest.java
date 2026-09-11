package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalCalculationBase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalAccumulationScope;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalTriggerMoment;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdOperator;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdTreatment;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdUnit;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record WithholdingRuleRequest(
        @NotBlank String ruleSetVersion,
        @NotNull FiscalOperationType operationType,
        String conceptCode,
        @NotNull WithholdingType withholdingType,
        @NotNull @PositiveOrZero BigDecimal rate,
        @NotNull FiscalThresholdUnit thresholdUnit,
        @NotNull @PositiveOrZero BigDecimal thresholdValue,
        @NotNull FiscalThresholdOperator thresholdOperator,
        @NotNull FiscalCalculationBase calculationBase,
        @NotNull FiscalThresholdTreatment thresholdTreatment,
        @NotNull WithholdingDecision decision,
        boolean requiresCompanyWithholdingAgent,
        boolean requiresCompanyVatResponsible,
        boolean requiresCompanyVatWithholdingAgent,
        boolean requiresCompanyIcaWithholdingAgent,
        String requiredThirdPartyTaxRegime,
        String requiredThirdPartyResponsibility,
        String municipalityCode,
        String ciiuCode,
        @NotNull LocalDate validFrom,
        LocalDate validTo,
        int priority,
        int specificity,
        @NotBlank String legalReference,
        @NotBlank @Pattern(regexp = "https://.*") String sourceUrl,
        boolean published,
        UUID targetThirdPartyId,
        String evidenceReference,
        FiscalTriggerMoment triggerMoment,
        FiscalAccumulationScope accumulationScope,
        String requiredThirdPartyPersonType,
        String requiredThirdPartyTaxResidency,
        String requiredThirdPartyIncomeTaxStatus,
        String requiredThirdPartySelfWithholdingScope) {
}
