CREATE TABLE accounting_expense (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    supplier_id UUID,
    expense_date DATE NOT NULL,
    concept VARCHAR(250) NOT NULL,
    subtotal NUMERIC(38, 2) NOT NULL,
    tax_total NUMERIC(38, 2) NOT NULL,
    total NUMERIC(38, 2) NOT NULL,
    payment_condition VARCHAR(20) NOT NULL,
    due_date DATE,
    evidence_url VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    confirmed_at TIMESTAMPTZ,
    CONSTRAINT uk_accounting_expense_idempotency UNIQUE (company_id, idempotency_key),
    CONSTRAINT ck_accounting_expense_status CHECK (status IN ('PENDING', 'CONFIRMED')),
    CONSTRAINT ck_accounting_expense_payment_condition CHECK (payment_condition IN ('CASH', 'CREDIT')),
    CONSTRAINT ck_accounting_expense_credit_due_date CHECK (payment_condition <> 'CREDIT' OR due_date IS NOT NULL)
);

CREATE INDEX idx_accounting_expense_company_status_date
    ON accounting_expense (company_id, status, expense_date);

CREATE TABLE accounting_accounts_payable (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    supplier_id UUID,
    source_type VARCHAR(40) NOT NULL,
    source_id UUID NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    total_amount NUMERIC(38, 2) NOT NULL,
    paid_amount NUMERIC(38, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_accounting_accounts_payable_source UNIQUE (company_id, source_type, source_id),
    CONSTRAINT ck_accounting_accounts_payable_status CHECK (status IN ('OPEN', 'PARTIALLY_PAID', 'PAID')),
    CONSTRAINT ck_accounting_accounts_payable_source_type CHECK (source_type IN ('PURCHASE', 'EXPENSE')),
    CONSTRAINT ck_accounting_accounts_payable_amounts CHECK (
        total_amount > 0
        AND paid_amount >= 0
        AND paid_amount <= total_amount
    )
);

CREATE INDEX idx_accounting_accounts_payable_company_status_due
    ON accounting_accounts_payable (company_id, status, due_date);

CREATE INDEX idx_accounting_accounts_payable_company_supplier
    ON accounting_accounts_payable (company_id, supplier_id);

CREATE TABLE accounting_accounts_payable_payment (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    accounts_payable_id UUID NOT NULL,
    payment_date DATE NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    payment_method VARCHAR(80) NOT NULL,
    reference VARCHAR(120),
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_accounting_payable_payment_payable
        FOREIGN KEY (accounts_payable_id) REFERENCES accounting_accounts_payable (id),
    CONSTRAINT ck_accounting_payable_payment_amount CHECK (amount > 0)
);

CREATE INDEX idx_accounting_payable_payment_payable_date
    ON accounting_accounts_payable_payment (accounts_payable_id, payment_date);
