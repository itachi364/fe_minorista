package com.msvanegasg.facturaelectronica.tenant.domain.model;

public enum LicenseFeature {
    COMPANY_BASIC(LicenseModule.COMPANY, true),
    BRANDING_BASIC(LicenseModule.COMPANY, true),
    CUSTOMERS(LicenseModule.THIRDPARTY, true),
    SUPPLIERS(LicenseModule.THIRDPARTY, true),
    PRODUCTS_SERVICES(LicenseModule.INVENTORY, true),
    INVENTORY_BASIC(LicenseModule.INVENTORY, true),
    INVENTORY_ADVANCED(LicenseModule.INVENTORY, true),
    POS_SALES(LicenseModule.BILLING, true),
    ELECTRONIC_BILLING(LicenseModule.BILLING, true),
    SALES_REGISTRY(LicenseModule.BILLING, true),
    FISCAL_SETTINGS_BASIC(LicenseModule.BILLING, true),
    FISCAL_DOCUMENTS_BASIC(LicenseModule.BILLING, true),
    REPORTS_BASIC(LicenseModule.REPORTS, true),
    CATALOGS_OPERATING(LicenseModule.CATALOGS, true),
    AUDIT_BASIC(LicenseModule.AUDIT, true),
    USERS_BASIC(LicenseModule.USERS, true),
    OPERATIONAL_PIN(LicenseModule.BILLING, true),
    ACCOUNTING_CORE(null, true),
    ACCOUNTING_ADVANCED(LicenseModule.ACCOUNTING, true),
    PURCHASES(LicenseModule.ACCOUNTING, true),
    EXPENSES(LicenseModule.ACCOUNTING, true),
    RECEIVABLES(LicenseModule.ACCOUNTING, true),
    FISCAL_RULES_ADVANCED(LicenseModule.ACCOUNTING, true),
    PAYROLL(LicenseModule.PAYROLL, true),
    REPORTS_ASYNC(LicenseModule.REPORTS, true),
    AUDIT_ADVANCED(LicenseModule.AUDIT, true),
    USERS_ADVANCED(LicenseModule.USERS, true),
    ACCOUNTANT_PORTAL(LicenseModule.ACCOUNTING, false),
    CUSTOM_RULES(null, false),
    CUSTOM_REPORTS(null, false),
    CUSTOM_WORKFLOWS(null, false),
    CUSTOM_INTEGRATIONS(null, false),
    PRIORITY_SUPPORT(null, false);

    private final LicenseModule module;
    private final boolean standard;

    LicenseFeature(LicenseModule module, boolean standard) {
        this.module = module;
        this.standard = standard;
    }

    public LicenseModule module() {
        return module;
    }

    public boolean standard() {
        return standard;
    }
}
