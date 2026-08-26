package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.SaleDocumentTypeOverrideJpaEntity;

public interface SaleDocumentTypeOverrideJpaRepository extends JpaRepository<SaleDocumentTypeOverrideJpaEntity, UUID> {

    Optional<SaleDocumentTypeOverrideJpaEntity> findFirstByCompanyIdAndSaleIdAndActiveTrueOrderByCreatedAtDesc(
            UUID companyId, UUID saleId);
}
