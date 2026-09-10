package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.time.LocalDate;

import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalEventType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FiscalLegalEventRequest(
        @NotNull FiscalLegalEventType eventType,
        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo,
        @NotBlank String reference,
        @NotBlank String officialUrl,
        String notes) {
}
