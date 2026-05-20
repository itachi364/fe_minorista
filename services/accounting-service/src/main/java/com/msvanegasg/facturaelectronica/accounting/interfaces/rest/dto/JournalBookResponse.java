package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record JournalBookResponse(
        UUID companyId,
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        List<JournalBookEntryResponse> entries) {
}
