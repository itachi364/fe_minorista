CREATE TABLE tenant.invoicing_rule_parameter (
    parameter_code VARCHAR(40) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    numeric_value NUMERIC(19, 2) NOT NULL,
    source_version VARCHAR(80) NOT NULL,
    PRIMARY KEY (parameter_code, valid_from),
    CHECK (valid_to >= valid_from),
    CHECK (numeric_value > 0)
);

INSERT INTO tenant.invoicing_rule_parameter
    (parameter_code, valid_from, valid_to, numeric_value, source_version)
VALUES
    ('UVT', DATE '2026-01-01', DATE '2026-12-31', 52374.00, 'DIAN-RES-238-2025');

CREATE TABLE tenant.company_invoicing_obligation_snapshot (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES tenant.company(id) ON DELETE RESTRICT,
    version BIGINT NOT NULL,
    current_snapshot BOOLEAN NOT NULL,
    status VARCHAR(40) NOT NULL,
    decision_code VARCHAR(100) NOT NULL,
    person_type VARCHAR(30),
    tax_regime VARCHAR(60),
    rut_generated_at DATE,
    customs_user BOOLEAN,
    establishment_count INTEGER,
    exploits_intangibles BOOLEAN,
    only_excluded_or_untaxed_operations BOOLEAN,
    previous_year_gross_activity_income NUMERIC(19, 2),
    current_year_gross_activity_income NUMERIC(19, 2),
    previous_year_taxed_financial_operations NUMERIC(19, 2),
    current_year_taxed_financial_operations NUMERIC(19, 2),
    largest_previous_year_taxed_contract NUMERIC(19, 2),
    largest_current_year_taxed_contract NUMERIC(19, 2),
    largest_same_customer_aggregate NUMERIC(19, 2),
    voluntary_electronic_invoicer BOOLEAN,
    special_exception_type VARCHAR(80),
    special_exception_scope VARCHAR(240),
    rut_asset_id UUID REFERENCES tenant.company_file_asset(id) ON DELETE RESTRICT,
    uvt_value NUMERIC(19, 2) NOT NULL,
    normative_rule_set_version VARCHAR(80) NOT NULL,
    evaluated_by UUID,
    evaluated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (company_id, version),
    CHECK (version > 0),
    CHECK (establishment_count IS NULL OR establishment_count >= 0)
);

CREATE UNIQUE INDEX uq_company_invoicing_obligation_current
    ON tenant.company_invoicing_obligation_snapshot(company_id)
    WHERE current_snapshot;

CREATE INDEX idx_company_invoicing_obligation_history
    ON tenant.company_invoicing_obligation_snapshot(company_id, version DESC);

CREATE TABLE tenant.company_invoicing_obligation_responsibility (
    snapshot_id UUID NOT NULL REFERENCES tenant.company_invoicing_obligation_snapshot(id) ON DELETE RESTRICT,
    responsibility_code VARCHAR(30) NOT NULL,
    PRIMARY KEY (snapshot_id, responsibility_code)
);

CREATE TABLE tenant.company_invoicing_obligation_ciiu (
    snapshot_id UUID NOT NULL REFERENCES tenant.company_invoicing_obligation_snapshot(id) ON DELETE RESTRICT,
    ciiu_code VARCHAR(10) NOT NULL,
    PRIMARY KEY (snapshot_id, ciiu_code)
);

CREATE TABLE tenant.company_invoicing_obligation_operation (
    snapshot_id UUID NOT NULL REFERENCES tenant.company_invoicing_obligation_snapshot(id) ON DELETE RESTRICT,
    operation_type VARCHAR(60) NOT NULL,
    PRIMARY KEY (snapshot_id, operation_type)
);

CREATE TABLE tenant.company_invoicing_obligation_reason (
    snapshot_id UUID NOT NULL REFERENCES tenant.company_invoicing_obligation_snapshot(id) ON DELETE RESTRICT,
    reason_order INTEGER NOT NULL,
    reason_code VARCHAR(100) NOT NULL,
    PRIMARY KEY (snapshot_id, reason_order)
);
