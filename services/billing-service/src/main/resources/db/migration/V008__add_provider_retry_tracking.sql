ALTER TABLE billing.electronic_document
    ADD COLUMN provider_retry_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN provider_last_retry_at TIMESTAMPTZ;