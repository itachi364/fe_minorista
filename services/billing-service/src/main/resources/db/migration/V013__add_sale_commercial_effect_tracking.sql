ALTER TABLE billing.sale
    ADD COLUMN IF NOT EXISTS inventory_applied_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS accounting_applied_at TIMESTAMPTZ;

ALTER TABLE billing.company_fiscal_policy
    DROP CONSTRAINT IF EXISTS ck_company_fiscal_policy_default_sale_document_type;

ALTER TABLE billing.company_fiscal_policy
    ADD CONSTRAINT ck_company_fiscal_policy_default_sale_document_type
        CHECK (default_sale_document_type IN ('ELECTRONIC_INVOICE', 'ELECTRONIC_POS', 'NON_FISCAL_SALE'));

ALTER TABLE billing.company_fiscal_policy
    ALTER COLUMN default_sale_document_type SET DEFAULT 'NON_FISCAL_SALE';
