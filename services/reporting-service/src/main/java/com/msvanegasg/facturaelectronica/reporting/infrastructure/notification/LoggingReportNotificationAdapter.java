package com.msvanegasg.facturaelectronica.reporting.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportNotificationPort;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJob;

@Component
public class LoggingReportNotificationAdapter implements ReportNotificationPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingReportNotificationAdapter.class);

    @Override
    public void notifyReady(ReportExportJob job, String downloadLink) {
        LOGGER.info("Report export job ready for notification. jobId={} companyId={} reportCode={}",
                job.id(), job.companyId(), job.reportCode());
    }
}
