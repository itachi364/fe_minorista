ALTER TABLE billing.sale
    ADD COLUMN IF NOT EXISTS buyer_identification_mode VARCHAR(30);

UPDATE billing.sale
SET buyer_identification_mode = CASE
    WHEN customer_id IS NULL THEN 'FINAL_CONSUMER'
    ELSE 'IDENTIFIED_CUSTOMER'
END
WHERE buyer_identification_mode IS NULL;

ALTER TABLE billing.sale
    ALTER COLUMN buyer_identification_mode SET NOT NULL;

ALTER TABLE billing.sale
    ADD CONSTRAINT ck_billing_sale_buyer_identification_mode
        CHECK (buyer_identification_mode IN ('IDENTIFIED_CUSTOMER', 'FINAL_CONSUMER'));

CREATE TABLE IF NOT EXISTS billing.final_consumer_profile (
    id UUID PRIMARY KEY,
    company_id UUID,
    profile_code VARCHAR(40) NOT NULL,
    identification_type_code INTEGER NOT NULL,
    identification_number VARCHAR(30) NOT NULL,
    display_name VARCHAR(180) NOT NULL,
    active BOOLEAN NOT NULL,
    source VARCHAR(80) NOT NULL,
    source_version VARCHAR(40) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_final_consumer_profile_scope UNIQUE (company_id, profile_code),
    CONSTRAINT ck_final_consumer_profile_code CHECK (profile_code = 'FINAL_CONSUMER')
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_final_consumer_profile_global
    ON billing.final_consumer_profile (profile_code)
    WHERE company_id IS NULL;

INSERT INTO billing.final_consumer_profile (
    id,
    company_id,
    profile_code,
    identification_type_code,
    identification_number,
    display_name,
    active,
    source,
    source_version,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000222',
    NULL,
    'FINAL_CONSUMER',
    31,
    '222222222222',
    'Consumidor final',
    true,
    'DIAN/Gobierno Colombia',
    '2026-08',
    now()
)
ON CONFLICT (profile_code) WHERE company_id IS NULL DO UPDATE SET
    identification_type_code = EXCLUDED.identification_type_code,
    identification_number = EXCLUDED.identification_number,
    display_name = EXCLUDED.display_name,
    active = EXCLUDED.active,
    source = EXCLUDED.source,
    source_version = EXCLUDED.source_version,
    updated_at = EXCLUDED.updated_at;
