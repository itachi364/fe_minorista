package com.msvanegasg.facturaelectronica.reporting.application.usecase;

public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(String reportCode) {
        super("No existe el reporte " + reportCode + ".");
    }
}
