package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;

public interface SaleRepositoryPort {

    Optional<Sale> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<Sale> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    Sale save(Sale sale);
}
