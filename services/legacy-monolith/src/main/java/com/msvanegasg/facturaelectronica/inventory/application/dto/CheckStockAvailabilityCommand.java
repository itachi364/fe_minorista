package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckStockAvailabilityCommand(
        UUID companyId,
        UUID productId,
        BigDecimal requestedQuantity) {
}
