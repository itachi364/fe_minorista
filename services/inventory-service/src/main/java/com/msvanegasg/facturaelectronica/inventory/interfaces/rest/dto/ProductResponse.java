package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(UUID id, UUID companyId, String sku, String barcode, String name, String description,
        BigDecimal salePrice, BigDecimal cost, boolean active, BigDecimal currentStock, Instant createdAt,
        Instant updatedAt) {
}
