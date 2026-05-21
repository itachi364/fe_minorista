package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;

public record CreateProductCommand(UUID companyId, String sku, String barcode, String name, String description,
        InventoryItemType itemType, Boolean saleEnabled, Boolean purchaseEnabled, Boolean stockTracked,
        BigDecimal salePrice, BigDecimal cost, BigDecimal initialStock, UUID createdBy, String idempotencyKey) {
}
