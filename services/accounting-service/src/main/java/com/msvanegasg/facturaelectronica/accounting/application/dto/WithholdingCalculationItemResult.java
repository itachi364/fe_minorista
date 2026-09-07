package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;

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
        String reason) {
}
