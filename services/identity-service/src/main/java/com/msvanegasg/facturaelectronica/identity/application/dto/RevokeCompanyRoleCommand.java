package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.util.UUID;

public record RevokeCompanyRoleCommand(UUID companyId, UUID userId, UUID roleId, String authorizationHeader) {
}
