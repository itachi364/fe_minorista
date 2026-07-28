package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.identity.application.port.out.AccessAuditRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.domain.model.AccessAuditEvent;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.AccessAuditJpaEntity;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository.AccessAuditJpaRepository;

@Component
public class AccessAuditPersistenceAdapter implements AccessAuditRepositoryPort {

    private final AccessAuditJpaRepository repository;

    public AccessAuditPersistenceAdapter(AccessAuditJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccessAuditEvent save(AccessAuditEvent event) {
        return toDomain(repository.save(toEntity(event)));
    }

    private static AccessAuditJpaEntity toEntity(AccessAuditEvent event) {
        AccessAuditJpaEntity entity = new AccessAuditJpaEntity();
        entity.setId(event.id());
        entity.setCompanyId(event.companyId());
        entity.setUserId(event.userId());
        entity.setAction(event.action());
        entity.setResourceType(event.resourceType());
        entity.setResourceId(event.resourceId());
        entity.setResult(event.result());
        entity.setDetail(event.detail());
        entity.setOccurredAt(event.occurredAt());
        return entity;
    }

    private static AccessAuditEvent toDomain(AccessAuditJpaEntity entity) {
        return new AccessAuditEvent(entity.getId(), entity.getCompanyId(), entity.getUserId(), entity.getAction(),
                entity.getResourceType(), entity.getResourceId(), entity.getResult(), entity.getDetail(),
                entity.getOccurredAt());
    }
}
