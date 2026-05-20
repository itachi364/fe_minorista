package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(
        @NotBlank String sku,
        String barcode,
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0.0") BigDecimal salePrice,
        @NotNull @DecimalMin(value = "0.0") BigDecimal cost,
        @DecimalMin(value = "0.0") BigDecimal initialStock) {
}
