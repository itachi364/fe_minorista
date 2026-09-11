package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.util.Map;

import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

public record FiscalAccumulationTotals(
        BigDecimal taxableBase,
        BigDecimal taxAmount,
        BigDecimal aiuAmount,
        BigDecimal grossPaymentAmount,
        Map<WithholdingType, BigDecimal> withheldByType) {

    public FiscalAccumulationTotals(BigDecimal taxableBase, BigDecimal taxAmount,
            Map<WithholdingType, BigDecimal> withheldByType) {
        this(taxableBase, taxAmount, BigDecimal.ZERO, taxableBase.add(taxAmount), withheldByType);
    }

    public static FiscalAccumulationTotals empty() {
        return new FiscalAccumulationTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                Map.of());
    }
}
