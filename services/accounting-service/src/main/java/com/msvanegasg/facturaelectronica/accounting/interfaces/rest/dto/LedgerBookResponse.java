package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LedgerBookResponse(
        UUID companyId,
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        List<LedgerAccountSummaryResponse> accounts) {
}
