package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleLineCommand(UUID productId, BigDecimal quantity, BigDecimal unitPrice, BigDecimal discountAmount,
        String taxCode, BigDecimal taxRate) {
}
