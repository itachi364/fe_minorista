package com.msvanegasg.facturaelectronica.reporting.application.dto;

public record ReportDownloadResult(
        String filename,
        String contentType,
        byte[] content,
        int presignedTtlSeconds) {
}
