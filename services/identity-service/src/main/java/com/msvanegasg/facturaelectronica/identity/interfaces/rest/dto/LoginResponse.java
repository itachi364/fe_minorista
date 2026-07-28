package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(UUID userId, String email, String fullName, String tokenType, String accessToken,
        Instant expiresAt) {
}
