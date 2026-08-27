ALTER TABLE accounting_entry
    ADD COLUMN accounting_rule_id UUID;

ALTER TABLE accounting_entry
    ADD CONSTRAINT fk_accounting_entry_rule
        FOREIGN KEY (accounting_rule_id) REFERENCES accounting_rule (id);

CREATE INDEX idx_accounting_entry_rule
    ON accounting_entry (accounting_rule_id);
