package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PurchaseLineRequest(
        UUID productId,
        String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
        @NotNull @DecimalMin(value = "0.0") BigDecimal unitCost,
        @NotNull @DecimalMin(value = "0.0") BigDecimal subtotal,
        @NotNull @DecimalMin(value = "0.0") BigDecimal tax,
        @NotNull @DecimalMin(value = "0.0") BigDecimal total) {
}
