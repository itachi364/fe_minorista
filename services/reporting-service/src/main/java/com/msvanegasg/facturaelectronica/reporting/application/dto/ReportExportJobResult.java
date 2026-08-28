package com.msvanegasg.facturaelectronica.reporting.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.reporting.domain.model.ChartType;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJobStatus;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportNotificationStatus;

public record ReportExportJobResult(
        UUID jobId,
        UUID companyId,
        UUID requestedByUserId,
        String reportCode,
        ReportExportFormat format,
        ChartType chartType,
        LocalDate from,
        LocalDate to,
        Map<String, String> filters,
        boolean notifyByEmail,
        ReportExportJobStatus status,
        boolean downloadAvailable,
        String filename,
        String contentType,
        Long fileSize,
        Instant requestedAt,
        Instant startedAt,
        Instant completedAt,
        Instant expiresAt,
        Instant tokenExpiresAt,
        ReportNotificationStatus notificationStatus,
        String notificationMessage,
        String failureMessage,
        int downloadAttempts,
        Instant lastDownloadedAt) {
}
