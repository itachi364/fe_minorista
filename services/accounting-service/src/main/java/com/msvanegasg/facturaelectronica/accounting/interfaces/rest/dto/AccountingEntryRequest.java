package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AccountingEntryRequest(
        @NotNull AccountingEventType eventType,
        @NotNull AccountingSourceType sourceType,
        @NotNull UUID sourceId,
        @NotNull LocalDate entryDate,
        @NotBlank String description,
        UUID thirdpartyId,
        @NotNull @PositiveOrZero BigDecimal subtotal,
        @NotNull @PositiveOrZero BigDecimal taxTotal,
        @NotNull @PositiveOrZero BigDecimal total) {
}
