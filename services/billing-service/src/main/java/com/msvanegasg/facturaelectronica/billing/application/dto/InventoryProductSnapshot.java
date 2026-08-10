package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.SaleItemType;

public record InventoryProductSnapshot(UUID id, String sku, String name, SaleItemType itemType, boolean saleEnabled,
        boolean stockTracked, BigDecimal salePrice, BigDecimal cost, String taxCategoryCode, String taxCode,
        String taxLabel, BigDecimal taxRate, BigDecimal currentStock) {

    public InventoryProductSnapshot(UUID id, String sku, String name, SaleItemType itemType, boolean saleEnabled,
            boolean stockTracked, BigDecimal cost, BigDecimal currentStock) {
        this(id, sku, name, itemType, saleEnabled, stockTracked, BigDecimal.ZERO, cost, "IVA", "IVA_19",
                "IVA 19%", new BigDecimal("19"), currentStock);
    }
}
