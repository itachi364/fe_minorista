package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.PaymentCondition;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExpenseRequest(
        UUID supplierId,
        @NotNull LocalDate expenseDate,
        @NotBlank String concept,
        @NotNull @DecimalMin("0.0") BigDecimal subtotal,
        @NotNull @DecimalMin("0.0") BigDecimal taxTotal,
        @NotNull @DecimalMin("0.0") BigDecimal total,
        PaymentCondition paymentCondition,
        LocalDate dueDate,
        String evidenceUrl) {
}
