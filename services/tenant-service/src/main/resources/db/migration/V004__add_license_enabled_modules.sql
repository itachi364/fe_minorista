ALTER TABLE tenant.company_license
    ADD COLUMN IF NOT EXISTS enabled_modules text[] NOT NULL DEFAULT '{}';
