package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreatePurchaseCommand(UUID companyId, UUID supplierId, BigDecimal subtotal, BigDecimal taxTotal,
        BigDecimal total, String evidenceUrl, String idempotencyKey, UUID createdBy, List<PurchaseLineCommand> lines) {
}
