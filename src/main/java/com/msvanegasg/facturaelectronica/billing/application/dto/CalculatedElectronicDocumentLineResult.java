package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CalculatedElectronicDocumentLineResult(
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
