package com.msvanegasg.facturaelectronica.reporting.interfaces.rest.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportDownloadLinkResult;

public record ReportDownloadLinkResponse(
        UUID jobId,
        String downloadLink,
        Instant expiresAt,
        int presignedTtlSeconds) {

    public static ReportDownloadLinkResponse from(ReportDownloadLinkResult result) {
        return new ReportDownloadLinkResponse(result.jobId(), result.downloadLink(), result.tokenExpiresAt(),
                result.presignedTtlSeconds());
    }
}
