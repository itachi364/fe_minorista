package com.msvanegasg.facturaelectronica.inventory.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseQuery;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;

public interface PurchaseRepositoryPort {

    Optional<Purchase> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<Purchase> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    List<Purchase> find(PurchaseQuery query);

    Purchase save(Purchase purchase);
}
