package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LedgerBookResult(
        UUID companyId,
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        List<LedgerAccountSummaryResult> accounts) {
}
