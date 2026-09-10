package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;

public record CalculateFiscalDocumentCommand(
        UUID companyId,
        FiscalOperationType operationType,
        UUID thirdPartyId,
        LocalDate operationDate,
        String municipalityCode,
        AccountingSourceType sourceType,
        UUID sourceId,
        List<FiscalDocumentLineCommand> lines) {
}
