CREATE TABLE IF NOT EXISTS dian_provider.dian_submission_event (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    document_id UUID NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    dian_code VARCHAR(80),
    dian_message VARCHAR(500),
    correlation_id VARCHAR(120),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_dian_submission_event_company_submission
    ON dian_provider.dian_submission_event (company_id, submission_id, created_at);

CREATE TABLE IF NOT EXISTS dian_provider.dian_submission_artifact (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    document_id UUID NOT NULL,
    artifact_type VARCHAR(40) NOT NULL,
    storage_bucket_reference VARCHAR(180),
    storage_key VARCHAR(500) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    file_name VARCHAR(220) NOT NULL,
    content_hash VARCHAR(120) NOT NULL,
    size_bytes BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID
);

CREATE INDEX IF NOT EXISTS idx_dian_submission_artifact_company_submission
    ON dian_provider.dian_submission_artifact (company_id, submission_id, artifact_type);

CREATE TABLE IF NOT EXISTS dian_provider.dian_technical_validation_result (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    document_id UUID NOT NULL,
    validation_type VARCHAR(40) NOT NULL,
    result VARCHAR(20) NOT NULL,
    rule_code VARCHAR(120),
    message VARCHAR(500),
    source_version VARCHAR(80) NOT NULL,
    validated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_dian_technical_validation_company_submission
    ON dian_provider.dian_technical_validation_result (company_id, submission_id, validation_type);
