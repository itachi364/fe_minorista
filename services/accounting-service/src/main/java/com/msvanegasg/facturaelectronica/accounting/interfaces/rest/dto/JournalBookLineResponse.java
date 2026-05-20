package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record JournalBookLineResponse(
        UUID lineId,
        UUID accountId,
        String accountCode,
        String accountName,
        UUID thirdpartyId,
        BigDecimal debitAmount,
        BigDecimal creditAmount,
        String description) {
}
