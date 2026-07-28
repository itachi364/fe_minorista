package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.ExpenseStatus;

public record ExpenseQuery(
        UUID companyId,
        ExpenseStatus status,
        UUID supplierId,
        LocalDate from,
        LocalDate to) {
}
