package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.PurchaseJpaEntity;

public interface PurchaseJpaRepository extends JpaRepository<PurchaseJpaEntity, UUID> {

    @EntityGraph(attributePaths = "lines")
    Optional<PurchaseJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    @EntityGraph(attributePaths = "lines")
    Optional<PurchaseJpaEntity> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    @EntityGraph(attributePaths = "lines")
    @Query("""
            select p from PurchaseJpaEntity p
            where p.companyId = :companyId
              and (:status is null or p.status = :status)
              and (:supplierId is null or p.supplierId = :supplierId)
              and (:from is null or p.createdAt >= :from)
              and (:to is null or p.createdAt < :to)
            order by p.createdAt desc
            """)
    List<PurchaseJpaEntity> findPurchases(@Param("companyId") UUID companyId,
            @Param("status") PurchaseStatus status, @Param("supplierId") UUID supplierId,
            @Param("from") Instant from, @Param("to") Instant to);
}
