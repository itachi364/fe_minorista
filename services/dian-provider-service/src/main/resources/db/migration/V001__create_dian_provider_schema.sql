CREATE SCHEMA IF NOT EXISTS dian_provider;

CREATE TABLE IF NOT EXISTS dian_provider.provider_submission (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    document_id UUID NOT NULL,
    document_type VARCHAR(40) NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    tracking_id VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL,
    cufe_cude VARCHAR(256),
    qr_content TEXT,
    error_code VARCHAR(80),
    error_message VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    raw_request TEXT,
    raw_response TEXT,
    CONSTRAINT uq_provider_submission_idempotency UNIQUE (company_id, document_id, document_type, idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_provider_submission_company_document
    ON dian_provider.provider_submission (company_id, document_id, document_type);

CREATE UNIQUE INDEX IF NOT EXISTS idx_provider_submission_tracking
    ON dian_provider.provider_submission (tracking_id);
