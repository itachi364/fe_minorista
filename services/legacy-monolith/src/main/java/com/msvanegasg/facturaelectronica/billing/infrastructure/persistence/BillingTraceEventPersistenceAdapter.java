package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentTraceEventRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentTraceEvent;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.BillingElectronicDocumentTraceEventJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.BillingElectronicDocumentTraceEventJpaRepository;

@Component
public class BillingTraceEventPersistenceAdapter implements ElectronicDocumentTraceEventRepositoryPort {

    private final BillingElectronicDocumentTraceEventJpaRepository repository;

    public BillingTraceEventPersistenceAdapter(BillingElectronicDocumentTraceEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ElectronicDocumentTraceEvent save(ElectronicDocumentTraceEvent event) {
        repository.save(BillingElectronicDocumentTraceEventJpaEntity.builder()
                .id(event.id())
                .companyId(event.companyId())
                .documentId(event.documentId())
                .previousStatus(event.previousStatus())
                .newStatus(event.newStatus())
                .action(event.action())
                .result(event.result())
                .detail(event.detail())
                .userId(event.userId())
                .occurredAt(event.occurredAt())
                .build());
        return event;
    }
}
