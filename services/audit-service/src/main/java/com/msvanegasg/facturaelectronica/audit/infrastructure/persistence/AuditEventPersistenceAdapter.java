package com.msvanegasg.facturaelectronica.audit.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventQuery;
import com.msvanegasg.facturaelectronica.audit.application.port.out.AuditEventRepositoryPort;
import com.msvanegasg.facturaelectronica.audit.domain.model.AuditEvent;
import com.msvanegasg.facturaelectronica.audit.infrastructure.persistence.entity.AuditEventJpaEntity;
import com.msvanegasg.facturaelectronica.audit.infrastructure.persistence.repository.AuditEventJpaRepository;

@Component
public class AuditEventPersistenceAdapter implements AuditEventRepositoryPort {

    private final AuditEventJpaRepository repository;

    public AuditEventPersistenceAdapter(AuditEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuditEvent save(AuditEvent event) {
        return toDomain(repository.save(toEntity(event)));
    }

    @Override
    public List<AuditEvent> find(AuditEventQuery query) {
        String resourceType = normalized(query.resourceType());
        String resourceId = normalized(query.resourceId());
        return repository.findByCompanyIdOrderByOccurredAtDesc(query.companyId()).stream()
                .filter(event -> resourceType == null || resourceType.equals(event.getResourceType()))
                .filter(event -> resourceId == null || resourceId.equals(event.getResourceId()))
                .filter(event -> query.from() == null || !event.getOccurredAt().isBefore(query.from()))
                .filter(event -> query.to() == null || !event.getOccurredAt().isAfter(query.to()))
                .filter(event -> query.userId() == null || query.userId().equals(event.getUserId()))
                .map(AuditEventPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<String> resourceTypes(UUID companyId) {
        return repository.findDistinctResourceTypesByCompanyId(companyId);
    }

    private static AuditEventJpaEntity toEntity(AuditEvent event) {
        AuditEventJpaEntity entity = new AuditEventJpaEntity();
        entity.setId(event.id());
        entity.setCompanyId(event.companyId());
        entity.setUserId(event.userId());
        entity.setEventType(event.eventType());
        entity.setResourceType(event.resourceType());
        entity.setResourceId(event.resourceId());
        entity.setAction(event.action());
        entity.setResult(event.result());
        entity.setDetail(event.detail());
        entity.setOccurredAt(event.occurredAt());
        return entity;
    }

    private static AuditEvent toDomain(AuditEventJpaEntity entity) {
        return new AuditEvent(entity.getId(), entity.getCompanyId(), entity.getUserId(), entity.getEventType(),
                entity.getResourceType(), entity.getResourceId(), entity.getAction(), entity.getResult(),
                entity.getDetail(), entity.getOccurredAt());
    }

    private static String normalized(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
