CREATE TABLE IF NOT EXISTS withholding_rule (
    id UUID PRIMARY KEY,
    company_id UUID,
    rule_set_version VARCHAR(40) NOT NULL,
    operation_type VARCHAR(40) NOT NULL,
    concept_code VARCHAR(80),
    withholding_type VARCHAR(30) NOT NULL,
    base_min_amount NUMERIC(38, 2) NOT NULL,
    rate NUMERIC(9, 6) NOT NULL,
    requires_company_withholding_agent BOOLEAN NOT NULL,
    requires_company_vat_responsible BOOLEAN NOT NULL,
    required_third_party_tax_regime VARCHAR(40),
    required_third_party_responsibility VARCHAR(20),
    municipality_code VARCHAR(20),
    ciiu_code VARCHAR(10),
    valid_from DATE NOT NULL,
    valid_to DATE,
    priority INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT ck_withholding_rule_operation
        CHECK (operation_type IN ('PURCHASE', 'EXPENSE', 'PAYMENT', 'PAYROLL')),
    CONSTRAINT ck_withholding_rule_type
        CHECK (withholding_type IN ('RETEFUENTE', 'RETEIVA', 'RETEICA', 'AUTORETENCION')),
    CONSTRAINT ck_withholding_rule_rate CHECK (rate >= 0),
    CONSTRAINT ck_withholding_rule_base CHECK (base_min_amount >= 0),
    CONSTRAINT ck_withholding_rule_validity CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX IF NOT EXISTS idx_withholding_rule_lookup
    ON withholding_rule (company_id, operation_type, active, valid_from, valid_to);

CREATE TABLE IF NOT EXISTS withholding_calculation_snapshot (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    source_id UUID NOT NULL,
    third_party_id UUID NOT NULL,
    operation_date DATE NOT NULL,
    withholding_type VARCHAR(30) NOT NULL,
    base_amount NUMERIC(38, 2) NOT NULL,
    rate NUMERIC(9, 6) NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    rule_version VARCHAR(40),
    decision VARCHAR(30) NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_withholding_snapshot_type
        CHECK (withholding_type IN ('RETEFUENTE', 'RETEIVA', 'RETEICA', 'AUTORETENCION')),
    CONSTRAINT ck_withholding_snapshot_decision
        CHECK (decision IN ('APPLIED', 'NOT_APPLIED', 'EXEMPT', 'BLOCKED')),
    CONSTRAINT ck_withholding_snapshot_amounts
        CHECK (base_amount >= 0 AND rate >= 0 AND amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_withholding_snapshot_source
    ON withholding_calculation_snapshot (company_id, source_type, source_id);

INSERT INTO withholding_rule (
    id,
    company_id,
    rule_set_version,
    operation_type,
    concept_code,
    withholding_type,
    base_min_amount,
    rate,
    requires_company_withholding_agent,
    requires_company_vat_responsible,
    required_third_party_tax_regime,
    required_third_party_responsibility,
    municipality_code,
    ciiu_code,
    valid_from,
    valid_to,
    priority,
    active
)
VALUES (
    '2d9ff575-86be-4c83-8d02-458a7c6f5820',
    NULL,
    'CO-DIAN-2026-RETEIVA-SIMPLE',
    'PURCHASE',
    'ANY',
    'RETEIVA',
    0,
    0.150000,
    false,
    true,
    'SIMPLE',
    NULL,
    NULL,
    NULL,
    DATE '2026-01-01',
    NULL,
    100,
    true
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO withholding_rule (
    id,
    company_id,
    rule_set_version,
    operation_type,
    concept_code,
    withholding_type,
    base_min_amount,
    rate,
    requires_company_withholding_agent,
    requires_company_vat_responsible,
    required_third_party_tax_regime,
    required_third_party_responsibility,
    municipality_code,
    ciiu_code,
    valid_from,
    valid_to,
    priority,
    active
)
VALUES (
    'd9580774-c6a7-4a0b-a08a-57376ae5ad20',
    NULL,
    'CO-DIAN-2026-RETEIVA-SIMPLE',
    'EXPENSE',
    'ANY',
    'RETEIVA',
    0,
    0.150000,
    false,
    true,
    'SIMPLE',
    NULL,
    NULL,
    NULL,
    DATE '2026-01-01',
    NULL,
    100,
    true
)
ON CONFLICT (id) DO NOTHING;
