package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.ExpenseType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.PaymentCondition;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ExpenseRequest(
        UUID supplierId,
        ExpenseType expenseType,
        @NotNull LocalDate expenseDate,
        @NotBlank String concept,
        @DecimalMin("0.0") BigDecimal subtotal,
        @DecimalMin("0.0") BigDecimal taxTotal,
        @NotNull @DecimalMin("0.0") BigDecimal total,
        PaymentCondition paymentCondition,
        LocalDate dueDate,
        @Size(max = 700)
        @Pattern(regexp = "^(https?://|/api/v1/companies/).+", message = "La evidencia debe ser una URL http/https o una referencia interna valida.")
        String evidenceUrl) {
}
