package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record JournalBookLineResult(
        UUID lineId,
        UUID accountId,
        String accountCode,
        String accountName,
        UUID thirdpartyId,
        BigDecimal debitAmount,
        BigDecimal creditAmount,
        String description) {
}
