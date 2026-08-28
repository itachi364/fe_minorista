package com.msvanegasg.facturaelectronica.reporting.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.reporting.infrastructure.persistence.entity.ReportExportDownloadAttemptJpaEntity;

public interface ReportExportDownloadAttemptJpaRepository
        extends JpaRepository<ReportExportDownloadAttemptJpaEntity, UUID> {
}
