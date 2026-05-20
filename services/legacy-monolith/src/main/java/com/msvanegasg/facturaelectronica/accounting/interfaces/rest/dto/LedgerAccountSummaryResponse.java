package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountNature;

public record LedgerAccountSummaryResponse(
        UUID accountId,
        String accountCode,
        String accountName,
        AccountNature nature,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        BigDecimal balance) {
}
