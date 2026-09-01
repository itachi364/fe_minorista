ALTER TABLE inventory.purchase_line
    ALTER COLUMN product_id DROP NOT NULL;

ALTER TABLE inventory.purchase_line
    ADD COLUMN description VARCHAR(300);

UPDATE inventory.purchase_line
SET description = 'Concepto de compra registrado'
WHERE description IS NULL;
