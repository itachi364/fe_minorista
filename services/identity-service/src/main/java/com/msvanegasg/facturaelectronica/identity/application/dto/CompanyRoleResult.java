package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.CompanyRole;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;

public record CompanyRoleResult(UUID id, UUID companyId, String name, String description,
        Set<PermissionCode> permissionCodes, boolean systemSeed, boolean active, UUID createdBy,
        Instant createdAt, Instant updatedAt) {

    public static CompanyRoleResult from(CompanyRole role) {
        return new CompanyRoleResult(role.id(), role.companyId(), role.name(), role.description(), role.permissionCodes(),
                role.systemSeed(), role.active(), role.createdBy(), role.createdAt(), role.updatedAt());
    }
}
