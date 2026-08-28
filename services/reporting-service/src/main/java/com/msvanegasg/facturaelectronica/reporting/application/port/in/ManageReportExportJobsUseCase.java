package com.msvanegasg.facturaelectronica.reporting.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.reporting.application.dto.CreateReportExportJobCommand;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportDownloadLinkResult;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportDownloadResult;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportExportJobResult;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJobStatus;

public interface ManageReportExportJobsUseCase {

    ReportExportJobResult create(CreateReportExportJobCommand command);

    List<ReportExportJobResult> find(UUID companyId, UUID requestedByUserId, ReportExportJobStatus status);

    ReportExportJobResult findById(UUID companyId, UUID jobId);

    ReportDownloadLinkResult createDownloadLink(UUID companyId, UUID jobId);

    ReportDownloadResult downloadByToken(String token);

    int processPending();
}
