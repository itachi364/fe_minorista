package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;

public record CompanyRoleResponse(UUID id, UUID companyId, String name, String description,
        Set<PermissionCode> permissionCodes, boolean systemSeed, boolean active, UUID createdBy,
        Instant createdAt, Instant updatedAt) {
}
