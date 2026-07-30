package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.GlobalRoleCode;

public record LoginResult(UUID userId, String email, String fullName, String accessToken, Instant expiresAt,
        Set<GlobalRoleCode> globalRoles) {
}