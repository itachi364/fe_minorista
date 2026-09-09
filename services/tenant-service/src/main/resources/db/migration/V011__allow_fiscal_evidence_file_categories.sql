ALTER TABLE tenant.company_file_asset
    DROP CONSTRAINT IF EXISTS ck_company_file_asset_category;

ALTER TABLE tenant.company_file_asset
    ADD CONSTRAINT ck_company_file_asset_category CHECK (category IN (
        'INVOICE',
        'LOGO',
        'BACKGROUND',
        'PURCHASE_EVIDENCE',
        'EXPENSE_EVIDENCE',
        'FISCAL_RULE_EVIDENCE',
        'RUT_EVIDENCE',
        'OTHER'
    ));
