ALTER TABLE billing.electronic_document
    ADD COLUMN inventory_applied_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN accounting_applied_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_billing_document_effects_pending
    ON billing.electronic_document (company_id, status, inventory_applied_at, accounting_applied_at);
