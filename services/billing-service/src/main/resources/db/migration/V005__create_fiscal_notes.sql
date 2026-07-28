CREATE TABLE billing.fiscal_note (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    original_document_id UUID NOT NULL,
    note_type VARCHAR(40) NOT NULL,
    adjustment_kind VARCHAR(30),
    status VARCHAR(30) NOT NULL,
    provider_status VARCHAR(30) NOT NULL,
    reason TEXT NOT NULL,
    prefix VARCHAR(10) NOT NULL,
    document_number BIGINT NOT NULL,
    cufe_cude VARCHAR(200) NOT NULL,
    qr_content TEXT NOT NULL,
    subtotal NUMERIC(19, 2) NOT NULL,
    tax_total NUMERIC(19, 2) NOT NULL,
    total NUMERIC(19, 2) NOT NULL,
    provider_tracking_id VARCHAR(120),
    provider_error_code VARCHAR(80),
    provider_error_message TEXT,
    idempotency_key VARCHAR(120) NOT NULL,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_billing_fiscal_note_original_document
        FOREIGN KEY (original_document_id) REFERENCES billing.electronic_document (id),
    CONSTRAINT uk_billing_fiscal_note_number UNIQUE (company_id, prefix, document_number),
    CONSTRAINT uk_billing_fiscal_note_idempotency UNIQUE (company_id, idempotency_key),
    CONSTRAINT ck_billing_fiscal_note_type CHECK (note_type IN ('CREDIT_NOTE', 'DEBIT_NOTE', 'POS_ADJUSTMENT_NOTE')),
    CONSTRAINT ck_billing_fiscal_note_adjustment_kind CHECK (adjustment_kind IS NULL OR adjustment_kind IN ('CANCELLATION', 'CORRECTION'))
);

CREATE INDEX idx_billing_fiscal_note_company_type
    ON billing.fiscal_note (company_id, note_type);

CREATE INDEX idx_billing_fiscal_note_original_document
    ON billing.fiscal_note (company_id, original_document_id);