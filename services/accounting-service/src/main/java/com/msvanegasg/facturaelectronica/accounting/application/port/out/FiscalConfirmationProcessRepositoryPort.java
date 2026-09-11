package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

public interface FiscalConfirmationProcessRepositoryPort {

    void markProcessing(UUID processId, UUID companyId, AccountingSourceType sourceType, UUID sourceId,
            Instant updatedAt);

    void markCompleted(UUID companyId, AccountingSourceType sourceType, UUID sourceId, UUID calculationId,
            UUID accountingEntryId, UUID payableId, Instant updatedAt);

    void markFailed(UUID processId, UUID companyId, AccountingSourceType sourceType, UUID sourceId,
            String lastError, Instant updatedAt);
}
