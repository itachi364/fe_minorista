package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SuggestedSupplyConsumptionResponse(UUID serviceProductId, UUID supplyProductId, String supplySku,
        String supplyName, BigDecimal currentStock, BigDecimal unitCost, String notes) {
}
