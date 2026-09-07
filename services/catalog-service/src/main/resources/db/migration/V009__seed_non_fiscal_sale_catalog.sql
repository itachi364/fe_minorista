INSERT INTO catalog.catalog_item (catalog_code, item_code, label, description, active, regulatory, source, source_version, sort_order)
VALUES ('FISCAL_DOCUMENT_TYPE', 'NON_FISCAL_SALE', 'Venta interna no fiscal',
        'Venta comercial sin transmision DIAN, sin CUFE/CUDE ni QR DIAN.', true, false, 'APP', '2026-09', 5)
ON CONFLICT (catalog_code, item_code) DO UPDATE
SET label = EXCLUDED.label,
    description = EXCLUDED.description,
    active = true,
    regulatory = false,
    source = EXCLUDED.source,
    source_version = EXCLUDED.source_version,
    sort_order = EXCLUDED.sort_order;
