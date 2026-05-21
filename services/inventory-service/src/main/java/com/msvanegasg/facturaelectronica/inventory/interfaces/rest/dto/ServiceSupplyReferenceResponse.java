package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record ServiceSupplyReferenceResponse(UUID id, UUID companyId, UUID serviceProductId, UUID supplyProductId,
        String notes, boolean active, Instant createdAt) {
}
