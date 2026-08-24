package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record BffUserSession(
        UUID userId,
        String subject,
        String email,
        String fullName,
        Set<String> groups,
        String accessToken,
        String idToken,
        String refreshToken,
        Instant expiresAt,
        Instant createdAt,
        boolean mfaAuthenticated) {

    public BffUserSession(UUID userId, String subject, String email, String fullName, Set<String> groups,
            String accessToken, String idToken, String refreshToken, Instant expiresAt, Instant createdAt) {
        this(userId, subject, email, fullName, groups, accessToken, idToken, refreshToken, expiresAt, createdAt, true);
    }
}
