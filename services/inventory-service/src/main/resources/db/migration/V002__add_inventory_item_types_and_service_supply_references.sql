ALTER TABLE inventory.product
    ADD COLUMN item_type VARCHAR(30) NOT NULL DEFAULT 'PHYSICAL_GOOD',
    ADD COLUMN sale_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN purchase_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN stock_tracked BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE inventory.product
    ADD CONSTRAINT ck_inventory_product_item_type
    CHECK (item_type IN ('PHYSICAL_GOOD', 'SERVICE', 'SUPPLY'));

ALTER TABLE inventory.product
    ADD CONSTRAINT ck_inventory_product_service_no_stock
    CHECK (item_type <> 'SERVICE' OR stock_tracked = FALSE);

CREATE TABLE inventory.service_supply_reference (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    service_product_id UUID NOT NULL,
    supply_product_id UUID NOT NULL,
    notes VARCHAR(300),
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_service_supply_reference_service FOREIGN KEY (service_product_id) REFERENCES inventory.product (id),
    CONSTRAINT fk_service_supply_reference_supply FOREIGN KEY (supply_product_id) REFERENCES inventory.product (id),
    CONSTRAINT uk_service_supply_reference UNIQUE (company_id, service_product_id, supply_product_id),
    CONSTRAINT ck_service_supply_reference_different_products CHECK (service_product_id <> supply_product_id)
);

CREATE INDEX idx_service_supply_reference_company_service
    ON inventory.service_supply_reference (company_id, service_product_id);
