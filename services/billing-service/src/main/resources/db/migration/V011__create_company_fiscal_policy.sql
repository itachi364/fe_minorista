CREATE TABLE IF NOT EXISTS billing.company_fiscal_policy (
    company_id UUID PRIMARY KEY,
    default_sale_document_type VARCHAR(40) NOT NULL DEFAULT 'ELECTRONIC_INVOICE',
    allow_document_type_override BOOLEAN NOT NULL DEFAULT TRUE,
    require_pin_for_override BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_company_fiscal_policy_default_sale_document_type
        CHECK (default_sale_document_type IN ('ELECTRONIC_INVOICE', 'ELECTRONIC_POS'))
);
