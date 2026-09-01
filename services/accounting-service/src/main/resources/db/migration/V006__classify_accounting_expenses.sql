ALTER TABLE accounting_expense
    ADD COLUMN expense_type VARCHAR(40) NOT NULL DEFAULT 'OPERATING_EXPENSE';

ALTER TABLE accounting_expense
    ADD CONSTRAINT ck_accounting_expense_type
    CHECK (expense_type IN ('OPERATING_EXPENSE', 'ASSET_PURCHASE'));
