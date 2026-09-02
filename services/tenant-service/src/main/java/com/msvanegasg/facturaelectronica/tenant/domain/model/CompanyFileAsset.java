package com.msvanegasg.facturaelectronica.tenant.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record CompanyFileAsset(
        UUID id,
        UUID companyId,
        CompanyFileCategory category,
        String originalFilename,
        String contentType,
        String storageKey,
        long fileSize,
        String contentHash,
        UUID uploadedBy,
        Instant uploadedAt) {

    public CompanyFileAsset {
        require(id, "id");
        require(companyId, "companyId");
        require(category, "category");
        originalFilename = normalizeRequired(originalFilename, 255, "originalFilename");
        contentType = normalizeRequired(contentType, 120, "contentType");
        storageKey = normalizeRequired(storageKey, 500, "storageKey");
        contentHash = normalizeRequired(contentHash, 128, "contentHash");
        require(uploadedAt, "uploadedAt");
        if (fileSize <= 0) {
            throw new IllegalArgumentException("fileSize must be greater than zero");
        }
    }

    private static String normalizeRequired(String value, int maxLength, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(field + " length is invalid");
        }
        return normalized;
    }

    private static void require(Object value, String field) {
        Objects.requireNonNull(value, field + " is required");
    }
}
