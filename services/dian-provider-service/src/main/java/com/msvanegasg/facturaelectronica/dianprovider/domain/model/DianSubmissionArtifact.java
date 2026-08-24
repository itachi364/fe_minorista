package com.msvanegasg.facturaelectronica.dianprovider.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record DianSubmissionArtifact(
        UUID id,
        UUID companyId,
        UUID submissionId,
        UUID documentId,
        DianArtifactType artifactType,
        String storageBucketReference,
        String storageKey,
        String contentType,
        String fileName,
        String contentHash,
        Long sizeBytes,
        Instant createdAt,
        UUID createdBy) {

    public DianSubmissionArtifact {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(submissionId, "submissionId is required");
        Objects.requireNonNull(documentId, "documentId is required");
        Objects.requireNonNull(artifactType, "artifactType is required");
        Objects.requireNonNull(storageKey, "storageKey is required");
        Objects.requireNonNull(contentType, "contentType is required");
        Objects.requireNonNull(fileName, "fileName is required");
        Objects.requireNonNull(contentHash, "contentHash is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        storageBucketReference = normalize(storageBucketReference);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
