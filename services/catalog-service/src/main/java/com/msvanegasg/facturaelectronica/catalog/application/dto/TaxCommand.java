package com.msvanegasg.facturaelectronica.catalog.application.dto;

import java.math.BigDecimal;

public record TaxCommand(
        String name,
        BigDecimal percentage,
        String type,
        String countryCode,
        String description) {
}
