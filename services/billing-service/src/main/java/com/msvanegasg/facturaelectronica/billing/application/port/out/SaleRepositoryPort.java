package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentQuery;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleQuery;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;

public interface SaleRepositoryPort {

    Optional<Sale> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<Sale> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    List<Sale> find(SaleQuery query);

    List<Sale> findByElectronicDocument(ElectronicDocumentQuery query);

    Optional<Sale> findByCompanyIdAndElectronicDocumentId(UUID companyId, UUID documentId);

    long countIssuedElectronicDocuments(UUID companyId, Instant fromInclusive, Instant toExclusive);

    Sale save(Sale sale);
}
