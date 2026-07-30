package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.GlobalRoleCode;

public record LoginResponse(UUID userId, String email, String fullName, String tokenType, String accessToken,
        Instant expiresAt, Set<GlobalRoleCode> globalRoles) {
}