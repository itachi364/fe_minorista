CREATE TABLE thirdparty.third_party_fiscal_history (
    id UUID PRIMARY KEY,
    third_party_id UUID NOT NULL REFERENCES thirdparty.third_party(id) ON DELETE CASCADE,
    company_id UUID NOT NULL,
    person_type VARCHAR(20) NOT NULL,
    identification_type_code INTEGER NOT NULL,
    identification_number VARCHAR(30) NOT NULL,
    verification_digit INTEGER,
    full_name VARCHAR(220),
    business_name VARCHAR(220),
    trade_name VARCHAR(220),
    email VARCHAR(150),
    phone VARCHAR(50),
    address VARCHAR(250),
    municipality_code VARCHAR(20),
    ciiu_codes VARCHAR(10)[] NOT NULL DEFAULT '{}',
    tax_responsibilities VARCHAR(20)[] NOT NULL DEFAULT '{}',
    tax_regime VARCHAR(30),
    roles VARCHAR(30)[] NOT NULL DEFAULT '{}',
    active BOOLEAN NOT NULL,
    effective_from TIMESTAMPTZ NOT NULL,
    effective_to TIMESTAMPTZ,
    CONSTRAINT ck_third_party_fiscal_history_period
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX idx_third_party_fiscal_history_effective
    ON thirdparty.third_party_fiscal_history
    (company_id, third_party_id, effective_from DESC, effective_to);

INSERT INTO thirdparty.third_party_fiscal_history (
    id, third_party_id, company_id, person_type, identification_type_code,
    identification_number, verification_digit, full_name, business_name, trade_name,
    email, phone, address, municipality_code, ciiu_codes, tax_responsibilities,
    tax_regime, roles, active, effective_from
)
SELECT gen_random_uuid(), party.id, party.company_id, party.person_type,
       party.identification_type_code, party.identification_number, party.verification_digit,
       party.full_name, party.business_name, party.trade_name, party.email, party.phone,
       party.address, party.municipality_code,
       COALESCE((SELECT array_agg(item.ciiu_code ORDER BY item.ciiu_code)
                 FROM thirdparty.third_party_ciiu item
                 WHERE item.third_party_id = party.id), ARRAY[]::VARCHAR(10)[]),
       COALESCE((SELECT array_agg(item.tax_responsibility_code ORDER BY item.tax_responsibility_code)
                 FROM thirdparty.third_party_tax_responsibility item
                 WHERE item.third_party_id = party.id), ARRAY[]::VARCHAR(20)[]),
       party.tax_regime,
       COALESCE((SELECT array_agg(item.role ORDER BY item.role)
                 FROM thirdparty.third_party_role item
                 WHERE item.third_party_id = party.id), ARRAY[]::VARCHAR(30)[]),
       party.active, CURRENT_TIMESTAMP
FROM thirdparty.third_party party;
