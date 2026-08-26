package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.SaleDocumentTypeOverrideRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleDocumentTypeOverride;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.SaleDocumentTypeOverrideJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.SaleDocumentTypeOverrideJpaRepository;

@Component
public class SaleDocumentTypeOverridePersistenceAdapter implements SaleDocumentTypeOverrideRepositoryPort {

    private final SaleDocumentTypeOverrideJpaRepository repository;

    public SaleDocumentTypeOverridePersistenceAdapter(SaleDocumentTypeOverrideJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public SaleDocumentTypeOverride save(SaleDocumentTypeOverride override) {
        return toDomain(repository.save(toEntity(override)));
    }

    @Override
    public Optional<SaleDocumentTypeOverride> findActiveByCompanyIdAndSaleId(UUID companyId, UUID saleId) {
        return repository.findFirstByCompanyIdAndSaleIdAndActiveTrueOrderByCreatedAtDesc(companyId, saleId)
                .map(SaleDocumentTypeOverridePersistenceAdapter::toDomain);
    }

    private static SaleDocumentTypeOverrideJpaEntity toEntity(SaleDocumentTypeOverride override) {
        SaleDocumentTypeOverrideJpaEntity entity = new SaleDocumentTypeOverrideJpaEntity();
        entity.setId(override.id());
        entity.setCompanyId(override.companyId());
        entity.setSaleId(override.saleId());
        entity.setDocumentType(override.documentType());
        entity.setAuthorizedBy(override.authorizedBy());
        entity.setReason(override.reason());
        entity.setActive(override.active());
        entity.setCreatedAt(override.createdAt());
        return entity;
    }

    private static SaleDocumentTypeOverride toDomain(SaleDocumentTypeOverrideJpaEntity entity) {
        return new SaleDocumentTypeOverride(entity.getId(), entity.getCompanyId(), entity.getSaleId(),
                entity.getDocumentType(), entity.getAuthorizedBy(), entity.getReason(), entity.isActive(),
                entity.getCreatedAt());
    }
}
