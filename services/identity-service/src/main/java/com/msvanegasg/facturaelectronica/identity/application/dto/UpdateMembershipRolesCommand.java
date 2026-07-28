package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.RoleCode;

public record UpdateMembershipRolesCommand(UUID companyId, UUID membershipId, Set<RoleCode> roles,
        String authorizationHeader) {
}
