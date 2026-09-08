ALTER TABLE withholding_rule
    ADD COLUMN target_third_party_id UUID,
    ADD COLUMN evidence_reference VARCHAR(500),
    ADD CONSTRAINT ck_withholding_rule_target_exemption
        CHECK (target_third_party_id IS NULL
            OR (decision = 'EXEMPT' AND evidence_reference IS NOT NULL AND length(trim(evidence_reference)) > 0));

CREATE INDEX idx_withholding_rule_target_third_party
    ON withholding_rule (company_id, target_third_party_id, withholding_type, active, valid_from, valid_to)
    WHERE target_third_party_id IS NOT NULL;
