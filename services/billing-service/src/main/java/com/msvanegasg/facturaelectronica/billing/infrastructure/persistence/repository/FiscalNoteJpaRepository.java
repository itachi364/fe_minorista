package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.FiscalNoteJpaEntity;

public interface FiscalNoteJpaRepository extends JpaRepository<FiscalNoteJpaEntity, UUID> {

    Optional<FiscalNoteJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<FiscalNoteJpaEntity> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);
}