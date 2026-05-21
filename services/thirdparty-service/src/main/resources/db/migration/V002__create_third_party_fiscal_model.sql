CREATE TABLE thirdparty.third_party (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    person_type VARCHAR(20) NOT NULL,
    identification_type_code VARCHAR(20) NOT NULL,
    identification_number VARCHAR(30) NOT NULL,
    verification_digit INTEGER,
    full_name VARCHAR(220),
    business_name VARCHAR(220),
    trade_name VARCHAR(220),
    email VARCHAR(150),
    phone VARCHAR(50),
    address VARCHAR(250),
    municipality_code VARCHAR(20),
    active BOOLEAN NOT NULL,
    CONSTRAINT uk_third_party_company_document UNIQUE (company_id, identification_type_code, identification_number),
    CONSTRAINT ck_third_party_person_type CHECK (person_type IN ('NATURAL', 'JURIDICA')),
    CONSTRAINT ck_third_party_name_by_person CHECK (
        (person_type = 'NATURAL' AND full_name IS NOT NULL)
        OR (person_type = 'JURIDICA' AND business_name IS NOT NULL)
    )
);

CREATE TABLE thirdparty.third_party_role (
    third_party_id UUID NOT NULL,
    role VARCHAR(30) NOT NULL,
    CONSTRAINT pk_third_party_role PRIMARY KEY (third_party_id, role),
    CONSTRAINT fk_third_party_role_party FOREIGN KEY (third_party_id) REFERENCES thirdparty.third_party (id),
    CONSTRAINT ck_third_party_role CHECK (role IN ('CUSTOMER', 'SUPPLIER'))
);

CREATE INDEX idx_third_party_company_active ON thirdparty.third_party (company_id, active);
CREATE INDEX idx_third_party_document ON thirdparty.third_party (company_id, identification_type_code, identification_number);
CREATE INDEX idx_third_party_role_role ON thirdparty.third_party_role (role);
