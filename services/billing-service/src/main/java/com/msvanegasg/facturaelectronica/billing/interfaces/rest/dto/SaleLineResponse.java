package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleLineResponse(UUID id, UUID productId, BigDecimal quantity, BigDecimal unitPrice,
        BigDecimal discountAmount, String taxCode, BigDecimal taxRate, BigDecimal subtotal, BigDecimal taxAmount,
        BigDecimal total) {
}
