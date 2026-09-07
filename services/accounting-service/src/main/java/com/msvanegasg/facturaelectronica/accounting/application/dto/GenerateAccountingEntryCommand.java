package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public record GenerateAccountingEntryCommand(
        UUID companyId,
        AccountingEventType eventType,
        AccountingSourceType sourceType,
        UUID sourceId,
        LocalDate entryDate,
        String description,
        UUID thirdpartyId,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total,
        BigDecimal retefuente,
        BigDecimal reteiva,
        BigDecimal reteica,
        BigDecimal selfWithholding,
        BigDecimal withholdingTotal,
        BigDecimal netPayable) {

    public GenerateAccountingEntryCommand(UUID companyId, AccountingEventType eventType, AccountingSourceType sourceType,
            UUID sourceId, LocalDate entryDate, String description, UUID thirdpartyId, BigDecimal subtotal,
            BigDecimal taxTotal, BigDecimal total) {
        this(companyId, eventType, sourceType, sourceId, entryDate, description, thirdpartyId, subtotal, taxTotal,
                total, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, total);
    }
}
