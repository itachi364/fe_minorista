package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record FiscalDocumentLineResult(
        UUID lineId,
        String conceptCode,
        String ciiuCode,
        BigDecimal taxableBaseAmount,
        BigDecimal taxAmount,
        List<WithholdingCalculationItemResult> items) {
    public FiscalDocumentLineResult {
        items = List.copyOf(items);
    }
}
