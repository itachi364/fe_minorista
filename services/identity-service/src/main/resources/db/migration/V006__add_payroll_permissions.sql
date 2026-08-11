INSERT INTO identity.permission_catalog (code, scope, module, description, active) VALUES
    ('PAYROLL_VIEW', 'COMPANY', 'payroll', 'View payroll information', true),
    ('PAYROLL_MANAGE', 'COMPANY', 'payroll', 'Manage payroll workers, settings and payments', true)
ON CONFLICT (code) DO UPDATE SET
    scope = EXCLUDED.scope,
    module = EXCLUDED.module,
    description = EXCLUDED.description,
    active = EXCLUDED.active;
