package com.msvanegasg.facturaelectronica.inventory.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.PaymentCondition;

public record CreatePurchaseCommand(UUID companyId, UUID supplierId, BigDecimal subtotal, BigDecimal taxTotal,
        BigDecimal total, PaymentCondition paymentCondition, LocalDate dueDate, String evidenceUrl,
        String idempotencyKey, UUID createdBy, List<PurchaseLineCommand> lines) {
}
