package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record SaleLineRequest(
        @NotNull UUID productId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
        @DecimalMin(value = "0.0") BigDecimal unitPrice,
        @DecimalMin(value = "0.0") BigDecimal discountAmount,
        String taxCode,
        @DecimalMin(value = "0.0") BigDecimal taxRate) {
}
