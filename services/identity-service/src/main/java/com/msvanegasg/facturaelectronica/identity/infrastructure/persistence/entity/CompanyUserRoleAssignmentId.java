package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class CompanyUserRoleAssignmentId implements Serializable {

    private UUID companyId;
    private UUID userId;
    private UUID roleId;

    public CompanyUserRoleAssignmentId() {
    }

    public CompanyUserRoleAssignmentId(UUID companyId, UUID userId, UUID roleId) {
        this.companyId = companyId;
        this.userId = userId;
        this.roleId = roleId;
    }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getRoleId() { return roleId; }
    public void setRoleId(UUID roleId) { this.roleId = roleId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CompanyUserRoleAssignmentId that)) {
            return false;
        }
        return Objects.equals(companyId, that.companyId)
                && Objects.equals(userId, that.userId)
                && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId, userId, roleId);
    }
}
