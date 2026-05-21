package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.SaleItemType;

public record InventoryProductSnapshot(UUID id, String sku, String name, SaleItemType itemType, boolean saleEnabled,
        boolean stockTracked, BigDecimal cost, BigDecimal currentStock) {
}
