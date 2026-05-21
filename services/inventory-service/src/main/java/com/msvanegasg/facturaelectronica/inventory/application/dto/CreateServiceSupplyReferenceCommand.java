package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.util.UUID;

public record CreateServiceSupplyReferenceCommand(UUID companyId, UUID serviceProductId, UUID supplyProductId,
        String notes) {
}
