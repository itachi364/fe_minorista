package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ServiceSupplyReferenceRequest(
        @NotNull UUID serviceProductId,
        @NotNull UUID supplyProductId,
        String notes) {
}
