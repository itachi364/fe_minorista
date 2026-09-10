ALTER TABLE fiscal_legal_source_event
    DROP CONSTRAINT ck_fiscal_legal_event_type;

ALTER TABLE fiscal_legal_source_event
    ADD CONSTRAINT ck_fiscal_legal_event_type
        CHECK (event_type IN ('PUBLISHED', 'EFFECTIVE', 'MODIFIED', 'SUSPENDED', 'REACTIVATED', 'REPEALED'));
