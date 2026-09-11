ALTER TABLE tenant.company_tax_profile
    ADD COLUMN tax_residency VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
    ADD COLUMN income_tax_status VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
    ADD COLUMN self_withholding_scopes VARCHAR(100)[] NOT NULL DEFAULT '{}',
    ADD COLUMN fiscal_evidence_reference VARCHAR(500);

ALTER TABLE tenant.company_tax_profile
    ADD CONSTRAINT ck_company_tax_profile_residency
        CHECK (tax_residency IN ('UNKNOWN', 'COLOMBIA', 'EXTERIOR')),
    ADD CONSTRAINT ck_company_tax_profile_income_tax_status
        CHECK (income_tax_status IN ('UNKNOWN', 'DECLARANTE', 'NO_DECLARANTE', 'NO_APLICA'));

ALTER TABLE tenant.company_tax_profile_history
    ADD COLUMN tax_residency VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
    ADD COLUMN income_tax_status VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
    ADD COLUMN self_withholding_scopes VARCHAR(100)[] NOT NULL DEFAULT '{}',
    ADD COLUMN fiscal_evidence_reference VARCHAR(500);

ALTER TABLE tenant.company_tax_profile_history
    ADD CONSTRAINT ck_company_tax_profile_history_residency
        CHECK (tax_residency IN ('UNKNOWN', 'COLOMBIA', 'EXTERIOR')),
    ADD CONSTRAINT ck_company_tax_profile_history_income_tax_status
        CHECK (income_tax_status IN ('UNKNOWN', 'DECLARANTE', 'NO_DECLARANTE', 'NO_APLICA'));

