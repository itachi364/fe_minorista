CREATE TABLE billing_issuer_profile (
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
    ON billing_issuer_profile (company_id, active);

CREATE TABLE billing_numbering_resolution (
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
    ON billing_numbering_resolution (company_id, document_type, environment, active, valid_from, valid_to);

CREATE TABLE billing_electronic_pos_document (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    sale_id UUID,
    buyer_name VARCHAR(200),
    buyer_document_type VARCHAR(30),
    buyer_document_number VARCHAR(50),
    prefix VARCHAR(4) NOT NULL,
    document_number BIGINT NOT NULL,
    cude VARCHAR(200) NOT NULL,
    subtotal NUMERIC(38, 2) NOT NULL,
    tax_total NUMERIC(38, 2) NOT NULL,
    total NUMERIC(38, 2) NOT NULL,
    status VARCHAR(40) NOT NULL,
    issue_at TIMESTAMP WITH TIME ZONE NOT NULL,
    provider_submission_id VARCHAR(120),
    provider_cufe_cude VARCHAR(200),
    provider_qr_content TEXT,
    provider_xml_content TEXT,
    provider_graphic_representation_content TEXT,
    provider_error_code VARCHAR(80),
    provider_error_message TEXT,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_billing_pos_company_fiscal_number UNIQUE (company_id, prefix, document_number)
);

CREATE INDEX idx_billing_electronic_pos_company_status
    ON billing_electronic_pos_document (company_id, status);

CREATE TABLE billing_electronic_pos_document_line (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    product_id UUID,
    quantity NUMERIC(38, 6) NOT NULL,
    unit_price NUMERIC(38, 2) NOT NULL,
    discount_amount NUMERIC(38, 2) NOT NULL,
    tax_code VARCHAR(50) NOT NULL,
    tax_rate NUMERIC(9, 4) NOT NULL,
    gross_amount NUMERIC(38, 2) NOT NULL,
    taxable_amount NUMERIC(38, 2) NOT NULL,
    tax_amount NUMERIC(38, 2) NOT NULL,
    line_total NUMERIC(38, 2) NOT NULL,
    CONSTRAINT fk_billing_pos_line_document
        FOREIGN KEY (document_id) REFERENCES billing_electronic_pos_document (id)
);

CREATE INDEX idx_billing_electronic_pos_line_document
    ON billing_electronic_pos_document_line (document_id);

CREATE TABLE billing_provider_submission (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    document_id UUID NOT NULL,
    document_type VARCHAR(40) NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    request_payload_hash VARCHAR(128) NOT NULL,
    status VARCHAR(30) NOT NULL,
    provider_submission_id VARCHAR(120),
    cufe_cude VARCHAR(200),
    qr_content TEXT,
    xml_content TEXT,
    graphic_representation_content TEXT,
    error_code VARCHAR(80),
    error_message TEXT,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_billing_provider_submission_document
    ON billing_provider_submission (company_id, document_id);

CREATE TABLE billing_electronic_document_trace_event (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    document_id UUID NOT NULL,
    previous_status VARCHAR(40) NOT NULL,
    new_status VARCHAR(40) NOT NULL,
    action VARCHAR(60) NOT NULL,
    result VARCHAR(30),
    detail TEXT,
    user_id UUID,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_billing_trace_document
    ON billing_electronic_document_trace_event (company_id, document_id, occurred_at);

CREATE TABLE billing_fiscal_audit_event (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    resource_id UUID NOT NULL,
    resource_type VARCHAR(80) NOT NULL,
    action VARCHAR(80) NOT NULL,
    result VARCHAR(80) NOT NULL,
    user_id UUID,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    detail TEXT
);

CREATE INDEX idx_billing_fiscal_audit_resource
    ON billing_fiscal_audit_event (company_id, resource_type, resource_id, occurred_at);
