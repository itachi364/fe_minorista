CREATE TABLE IF NOT EXISTS identity.permission_catalog (
    code VARCHAR(80) PRIMARY KEY,
    scope VARCHAR(20) NOT NULL,
    module VARCHAR(60) NOT NULL,
    description VARCHAR(250) NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT chk_identity_permission_catalog_scope CHECK (scope IN ('GLOBAL', 'COMPANY')),
    CONSTRAINT chk_identity_permission_catalog_global_prefix CHECK (
        (code LIKE 'GLOBAL_%' AND scope = 'GLOBAL') OR (code NOT LIKE 'GLOBAL_%' AND scope = 'COMPANY')
    )
);

CREATE TABLE IF NOT EXISTS identity.company_role (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(250),
    system_seed BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,
    created_by UUID REFERENCES identity.user_account(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_identity_company_role_company_name
    ON identity.company_role (company_id, lower(name));

CREATE TABLE IF NOT EXISTS identity.company_role_permission (
    role_id UUID NOT NULL REFERENCES identity.company_role(id) ON DELETE CASCADE,
    permission_code VARCHAR(80) NOT NULL REFERENCES identity.permission_catalog(code),
    PRIMARY KEY (role_id, permission_code),
    CONSTRAINT chk_identity_company_role_permission_no_global CHECK (permission_code NOT LIKE 'GLOBAL_%')
);

CREATE TABLE IF NOT EXISTS identity.company_user_role_assignment (
    company_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES identity.user_account(id),
    role_id UUID NOT NULL REFERENCES identity.company_role(id),
    assigned_by UUID REFERENCES identity.user_account(id),
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    revoked_by UUID REFERENCES identity.user_account(id),
    PRIMARY KEY (company_id, user_id, role_id),
    CONSTRAINT fk_identity_assignment_role_company FOREIGN KEY (role_id) REFERENCES identity.company_role(id)
);

CREATE INDEX IF NOT EXISTS idx_identity_company_role_company_active
    ON identity.company_role (company_id, active);

CREATE INDEX IF NOT EXISTS idx_identity_company_assignment_user_active
    ON identity.company_user_role_assignment (company_id, user_id, revoked_at);

INSERT INTO identity.permission_catalog (code, scope, module, description, active) VALUES
    ('GLOBAL_COMPANIES_MANAGE', 'GLOBAL', 'platform', 'Manage contracting companies', true),
    ('GLOBAL_LICENSES_MANAGE', 'GLOBAL', 'platform', 'Manage platform licenses', true),
    ('GLOBAL_USERS_MANAGE', 'GLOBAL', 'platform', 'Manage global users', true),
    ('GLOBAL_ROLES_MANAGE', 'GLOBAL', 'platform', 'Manage global roles', true),
    ('GLOBAL_AUDIT_VIEW', 'GLOBAL', 'audit', 'View global audit', true),
    ('COMPANY_USERS_MANAGE', 'COMPANY', 'users', 'Manage company users', true),
    ('COMPANY_ROLES_MANAGE', 'COMPANY', 'users', 'Manage company roles and permissions', true),
    ('COMPANY_SETTINGS_MANAGE', 'COMPANY', 'settings', 'Manage company settings', true),
    ('SALES_CREATE', 'COMPANY', 'sales', 'Create sales and POS transactions', true),
    ('SALES_CANCEL', 'COMPANY', 'sales', 'Cancel sales', true),
    ('FISCAL_DOCUMENTS_ISSUE', 'COMPANY', 'billing', 'Issue fiscal electronic documents', true),
    ('INVENTORY_VIEW', 'COMPANY', 'inventory', 'View inventory', true),
    ('INVENTORY_MANAGE', 'COMPANY', 'inventory', 'Manage inventory', true),
    ('PURCHASES_MANAGE', 'COMPANY', 'purchases', 'Manage purchases', true),
    ('ACCOUNTING_VIEW', 'COMPANY', 'accounting', 'View accounting information', true),
    ('ACCOUNTING_MANAGE', 'COMPANY', 'accounting', 'Manage accounting information', true),
    ('REPORTS_VIEW', 'COMPANY', 'reports', 'View reports', true),
    ('AUDIT_VIEW', 'COMPANY', 'audit', 'View company audit', true),
    ('USERS_MANAGE', 'COMPANY', 'users', 'Legacy users management permission', true),
    ('ROLES_MANAGE', 'COMPANY', 'users', 'Legacy roles management permission', true),
    ('LICENSE_MANAGE', 'COMPANY', 'license', 'Legacy license management permission', true)
ON CONFLICT (code) DO UPDATE SET
    scope = EXCLUDED.scope,
    module = EXCLUDED.module,
    description = EXCLUDED.description,
    active = EXCLUDED.active;
