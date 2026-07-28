package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.UserAccount;
import com.msvanegasg.facturaelectronica.identity.domain.model.UserStatus;

public record UserResult(UUID id, String email, String fullName, UserStatus status, Instant createdAt,
        Instant updatedAt) {

    public static UserResult from(UserAccount user) {
        return new UserResult(user.id(), user.email(), user.fullName(), user.status(), user.createdAt(),
                user.updatedAt());
    }
}
