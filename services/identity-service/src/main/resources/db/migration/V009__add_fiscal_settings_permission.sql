INSERT INTO identity.permission_catalog (code, scope, module, description, active) VALUES
    ('FISCAL_SETTINGS_MANAGE', 'COMPANY', 'fiscal', 'Manage company fiscal settings and rules', true)
ON CONFLICT (code) DO UPDATE SET
    scope = EXCLUDED.scope,
    module = EXCLUDED.module,
    description = EXCLUDED.description,
    active = EXCLUDED.active;

INSERT INTO identity.company_role_permission (role_id, permission_code)
SELECT role.id, 'FISCAL_SETTINGS_MANAGE'
FROM identity.company_role role
WHERE lower(role.name) = 'owner'
ON CONFLICT (role_id, permission_code) DO NOTHING;
