package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.RoleCode;

public record AssignRolesCommand(UUID companyId, UUID userId, Set<RoleCode> roles, String authorizationHeader) {
}
