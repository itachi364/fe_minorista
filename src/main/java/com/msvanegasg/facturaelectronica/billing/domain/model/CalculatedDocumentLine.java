package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record CalculatedDocumentLine(
        UUID productId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        String taxCode,
        BigDecimal taxRate,
        BigDecimal grossAmount,
        BigDecimal taxableAmount,
        BigDecimal taxAmount,
        BigDecimal lineTotal) {
}
