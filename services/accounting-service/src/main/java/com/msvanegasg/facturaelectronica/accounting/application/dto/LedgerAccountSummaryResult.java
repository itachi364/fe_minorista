package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountNature;

public record LedgerAccountSummaryResult(
        UUID accountId,
        String accountCode,
        String accountName,
        AccountNature nature,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        BigDecimal balance) {
}
