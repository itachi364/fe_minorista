package com.msvanegasg.facturaelectronica.reporting.application.port.out;

import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJob;

public interface ReportNotificationPort {

    void notifyReady(ReportExportJob job, String downloadLink);
}
