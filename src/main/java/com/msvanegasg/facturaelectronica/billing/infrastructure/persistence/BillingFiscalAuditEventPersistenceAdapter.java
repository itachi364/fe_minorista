package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalAuditEventRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalAuditEvent;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.BillingFiscalAuditEventJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.BillingFiscalAuditEventJpaRepository;

@Component
public class BillingFiscalAuditEventPersistenceAdapter implements FiscalAuditEventRepositoryPort {

    private final BillingFiscalAuditEventJpaRepository repository;

    public BillingFiscalAuditEventPersistenceAdapter(BillingFiscalAuditEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public FiscalAuditEvent save(FiscalAuditEvent event) {
        repository.save(BillingFiscalAuditEventJpaEntity.builder()
                .id(event.id())
                .companyId(event.companyId())
                .resourceId(event.resourceId())
                .resourceType(event.resourceType())
                .action(event.action())
                .result(event.result())
                .userId(event.userId())
                .occurredAt(event.occurredAt())
                .detail(event.detail())
                .build());
        return event;
    }
}
