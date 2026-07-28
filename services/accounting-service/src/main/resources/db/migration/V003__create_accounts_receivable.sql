CREATE TABLE accounting_accounts_receivable (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    source_id UUID NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    total_amount NUMERIC(38, 2) NOT NULL,
    paid_amount NUMERIC(38, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_accounting_accounts_receivable_source UNIQUE (company_id, source_type, source_id),
    CONSTRAINT uk_accounting_accounts_receivable_idempotency UNIQUE (company_id, idempotency_key),
    CONSTRAINT ck_accounting_accounts_receivable_status CHECK (status IN ('OPEN', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'CANCELLED')),
    CONSTRAINT ck_accounting_accounts_receivable_source_type CHECK (source_type IN ('SALE', 'ELECTRONIC_INVOICE', 'ELECTRONIC_POS', 'OPENING_BALANCE')),
    CONSTRAINT ck_accounting_accounts_receivable_dates CHECK (due_date >= issue_date),
    CONSTRAINT ck_accounting_accounts_receivable_amounts CHECK (
        total_amount > 0
        AND paid_amount >= 0
        AND paid_amount <= total_amount
    )
);

CREATE INDEX idx_accounting_accounts_receivable_company_status_due
    ON accounting_accounts_receivable (company_id, status, due_date);

CREATE INDEX idx_accounting_accounts_receivable_company_customer
    ON accounting_accounts_receivable (company_id, customer_id);

CREATE TABLE accounting_accounts_receivable_payment (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    accounts_receivable_id UUID NOT NULL,
    payment_date DATE NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    payment_method VARCHAR(80) NOT NULL,
    reference VARCHAR(120),
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_accounting_receivable_payment_receivable
        FOREIGN KEY (accounts_receivable_id) REFERENCES accounting_accounts_receivable (id),
    CONSTRAINT ck_accounting_receivable_payment_amount CHECK (amount > 0)
);

CREATE INDEX idx_accounting_receivable_payment_receivable_date
    ON accounting_accounts_receivable_payment (accounts_receivable_id, payment_date);