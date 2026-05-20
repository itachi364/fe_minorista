CREATE SCHEMA IF NOT EXISTS tenant;

CREATE TABLE IF NOT EXISTS tenant.company (
    id uuid PRIMARY KEY,
    legal_name varchar(180) NOT NULL,
    trade_name varchar(180),
    identification_type_id uuid NOT NULL,
    identification_number varchar(30) NOT NULL,
    verification_digit varchar(2),
    email varchar(180) NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uk_tenant_company_identification UNIQUE (identification_type_id, identification_number),
    CONSTRAINT ck_tenant_company_status CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);

CREATE INDEX IF NOT EXISTS ix_tenant_company_status ON tenant.company (status);
