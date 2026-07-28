CREATE TABLE accounting_outbox_event (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(120) NOT NULL,
    event_version INTEGER NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    company_id UUID NOT NULL,
    aggregate_type VARCHAR(120) NOT NULL,
    aggregate_id UUID NOT NULL,
    producer VARCHAR(120) NOT NULL,
    correlation_id VARCHAR(120),
    idempotency_key VARCHAR(180) NOT NULL,
    payload_json TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    publish_attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_accounting_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_accounting_outbox_pending
    ON accounting_outbox_event (status, occurred_at);

CREATE TABLE accounting_inbox_event (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    company_id UUID NOT NULL,
    consumer VARCHAR(120) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_accounting_inbox_event_consumer UNIQUE (event_id, consumer)
);

CREATE INDEX idx_accounting_inbox_company
    ON accounting_inbox_event (company_id, processed_at);
