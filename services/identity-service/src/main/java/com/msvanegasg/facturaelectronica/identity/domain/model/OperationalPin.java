package com.msvanegasg.facturaelectronica.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public record OperationalPin(UUID companyId, String pinHash, int failedAttempts, Instant lockedAt,
        boolean mustChange, Instant updatedAt) {

    private static final int MAX_ATTEMPTS = 3;
    private static final Pattern PIN_PATTERN = Pattern.compile("^\\d{6}$");

    public OperationalPin {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(pinHash, "pinHash is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");
        if (failedAttempts < 0 || failedAttempts > MAX_ATTEMPTS) {
            throw new IllegalArgumentException("failedAttempts must be between 0 and 3");
        }
    }

    public static void validateRawPin(String rawPin) {
        if (rawPin == null || !PIN_PATTERN.matcher(rawPin).matches()) {
            throw new IllegalArgumentException("El PIN operacional debe tener exactamente 6 digitos.");
        }
    }

    public static OperationalPin configure(UUID companyId, String pinHash, Instant updatedAt) {
        return new OperationalPin(companyId, pinHash, 0, null, false, updatedAt);
    }

    public OperationalPin registerFailedAttempt(Instant now) {
        int attempts = Math.min(MAX_ATTEMPTS, failedAttempts + 1);
        return new OperationalPin(companyId, pinHash, attempts, attempts >= MAX_ATTEMPTS ? now : lockedAt,
                mustChange, now);
    }

    public OperationalPin registerSuccess(Instant now) {
        if (failedAttempts == 0 && lockedAt == null) {
            return this;
        }
        return new OperationalPin(companyId, pinHash, 0, null, mustChange, now);
    }

    public OperationalPin unlockRequiringChange(Instant now) {
        return new OperationalPin(companyId, pinHash, 0, null, true, now);
    }

    public boolean locked() {
        return lockedAt != null || failedAttempts >= MAX_ATTEMPTS;
    }

    public int remainingAttempts() {
        return Math.max(0, MAX_ATTEMPTS - failedAttempts);
    }
}
