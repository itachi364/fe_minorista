package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.BillingElectronicPosDocumentJpaEntity;

public interface BillingElectronicPosDocumentJpaRepository
        extends JpaRepository<BillingElectronicPosDocumentJpaEntity, UUID> {

    Optional<BillingElectronicPosDocumentJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);
}
