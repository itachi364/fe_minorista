ALTER TABLE tenant.company DROP CONSTRAINT IF EXISTS uk_tenant_company_identification;

ALTER TABLE tenant.company ADD COLUMN IF NOT EXISTS identification_type_code integer;

UPDATE tenant.company
SET identification_type_code = 31
WHERE identification_type_code IS NULL;

ALTER TABLE tenant.company ALTER COLUMN identification_type_code SET NOT NULL;

ALTER TABLE tenant.company ADD CONSTRAINT ck_tenant_company_identification_type_code
    CHECK (identification_type_code IN (11, 12, 13, 21, 22, 31, 41, 42, 43, 47, 48));

ALTER TABLE tenant.company ADD CONSTRAINT uk_tenant_company_identification
    UNIQUE (identification_type_code, identification_number);

ALTER TABLE tenant.company DROP COLUMN IF EXISTS identification_type_id;
