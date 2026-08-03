package com.msvanegasg.facturaelectronica.identity.domain.model;

import java.util.EnumSet;
import java.util.Set;

public enum RoleCode {
    OWNER(companyPermissions()),
    ADMIN(EnumSet.of(PermissionCode.USERS_MANAGE, PermissionCode.ROLES_MANAGE,
            PermissionCode.COMPANY_USERS_MANAGE, PermissionCode.COMPANY_ROLES_MANAGE,
            PermissionCode.SALES_CREATE, PermissionCode.FISCAL_DOCUMENTS_ISSUE, PermissionCode.INVENTORY_MANAGE,
            PermissionCode.ACCOUNTING_MANAGE, PermissionCode.REPORTS_VIEW, PermissionCode.AUDIT_VIEW)),
    CASHIER(EnumSet.of(PermissionCode.SALES_CREATE, PermissionCode.FISCAL_DOCUMENTS_ISSUE,
            PermissionCode.REPORTS_VIEW)),
    ACCOUNTANT(EnumSet.of(PermissionCode.ACCOUNTING_MANAGE, PermissionCode.ACCOUNTING_VIEW,
            PermissionCode.REPORTS_VIEW)),
    AUDITOR(EnumSet.of(PermissionCode.REPORTS_VIEW, PermissionCode.AUDIT_VIEW));

    private final Set<PermissionCode> permissions;

    RoleCode(Set<PermissionCode> permissions) {
        this.permissions = Set.copyOf(permissions);
    }

    public Set<PermissionCode> permissions() {
        return permissions;
    }

    private static EnumSet<PermissionCode> companyPermissions() {
        EnumSet<PermissionCode> permissions = EnumSet.noneOf(PermissionCode.class);
        for (PermissionCode permission : PermissionCode.values()) {
            if (permission.companyScoped()) {
                permissions.add(permission);
            }
        }
        return permissions;
    }
}
