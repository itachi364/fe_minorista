package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record InventoryMovementQuery(
        UUID companyId,
        UUID productId,
        LocalDate from,
        LocalDate to) {
}
