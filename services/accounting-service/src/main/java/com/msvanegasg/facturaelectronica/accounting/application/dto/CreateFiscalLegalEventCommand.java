package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalEventType;

public record CreateFiscalLegalEventCommand(
        UUID sourceId,
        FiscalLegalEventType eventType,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String reference,
        String officialUrl,
        String notes,
        UUID userId) {
}
