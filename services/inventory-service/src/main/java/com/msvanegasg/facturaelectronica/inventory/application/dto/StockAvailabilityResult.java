package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StockAvailabilityResult(UUID companyId, UUID productId, BigDecimal requestedQuantity,
        BigDecimal currentStock, boolean available) {
}
