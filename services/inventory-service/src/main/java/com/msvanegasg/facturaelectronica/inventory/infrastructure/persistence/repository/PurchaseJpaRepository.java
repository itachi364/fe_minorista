package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.PurchaseJpaEntity;

public interface PurchaseJpaRepository extends JpaRepository<PurchaseJpaEntity, UUID> {

    Optional<PurchaseJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<PurchaseJpaEntity> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);
}
