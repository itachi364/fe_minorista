package com.msvanegasg.facturaelectronica.reporting.infrastructure.worker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.reporting.application.port.in.ManageReportExportJobsUseCase;

@Component
@ConditionalOnProperty(prefix = "reporting.exports", name = "worker-enabled", havingValue = "true",
        matchIfMissing = true)
public class ReportExportWorkerScheduler {

    private final ManageReportExportJobsUseCase useCase;

    public ReportExportWorkerScheduler(ManageReportExportJobsUseCase useCase) {
        this.useCase = useCase;
    }

    @Scheduled(fixedDelayString = "${reporting.exports.worker-fixed-delay-ms:5000}")
    public void processPendingJobs() {
        useCase.processPending();
    }
}
