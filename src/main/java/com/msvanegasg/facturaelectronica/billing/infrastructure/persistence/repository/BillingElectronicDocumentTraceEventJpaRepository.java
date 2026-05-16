package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.BillingElectronicDocumentTraceEventJpaEntity;

public interface BillingElectronicDocumentTraceEventJpaRepository
        extends JpaRepository<BillingElectronicDocumentTraceEventJpaEntity, UUID> {
}
