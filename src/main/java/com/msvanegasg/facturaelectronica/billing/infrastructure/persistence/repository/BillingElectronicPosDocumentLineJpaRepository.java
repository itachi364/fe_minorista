package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.BillingElectronicPosDocumentLineJpaEntity;

public interface BillingElectronicPosDocumentLineJpaRepository
        extends JpaRepository<BillingElectronicPosDocumentLineJpaEntity, UUID> {

    List<BillingElectronicPosDocumentLineJpaEntity> findByDocumentId(UUID documentId);

    void deleteByDocumentId(UUID documentId);
}
