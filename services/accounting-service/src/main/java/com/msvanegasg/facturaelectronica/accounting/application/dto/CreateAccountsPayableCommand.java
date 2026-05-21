package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public record CreateAccountsPayableCommand(UUID companyId, UUID supplierId, AccountingSourceType sourceType,
        UUID sourceId, LocalDate issueDate, LocalDate dueDate, BigDecimal totalAmount) {
}
