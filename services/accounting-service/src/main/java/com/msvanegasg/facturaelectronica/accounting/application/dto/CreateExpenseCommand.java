package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.PaymentCondition;

public record CreateExpenseCommand(UUID companyId, UUID supplierId, LocalDate expenseDate, String concept,
        BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total, PaymentCondition paymentCondition,
        LocalDate dueDate, String evidenceUrl, String idempotencyKey) {
}
