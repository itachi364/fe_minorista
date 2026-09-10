package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FiscalLegalSource(
        UUID id,
        String code,
        String title,
        String authority,
        String officialUrl,
        LocalDate issuedOn,
        LocalDate reviewDueOn,
        boolean active,
        Instant createdAt,
        UUID createdBy) {
}
