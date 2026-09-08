package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalCalculationBase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdOperator;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdTreatment;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdUnit;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

public record CreateWithholdingRuleCommand(UUID companyId, String ruleSetVersion, FiscalOperationType operationType,
        String conceptCode, WithholdingType withholdingType, BigDecimal rate, FiscalThresholdUnit thresholdUnit,
        BigDecimal thresholdValue, FiscalThresholdOperator thresholdOperator, FiscalCalculationBase calculationBase,
        FiscalThresholdTreatment thresholdTreatment, WithholdingDecision decision,
        boolean requiresCompanyWithholdingAgent, boolean requiresCompanyVatResponsible,
        boolean requiresCompanyVatWithholdingAgent, boolean requiresCompanyIcaWithholdingAgent,
        String requiredThirdPartyTaxRegime, String requiredThirdPartyResponsibility, String municipalityCode,
        String ciiuCode, LocalDate validFrom, LocalDate validTo, int priority, int specificity,
        String legalReference, String sourceUrl, boolean published, UUID targetThirdPartyId,
        String evidenceReference) {
}
