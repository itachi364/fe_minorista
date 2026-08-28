package com.msvanegasg.facturaelectronica.reporting.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportExportDownloadAttemptPort;
import com.msvanegasg.facturaelectronica.reporting.infrastructure.persistence.entity.ReportExportDownloadAttemptJpaEntity;
import com.msvanegasg.facturaelectronica.reporting.infrastructure.persistence.repository.ReportExportDownloadAttemptJpaRepository;

@Repository
public class ReportExportDownloadAttemptPersistenceAdapter implements ReportExportDownloadAttemptPort {

    private final ReportExportDownloadAttemptJpaRepository repository;

    public ReportExportDownloadAttemptPersistenceAdapter(ReportExportDownloadAttemptJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void record(UUID id, UUID jobId, UUID companyId, Instant attemptedAt, String result, String detail) {
        ReportExportDownloadAttemptJpaEntity entity = new ReportExportDownloadAttemptJpaEntity();
        entity.setId(id);
        entity.setJobId(jobId);
        entity.setCompanyId(companyId);
        entity.setAttemptedAt(attemptedAt);
        entity.setResult(result);
        entity.setDetail(detail == null || detail.length() <= 300 ? detail : detail.substring(0, 300));
        repository.save(entity);
    }
}
