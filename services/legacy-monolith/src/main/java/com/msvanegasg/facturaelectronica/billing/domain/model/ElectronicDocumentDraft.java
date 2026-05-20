package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ElectronicDocumentDraft {

    private final UUID id;
    private final UUID companyId;
    private final ElectronicDocumentType documentType;
    private final ElectronicDocumentStatus status;
    private final String idempotencyKey;
    private final Instant createdAt;

    private ElectronicDocumentDraft(
            UUID id,
            UUID companyId,
            ElectronicDocumentType documentType,
            ElectronicDocumentStatus status,
            String idempotencyKey,
            Instant createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.documentType = documentType;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
    }

    public static ElectronicDocumentDraft create(
            UUID id,
            UUID companyId,
            ElectronicDocumentType documentType,
            String idempotencyKey,
            Instant createdAt) {
        requireNonNull(id, "id");
        requireNonNull(companyId, "companyId");
        requireNonNull(documentType, "documentType");
        requireNonBlank(idempotencyKey, "idempotencyKey");
        requireNonNull(createdAt, "createdAt");

        return new ElectronicDocumentDraft(
                id,
                companyId,
                documentType,
                ElectronicDocumentStatus.DRAFT,
                idempotencyKey,
                createdAt);
    }

    public UUID id() {
        return id;
    }

    public UUID companyId() {
        return companyId;
    }

    public ElectronicDocumentType documentType() {
        return documentType;
    }

    public ElectronicDocumentStatus status() {
        return status;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }

    public Instant createdAt() {
        return createdAt;
    }

    private static void requireNonNull(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
