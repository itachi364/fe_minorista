package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;

public record CreateProductCommand(UUID companyId, String sku, String barcode, String name, String description,
        InventoryItemType itemType, Boolean saleEnabled, Boolean purchaseEnabled, Boolean stockTracked,
        BigDecimal salePrice, BigDecimal cost, BigDecimal initialStock, String taxCategoryCode, String taxCode,
        String taxLabel, BigDecimal taxRate, UUID createdBy, String idempotencyKey) {

    public CreateProductCommand(UUID companyId, String sku, String barcode, String name, String description,
            InventoryItemType itemType, Boolean saleEnabled, Boolean purchaseEnabled, Boolean stockTracked,
            BigDecimal salePrice, BigDecimal cost, BigDecimal initialStock, UUID createdBy, String idempotencyKey) {
        this(companyId, sku, barcode, name, description, itemType, saleEnabled, purchaseEnabled, stockTracked,
                salePrice, cost, initialStock, "IVA", "IVA_19", "IVA 19%", new BigDecimal("19"), createdBy,
                idempotencyKey);
    }
}
