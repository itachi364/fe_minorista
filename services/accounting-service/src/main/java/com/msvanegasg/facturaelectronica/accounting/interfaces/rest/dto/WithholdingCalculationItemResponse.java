package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;

import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

public record WithholdingCalculationItemResponse(
        WithholdingType withholdingType,
        String conceptCode,
        BigDecimal baseAmount,
        BigDecimal rate,
        BigDecimal amount,
        String ruleVersion,
        WithholdingDecision decision,
        String reason) {
}
