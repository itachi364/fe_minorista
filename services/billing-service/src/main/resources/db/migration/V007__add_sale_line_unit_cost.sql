ALTER TABLE billing.sale_line
    ADD COLUMN unit_cost NUMERIC(19, 2) NOT NULL DEFAULT 0;
