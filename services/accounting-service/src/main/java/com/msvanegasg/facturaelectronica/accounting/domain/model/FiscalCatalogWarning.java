package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public record FiscalCatalogWarning(
        UUID sourceId,
        String sourceCode,
        String severity,
        String code,
        String message,
        LocalDate dueOn) {
}
