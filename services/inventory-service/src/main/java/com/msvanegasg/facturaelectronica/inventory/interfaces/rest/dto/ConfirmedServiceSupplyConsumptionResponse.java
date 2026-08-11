package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.util.List;
import java.util.UUID;

public record ConfirmedServiceSupplyConsumptionResponse(UUID serviceProductId, UUID sourceDocumentId,
        List<InventoryMovementResponse> movements) {
}
