package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ConfirmServiceSupplyConsumptionCommand(UUID companyId, UUID serviceProductId, UUID sourceDocumentId,
        String reason, UUID createdBy, String idempotencyKey, List<Line> lines) {

    public record Line(UUID supplyProductId, BigDecimal quantity) {
    }
}
