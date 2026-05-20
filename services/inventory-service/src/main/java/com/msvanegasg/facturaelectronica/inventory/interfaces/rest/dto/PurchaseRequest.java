package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PurchaseRequest(
        UUID supplierId,
        @NotNull @DecimalMin(value = "0.0") BigDecimal subtotal,
        @NotNull @DecimalMin(value = "0.0") BigDecimal taxTotal,
        @NotNull @DecimalMin(value = "0.0") BigDecimal total,
        String evidenceUrl,
        @NotEmpty List<@Valid PurchaseLineRequest> lines) {
}
