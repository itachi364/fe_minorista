package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import java.time.Instant;
import java.util.Set;

public record BffUserSession(
        String subject,
        String email,
        String fullName,
        Set<String> groups,
        String accessToken,
        String idToken,
        String refreshToken,
        Instant expiresAt,
        Instant createdAt) {
}
