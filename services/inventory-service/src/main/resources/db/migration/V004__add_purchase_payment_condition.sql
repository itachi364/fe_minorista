ALTER TABLE inventory.purchase
    ADD COLUMN payment_condition VARCHAR(20) NOT NULL DEFAULT 'CASH',
    ADD COLUMN due_date DATE;

ALTER TABLE inventory.purchase
    ADD CONSTRAINT ck_inventory_purchase_payment_condition
    CHECK (payment_condition IN ('CASH', 'CREDIT'));

ALTER TABLE inventory.purchase
    ADD CONSTRAINT ck_inventory_purchase_credit_due_date
    CHECK (payment_condition <> 'CREDIT' OR due_date IS NOT NULL);
