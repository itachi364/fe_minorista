package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateFiscalLegalSourceCommand(
        String code,
        String title,
        String authority,
        String officialUrl,
        LocalDate issuedOn,
        LocalDate reviewDueOn,
        UUID userId) {
}
