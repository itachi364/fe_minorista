package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.SaleJpaEntity;

public interface SaleJpaRepository extends JpaRepository<SaleJpaEntity, UUID> {

    @EntityGraph(attributePaths = { "lines", "electronicDocument" })
    Optional<SaleJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    @EntityGraph(attributePaths = { "lines", "electronicDocument" })
    Optional<SaleJpaEntity> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);
}
