CREATE TABLE fiscal_rule_import (
    id UUID PRIMARY KEY,
    file_name VARCHAR(250) NOT NULL,
    file_sha256 VARCHAR(64) NOT NULL,
    row_count INTEGER NOT NULL,
    package_count INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_detail VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT ck_fiscal_rule_import_status CHECK (status IN ('VALIDATED', 'IMPORTED', 'REJECTED')),
    CONSTRAINT ck_fiscal_rule_import_counts CHECK (row_count >= 0 AND package_count >= 0)
);

CREATE INDEX idx_fiscal_rule_import_created_at ON fiscal_rule_import (created_at DESC);

ALTER TABLE fiscal_rule_set
    ADD COLUMN import_id UUID REFERENCES fiscal_rule_import(id),
    ADD COLUMN municipality_name VARCHAR(160);

CREATE UNIQUE INDEX uk_fiscal_rule_set_active_municipal_version
    ON fiscal_rule_set (municipality_code, code, version)
    WHERE scope = 'MUNICIPAL';

CREATE INDEX idx_withholding_rule_municipal_package
    ON withholding_rule (fiscal_rule_set_id, municipality_code, published, active);
