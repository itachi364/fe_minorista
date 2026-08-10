ALTER TABLE inventory.product
    ADD COLUMN IF NOT EXISTS tax_category_code VARCHAR(40),
    ADD COLUMN IF NOT EXISTS tax_code VARCHAR(80),
    ADD COLUMN IF NOT EXISTS tax_label VARCHAR(180),
    ADD COLUMN IF NOT EXISTS tax_rate NUMERIC(7, 4);

UPDATE inventory.product
SET
    tax_category_code = COALESCE(tax_category_code, 'IVA'),
    tax_code = COALESCE(tax_code, 'IVA_19'),
    tax_label = COALESCE(tax_label, 'IVA 19%'),
    tax_rate = COALESCE(tax_rate, 19)
WHERE tax_code IS NULL
   OR tax_label IS NULL
   OR tax_rate IS NULL
   OR tax_category_code IS NULL;

ALTER TABLE inventory.product
    ALTER COLUMN tax_category_code SET NOT NULL,
    ALTER COLUMN tax_code SET NOT NULL,
    ALTER COLUMN tax_label SET NOT NULL,
    ALTER COLUMN tax_rate SET NOT NULL;

ALTER TABLE inventory.product
    ADD CONSTRAINT ck_inventory_product_tax_rate_non_negative CHECK (tax_rate >= 0);

CREATE INDEX IF NOT EXISTS idx_inventory_product_company_barcode_active
    ON inventory.product (company_id, barcode, active);
