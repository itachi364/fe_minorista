CREATE SCHEMA IF NOT EXISTS inventory;

CREATE TABLE inventory.product (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    sku VARCHAR(80) NOT NULL,
    barcode VARCHAR(80),
    name VARCHAR(160) NOT NULL,
    description VARCHAR(500),
    sale_price NUMERIC(19, 2) NOT NULL,
    cost NUMERIC(19, 2) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_inventory_product_company_sku UNIQUE (company_id, sku),
    CONSTRAINT uk_inventory_product_company_barcode UNIQUE (company_id, barcode)
);

CREATE TABLE inventory.stock_balance (
    company_id UUID NOT NULL,
    product_id UUID NOT NULL,
    current_stock NUMERIC(19, 4) NOT NULL,
    reserved_stock NUMERIC(19, 4) NOT NULL,
    average_cost NUMERIC(19, 2) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (company_id, product_id),
    CONSTRAINT fk_stock_balance_product FOREIGN KEY (product_id) REFERENCES inventory.product (id),
    CONSTRAINT ck_stock_balance_non_negative CHECK (current_stock >= 0 AND reserved_stock >= 0)
);

CREATE TABLE inventory.inventory_movement (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    product_id UUID NOT NULL,
    movement_type VARCHAR(30) NOT NULL,
    quantity NUMERIC(19, 4) NOT NULL,
    unit_cost NUMERIC(19, 2) NOT NULL,
    previous_stock NUMERIC(19, 4) NOT NULL,
    resulting_stock NUMERIC(19, 4) NOT NULL,
    source_document_type VARCHAR(40) NOT NULL,
    source_document_id UUID NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    created_by UUID,
    movement_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_inventory_movement_product FOREIGN KEY (product_id) REFERENCES inventory.product (id),
    CONSTRAINT uk_inventory_movement_idempotency UNIQUE (company_id, source_document_type, source_document_id, movement_type, idempotency_key),
    CONSTRAINT ck_inventory_movement_type CHECK (movement_type IN ('PURCHASE_IN', 'SALE_OUT', 'RETURN_IN', 'ADJUSTMENT_IN', 'ADJUSTMENT_OUT')),
    CONSTRAINT ck_inventory_source_type CHECK (source_document_type IN ('PURCHASE', 'SALE', 'RETURN', 'ADJUSTMENT', 'INITIAL_STOCK'))
);

CREATE TABLE inventory.purchase (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    supplier_id UUID,
    status VARCHAR(20) NOT NULL,
    subtotal NUMERIC(19, 2) NOT NULL,
    tax_total NUMERIC(19, 2) NOT NULL,
    total NUMERIC(19, 2) NOT NULL,
    evidence_url VARCHAR(500),
    idempotency_key VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    confirmed_at TIMESTAMPTZ,
    CONSTRAINT uk_inventory_purchase_idempotency UNIQUE (company_id, idempotency_key),
    CONSTRAINT ck_inventory_purchase_status CHECK (status IN ('PENDING', 'CONFIRMED'))
);

CREATE TABLE inventory.purchase_line (
    id UUID PRIMARY KEY,
    purchase_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity NUMERIC(19, 4) NOT NULL,
    unit_cost NUMERIC(19, 2) NOT NULL,
    subtotal NUMERIC(19, 2) NOT NULL,
    tax NUMERIC(19, 2) NOT NULL,
    total NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_purchase_line_purchase FOREIGN KEY (purchase_id) REFERENCES inventory.purchase (id),
    CONSTRAINT fk_purchase_line_product FOREIGN KEY (product_id) REFERENCES inventory.product (id)
);

CREATE INDEX idx_inventory_product_company ON inventory.product (company_id);
CREATE INDEX idx_inventory_movement_company_product ON inventory.inventory_movement (company_id, product_id, movement_at);
CREATE INDEX idx_inventory_purchase_company_status ON inventory.purchase (company_id, status);
