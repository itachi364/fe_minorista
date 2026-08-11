package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SuggestedSupplyConsumptionResult(UUID serviceProductId, UUID supplyProductId, String supplySku,
        String supplyName, BigDecimal currentStock, BigDecimal unitCost, String notes) {
}
