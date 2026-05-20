package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductCommand(UUID companyId, String sku, String barcode, String name, String description,
        BigDecimal salePrice, BigDecimal cost, BigDecimal initialStock, UUID createdBy, String idempotencyKey) {
}
