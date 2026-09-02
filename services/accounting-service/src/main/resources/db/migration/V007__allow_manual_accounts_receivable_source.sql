ALTER TABLE accounting_accounts_receivable
    DROP CONSTRAINT ck_accounting_accounts_receivable_source_type;

ALTER TABLE accounting_accounts_receivable
    ADD CONSTRAINT ck_accounting_accounts_receivable_source_type
    CHECK (source_type IN ('SALE', 'ELECTRONIC_INVOICE', 'ELECTRONIC_POS', 'OPENING_BALANCE', 'ADJUSTMENT'));
