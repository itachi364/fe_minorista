package com.msvanegasg.facturaelectronica.identity.domain.model;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record CompanyMembership(
        UUID id,
        UUID companyId,
        UUID userId,
        Set<RoleCode> roles,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {

    public CompanyMembership {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(userId, "userId is required");
        roles = normalizeRoles(roles);
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static CompanyMembership create(UUID id, UUID companyId, UUID userId, Set<RoleCode> roles, Instant now) {
        return new CompanyMembership(id, companyId, userId, roles, true, now, now);
    }

    public CompanyMembership replaceRoles(Set<RoleCode> newRoles, Instant now) {
        return new CompanyMembership(id, companyId, userId, newRoles, active, createdAt, now);
    }

    public Set<PermissionCode> permissions() {
        EnumSet<PermissionCode> permissions = EnumSet.noneOf(PermissionCode.class);
        roles.forEach(role -> permissions.addAll(role.permissions()));
        return Set.copyOf(permissions);
    }

    public boolean hasPermission(PermissionCode permission) {
        return active && permissions().contains(permission);
    }

    private static Set<RoleCode> normalizeRoles(Set<RoleCode> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("roles are required");
        }
        return Set.copyOf(roles);
    }
}
