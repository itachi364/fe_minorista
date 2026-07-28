CREATE TABLE IF NOT EXISTS tenant.company_license (
    id uuid PRIMARY KEY,
    company_id uuid NOT NULL,
    plan_code varchar(60) NOT NULL,
    status varchar(20) NOT NULL,
    valid_from date NOT NULL,
    valid_to date NOT NULL,
    max_users integer,
    max_monthly_documents integer,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uk_tenant_company_license_company UNIQUE (company_id),
    CONSTRAINT fk_tenant_company_license_company FOREIGN KEY (company_id) REFERENCES tenant.company (id),
    CONSTRAINT ck_tenant_company_license_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT ck_tenant_company_license_validity CHECK (valid_to >= valid_from),
    CONSTRAINT ck_tenant_company_license_max_users CHECK (max_users IS NULL OR max_users > 0),
    CONSTRAINT ck_tenant_company_license_max_monthly_documents CHECK (
        max_monthly_documents IS NULL OR max_monthly_documents > 0
    )
);

CREATE INDEX IF NOT EXISTS ix_tenant_company_license_company ON tenant.company_license (company_id);
CREATE INDEX IF NOT EXISTS ix_tenant_company_license_status ON tenant.company_license (status);
