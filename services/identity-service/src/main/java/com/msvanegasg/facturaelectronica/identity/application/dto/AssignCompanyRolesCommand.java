package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.util.Set;
import java.util.UUID;

public record AssignCompanyRolesCommand(UUID companyId, UUID userId, Set<UUID> roleIds, String authorizationHeader) {
}
