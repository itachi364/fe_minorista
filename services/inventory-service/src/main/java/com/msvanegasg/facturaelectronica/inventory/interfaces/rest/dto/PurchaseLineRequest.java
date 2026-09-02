package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;

public record PurchaseLineRequest(
        UUID productId,
        String description,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
        @DecimalMin(value = "0.0") BigDecimal unitCost,
        @DecimalMin(value = "0.0") BigDecimal subtotal,
        @DecimalMin(value = "0.0") BigDecimal tax,
        @DecimalMin(value = "0.0") BigDecimal total) {
}
