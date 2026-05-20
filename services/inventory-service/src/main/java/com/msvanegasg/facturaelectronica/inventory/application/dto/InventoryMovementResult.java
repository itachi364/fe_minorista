package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;

public record InventoryMovementResult(UUID id, UUID companyId, UUID productId, InventoryMovementType movementType,
        BigDecimal quantity, BigDecimal unitCost, BigDecimal previousStock, BigDecimal resultingStock,
        InventorySourceDocumentType sourceDocumentType, UUID sourceDocumentId, String idempotencyKey, UUID createdBy,
        Instant movementAt) {
}
