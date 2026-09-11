package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PurchaseFiscalConfirmationRequest(
        @NotNull UUID sourceId,
        @NotNull UUID supplierId,
        @NotNull LocalDate operationDate,
        String municipalityCode,
        boolean creditPurchase,
        LocalDate dueDate,
        @NotNull @DecimalMin("0") BigDecimal subtotal,
        @NotNull @DecimalMin("0") BigDecimal taxTotal,
        @NotNull @DecimalMin("0") BigDecimal total,
        UUID contractId,
        @NotEmpty List<@Valid FiscalDocumentLineRequest> lines) {
}
