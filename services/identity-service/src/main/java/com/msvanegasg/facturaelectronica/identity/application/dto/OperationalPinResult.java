package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.time.Instant;
import java.util.UUID;

public record OperationalPinResult(UUID companyId, boolean configured, boolean valid, boolean locked,
        boolean mustChange, int remainingAttempts, Instant updatedAt) {
}
