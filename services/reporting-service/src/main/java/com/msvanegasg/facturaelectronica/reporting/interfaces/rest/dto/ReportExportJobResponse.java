package com.msvanegasg.facturaelectronica.reporting.interfaces.rest.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportExportJobResult;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ChartType;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJobStatus;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportNotificationStatus;

public record ReportExportJobResponse(
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

    public static ReportExportJobResponse from(ReportExportJobResult result) {
        return new ReportExportJobResponse(result.jobId(), result.companyId(), result.requestedByUserId(),
                result.reportCode(), result.format(), result.chartType(), result.from(), result.to(), result.filters(),
                result.notifyByEmail(), result.status(), result.downloadAvailable(), result.filename(),
                result.contentType(), result.fileSize(), result.requestedAt(), result.startedAt(),
                result.completedAt(), result.expiresAt(), result.tokenExpiresAt(), result.notificationStatus(),
                result.notificationMessage(), result.failureMessage(), result.downloadAttempts(),
                result.lastDownloadedAt());
    }
}
