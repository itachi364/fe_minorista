package com.msvanegasg.facturaelectronica.catalog.application.dto;

public record IncreaseProductStockCommand(
        Long barcode,
        Integer quantityToAdd) {
}
