package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.time.Instant;
import java.util.UUID;

public record LoginResult(UUID userId, String email, String fullName, String accessToken, Instant expiresAt) {
}
