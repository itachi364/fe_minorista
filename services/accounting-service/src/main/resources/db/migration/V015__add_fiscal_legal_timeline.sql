CREATE TABLE fiscal_legal_source (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    title VARCHAR(250) NOT NULL,
    authority VARCHAR(120) NOT NULL,
    official_url VARCHAR(500) NOT NULL,
    issued_on DATE,
    review_due_on DATE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID
);

CREATE TABLE fiscal_legal_source_event (
    id UUID PRIMARY KEY,
    source_id UUID NOT NULL REFERENCES fiscal_legal_source(id),
    event_type VARCHAR(30) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    reference VARCHAR(250) NOT NULL,
    official_url VARCHAR(500) NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT ck_fiscal_legal_event_type
        CHECK (event_type IN ('PUBLISHED', 'EFFECTIVE', 'MODIFIED', 'SUSPENDED', 'REPEALED')),
    CONSTRAINT ck_fiscal_legal_event_validity
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX idx_fiscal_legal_event_effective
    ON fiscal_legal_source_event (source_id, effective_from, effective_to);

CREATE TABLE fiscal_rule_set (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    version VARCHAR(80) NOT NULL,
    scope VARCHAR(30) NOT NULL,
    municipality_code VARCHAR(5),
    legal_source_id UUID NOT NULL REFERENCES fiscal_legal_source(id),
    status VARCHAR(20) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE,
    published_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT uk_fiscal_rule_set_version UNIQUE (code, version),
    CONSTRAINT ck_fiscal_rule_set_scope CHECK (scope IN ('NATIONAL', 'MUNICIPAL', 'COMPANY')),
    CONSTRAINT ck_fiscal_rule_set_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'SUSPENDED', 'EXPIRED')),
    CONSTRAINT ck_fiscal_rule_set_validity CHECK (valid_to IS NULL OR valid_to >= valid_from),
    CONSTRAINT ck_fiscal_rule_set_municipality
        CHECK ((scope = 'MUNICIPAL' AND municipality_code IS NOT NULL)
            OR (scope <> 'MUNICIPAL' AND municipality_code IS NULL))
);

CREATE INDEX idx_fiscal_rule_set_effective
    ON fiscal_rule_set (scope, municipality_code, status, valid_from, valid_to);

ALTER TABLE withholding_rule
    ADD COLUMN legal_source_id UUID REFERENCES fiscal_legal_source(id),
    ADD COLUMN fiscal_rule_set_id UUID REFERENCES fiscal_rule_set(id);

ALTER TABLE withholding_calculation_snapshot
    ADD COLUMN legal_source_event_id UUID REFERENCES fiscal_legal_source_event(id),
    ADD COLUMN legal_status VARCHAR(30),
    ADD COLUMN profile_evidence VARCHAR(1000);

INSERT INTO fiscal_legal_source (
    id, code, title, authority, official_url, issued_on, review_due_on, active)
VALUES (
    '05722025-0000-4000-8000-000000000001',
    'CO-DECRETO-572-2025',
    'Decreto 572 de 2025',
    'Ministerio de Hacienda y Credito Publico',
    'https://normograma.dian.gov.co/dian/compilacion/docs/decreto_0572_2025.htm',
    DATE '2025-05-28', DATE '2026-11-08', true
);

INSERT INTO fiscal_legal_source_event (
    id, source_id, event_type, effective_from, effective_to, reference, official_url, notes)
VALUES
('05722025-0000-4000-8000-000000000011', '05722025-0000-4000-8000-000000000001',
 'PUBLISHED', DATE '2025-05-28', NULL, 'Decreto 572 de 2025',
 'https://normograma.dian.gov.co/dian/compilacion/docs/decreto_0572_2025.htm',
 'Publicacion del decreto.'),
('05722025-0000-4000-8000-000000000012', '05722025-0000-4000-8000-000000000001',
 'EFFECTIVE', DATE '2025-06-01', DATE '2026-05-07', 'Decreto 572 de 2025',
 'https://normograma.dian.gov.co/dian/compilacion/docs/decreto_0572_2025.htm',
 'Vigencia operativa previa a la suspension provisional.'),
('05722025-0000-4000-8000-000000000013', '05722025-0000-4000-8000-000000000001',
 'SUSPENDED', DATE '2026-05-08', NULL, 'Auto del Consejo de Estado del 7 de mayo de 2026',
 'https://www.dian.gov.co/Prensa/Paginas/NG-Comunicado-de-Prensa-070-2026.aspx',
 'Suspension provisional de los articulos 2 a 8; aplican las disposiciones anteriores mientras subsista la medida.'
);

INSERT INTO fiscal_rule_set (
    id, code, version, scope, legal_source_id, status, valid_from, valid_to, reviewed_at, published_at)
VALUES (
    '05722025-0000-4000-8000-000000000021', 'CO-DUR-572-2025', '1', 'NATIONAL',
    '05722025-0000-4000-8000-000000000001', 'SUSPENDED', DATE '2025-06-01', DATE '2026-05-07',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
), (
    '05722025-0000-4000-8000-000000000022', 'CO-DUR-572-2025-AUTORETENCION', '1', 'NATIONAL',
    '05722025-0000-4000-8000-000000000001', 'SUSPENDED', DATE '2025-06-01', DATE '2026-05-07',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

UPDATE withholding_rule
SET valid_to = DATE '2026-05-07',
    legal_source_id = '05722025-0000-4000-8000-000000000001',
    fiscal_rule_set_id = CASE
        WHEN rule_set_version = 'CO-DUR-572-2025-AUTORETENCION'
            THEN '05722025-0000-4000-8000-000000000022'::UUID
        ELSE '05722025-0000-4000-8000-000000000021'::UUID
    END
WHERE rule_set_version LIKE 'CO-DUR-572-2025%'
  AND (valid_to IS NULL OR valid_to > DATE '2026-05-07');
