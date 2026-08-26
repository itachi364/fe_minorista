package com.msvanegasg.facturaelectronica.identity.domain.model;

public enum PermissionCode {
    GLOBAL_COMPANIES_MANAGE(PermissionScope.GLOBAL, "platform", "Manage contracting companies"),
    GLOBAL_LICENSES_MANAGE(PermissionScope.GLOBAL, "platform", "Manage platform licenses"),
    GLOBAL_USERS_MANAGE(PermissionScope.GLOBAL, "platform", "Manage global users"),
    GLOBAL_ROLES_MANAGE(PermissionScope.GLOBAL, "platform", "Manage global roles"),
    GLOBAL_AUDIT_VIEW(PermissionScope.GLOBAL, "audit", "View global audit"),

    COMPANY_USERS_MANAGE(PermissionScope.COMPANY, "users", "Manage company users"),
    COMPANY_ROLES_MANAGE(PermissionScope.COMPANY, "users", "Manage company roles and permissions"),
    COMPANY_SETTINGS_MANAGE(PermissionScope.COMPANY, "settings", "Manage company settings"),
    COMPANY_CATALOGS_MANAGE(PermissionScope.COMPANY, "catalogs", "Manage company catalogs"),
    OPERATIONAL_PIN_MANAGE(PermissionScope.COMPANY, "settings", "Manage operational PIN for fiscal overrides"),
    SALES_CREATE(PermissionScope.COMPANY, "sales", "Create sales and POS transactions"),
    SALES_CANCEL(PermissionScope.COMPANY, "sales", "Cancel sales"),
    FISCAL_DOCUMENTS_ISSUE(PermissionScope.COMPANY, "billing", "Issue fiscal electronic documents"),
    INVENTORY_VIEW(PermissionScope.COMPANY, "inventory", "View inventory"),
    INVENTORY_MANAGE(PermissionScope.COMPANY, "inventory", "Manage inventory"),
    PURCHASES_MANAGE(PermissionScope.COMPANY, "purchases", "Manage purchases"),
    ACCOUNTING_VIEW(PermissionScope.COMPANY, "accounting", "View accounting information"),
    ACCOUNTING_MANAGE(PermissionScope.COMPANY, "accounting", "Manage accounting information"),
    PAYROLL_VIEW(PermissionScope.COMPANY, "payroll", "View payroll information"),
    PAYROLL_MANAGE(PermissionScope.COMPANY, "payroll", "Manage payroll workers, settings and payments"),
    REPORTS_VIEW(PermissionScope.COMPANY, "reports", "View reports"),
    AUDIT_VIEW(PermissionScope.COMPANY, "audit", "View company audit"),

    USERS_MANAGE(PermissionScope.COMPANY, "users", "Legacy users management permission"),
    ROLES_MANAGE(PermissionScope.COMPANY, "users", "Legacy roles management permission"),
    LICENSE_MANAGE(PermissionScope.COMPANY, "license", "Legacy license management permission");

    private final PermissionScope scope;
    private final String module;
    private final String description;

    PermissionCode(PermissionScope scope, String module, String description) {
        this.scope = scope;
        this.module = module;
        this.description = description;
    }

    public PermissionScope scope() {
        return scope;
    }

    public String module() {
        return module;
    }

    public String description() {
        return description;
    }

    public boolean companyScoped() {
        return scope == PermissionScope.COMPANY && !name().startsWith("GLOBAL_");
    }
}
