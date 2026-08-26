CREATE TABLE IF NOT EXISTS billing.sale_document_type_override (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    sale_id UUID NOT NULL REFERENCES billing.sale(id),
    document_type VARCHAR(40) NOT NULL,
    authorized_by UUID,
    reason VARCHAR(250),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_sale_document_type_override_document_type
        CHECK (document_type IN ('ELECTRONIC_INVOICE', 'ELECTRONIC_POS'))
);

CREATE INDEX IF NOT EXISTS idx_sale_document_type_override_active
    ON billing.sale_document_type_override (company_id, sale_id, active, created_at DESC);
