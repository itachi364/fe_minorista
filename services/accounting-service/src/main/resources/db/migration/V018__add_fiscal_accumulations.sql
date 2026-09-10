ALTER TABLE fiscal_line_calculation_snapshot
    ADD COLUMN previous_accumulated_base NUMERIC(38, 2) NOT NULL DEFAULT 0,
    ADD COLUMN cumulative_base NUMERIC(38, 2) NOT NULL DEFAULT 0;

CREATE TABLE fiscal_accumulation_line (
    id UUID PRIMARY KEY,
    calculation_id UUID NOT NULL REFERENCES fiscal_document_calculation(id),
    company_id UUID NOT NULL,
    third_party_id UUID NOT NULL,
    operation_date DATE NOT NULL,
    concept_code VARCHAR(80) NOT NULL,
    line_id UUID NOT NULL,
    taxable_base NUMERIC(38, 2) NOT NULL,
    tax_amount NUMERIC(38, 2) NOT NULL,
    retefuente_amount NUMERIC(38, 2) NOT NULL DEFAULT 0,
    reteiva_amount NUMERIC(38, 2) NOT NULL DEFAULT 0,
    reteica_amount NUMERIC(38, 2) NOT NULL DEFAULT 0,
    self_withholding_amount NUMERIC(38, 2) NOT NULL DEFAULT 0,
    reversed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_fiscal_accumulation_line UNIQUE (calculation_id, line_id),
    CONSTRAINT ck_fiscal_accumulation_amounts CHECK (
        taxable_base >= 0 AND tax_amount >= 0 AND retefuente_amount >= 0
        AND reteiva_amount >= 0 AND reteica_amount >= 0 AND self_withholding_amount >= 0
    )
);

CREATE INDEX idx_fiscal_accumulation_daily
    ON fiscal_accumulation_line
    (company_id, third_party_id, operation_date, concept_code)
    WHERE reversed_at IS NULL;
