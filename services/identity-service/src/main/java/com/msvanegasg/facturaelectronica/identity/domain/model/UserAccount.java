package com.msvanegasg.facturaelectronica.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record UserAccount(
        UUID id,
        String email,
        String fullName,
        String passwordHash,
        String cognitoSubject,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public UserAccount {
        Objects.requireNonNull(id, "id is required");
        email = required(email, "email").toLowerCase();
        fullName = required(fullName, "fullName");
        passwordHash = required(passwordHash, "passwordHash");
        cognitoSubject = optional(cognitoSubject);
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public UserAccount(UUID id, String email, String fullName, String passwordHash, UserStatus status,
            Instant createdAt, Instant updatedAt) {
        this(id, email, fullName, passwordHash, null, status, createdAt, updatedAt);
    }

    public static UserAccount create(UUID id, String email, String fullName, String passwordHash, Instant now) {
        return new UserAccount(id, email, fullName, passwordHash, null, UserStatus.ACTIVE, now, now);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public UserAccount update(String newEmail, String newFullName, Instant now) {
        return new UserAccount(id, newEmail, newFullName, passwordHash, cognitoSubject, status, createdAt, now);
    }

    public UserAccount linkCognitoSubject(String subject, String newFullName, Instant now) {
        String normalizedSubject = required(subject, "subject");
        if (cognitoSubject != null && !cognitoSubject.equals(normalizedSubject)) {
            throw new IllegalStateException("user is already linked to another Cognito subject");
        }
        String resolvedFullName = newFullName == null || newFullName.isBlank() ? fullName : newFullName;
        return new UserAccount(id, email, resolvedFullName, passwordHash, normalizedSubject, status, createdAt, now);
    }

    public UserAccount activate(Instant now) {
        return new UserAccount(id, email, fullName, passwordHash, cognitoSubject, UserStatus.ACTIVE, createdAt, now);
    }

    public UserAccount deactivate(Instant now) {
        return new UserAccount(id, email, fullName, passwordHash, cognitoSubject, UserStatus.INACTIVE, createdAt, now);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String optional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
