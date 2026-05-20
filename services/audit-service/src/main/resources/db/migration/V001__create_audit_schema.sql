CREATE TABLE audit_event (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    user_id UUID,
    event_type VARCHAR(80) NOT NULL,
    resource_type VARCHAR(80) NOT NULL,
    resource_id VARCHAR(120),
    action VARCHAR(80) NOT NULL,
    result VARCHAR(40) NOT NULL,
    detail JSONB,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_audit_event_company_occurred_at
    ON audit_event (company_id, occurred_at DESC);

CREATE INDEX idx_audit_event_company_resource
    ON audit_event (company_id, resource_type, resource_id);

CREATE INDEX idx_audit_event_company_user
    ON audit_event (company_id, user_id);
