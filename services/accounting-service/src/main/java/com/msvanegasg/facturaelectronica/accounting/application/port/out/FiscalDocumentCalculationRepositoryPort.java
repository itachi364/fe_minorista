package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentCalculationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalAccumulationTotals;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public interface FiscalDocumentCalculationRepositoryPort {
    Optional<FiscalDocumentCalculationResult> findBySource(UUID companyId, AccountingSourceType sourceType,
            UUID sourceId);
    FiscalDocumentCalculationResult save(FiscalDocumentCalculationResult result);

    default void lockAccumulation(UUID companyId, UUID thirdPartyId, LocalDate operationDate) {
    }

    default FiscalAccumulationTotals findDailyAccumulation(UUID companyId, UUID thirdPartyId,
            LocalDate operationDate, String conceptCode) {
        return FiscalAccumulationTotals.empty();
    }

    default void saveAccumulations(FiscalDocumentCalculationResult result) {
    }

    default boolean isPeriodClosed(UUID companyId, LocalDate operationDate) {
        return false;
    }
}
