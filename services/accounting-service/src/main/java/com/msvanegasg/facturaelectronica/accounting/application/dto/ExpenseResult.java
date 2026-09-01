package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.ExpenseStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.ExpenseType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.PaymentCondition;

public record ExpenseResult(UUID id, UUID companyId, UUID supplierId, ExpenseType expenseType, LocalDate expenseDate,
        String concept, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total, PaymentCondition paymentCondition,
        LocalDate dueDate, String evidenceUrl, ExpenseStatus status, String idempotencyKey, Instant createdAt,
        Instant confirmedAt) {
}
