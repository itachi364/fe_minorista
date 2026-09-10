package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FiscalLegalSourceEvent(
        UUID id,
        UUID sourceId,
        FiscalLegalEventType eventType,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String reference,
        String officialUrl,
        String notes,
        Instant createdAt,
        UUID createdBy) {

    public boolean appliesOn(LocalDate date) {
        return !date.isBefore(effectiveFrom) && (effectiveTo == null || !date.isAfter(effectiveTo));
    }
}
