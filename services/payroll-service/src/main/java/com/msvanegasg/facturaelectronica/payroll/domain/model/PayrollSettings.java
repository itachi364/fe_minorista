package com.msvanegasg.facturaelectronica.payroll.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PayrollSettings(UUID companyId, boolean electronicPayrollEnabled, String providerMode, Instant updatedAt) {

    public PayrollSettings {
        Objects.requireNonNull(companyId, "companyId is required");
        providerMode = providerMode == null || providerMode.isBlank() ? "MOCK" : providerMode.trim().toUpperCase();
        updatedAt = updatedAt == null ? Instant.now() : updatedAt;
    }
}
