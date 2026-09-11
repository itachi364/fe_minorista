package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record FiscalDocumentLineRequest(
        @NotNull UUID lineId,
        String conceptCode,
        String ciiuCode,
        @NotNull @DecimalMin("0") BigDecimal taxableBaseAmount,
        @NotNull @DecimalMin("0") BigDecimal taxAmount,
        @DecimalMin("0") BigDecimal aiuAmount,
        @DecimalMin("0") BigDecimal grossPaymentAmount) {

    public FiscalDocumentLineRequest(UUID lineId, String conceptCode, String ciiuCode,
            BigDecimal taxableBaseAmount, BigDecimal taxAmount) {
        this(lineId, conceptCode, ciiuCode, taxableBaseAmount, taxAmount, null, null);
    }
}
