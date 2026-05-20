package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountingEntryLineResult(
        UUID id,
        UUID accountId,
        String accountCode,
        String accountName,
        UUID thirdpartyId,
        BigDecimal debitAmount,
        BigDecimal creditAmount,
        String description) {
}
