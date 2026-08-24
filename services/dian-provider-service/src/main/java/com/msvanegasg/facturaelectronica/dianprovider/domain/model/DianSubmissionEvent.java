package com.msvanegasg.facturaelectronica.dianprovider.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record DianSubmissionEvent(
        UUID id,
        UUID companyId,
        UUID submissionId,
        UUID documentId,
        DianSubmissionEventType eventType,
        DianSubmissionEventStatus status,
        String dianCode,
        String dianMessage,
        String correlationId,
        Instant createdAt) {

    public DianSubmissionEvent {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(submissionId, "submissionId is required");
        Objects.requireNonNull(documentId, "documentId is required");
        Objects.requireNonNull(eventType, "eventType is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        dianCode = normalize(dianCode);
        dianMessage = normalize(dianMessage);
        correlationId = normalize(correlationId);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
