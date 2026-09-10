package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FiscalDocumentLineCommand(
        UUID lineId,
        String conceptCode,
        String ciiuCode,
        BigDecimal taxableBaseAmount,
        BigDecimal taxAmount) {
}
