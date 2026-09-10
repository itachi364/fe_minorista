package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CalculateFiscalDocumentCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentCalculationResult;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public interface CalculateFiscalDocumentUseCase {
    FiscalDocumentCalculationResult calculate(CalculateFiscalDocumentCommand command);
    Optional<FiscalDocumentCalculationResult> findBySource(UUID companyId, AccountingSourceType sourceType,
            UUID sourceId);
}
