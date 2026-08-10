ALTER TABLE thirdparty.third_party
    ADD COLUMN IF NOT EXISTS tax_regime VARCHAR(30);

CREATE TABLE IF NOT EXISTS thirdparty.third_party_tax_responsibility (
    third_party_id UUID NOT NULL,
    tax_responsibility_code VARCHAR(20) NOT NULL,
    CONSTRAINT pk_third_party_tax_responsibility PRIMARY KEY (third_party_id, tax_responsibility_code),
    CONSTRAINT fk_third_party_tax_responsibility_party
        FOREIGN KEY (third_party_id) REFERENCES thirdparty.third_party (id),
    CONSTRAINT ck_third_party_tax_responsibility_code
        CHECK (tax_responsibility_code IN ('O-13', 'O-15', 'O-23', 'O-47', 'R-99-PN'))
);

ALTER TABLE thirdparty.third_party
    ADD CONSTRAINT ck_third_party_tax_regime
    CHECK (tax_regime IS NULL OR tax_regime IN ('ORDINARIO', 'SIMPLE', 'RESPONSABLE_IVA', 'NO_RESPONSABLE_IVA'));
