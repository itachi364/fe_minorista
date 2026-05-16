package com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ElectronicPosLineRequest(
        UUID productId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
        @NotNull @DecimalMin("0.0") BigDecimal unitPrice,
        @NotNull @DecimalMin("0.0") BigDecimal discountAmount,
        @NotBlank String taxCode,
        @NotNull @DecimalMin("0.0") BigDecimal taxRate) {
}
