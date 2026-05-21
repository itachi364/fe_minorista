package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ServiceSupplyReferenceResult(UUID id, UUID companyId, UUID serviceProductId, UUID supplyProductId,
        String notes, boolean active, Instant createdAt) {
}
