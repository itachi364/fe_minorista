package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountsReceivableRequest(
        @NotNull UUID customerId,
        AccountingSourceType sourceType,
        UUID sourceId,
        @NotNull LocalDate issueDate,
        @NotNull LocalDate dueDate,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal totalAmount,
        @NotBlank String idempotencyKey) {
}
