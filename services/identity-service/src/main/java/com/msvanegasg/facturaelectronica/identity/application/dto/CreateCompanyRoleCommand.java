package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;

public record CreateCompanyRoleCommand(UUID companyId, String name, String description,
        Set<PermissionCode> permissionCodes, String authorizationHeader) {
}
