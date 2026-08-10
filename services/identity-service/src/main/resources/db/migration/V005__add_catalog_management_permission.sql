INSERT INTO identity.permission_catalog (code, scope, module, description, active) VALUES
    ('COMPANY_CATALOGS_MANAGE', 'COMPANY', 'catalogs', 'Manage company catalogs', true)
ON CONFLICT (code) DO UPDATE SET
    scope = EXCLUDED.scope,
    module = EXCLUDED.module,
    description = EXCLUDED.description,
    active = EXCLUDED.active;
