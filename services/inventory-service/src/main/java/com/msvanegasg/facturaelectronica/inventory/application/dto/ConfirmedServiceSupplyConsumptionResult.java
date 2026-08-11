package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.util.List;
import java.util.UUID;

public record ConfirmedServiceSupplyConsumptionResult(UUID serviceProductId, UUID sourceDocumentId,
        List<InventoryMovementResult> movements) {
}
