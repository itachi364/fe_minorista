package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StockAvailabilityResponse(UUID companyId, UUID productId, BigDecimal requestedQuantity,
        BigDecimal availableQuantity, boolean available) {
}
