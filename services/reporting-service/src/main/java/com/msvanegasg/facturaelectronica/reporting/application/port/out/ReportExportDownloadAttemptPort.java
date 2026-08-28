package com.msvanegasg.facturaelectronica.reporting.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface ReportExportDownloadAttemptPort {

    void record(UUID id, UUID jobId, UUID companyId, Instant attemptedAt, String result, String detail);
}
