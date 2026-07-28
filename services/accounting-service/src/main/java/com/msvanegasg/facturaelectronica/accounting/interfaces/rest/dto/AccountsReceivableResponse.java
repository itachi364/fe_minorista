package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivableStatus;

public record AccountsReceivableResponse(UUID id, UUID companyId, UUID customerId, AccountingSourceType sourceType,
        UUID sourceId, LocalDate issueDate, LocalDate dueDate, BigDecimal totalAmount, BigDecimal paidAmount,
        BigDecimal balance, AccountsReceivableStatus status, String idempotencyKey, Instant createdAt) {
}