package com.msvanegasg.facturaelectronica.payroll.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ElectronicPayrollDocument(UUID id, UUID companyId, UUID dailyLaborPaymentId, String cune, String status,
        String providerResponse, Instant createdAt) {

    public ElectronicPayrollDocument {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(dailyLaborPaymentId, "dailyLaborPaymentId is required");
        cune = required(cune, "cune");
        status = required(status, "status").toUpperCase();
        providerResponse = providerResponse == null || providerResponse.isBlank() ? null : providerResponse.trim();
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
