package com.msvanegasg.facturaelectronica.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record UserAccount(
        UUID id,
        String email,
        String fullName,
        String passwordHash,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public UserAccount {
        Objects.requireNonNull(id, "id is required");
        email = required(email, "email").toLowerCase();
        fullName = required(fullName, "fullName");
        passwordHash = required(passwordHash, "passwordHash");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static UserAccount create(UUID id, String email, String fullName, String passwordHash, Instant now) {
        return new UserAccount(id, email, fullName, passwordHash, UserStatus.ACTIVE, now, now);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public UserAccount update(String newEmail, String newFullName, Instant now) {
        return new UserAccount(id, newEmail, newFullName, passwordHash, status, createdAt, now);
    }

    public UserAccount activate(Instant now) {
        return new UserAccount(id, email, fullName, passwordHash, UserStatus.ACTIVE, createdAt, now);
    }

    public UserAccount deactivate(Instant now) {
        return new UserAccount(id, email, fullName, passwordHash, UserStatus.INACTIVE, createdAt, now);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
