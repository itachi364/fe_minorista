package com.msvanegasg.facturaelectronica.reporting.application.usecase;

import java.util.UUID;

public class ReportExportJobNotFoundException extends RuntimeException {

    public ReportExportJobNotFoundException(UUID jobId) {
        super("No existe el trabajo de reporte " + jobId + ".");
    }

    public ReportExportJobNotFoundException() {
        super("No existe un enlace de descarga valido para el reporte.");
    }
}
