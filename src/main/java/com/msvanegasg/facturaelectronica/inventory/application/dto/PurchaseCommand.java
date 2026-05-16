package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseCommand(
        Long supplierDocumentNumber,
        Long supplierDocumentTypeId,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total,
        String evidenceUrl,
        List<PurchaseLineCommand> lines) {
}
