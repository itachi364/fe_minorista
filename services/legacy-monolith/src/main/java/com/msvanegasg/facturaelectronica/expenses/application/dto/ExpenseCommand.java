package com.msvanegasg.facturaelectronica.expenses.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.msvanegasg.facturaelectronica.enums.Estado;

public record ExpenseCommand(
        LocalDateTime date,
        BigDecimal amount,
        String description,
        Long expenseTypeId,
        Long paymentMethodId,
        String evidenceUrl,
        Estado status) {
}
