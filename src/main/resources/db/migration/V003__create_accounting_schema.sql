CREATE TABLE accounting_account (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(40) NOT NULL,
    level VARCHAR(40) NOT NULL,
    nature VARCHAR(20) NOT NULL,
    parent_account_id UUID,
    active BOOLEAN NOT NULL,
    CONSTRAINT uk_accounting_account_company_code UNIQUE (company_id, code),
    CONSTRAINT fk_accounting_account_parent
        FOREIGN KEY (parent_account_id) REFERENCES accounting_account (id)
);

CREATE INDEX idx_accounting_account_company_active
    ON accounting_account (company_id, active);

CREATE TABLE accounting_rule (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    name VARCHAR(200) NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE UNIQUE INDEX uk_accounting_rule_company_event_active
    ON accounting_rule (company_id, event_type)
    WHERE active = true;

CREATE TABLE accounting_rule_line (
    id UUID PRIMARY KEY,
    rule_id UUID NOT NULL,
    line_order INTEGER NOT NULL,
    account_code VARCHAR(30) NOT NULL,
    side VARCHAR(20) NOT NULL,
    amount_type VARCHAR(30) NOT NULL,
    description VARCHAR(250),
    CONSTRAINT fk_accounting_rule_line_rule
        FOREIGN KEY (rule_id) REFERENCES accounting_rule (id)
);

CREATE INDEX idx_accounting_rule_line_rule_order
    ON accounting_rule_line (rule_id, line_order);

CREATE TABLE accounting_entry (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    entry_date DATE NOT NULL,
    description VARCHAR(250) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    source_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    debit_total NUMERIC(38, 2) NOT NULL,
    credit_total NUMERIC(38, 2) NOT NULL,
    CONSTRAINT uk_accounting_entry_company_source UNIQUE (company_id, source_type, source_id)
);

CREATE INDEX idx_accounting_entry_company_date_status
    ON accounting_entry (company_id, entry_date, status);

CREATE TABLE accounting_entry_line (
    id UUID PRIMARY KEY,
    entry_id UUID NOT NULL,
    line_order INTEGER NOT NULL,
    account_id UUID NOT NULL,
    account_code VARCHAR(30) NOT NULL,
    account_name VARCHAR(200) NOT NULL,
    thirdparty_id UUID,
    debit_amount NUMERIC(38, 2) NOT NULL,
    credit_amount NUMERIC(38, 2) NOT NULL,
    description VARCHAR(250),
    CONSTRAINT fk_accounting_entry_line_entry
        FOREIGN KEY (entry_id) REFERENCES accounting_entry (id),
    CONSTRAINT fk_accounting_entry_line_account
        FOREIGN KEY (account_id) REFERENCES accounting_account (id),
    CONSTRAINT chk_accounting_entry_line_single_side
        CHECK (
            (debit_amount > 0 AND credit_amount = 0)
            OR (debit_amount = 0 AND credit_amount > 0)
        )
);

CREATE INDEX idx_accounting_entry_line_entry_order
    ON accounting_entry_line (entry_id, line_order);

CREATE INDEX idx_accounting_entry_line_account
    ON accounting_entry_line (account_id);
