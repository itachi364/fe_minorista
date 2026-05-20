package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResult(UUID id, UUID companyId, String sku, String barcode, String name, String description,
        BigDecimal salePrice, BigDecimal cost, boolean active, BigDecimal currentStock, Instant createdAt,
        Instant updatedAt) {
}
