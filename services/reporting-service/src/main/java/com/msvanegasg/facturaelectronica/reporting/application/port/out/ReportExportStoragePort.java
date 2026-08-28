package com.msvanegasg.facturaelectronica.reporting.application.port.out;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportExportResult;

public interface ReportExportStoragePort {

    StoredReport store(UUID companyId, UUID jobId, ReportExportResult export);

    byte[] read(String storageKey);

    record StoredReport(String storageKey, String filename, String contentType, long fileSize) {
    }
}
