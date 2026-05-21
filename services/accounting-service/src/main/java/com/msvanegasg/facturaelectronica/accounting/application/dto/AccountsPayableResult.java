package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayableStatus;

public record AccountsPayableResult(UUID id, UUID companyId, UUID supplierId, AccountingSourceType sourceType,
        UUID sourceId, LocalDate issueDate, LocalDate dueDate, BigDecimal totalAmount, BigDecimal paidAmount,
        BigDecimal balance, AccountsPayableStatus status, Instant createdAt) {
}
