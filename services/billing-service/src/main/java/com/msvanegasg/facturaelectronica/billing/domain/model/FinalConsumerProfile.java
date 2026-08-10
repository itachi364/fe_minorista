package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record FinalConsumerProfile(UUID id, UUID companyId, String profileCode, int identificationTypeCode,
        String identificationNumber, String displayName, boolean active, String source, String sourceVersion,
        Instant updatedAt) {

    public FinalConsumerProfile {
        Objects.requireNonNull(id, "id is required");
        profileCode = requireText(profileCode, "profileCode");
        identificationNumber = requireText(identificationNumber, "identificationNumber");
        displayName = requireText(displayName, "displayName");
        source = requireText(source, "source");
        sourceVersion = requireText(sourceVersion, "sourceVersion");
        Objects.requireNonNull(updatedAt, "updatedAt is required");
        if (!"FINAL_CONSUMER".equals(profileCode)) {
            throw new IllegalArgumentException("profileCode must be FINAL_CONSUMER");
        }
        if (identificationTypeCode < 1 || identificationTypeCode > 99) {
            throw new IllegalArgumentException("identificationTypeCode must be a DIAN numeric code");
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
