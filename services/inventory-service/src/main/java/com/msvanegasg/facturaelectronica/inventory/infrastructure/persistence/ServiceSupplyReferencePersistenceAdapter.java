package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.inventory.application.port.out.ServiceSupplyReferenceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.ServiceSupplyReference;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.ServiceSupplyReferenceJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository.ServiceSupplyReferenceJpaRepository;

@Component
public class ServiceSupplyReferencePersistenceAdapter implements ServiceSupplyReferenceRepositoryPort {

    private final ServiceSupplyReferenceJpaRepository repository;

    public ServiceSupplyReferencePersistenceAdapter(ServiceSupplyReferenceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByCompanyIdAndServiceProductIdAndSupplyProductId(UUID companyId, UUID serviceProductId,
            UUID supplyProductId) {
        return repository.existsByCompanyIdAndServiceProductIdAndSupplyProductId(companyId, serviceProductId,
                supplyProductId);
    }

    @Override
    public List<ServiceSupplyReference> findByCompanyIdAndServiceProductId(UUID companyId, UUID serviceProductId) {
        return repository.findByCompanyIdAndServiceProductIdAndActiveTrue(companyId, serviceProductId).stream()
                .map(ServiceSupplyReferencePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public ServiceSupplyReference save(ServiceSupplyReference reference) {
        return toDomain(repository.save(toEntity(reference)));
    }

    private static ServiceSupplyReference toDomain(ServiceSupplyReferenceJpaEntity entity) {
        return new ServiceSupplyReference(entity.getId(), entity.getCompanyId(), entity.getServiceProductId(),
                entity.getSupplyProductId(), entity.getNotes(), entity.isActive(), entity.getCreatedAt());
    }

    private static ServiceSupplyReferenceJpaEntity toEntity(ServiceSupplyReference reference) {
        ServiceSupplyReferenceJpaEntity entity = new ServiceSupplyReferenceJpaEntity();
        entity.setId(reference.id());
        entity.setCompanyId(reference.companyId());
        entity.setServiceProductId(reference.serviceProductId());
        entity.setSupplyProductId(reference.supplyProductId());
        entity.setNotes(reference.notes());
        entity.setActive(reference.active());
        entity.setCreatedAt(reference.createdAt());
        return entity;
    }
}
