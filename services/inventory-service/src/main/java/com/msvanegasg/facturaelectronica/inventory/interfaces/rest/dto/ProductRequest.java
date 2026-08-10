package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;

import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(
        @NotBlank String sku,
        String barcode,
        @NotBlank String name,
        String description,
        InventoryItemType itemType,
        Boolean saleEnabled,
        Boolean purchaseEnabled,
        Boolean stockTracked,
        @NotNull @DecimalMin(value = "0.0") BigDecimal salePrice,
        @NotNull @DecimalMin(value = "0.0") BigDecimal cost,
        @DecimalMin(value = "0.0") BigDecimal initialStock,
        String taxCategoryCode,
        String taxCode,
        String taxLabel,
        @DecimalMin(value = "0.0") BigDecimal taxRate) {
}
