CREATE TABLE tenant.company_tax_profile_history (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES tenant.company(id) ON DELETE CASCADE,
    company_size VARCHAR(30),
    financial_reporting_group VARCHAR(30),
    tax_regime VARCHAR(60) NOT NULL,
    rut_responsibilities VARCHAR(30)[] NOT NULL DEFAULT '{}',
    vat_responsible BOOLEAN NOT NULL,
    withholding_agent BOOLEAN NOT NULL,
    vat_withholding_agent BOOLEAN NOT NULL,
    ica_withholding_agent BOOLEAN NOT NULL,
    large_taxpayer BOOLEAN NOT NULL,
    self_withholding BOOLEAN NOT NULL,
    simple_regime BOOLEAN NOT NULL,
    ica_municipality_code VARCHAR(10),
    ciiu_codes VARCHAR(10)[] NOT NULL DEFAULT '{}',
    updated_by UUID,
    effective_from TIMESTAMPTZ NOT NULL,
    effective_to TIMESTAMPTZ,
    CONSTRAINT ck_company_tax_profile_history_period
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX idx_company_tax_profile_history_effective
    ON tenant.company_tax_profile_history (company_id, effective_from DESC, effective_to);

INSERT INTO tenant.company_tax_profile_history (
    id, company_id, company_size, financial_reporting_group, tax_regime,
    rut_responsibilities, vat_responsible, withholding_agent, vat_withholding_agent,
    ica_withholding_agent, large_taxpayer, self_withholding, simple_regime,
    ica_municipality_code, ciiu_codes, updated_by, effective_from
)
SELECT gen_random_uuid(), profile.company_id, profile.company_size,
       profile.financial_reporting_group, profile.tax_regime,
       COALESCE((SELECT array_agg(item.responsibility_code ORDER BY item.responsibility_code)
                 FROM tenant.company_tax_profile_responsibility item
                 WHERE item.company_id = profile.company_id), ARRAY[]::VARCHAR(30)[]),
       profile.vat_responsible, profile.withholding_agent, profile.vat_withholding_agent,
       profile.ica_withholding_agent, profile.large_taxpayer, profile.self_withholding,
       profile.simple_regime, profile.ica_municipality_code,
       COALESCE((SELECT array_agg(item.ciiu_code ORDER BY item.ciiu_code)
                 FROM tenant.company_tax_profile_ciiu item
                 WHERE item.company_id = profile.company_id), ARRAY[]::VARCHAR(10)[]),
       profile.updated_by, profile.updated_at
FROM tenant.company_tax_profile profile;
