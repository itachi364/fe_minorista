package com.msvanegasg.facturaelectronica.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record UserSession(
        UUID id,
        UUID userId,
        String tokenHash,
        Instant expiresAt,
        Instant createdAt,
        Instant revokedAt) {

    public UserSession {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(userId, "userId is required");
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash is required");
        }
        Objects.requireNonNull(expiresAt, "expiresAt is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
    }

    public static UserSession create(UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant now) {
        return new UserSession(id, userId, tokenHash, expiresAt, now, null);
    }

    public boolean isValidAt(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public UserSession revoke(Instant now) {
        return new UserSession(id, userId, tokenHash, expiresAt, createdAt, now);
    }
}
