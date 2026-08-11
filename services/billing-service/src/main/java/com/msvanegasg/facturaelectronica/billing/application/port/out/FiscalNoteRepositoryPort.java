package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNote;

public interface FiscalNoteRepositoryPort {

    Optional<FiscalNote> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<FiscalNote> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    long countIssuedFiscalNotes(UUID companyId, Instant fromInclusive, Instant toExclusive);

    FiscalNote save(FiscalNote note);
}
