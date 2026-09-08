CREATE TABLE tenant.company_tax_profile (
    company_id UUID PRIMARY KEY REFERENCES tenant.company(id) ON DELETE CASCADE,
    company_size VARCHAR(30),
    financial_reporting_group VARCHAR(30),
    tax_regime VARCHAR(60) NOT NULL,
    vat_responsible BOOLEAN NOT NULL,
    withholding_agent BOOLEAN NOT NULL,
    vat_withholding_agent BOOLEAN NOT NULL,
    ica_withholding_agent BOOLEAN NOT NULL,
    large_taxpayer BOOLEAN NOT NULL,
    self_withholding BOOLEAN NOT NULL,
    simple_regime BOOLEAN NOT NULL,
    ica_municipality_code VARCHAR(10),
    updated_by UUID,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE tenant.company_tax_profile_responsibility (
    company_id UUID NOT NULL REFERENCES tenant.company_tax_profile(company_id) ON DELETE CASCADE,
    responsibility_code VARCHAR(30) NOT NULL,
    PRIMARY KEY (company_id, responsibility_code)
);

CREATE TABLE tenant.company_tax_profile_ciiu (
    company_id UUID NOT NULL REFERENCES tenant.company_tax_profile(company_id) ON DELETE CASCADE,
    ciiu_code VARCHAR(10) NOT NULL,
    PRIMARY KEY (company_id, ciiu_code)
);
