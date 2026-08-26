CREATE TABLE IF NOT EXISTS identity.operational_pin (
    company_id UUID PRIMARY KEY,
    pin_hash VARCHAR(500) NOT NULL,
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    locked_at TIMESTAMPTZ,
    must_change BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_identity_operational_pin_attempts CHECK (failed_attempts BETWEEN 0 AND 3)
);

INSERT INTO identity.permission_catalog (code, scope, module, description, active) VALUES
    ('OPERATIONAL_PIN_MANAGE', 'COMPANY', 'settings', 'Manage operational PIN for fiscal overrides', true)
ON CONFLICT (code) DO UPDATE SET
    scope = EXCLUDED.scope,
    module = EXCLUDED.module,
    description = EXCLUDED.description,
    active = EXCLUDED.active;
