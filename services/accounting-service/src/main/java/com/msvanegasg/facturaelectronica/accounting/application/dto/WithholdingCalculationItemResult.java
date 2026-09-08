package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

public record WithholdingCalculationItemResult(
        WithholdingType withholdingType,
        String conceptCode,
        BigDecimal baseAmount,
        BigDecimal rate,
        BigDecimal amount,
        String ruleVersion,
        WithholdingDecision decision,
        String reason,
        UUID ruleId,
        String parameterVersion,
        String legalReference,
        String sourceUrl) {

    public WithholdingCalculationItemResult(WithholdingType withholdingType, String conceptCode,
            BigDecimal baseAmount, BigDecimal rate, BigDecimal amount, String ruleVersion,
            WithholdingDecision decision, String reason) {
        this(withholdingType, conceptCode, baseAmount, rate, amount, ruleVersion, decision, reason,
                null, null, null, null);
    }
}
