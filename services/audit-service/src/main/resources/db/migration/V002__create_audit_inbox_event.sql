CREATE TABLE audit_inbox_event (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    company_id UUID NOT NULL,
    consumer_name VARCHAR(120) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_audit_inbox_event_consumer UNIQUE (event_id, consumer_name)
);

CREATE INDEX idx_audit_inbox_event_company_processed_at
    ON audit_inbox_event (company_id, processed_at DESC);
