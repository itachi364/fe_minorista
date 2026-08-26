package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.identity.application.port.out.OperationalPinRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.domain.model.OperationalPin;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.OperationalPinJpaEntity;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository.OperationalPinJpaRepository;

@Component
public class OperationalPinPersistenceAdapter implements OperationalPinRepositoryPort {

    private final OperationalPinJpaRepository repository;

    public OperationalPinPersistenceAdapter(OperationalPinJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public OperationalPin save(OperationalPin operationalPin) {
        return toDomain(repository.save(toEntity(operationalPin)));
    }

    @Override
    public Optional<OperationalPin> findByCompanyId(UUID companyId) {
        return repository.findById(companyId).map(OperationalPinPersistenceAdapter::toDomain);
    }

    private static OperationalPinJpaEntity toEntity(OperationalPin pin) {
        OperationalPinJpaEntity entity = new OperationalPinJpaEntity();
        entity.setCompanyId(pin.companyId());
        entity.setPinHash(pin.pinHash());
        entity.setFailedAttempts(pin.failedAttempts());
        entity.setLockedAt(pin.lockedAt());
        entity.setMustChange(pin.mustChange());
        entity.setUpdatedAt(pin.updatedAt());
        return entity;
    }

    private static OperationalPin toDomain(OperationalPinJpaEntity entity) {
        return new OperationalPin(entity.getCompanyId(), entity.getPinHash(), entity.getFailedAttempts(),
                entity.getLockedAt(), entity.isMustChange(), entity.getUpdatedAt());
    }
}
