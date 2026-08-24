package com.msvanegasg.facturaelectronica.reporting.domain.model;

public enum ReportExportFormat {
    CSV("text/csv; charset=UTF-8", "csv"),
    XLS("application/vnd.ms-excel; charset=UTF-8", "xls");

    private final String contentType;
    private final String extension;

    ReportExportFormat(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String contentType() {
        return contentType;
    }

    public String extension() {
        return extension;
    }
}
