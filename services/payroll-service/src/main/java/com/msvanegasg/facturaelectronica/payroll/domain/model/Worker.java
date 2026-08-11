package com.msvanegasg.facturaelectronica.payroll.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Worker(UUID id, UUID companyId, int identificationTypeCode, String identificationNumber,
        Integer verificationDigit, String fullName, String workerClassification, boolean active, Instant createdAt) {

    public Worker {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(companyId, "companyId is required");
        if (identificationTypeCode <= 0 || identificationTypeCode > 99) {
            throw new IllegalArgumentException("identificationTypeCode must be a DIAN numeric code");
        }
        identificationNumber = required(identificationNumber, "identificationNumber");
        fullName = required(fullName, "fullName");
        workerClassification = required(workerClassification, "workerClassification").toUpperCase();
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
