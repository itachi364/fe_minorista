package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.RoleCode;

public record MembershipResponse(UUID id, UUID companyId, UUID userId, Set<RoleCode> roles, boolean active,
        Instant createdAt, Instant updatedAt) {
}
