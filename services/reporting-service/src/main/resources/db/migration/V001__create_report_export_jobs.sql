CREATE TABLE report_export_job (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    requested_by_user_id UUID,
    report_code VARCHAR(80) NOT NULL,
    format VARCHAR(12) NOT NULL,
    chart_type VARCHAR(20) NOT NULL,
    from_date DATE,
    to_date DATE,
    filters_json TEXT NOT NULL,
    notify_by_email BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ NOT NULL,
    token_hash VARCHAR(128),
    token_expires_at TIMESTAMPTZ,
    storage_key VARCHAR(500),
    filename VARCHAR(220),
    content_type VARCHAR(120),
    file_size BIGINT,
    failure_message VARCHAR(500),
    notification_status VARCHAR(20) NOT NULL DEFAULT 'NOT_REQUESTED',
    notification_message VARCHAR(500),
    download_attempts INTEGER NOT NULL DEFAULT 0,
    last_downloaded_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_report_export_job_company_status
    ON report_export_job (company_id, status, requested_at DESC);

CREATE INDEX idx_report_export_job_company_user
    ON report_export_job (company_id, requested_by_user_id, requested_at DESC);

CREATE INDEX idx_report_export_job_token_hash
    ON report_export_job (token_hash);

CREATE INDEX idx_report_export_job_expires_at
    ON report_export_job (expires_at);

CREATE TABLE report_export_download_attempt (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES report_export_job (id),
    company_id UUID NOT NULL,
    attempted_at TIMESTAMPTZ NOT NULL,
    result VARCHAR(20) NOT NULL,
    detail VARCHAR(300)
);

CREATE INDEX idx_report_export_download_attempt_job
    ON report_export_download_attempt (job_id, attempted_at DESC);
