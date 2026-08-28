package com.msvanegasg.facturaelectronica.reporting.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ReportDownloadLinkResult(
        UUID jobId,
        String downloadLink,
        Instant tokenExpiresAt,
        int presignedTtlSeconds) {
}
