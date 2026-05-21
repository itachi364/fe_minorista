ALTER TABLE billing.sale_line
    ADD COLUMN product_sku VARCHAR(80),
    ADD COLUMN product_name VARCHAR(160),
    ADD COLUMN item_type VARCHAR(30) NOT NULL DEFAULT 'PHYSICAL_GOOD',
    ADD COLUMN stock_tracked BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE billing.sale_line
    ADD CONSTRAINT ck_billing_sale_line_item_type
    CHECK (item_type IN ('PHYSICAL_GOOD', 'SERVICE', 'SUPPLY'));
