package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public record JournalBookEntryResult(
        UUID entryId,
        LocalDate entryDate,
        String description,
        AccountingSourceType sourceType,
        UUID sourceId,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        List<JournalBookLineResult> lines) {
}
