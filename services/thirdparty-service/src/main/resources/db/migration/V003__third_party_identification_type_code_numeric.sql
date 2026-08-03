ALTER TABLE thirdparty.third_party DROP CONSTRAINT IF EXISTS uk_third_party_company_document;
DROP INDEX IF EXISTS thirdparty.idx_third_party_document;

ALTER TABLE thirdparty.third_party ADD COLUMN IF NOT EXISTS identification_type_code_numeric INTEGER;

UPDATE thirdparty.third_party
SET identification_type_code_numeric = CASE UPPER(TRIM(identification_type_code))
    WHEN 'NIT' THEN 31
    WHEN 'CC' THEN 13
    WHEN 'CE' THEN 22
    WHEN 'TI' THEN 12
    WHEN 'RC' THEN 11
    WHEN 'TE' THEN 21
    WHEN 'PA' THEN 41
    WHEN 'PASAPORTE' THEN 41
    WHEN 'PEP' THEN 47
    WHEN 'PPT' THEN 48
    ELSE CASE WHEN TRIM(identification_type_code) ~ '^[0-9]+$' THEN TRIM(identification_type_code)::INTEGER END
END
WHERE identification_type_code_numeric IS NULL;

ALTER TABLE thirdparty.third_party DROP COLUMN identification_type_code;
ALTER TABLE thirdparty.third_party RENAME COLUMN identification_type_code_numeric TO identification_type_code;
ALTER TABLE thirdparty.third_party ALTER COLUMN identification_type_code SET NOT NULL;

ALTER TABLE thirdparty.third_party ADD CONSTRAINT ck_third_party_identification_type_code
    CHECK (identification_type_code IN (11, 12, 13, 21, 22, 31, 41, 42, 43, 47, 48));

ALTER TABLE thirdparty.third_party ADD CONSTRAINT uk_third_party_company_document
    UNIQUE (company_id, identification_type_code, identification_number);

CREATE INDEX idx_third_party_document
    ON thirdparty.third_party (company_id, identification_type_code, identification_number);

