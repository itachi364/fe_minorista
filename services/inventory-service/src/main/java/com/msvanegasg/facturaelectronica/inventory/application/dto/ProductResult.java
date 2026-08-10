package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;

public record ProductResult(UUID id, UUID companyId, String sku, String barcode, String name, String description,
        InventoryItemType itemType, boolean saleEnabled, boolean purchaseEnabled, boolean stockTracked,
        BigDecimal salePrice, BigDecimal cost, String taxCategoryCode, String taxCode, String taxLabel,
        BigDecimal taxRate, boolean active, BigDecimal currentStock, Instant createdAt, Instant updatedAt) {

    public ProductResult(UUID id, UUID companyId, String sku, String barcode, String name, String description,
            InventoryItemType itemType, boolean saleEnabled, boolean purchaseEnabled, boolean stockTracked,
            BigDecimal salePrice, BigDecimal cost, boolean active, BigDecimal currentStock, Instant createdAt,
            Instant updatedAt) {
        this(id, companyId, sku, barcode, name, description, itemType, saleEnabled, purchaseEnabled, stockTracked,
                salePrice, cost, "IVA", "IVA_19", "IVA 19%", new BigDecimal("19"), active, currentStock, createdAt,
                updatedAt);
    }
}
