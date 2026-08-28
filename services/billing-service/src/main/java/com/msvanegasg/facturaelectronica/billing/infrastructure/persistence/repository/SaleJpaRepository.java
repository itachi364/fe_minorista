package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.SaleJpaEntity;

public interface SaleJpaRepository extends JpaRepository<SaleJpaEntity, UUID>, SaleJpaRepositoryCustom {

    @EntityGraph(attributePaths = { "lines", "electronicDocument" })
    Optional<SaleJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    @EntityGraph(attributePaths = { "lines", "electronicDocument" })
    Optional<SaleJpaEntity> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    @EntityGraph(attributePaths = { "lines", "electronicDocument" })
    @Query("select s from SaleJpaEntity s join s.electronicDocument d where s.companyId = :companyId and d.id = :documentId")
    Optional<SaleJpaEntity> findByCompanyIdAndElectronicDocumentId(@Param("companyId") UUID companyId,
            @Param("documentId") UUID documentId);

    @Query("""
            select count(d.id)
            from SaleJpaEntity s join s.electronicDocument d
            where s.companyId = :companyId
              and d.issuedAt >= :from
              and d.issuedAt < :to
            """)
    long countIssuedElectronicDocuments(@Param("companyId") UUID companyId, @Param("from") Instant from,
            @Param("to") Instant to);
}
