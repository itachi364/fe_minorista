package com.msvanegasg.facturaelectronica.reporting.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJobStatus;
import com.msvanegasg.facturaelectronica.reporting.infrastructure.persistence.entity.ReportExportJobJpaEntity;

public interface ReportExportJobJpaRepository extends JpaRepository<ReportExportJobJpaEntity, UUID> {

    Optional<ReportExportJobJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<ReportExportJobJpaEntity> findByTokenHash(String tokenHash);

    List<ReportExportJobJpaEntity> findByCompanyIdOrderByRequestedAtDesc(UUID companyId);

    List<ReportExportJobJpaEntity> findByCompanyIdAndRequestedByUserIdOrderByRequestedAtDesc(UUID companyId,
            UUID requestedByUserId);

    List<ReportExportJobJpaEntity> findByCompanyIdAndStatusOrderByRequestedAtDesc(UUID companyId,
            ReportExportJobStatus status);

    List<ReportExportJobJpaEntity> findByCompanyIdAndRequestedByUserIdAndStatusOrderByRequestedAtDesc(UUID companyId,
            UUID requestedByUserId, ReportExportJobStatus status);

    List<ReportExportJobJpaEntity> findByStatusOrderByRequestedAtAsc(ReportExportJobStatus status, Pageable pageable);

    List<ReportExportJobJpaEntity> findByStatusAndExpiresAtBefore(ReportExportJobStatus status, Instant expiresAt);
}
