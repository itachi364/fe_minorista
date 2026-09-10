CREATE TABLE fiscal_document_calculation (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    source_id UUID NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    operation_date DATE NOT NULL,
    third_party_id UUID NOT NULL,
    operation_municipality_code VARCHAR(5),
    status VARCHAR(20) NOT NULL,
    profile_evidence JSONB NOT NULL,
    result_payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_fiscal_document_calculation_source UNIQUE (company_id, source_type, source_id),
    CONSTRAINT ck_fiscal_document_calculation_status CHECK (status IN ('APPLIED', 'NOT_APPLIED', 'BLOCKED'))
);

CREATE TABLE fiscal_line_calculation_snapshot (
    id UUID PRIMARY KEY,
    calculation_id UUID NOT NULL REFERENCES fiscal_document_calculation(id),
    line_id UUID NOT NULL,
    item_index INTEGER NOT NULL,
    concept_code VARCHAR(80) NOT NULL,
    withholding_type VARCHAR(30) NOT NULL,
    decision VARCHAR(30) NOT NULL,
    base_amount NUMERIC(38, 2) NOT NULL,
    rate NUMERIC(9, 6) NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    rule_id UUID,
    rule_version VARCHAR(40),
    parameter_version VARCHAR(40),
    legal_reference VARCHAR(250),
    source_url VARCHAR(500),
    reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_fiscal_line_calculation_item UNIQUE (calculation_id, line_id, item_index),
    CONSTRAINT ck_fiscal_line_snapshot_amounts CHECK (base_amount >= 0 AND rate >= 0 AND amount >= 0)
);

CREATE INDEX idx_fiscal_line_snapshot_line
    ON fiscal_line_calculation_snapshot (calculation_id, line_id, withholding_type);
