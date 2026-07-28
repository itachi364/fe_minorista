package com.msvanegasg.facturaelectronica.providerretry;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleLineSnapshot(UUID lineId, UUID productId, String productSku, String productName, String itemType,
        boolean stockTracked, BigDecimal quantity, BigDecimal unitCost, BigDecimal unitPrice, BigDecimal discountAmount,
        String taxCode, BigDecimal taxRate, BigDecimal subtotal, BigDecimal taxAmount, BigDecimal total) {
}