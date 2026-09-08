CREATE TABLE fiscal_parameter (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL,
    version VARCHAR(40) NOT NULL,
    value NUMERIC(38, 6) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE,
    legal_reference VARCHAR(250) NOT NULL,
    source_url VARCHAR(500) NOT NULL,
    published BOOLEAN NOT NULL,
    CONSTRAINT uk_fiscal_parameter_version UNIQUE (code, version),
    CONSTRAINT ck_fiscal_parameter_value CHECK (value >= 0),
    CONSTRAINT ck_fiscal_parameter_validity CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX idx_fiscal_parameter_effective
    ON fiscal_parameter (code, published, valid_from, valid_to);

INSERT INTO fiscal_parameter (id, code, version, value, valid_from, valid_to, legal_reference, source_url, published)
VALUES ('23820250-0000-4000-8000-000000000001', 'UVT', 'CO-DIAN-UVT-2026', 52374,
        DATE '2026-01-01', DATE '2026-12-31', 'Resolucion DIAN 000238 de 2025',
        'https://normograma.dian.gov.co/dian/compilacion/docs/resolucion_dian_0238_2025.htm', true);

ALTER TABLE withholding_rule
    ADD COLUMN threshold_unit VARCHAR(10) NOT NULL DEFAULT 'COP',
    ADD COLUMN threshold_value NUMERIC(38, 6) NOT NULL DEFAULT 0,
    ADD COLUMN threshold_operator VARCHAR(10) NOT NULL DEFAULT 'GTE',
    ADD COLUMN calculation_base VARCHAR(30) NOT NULL DEFAULT 'TAXABLE_BASE',
    ADD COLUMN threshold_treatment VARCHAR(20) NOT NULL DEFAULT 'FULL_AMOUNT',
    ADD COLUMN decision VARCHAR(30) NOT NULL DEFAULT 'APPLIED',
    ADD COLUMN requires_company_vat_withholding_agent BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN requires_company_ica_withholding_agent BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN legal_reference VARCHAR(250),
    ADD COLUMN source_url VARCHAR(500),
    ADD COLUMN specificity INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN published BOOLEAN NOT NULL DEFAULT true;

UPDATE withholding_rule
SET threshold_value = base_min_amount,
    calculation_base = CASE WHEN withholding_type = 'RETEIVA' THEN 'VAT_AMOUNT' ELSE 'TAXABLE_BASE' END;

-- V008 used VAT responsibility as a provisional proxy. The definitive rule requires VAT withholding-agent status.
UPDATE withholding_rule
SET requires_company_vat_responsible = false,
    requires_company_vat_withholding_agent = true,
    legal_reference = 'Estatuto Tributario, articulos 437-1, 437-2 y 911',
    source_url = 'https://www.secretariasenado.gov.co/senado/basedoc/estatuto_tributario_pr017.html'
WHERE rule_set_version = 'CO-DIAN-2026-RETEIVA-SIMPLE';

ALTER TABLE withholding_rule
    ADD CONSTRAINT ck_withholding_rule_threshold_unit CHECK (threshold_unit IN ('COP', 'UVT')),
    ADD CONSTRAINT ck_withholding_rule_threshold_operator CHECK (threshold_operator IN ('GT', 'GTE')),
    ADD CONSTRAINT ck_withholding_rule_calculation_base
        CHECK (calculation_base IN ('TAXABLE_BASE', 'VAT_AMOUNT', 'COMPANY_INCOME')),
    ADD CONSTRAINT ck_withholding_rule_threshold_treatment
        CHECK (threshold_treatment IN ('FULL_AMOUNT', 'EXCESS')),
    ADD CONSTRAINT ck_withholding_rule_decision
        CHECK (decision IN ('APPLIED', 'NOT_APPLIED', 'EXEMPT', 'BLOCKED')),
    ADD CONSTRAINT ck_withholding_rule_threshold_value CHECK (threshold_value >= 0),
    DROP CONSTRAINT ck_withholding_rule_operation,
    ADD CONSTRAINT ck_withholding_rule_operation
        CHECK (operation_type IN ('PURCHASE', 'EXPENSE', 'PAYMENT', 'PAYROLL', 'RECEIPT'));

CREATE INDEX idx_withholding_rule_precedence
    ON withholding_rule (withholding_type, published, active, priority, specificity);

ALTER TABLE withholding_calculation_snapshot
    ADD COLUMN rule_id UUID,
    ADD COLUMN parameter_version VARCHAR(40),
    ADD COLUMN legal_reference VARCHAR(250),
    ADD COLUMN source_url VARCHAR(500);

-- Reglas nacionales con condiciones inequívocas y vigencia posterior al Decreto 572 de 2025.
INSERT INTO withholding_rule (
    id, company_id, rule_set_version, operation_type, concept_code, withholding_type, base_min_amount, rate,
    requires_company_withholding_agent, requires_company_vat_responsible,
    required_third_party_tax_regime, required_third_party_responsibility, municipality_code, ciiu_code,
    valid_from, valid_to, priority, active, threshold_unit, threshold_value, threshold_operator,
    calculation_base, threshold_treatment, decision, requires_company_vat_withholding_agent,
    requires_company_ica_withholding_agent, legal_reference, source_url, specificity, published)
VALUES
('57220250-0000-4000-8000-000000000001', NULL, 'CO-DUR-572-2025', 'PURCHASE', 'AGRICULTURAL_UNPROCESSED',
 'RETEFUENTE', 0, 0.015000, true, false, NULL, NULL, NULL, NULL, DATE '2025-06-01', NULL, 100, true,
 'UVT', 70, 'GT', 'TAXABLE_BASE', 'FULL_AMOUNT', 'APPLIED', false, false,
 'DUR 1625 de 2016, articulo 1.2.4.6.7, modificado por Decreto 572 de 2025',
 'https://normograma.dian.gov.co/dian/compilacion/docs/decreto_0572_2025.htm', 20, true),
('57220250-0000-4000-8000-000000000002', NULL, 'CO-DUR-572-2025', 'PURCHASE', 'COFFEE_PARCHMENT_CHERRY',
 'RETEFUENTE', 0, 0.005000, true, false, NULL, NULL, NULL, NULL, DATE '2025-06-01', NULL, 100, true,
 'UVT', 70, 'GT', 'TAXABLE_BASE', 'FULL_AMOUNT', 'APPLIED', false, false,
 'DUR 1625 de 2016, articulo 1.2.4.6.8, modificado por Decreto 572 de 2025',
 'https://normograma.dian.gov.co/dian/compilacion/docs/decreto_0572_2025.htm', 20, true),
('57220250-0000-4000-8000-000000000003', NULL, 'CO-DUR-572-2025', 'PURCHASE', 'GOLD_INTERNATIONAL_TRADING',
 'RETEFUENTE', 0, 0.025000, true, false, NULL, NULL, NULL, NULL, DATE '2025-06-01', NULL, 100, true,
 'COP', 0, 'GTE', 'TAXABLE_BASE', 'FULL_AMOUNT', 'APPLIED', false, false,
 'DUR 1625 de 2016, articulo 1.2.4.6.9, modificado por Decreto 572 de 2025',
 'https://normograma.dian.gov.co/dian/compilacion/docs/decreto_0572_2025.htm', 20, true),
('57220250-0000-4000-8000-000000000004', NULL, 'CO-DUR-572-2025', 'EXPENSE', 'SERVICE_GENERAL_DECLARANT',
 'RETEFUENTE', 0, 0.040000, true, false, NULL, 'O-05', NULL, NULL, DATE '2025-06-01', NULL, 100, true,
 'UVT', 2, 'GTE', 'TAXABLE_BASE', 'FULL_AMOUNT', 'APPLIED', false, false,
 'DUR 1625 de 2016, articulos 1.2.4.4.1 y 1.2.4.4.14',
 'https://normograma.dian.gov.co/dian/compilacion/docs/decreto_1625_2016.htm', 20, true),
('57220250-0000-4000-8000-000000000005', NULL, 'CO-DUR-572-2025', 'EXPENSE', 'REAL_ESTATE_LEASE',
 'RETEFUENTE', 0, 0.035000, true, false, NULL, NULL, NULL, NULL, DATE '2025-06-01', NULL, 100, true,
 'UVT', 10, 'GTE', 'TAXABLE_BASE', 'FULL_AMOUNT', 'APPLIED', false, false,
 'DUR 1625 de 2016, articulo 1.2.4.10.6',
 'https://normograma.dian.gov.co/dian/compilacion/docs/decreto_1625_2016.htm', 20, true),
('57220250-0000-4000-8000-000000000006', NULL, 'CO-DUR-572-2025', 'EXPENSE', 'CARGO_TRANSPORT',
 'RETEFUENTE', 0, 0.010000, true, false, NULL, NULL, NULL, NULL, DATE '2025-06-01', NULL, 100, true,
 'UVT', 2, 'GTE', 'TAXABLE_BASE', 'FULL_AMOUNT', 'APPLIED', false, false,
 'DUR 1625 de 2016, articulo 1.2.4.4.8',
 'https://normograma.dian.gov.co/dian/compilacion/docs/decreto_1625_2016.htm', 20, true),
('57220250-0000-4000-8000-000000000007', NULL, 'CO-ET-SIMPLE', 'PURCHASE', 'ANY',
 'RETEFUENTE', 0, 0, true, false, 'SIMPLE', NULL, NULL, NULL, DATE '2025-06-01', NULL, 1, true,
 'COP', 0, 'GTE', 'TAXABLE_BASE', 'FULL_AMOUNT', 'NOT_APPLIED', false, false,
 'Estatuto Tributario, articulo 911',
 'https://www.secretariasenado.gov.co/senado/basedoc/estatuto_tributario_pr017.html', 100, true),
('57220250-0000-4000-8000-000000000008', NULL, 'CO-ET-SIMPLE', 'EXPENSE', 'ANY',
 'RETEFUENTE', 0, 0, true, false, 'SIMPLE', NULL, NULL, NULL, DATE '2025-06-01', NULL, 1, true,
 'COP', 0, 'GTE', 'TAXABLE_BASE', 'FULL_AMOUNT', 'NOT_APPLIED', false, false,
 'Estatuto Tributario, articulo 911',
 'https://www.secretariasenado.gov.co/senado/basedoc/estatuto_tributario_pr017.html', 100, true);
