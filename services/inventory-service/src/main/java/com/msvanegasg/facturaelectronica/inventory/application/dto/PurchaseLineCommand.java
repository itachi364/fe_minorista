package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseLineCommand(UUID productId, BigDecimal quantity, BigDecimal unitCost, BigDecimal subtotal,
        BigDecimal tax, BigDecimal total) {
}
