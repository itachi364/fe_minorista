package com.msvanegasg.facturaelectronica.dianprovider.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record DianTechnicalValidationResult(
        UUID id,
        UUID companyId,
        UUID submissionId,
        UUID documentId,
        DianValidationType validationType,
        DianValidationStatus result,
        String ruleCode,
        String message,
        String sourceVersion,
        Instant validatedAt) {

    public DianTechnicalValidationResult {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(submissionId, "submissionId is required");
        Objects.requireNonNull(documentId, "documentId is required");
        Objects.requireNonNull(validationType, "validationType is required");
        Objects.requireNonNull(result, "result is required");
        Objects.requireNonNull(sourceVersion, "sourceVersion is required");
        Objects.requireNonNull(validatedAt, "validatedAt is required");
        ruleCode = normalize(ruleCode);
        message = normalize(message);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
