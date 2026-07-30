package com.msvanegasg.facturaelectronica.catalog.application.dto;

import java.math.BigDecimal;

public record ProductCommand(
        String name,
        String description,
        BigDecimal basePrice,
        Integer stockQuantity,
        Long categoryId,
        Long barcode) {
}
