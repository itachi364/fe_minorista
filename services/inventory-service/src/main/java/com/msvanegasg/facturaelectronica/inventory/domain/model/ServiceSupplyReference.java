package com.msvanegasg.facturaelectronica.inventory.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ServiceSupplyReference(
        UUID id,
        UUID companyId,
        UUID serviceProductId,
        UUID supplyProductId,
        String notes,
        boolean active,
        Instant createdAt) {

    public ServiceSupplyReference {
        require(id, "id");
        require(companyId, "companyId");
        require(serviceProductId, "serviceProductId");
        require(supplyProductId, "supplyProductId");
        if (serviceProductId.equals(supplyProductId)) {
            throw new IllegalArgumentException("service and supply product must be different");
        }
        notes = normalizeOptional(notes, 300, "notes");
        require(createdAt, "createdAt");
    }

    public static ServiceSupplyReference create(UUID id, UUID companyId, UUID serviceProductId, UUID supplyProductId,
            String notes, Instant now) {
        return new ServiceSupplyReference(id, companyId, serviceProductId, supplyProductId, notes, true, now);
    }

    private static String normalizeOptional(String value, int maxLength, String field) {
        if (value == null || value.isBlank()) {
            return null;
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
