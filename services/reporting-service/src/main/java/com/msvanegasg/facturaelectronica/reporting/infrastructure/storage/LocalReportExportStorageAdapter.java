package com.msvanegasg.facturaelectronica.reporting.infrastructure.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportExportResult;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportExportStoragePort;
import com.msvanegasg.facturaelectronica.reporting.infrastructure.config.ReportExportProperties;

@Component
public class LocalReportExportStorageAdapter implements ReportExportStoragePort {

    private final Path root;

    public LocalReportExportStorageAdapter(ReportExportProperties properties) {
        this.root = Path.of(properties.localStoragePath()).toAbsolutePath().normalize();
    }

    @Override
    public StoredReport store(UUID companyId, UUID jobId, ReportExportResult export) {
        try {
            String filename = sanitize(export.filename());
            String storageKey = companyId + "/" + jobId + "/" + filename;
            Path target = resolve(storageKey);
            Files.createDirectories(target.getParent());
            Files.write(target, export.content());
            return new StoredReport(storageKey, filename, export.contentType(), export.content().length);
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible almacenar el reporte exportado.", exception);
        }
    }

    @Override
    public byte[] read(String storageKey) {
        try {
            return Files.readAllBytes(resolve(storageKey));
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible leer el reporte exportado.", exception);
        }
    }

    private Path resolve(String storageKey) {
        Path target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("storageKey is outside export storage");
        }
        return target;
    }

    private static String sanitize(String value) {
        return value == null || value.isBlank() ? "reporte.csv" : value.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
