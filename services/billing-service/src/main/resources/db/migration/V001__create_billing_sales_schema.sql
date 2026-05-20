CREATE SCHEMA IF NOT EXISTS billing;

CREATE TABLE billing.sale (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    customer_id UUID,
    payment_method_id UUID,
    sale_channel VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    subtotal NUMERIC(19, 2) NOT NULL,
    discount_total NUMERIC(19, 2) NOT NULL,
    tax_total NUMERIC(19, 2) NOT NULL,
    total NUMERIC(19, 2) NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    confirmed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_billing_sale_idempotency UNIQUE (company_id, idempotency_key)
);

CREATE INDEX idx_billing_sale_company_status
    ON billing.sale (company_id, status);

CREATE TABLE billing.sale_line (
    id UUID PRIMARY KEY,
    sale_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity NUMERIC(19, 4) NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    discount_amount NUMERIC(19, 2) NOT NULL,
    tax_code VARCHAR(50) NOT NULL,
    tax_rate NUMERIC(7, 4) NOT NULL,
    subtotal NUMERIC(19, 2) NOT NULL,
    tax_amount NUMERIC(19, 2) NOT NULL,
    total NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_billing_sale_line_sale
        FOREIGN KEY (sale_id) REFERENCES billing.sale (id),
    CONSTRAINT chk_billing_sale_line_quantity CHECK (quantity > 0),
    CONSTRAINT chk_billing_sale_line_discount CHECK (discount_amount >= 0)
);

CREATE INDEX idx_billing_sale_line_sale
    ON billing.sale_line (sale_id);

CREATE TABLE billing.electronic_document (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    sale_id UUID NOT NULL,
    document_type VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    provider_status VARCHAR(30) NOT NULL,
    prefix VARCHAR(10) NOT NULL,
    document_number BIGINT NOT NULL,
    cufe_cude VARCHAR(200) NOT NULL,
    qr_content TEXT NOT NULL,
    subtotal NUMERIC(19, 2) NOT NULL,
    tax_total NUMERIC(19, 2) NOT NULL,
    total NUMERIC(19, 2) NOT NULL,
    provider_tracking_id VARCHAR(120),
    provider_error_code VARCHAR(80),
    provider_error_message TEXT,
    idempotency_key VARCHAR(120) NOT NULL,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_billing_document_sale
        FOREIGN KEY (sale_id) REFERENCES billing.sale (id),
    CONSTRAINT uk_billing_document_sale UNIQUE (company_id, sale_id),
    CONSTRAINT uk_billing_document_number UNIQUE (company_id, prefix, document_number),
    CONSTRAINT uk_billing_document_idempotency UNIQUE (company_id, idempotency_key)
);

CREATE INDEX idx_billing_document_company_status
    ON billing.electronic_document (company_id, status);
