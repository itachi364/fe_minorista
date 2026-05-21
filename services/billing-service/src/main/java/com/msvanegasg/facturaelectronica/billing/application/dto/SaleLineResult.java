package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.SaleItemType;

public record SaleLineResult(UUID id, UUID productId, String productSku, String productName, SaleItemType itemType,
        boolean stockTracked, BigDecimal quantity, BigDecimal unitPrice, BigDecimal discountAmount, String taxCode,
        BigDecimal taxRate, BigDecimal subtotal, BigDecimal taxAmount, BigDecimal total) {
}
