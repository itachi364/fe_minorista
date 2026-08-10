ALTER TABLE billing.sale
    ADD COLUMN IF NOT EXISTS payment_method_code VARCHAR(30) NOT NULL DEFAULT 'CASH',
    ADD COLUMN IF NOT EXISTS virtual_wallet_code VARCHAR(50);

ALTER TABLE billing.sale
    ADD CONSTRAINT ck_billing_sale_payment_method_code
    CHECK (payment_method_code IN ('CASH', 'DEBIT_CARD', 'CREDIT_CARD', 'BREB_KEY', 'BANK_TRANSFER', 'VIRTUAL_WALLET'));

ALTER TABLE billing.sale
    ADD CONSTRAINT ck_billing_sale_virtual_wallet_code
    CHECK (
        (payment_method_code = 'VIRTUAL_WALLET'
            AND virtual_wallet_code IN ('NEQUI', 'DAVIPLATA', 'MOVII', 'DALE', 'RAPPIPAY', 'POWWI',
                'CFA_EXPRESS', 'AV_VILLAS_DIGITAL_DEPOSIT', 'MOSI', 'BBVA_DINERO_MOVIL'))
        OR (payment_method_code <> 'VIRTUAL_WALLET' AND virtual_wallet_code IS NULL)
    );
