CREATE TABLE fiscal_account_mapping (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    withholding_type VARCHAR(30) NOT NULL,
    payable_account_code VARCHAR(30) NOT NULL,
    receivable_account_code VARCHAR(30),
    valid_from DATE NOT NULL,
    valid_to DATE,
    active BOOLEAN NOT NULL,
    updated_by UUID,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_fiscal_account_mapping_version
        UNIQUE (company_id, withholding_type, valid_from),
    CONSTRAINT ck_fiscal_account_mapping_type
        CHECK (withholding_type IN ('RETEFUENTE', 'RETEIVA', 'RETEICA', 'AUTORETENCION')),
    CONSTRAINT ck_fiscal_account_mapping_period
        CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX idx_fiscal_account_mapping_effective
    ON fiscal_account_mapping (company_id, withholding_type, active, valid_from, valid_to);

CREATE TABLE fiscal_calculation_reversal (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    calculation_id UUID NOT NULL REFERENCES fiscal_document_calculation(id),
    reason VARCHAR(500) NOT NULL,
    reversed_by UUID,
    reversed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_fiscal_calculation_reversal UNIQUE (company_id, calculation_id)
);

CREATE TABLE fiscal_period_close (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    fiscal_year INTEGER NOT NULL,
    fiscal_month INTEGER NOT NULL,
    snapshot_payload JSONB NOT NULL,
    closed_by UUID,
    closed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_fiscal_period_close UNIQUE (company_id, fiscal_year, fiscal_month),
    CONSTRAINT ck_fiscal_period_month CHECK (fiscal_month BETWEEN 1 AND 12)
);

CREATE TABLE withholding_certificate (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    third_party_id UUID NOT NULL,
    fiscal_year INTEGER NOT NULL,
    version INTEGER NOT NULL,
    total_base NUMERIC(38, 2) NOT NULL,
    total_withheld NUMERIC(38, 2) NOT NULL,
    content_csv TEXT NOT NULL,
    generated_by UUID,
    generated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_withholding_certificate_version
        UNIQUE (company_id, third_party_id, fiscal_year, version)
);

CREATE INDEX idx_withholding_certificate_lookup
    ON withholding_certificate (company_id, third_party_id, fiscal_year, version DESC);
