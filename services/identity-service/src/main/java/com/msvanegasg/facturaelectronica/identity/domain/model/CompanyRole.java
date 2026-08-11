package com.msvanegasg.facturaelectronica.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record CompanyRole(
        UUID id,
        UUID companyId,
        String name,
        String description,
        Set<PermissionCode> permissionCodes,
        boolean systemSeed,
        boolean active,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt) {

    public CompanyRole {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(companyId, "companyId is required");
        name = require(name, "name");
        description = description == null ? "" : description.trim();
        permissionCodes = normalizeCompanyPermissions(permissionCodes);
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static CompanyRole create(UUID id, UUID companyId, String name, String description,
            Set<PermissionCode> permissionCodes, boolean systemSeed, UUID createdBy, Instant now) {
        return new CompanyRole(id, companyId, name, description, permissionCodes, systemSeed, true, createdBy, now, now);
    }

    public CompanyRole update(String newName, String newDescription, Set<PermissionCode> newPermissions, Instant now) {
        return new CompanyRole(id, companyId, newName, newDescription, newPermissions, systemSeed, active, createdBy,
                createdAt, now);
    }

    public CompanyRole deactivate(Instant now) {
        return new CompanyRole(id, companyId, name, description, permissionCodes, systemSeed, false, createdBy,
                createdAt, now);
    }

    public CompanyRole activate(Instant now) {
        return new CompanyRole(id, companyId, name, description, permissionCodes, systemSeed, true, createdBy,
                createdAt, now);
    }

    private static Set<PermissionCode> normalizeCompanyPermissions(Set<PermissionCode> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            throw new IllegalArgumentException("permissionCodes are required");
        }
        Set<PermissionCode> normalized = Set.copyOf(permissions);
        boolean hasGlobalPermission = normalized.stream().anyMatch(permission -> !permission.companyScoped());
        if (hasGlobalPermission) {
            throw new IllegalArgumentException("company roles cannot include global permissions");
        }
        return normalized;
    }

    private static String require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
