CREATE TABLE IF NOT EXISTS dian_provider.dian_company_configuration (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL UNIQUE,
    mode VARCHAR(20) NOT NULL,
    environment VARCHAR(20) NOT NULL,
    software_id VARCHAR(120),
    software_pin_secret_ref VARCHAR(500),
    technical_key_secret_ref VARCHAR(500),
    certificate_secret_ref VARCHAR(500),
    certificate_alias VARCHAR(180),
    certificate_fingerprint VARCHAR(180),
    certificate_expires_at TIMESTAMP WITH TIME ZONE,
    service_base_url VARCHAR(500),
    test_set_id VARCHAR(120),
    accepted_responsibility BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL,
    last_test_status VARCHAR(30) NOT NULL,
    last_test_at TIMESTAMP WITH TIME ZONE,
    last_test_message VARCHAR(500),
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_dian_company_configuration_status
    ON dian_provider.dian_company_configuration (status);

CREATE INDEX IF NOT EXISTS idx_dian_company_configuration_company_status
    ON dian_provider.dian_company_configuration (company_id, status);
