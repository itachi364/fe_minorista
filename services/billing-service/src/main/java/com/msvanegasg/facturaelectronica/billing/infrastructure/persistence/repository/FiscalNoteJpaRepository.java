package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.FiscalNoteJpaEntity;

public interface FiscalNoteJpaRepository extends JpaRepository<FiscalNoteJpaEntity, UUID> {

    Optional<FiscalNoteJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<FiscalNoteJpaEntity> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    @Query("""
            select count(n.id)
            from FiscalNoteJpaEntity n
            where n.companyId = :companyId
              and n.issuedAt >= :from
              and n.issuedAt < :to
            """)
    long countIssuedFiscalNotes(@Param("companyId") UUID companyId, @Param("from") Instant from,
            @Param("to") Instant to);
}
