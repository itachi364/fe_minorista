package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FiscalDocumentLineCommand(
        UUID lineId,
        String conceptCode,
        String ciiuCode,
        BigDecimal taxableBaseAmount,
        BigDecimal taxAmount,
        BigDecimal aiuAmount,
        BigDecimal grossPaymentAmount) {

    public FiscalDocumentLineCommand(UUID lineId, String conceptCode, String ciiuCode,
            BigDecimal taxableBaseAmount, BigDecimal taxAmount) {
        this(lineId, conceptCode, ciiuCode, taxableBaseAmount, taxAmount, BigDecimal.ZERO,
                taxableBaseAmount == null || taxAmount == null ? BigDecimal.ZERO : taxableBaseAmount.add(taxAmount));
    }
}
