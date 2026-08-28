package com.msvanegasg.facturaelectronica.reporting.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJob;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJobStatus;

public interface ReportExportJobRepositoryPort {

    ReportExportJob save(ReportExportJob job);

    Optional<ReportExportJob> findByCompanyIdAndId(UUID companyId, UUID jobId);

    Optional<ReportExportJob> findByTokenHash(String tokenHash);

    List<ReportExportJob> findByCompany(UUID companyId, UUID requestedByUserId, ReportExportJobStatus status);

    List<ReportExportJob> findPending(int limit);

    List<ReportExportJob> findReadyExpiredBefore(Instant now);
}
