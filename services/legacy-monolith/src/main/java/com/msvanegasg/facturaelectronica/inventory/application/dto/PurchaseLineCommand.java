package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;

public record PurchaseLineCommand(
        Long barcode,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal lineTotal) {
}
