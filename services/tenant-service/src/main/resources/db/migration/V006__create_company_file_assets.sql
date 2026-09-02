CREATE TABLE tenant.company_file_asset (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    category VARCHAR(40) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_hash VARCHAR(128) NOT NULL,
    uploaded_by UUID,
    uploaded_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_company_file_asset_company FOREIGN KEY (company_id) REFERENCES tenant.company (id),
    CONSTRAINT ck_company_file_asset_category CHECK (category IN ('INVOICE', 'LOGO', 'BACKGROUND', 'PURCHASE_EVIDENCE', 'EXPENSE_EVIDENCE', 'OTHER')),
    CONSTRAINT ck_company_file_asset_size CHECK (file_size > 0)
);

CREATE INDEX idx_company_file_asset_company_category
    ON tenant.company_file_asset (company_id, category, uploaded_at DESC);
