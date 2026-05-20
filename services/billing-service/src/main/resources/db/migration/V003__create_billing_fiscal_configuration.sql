CREATE TABLE billing.issuer_profile (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    legal_name VARCHAR(200) NOT NULL,
    nit VARCHAR(30) NOT NULL,
    verification_digit VARCHAR(5) NOT NULL,
    tax_responsibilities TEXT,
    municipality_code VARCHAR(20),
    address VARCHAR(250),
    active BOOLEAN NOT NULL
);

CREATE INDEX idx_billing_issuer_profile_company_active
    ON billing.issuer_profile (company_id, active);

CREATE TABLE billing.numbering_resolution (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    document_type VARCHAR(40) NOT NULL,
    resolution_number VARCHAR(80) NOT NULL,
    prefix VARCHAR(4) NOT NULL,
    from_number BIGINT NOT NULL,
    to_number BIGINT NOT NULL,
    current_number BIGINT NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    environment VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE INDEX idx_billing_numbering_resolution_lookup
    ON billing.numbering_resolution (company_id, document_type, environment, active, valid_from, valid_to);
