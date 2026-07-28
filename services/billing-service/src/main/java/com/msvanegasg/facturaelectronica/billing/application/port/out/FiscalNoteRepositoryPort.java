package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNote;

public interface FiscalNoteRepositoryPort {

    Optional<FiscalNote> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<FiscalNote> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    FiscalNote save(FiscalNote note);
}