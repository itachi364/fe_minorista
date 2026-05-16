package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;

public record RegisterInventoryMovementCommand(
        UUID companyId,
        UUID productId,
        InventoryMovementType movementType,
        BigDecimal quantity,
        InventorySourceDocumentType sourceDocumentType,
        UUID sourceDocumentId,
        UUID createdBy) {
}
