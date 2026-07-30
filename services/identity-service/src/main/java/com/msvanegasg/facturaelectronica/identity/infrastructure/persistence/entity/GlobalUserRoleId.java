package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.GlobalRoleCode;

public class GlobalUserRoleId implements Serializable {

    private UUID userId;
    private GlobalRoleCode roleCode;

    public GlobalUserRoleId() {
    }

    public GlobalUserRoleId(UUID userId, GlobalRoleCode roleCode) {
        this.userId = userId;
        this.roleCode = roleCode;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public GlobalRoleCode getRoleCode() { return roleCode; }
    public void setRoleCode(GlobalRoleCode roleCode) { this.roleCode = roleCode; }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GlobalUserRoleId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && roleCode == that.roleCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleCode);
    }
}