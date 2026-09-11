ALTER TABLE withholding_rule
    ADD COLUMN trigger_moment VARCHAR(20) NOT NULL DEFAULT 'ACCRUAL',
    ADD COLUMN accumulation_scope VARCHAR(20) NOT NULL DEFAULT 'DAY',
    ADD COLUMN required_third_party_person_type VARCHAR(20),
    ADD COLUMN required_third_party_tax_residency VARCHAR(20),
    ADD COLUMN required_third_party_income_tax_status VARCHAR(20),
    ADD COLUMN required_third_party_self_withholding_scope VARCHAR(100);

UPDATE withholding_rule SET trigger_moment = 'PAYMENT' WHERE operation_type = 'PAYMENT';

ALTER TABLE withholding_rule
    DROP CONSTRAINT ck_withholding_rule_calculation_base,
    ADD CONSTRAINT ck_withholding_rule_calculation_base CHECK
        (calculation_base IN ('TAXABLE_BASE', 'VAT_AMOUNT', 'AIU', 'GROSS_PAYMENT', 'COMPANY_INCOME')),
    DROP CONSTRAINT ck_withholding_rule_threshold_treatment,
    ADD CONSTRAINT ck_withholding_rule_threshold_treatment CHECK
        (threshold_treatment IN ('FULL_AMOUNT', 'EXCESS', 'BRACKETED')),
    ADD CONSTRAINT ck_withholding_rule_trigger_moment CHECK (trigger_moment IN ('ACCRUAL', 'PAYMENT')),
    ADD CONSTRAINT ck_withholding_rule_accumulation_scope CHECK
        (accumulation_scope IN ('OPERATION', 'CONTRACT', 'DAY', 'MONTH', 'YEAR')),
    ADD CONSTRAINT ck_withholding_rule_person_type CHECK
        (required_third_party_person_type IS NULL OR required_third_party_person_type IN ('NATURAL', 'JURIDICA')),
    ADD CONSTRAINT ck_withholding_rule_residency CHECK
        (required_third_party_tax_residency IS NULL OR required_third_party_tax_residency IN ('COLOMBIA', 'EXTERIOR')),
    ADD CONSTRAINT ck_withholding_rule_income_tax_status CHECK
        (required_third_party_income_tax_status IS NULL
         OR required_third_party_income_tax_status IN ('DECLARANTE', 'NO_DECLARANTE', 'NO_APLICA'));

ALTER TABLE fiscal_document_calculation
    ADD COLUMN contract_id UUID,
    ADD COLUMN payment_id UUID;

ALTER TABLE fiscal_accumulation_line
    ADD COLUMN contract_id UUID,
    ADD COLUMN payment_id UUID,
    ADD COLUMN aiu_amount NUMERIC(38, 2) NOT NULL DEFAULT 0,
    ADD COLUMN gross_payment_amount NUMERIC(38, 2) NOT NULL DEFAULT 0;

CREATE INDEX idx_fiscal_accumulation_contract
    ON fiscal_accumulation_line (company_id, third_party_id, contract_id, concept_code, operation_date)
    WHERE reversed_at IS NULL AND contract_id IS NOT NULL;
CREATE INDEX idx_fiscal_accumulation_period
    ON fiscal_accumulation_line (company_id, third_party_id, concept_code, operation_date)
    WHERE reversed_at IS NULL;

CREATE TABLE account_presentation_mapping (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    account_id UUID NOT NULL REFERENCES accounting_account(id),
    financial_reporting_group VARCHAR(20) NOT NULL,
    statement_section VARCHAR(60) NOT NULL,
    presentation_concept VARCHAR(120) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE,
    evidence_reference VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT true,
    updated_by UUID,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_account_presentation_version UNIQUE
        (company_id, account_id, financial_reporting_group, valid_from),
    CONSTRAINT ck_account_presentation_group CHECK
        (financial_reporting_group IN ('GRUPO_1', 'GRUPO_2', 'GRUPO_3')),
    CONSTRAINT ck_account_presentation_period CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX idx_account_presentation_effective ON account_presentation_mapping
    (company_id, financial_reporting_group, active, valid_from, valid_to);

CREATE TABLE national_fiscal_concept_catalog (
    code VARCHAR(80) PRIMARY KEY,
    description VARCHAR(250) NOT NULL,
    withholding_type VARCHAR(30) NOT NULL,
    form_350_section VARCHAR(80),
    operational_status VARCHAR(30) NOT NULL,
    legal_reference VARCHAR(250) NOT NULL,
    source_url VARCHAR(500) NOT NULL,
    reviewed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_national_concept_status CHECK
        (operational_status IN ('VERIFIED_RULE', 'REQUIRES_REVIEW'))
);

INSERT INTO national_fiscal_concept_catalog
    (code, description, withholding_type, form_350_section, operational_status,
     legal_reference, source_url, reviewed_at)
VALUES
('GENERAL_PURCHASE', 'Compras generales', 'RETEFUENTE', 'COMPRAS', 'REQUIRES_REVIEW',
 'DUR 1625 de 2016', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('AGRICULTURAL_UNPROCESSED', 'Productos agricolas o pecuarios sin procesamiento industrial', 'RETEFUENTE', 'COMPRAS', 'VERIFIED_RULE',
 'DUR 1625 de 2016, articulo 1.2.4.6.7', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('COFFEE_PARCHMENT_CHERRY', 'Cafe pergamino o cereza', 'RETEFUENTE', 'COMPRAS', 'VERIFIED_RULE',
 'DUR 1625 de 2016, articulo 1.2.4.6.8', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('SERVICE_GENERAL_DECLARANT', 'Servicios generales a declarantes', 'RETEFUENTE', 'SERVICIOS', 'VERIFIED_RULE',
 'DUR 1625 de 2016', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('FEES', 'Honorarios', 'RETEFUENTE', 'HONORARIOS', 'REQUIRES_REVIEW',
 'DUR 1625 de 2016', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('COMMISSIONS', 'Comisiones', 'RETEFUENTE', 'COMISIONES', 'REQUIRES_REVIEW',
 'DUR 1625 de 2016', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('REAL_ESTATE_LEASE', 'Arrendamiento de bienes inmuebles', 'RETEFUENTE', 'ARRENDAMIENTOS', 'VERIFIED_RULE',
 'DUR 1625 de 2016', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('CARGO_TRANSPORT', 'Transporte de carga', 'RETEFUENTE', 'SERVICIOS', 'VERIFIED_RULE',
 'DUR 1625 de 2016', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('CONTRACT', 'Contratos sujetos a retencion', 'RETEFUENTE', 'OTROS_PAGOS', 'REQUIRES_REVIEW',
 'DUR 1625 de 2016', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('FINANCIAL_YIELD', 'Rendimientos financieros', 'RETEFUENTE', 'RENDIMIENTOS_FINANCIEROS', 'REQUIRES_REVIEW',
 'Estatuto Tributario y DUR 1625 de 2016', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('LABOR_PAYMENT', 'Pagos laborales', 'RETEFUENTE', 'RENTAS_DE_TRABAJO', 'REQUIRES_REVIEW',
 'Estatuto Tributario y DUR 1625 de 2016', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('NON_LABOR_PAYMENT', 'Pagos no laborales', 'RETEFUENTE', 'OTROS_PAGOS', 'REQUIRES_REVIEW',
 'Estatuto Tributario y DUR 1625 de 2016', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('REAL_ESTATE_PURCHASE', 'Enajenacion o adquisicion de inmuebles', 'RETEFUENTE', 'COMPRAS', 'REQUIRES_REVIEW',
 'Estatuto Tributario y DUR 1625 de 2016', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('PAYMENT_ABROAD', 'Pagos o abonos en cuenta al exterior', 'RETEFUENTE', 'PAGOS_EXTERIOR', 'REQUIRES_REVIEW',
 'Estatuto Tributario y DUR 1625 de 2016', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('OTHER_INCOME', 'Otros ingresos tributarios', 'RETEFUENTE', 'OTROS_PAGOS', 'REQUIRES_REVIEW',
 'Estatuto Tributario y DUR 1625 de 2016', 'https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233', now()),
('VAT_WITHHOLDING', 'Retencion en la fuente a titulo de IVA', 'RETEIVA', 'RETENCION_IVA', 'REQUIRES_REVIEW',
 'Estatuto Tributario, articulos 437-1 y 437-2', 'https://normograma.dian.gov.co/dian/compilacion/docs/oficio_dian_9862_2026.htm', now());

ALTER TABLE fiscal_calculation_reversal
    ADD COLUMN compensating_entry_id UUID REFERENCES accounting_entry(id);

ALTER TABLE withholding_certificate
    ADD COLUMN certificate_city VARCHAR(120),
    ADD COLUMN issuer_identification VARCHAR(40),
    ADD COLUMN issuer_name VARCHAR(220),
    ADD COLUMN issuer_address VARCHAR(250),
    ADD COLUMN beneficiary_identification VARCHAR(40),
    ADD COLUMN beneficiary_name VARCHAR(220),
    ADD COLUMN content_type VARCHAR(80) NOT NULL DEFAULT 'text/csv',
    ADD COLUMN private_storage_key VARCHAR(500),
    ADD COLUMN notification_status VARCHAR(30) NOT NULL DEFAULT 'NOT_REQUESTED';

CREATE TABLE fiscal_confirmation_process (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    source_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    calculation_id UUID,
    accounting_entry_id UUID,
    payable_id UUID,
    last_error VARCHAR(500),
    attempt_count INTEGER NOT NULL DEFAULT 1,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_fiscal_confirmation_source UNIQUE (company_id, source_type, source_id),
    CONSTRAINT ck_fiscal_confirmation_status CHECK
        (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'COMPENSATING', 'COMPENSATED'))
);

CREATE VIEW fiscal_form_350_auxiliary AS
SELECT calculation.company_id,
       EXTRACT(YEAR FROM calculation.operation_date)::INTEGER AS fiscal_year,
       EXTRACT(MONTH FROM calculation.operation_date)::INTEGER AS fiscal_month,
       line.concept_code,
       catalog.form_350_section,
       line.withholding_type,
       SUM(line.base_amount) AS base_amount,
       SUM(line.amount) AS withheld_amount,
       COUNT(DISTINCT calculation.id) AS document_count
FROM fiscal_line_calculation_snapshot line
JOIN fiscal_document_calculation calculation ON calculation.id = line.calculation_id
LEFT JOIN fiscal_calculation_reversal reversal ON reversal.calculation_id = calculation.id
LEFT JOIN national_fiscal_concept_catalog catalog ON catalog.code = line.concept_code
WHERE line.decision = 'APPLIED' AND reversal.id IS NULL
GROUP BY calculation.company_id, EXTRACT(YEAR FROM calculation.operation_date),
         EXTRACT(MONTH FROM calculation.operation_date), line.concept_code,
         catalog.form_350_section, line.withholding_type;
