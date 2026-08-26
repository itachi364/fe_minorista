package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record OperationalPinResponse(UUID companyId, boolean configured, boolean valid, boolean locked,
        boolean mustChange, int remainingAttempts, Instant updatedAt) {
}
