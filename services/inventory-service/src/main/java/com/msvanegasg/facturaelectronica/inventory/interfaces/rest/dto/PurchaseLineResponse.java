package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseLineResponse(UUID id, UUID productId, BigDecimal quantity, BigDecimal unitCost,
        BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
}
