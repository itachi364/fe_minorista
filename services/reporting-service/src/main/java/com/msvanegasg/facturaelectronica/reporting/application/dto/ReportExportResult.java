package com.msvanegasg.facturaelectronica.reporting.application.dto;

import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;

public record ReportExportResult(String filename, String contentType, byte[] content, ReportExportFormat format) {
}
