package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public record AccountingEntryResult(
        UUID id,
        UUID companyId,
        LocalDate entryDate,
        String description,
        AccountingSourceType sourceType,
        UUID sourceId,
        UUID accountingRuleId,
        AccountingEntryStatus status,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        List<AccountingEntryLineResult> lines) {
}
