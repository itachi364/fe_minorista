package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record LedgerBookQuery(
        UUID companyId,
        LocalDate fromDate,
        LocalDate toDate) {
}
