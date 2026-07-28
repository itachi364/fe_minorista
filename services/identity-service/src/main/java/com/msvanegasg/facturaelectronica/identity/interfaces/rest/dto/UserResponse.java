package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.UserStatus;

public record UserResponse(UUID id, String email, String fullName, UserStatus status, Instant createdAt,
        Instant updatedAt) {
}
