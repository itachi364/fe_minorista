INSERT INTO catalog.catalog_definition (
    catalog_code,
    label,
    description,
    regulatory,
    company_configurable,
    global_editable_by_root,
    active,
    sort_order
) VALUES (
    'FISCAL_CONCEPT',
    'Conceptos fiscales',
    'Conceptos operativos usados para seleccionar reglas de retencion.',
    false,
    false,
    true,
    true,
    36
)
ON CONFLICT (catalog_code) DO UPDATE SET
    label = EXCLUDED.label,
    description = EXCLUDED.description,
    regulatory = EXCLUDED.regulatory,
    company_configurable = EXCLUDED.company_configurable,
    global_editable_by_root = EXCLUDED.global_editable_by_root,
    active = EXCLUDED.active,
    sort_order = EXCLUDED.sort_order;

INSERT INTO catalog.catalog_item (
    catalog_code,
    item_code,
    label,
    description,
    active,
    regulatory,
    source,
    source_version,
    sort_order
) VALUES
    ('FISCAL_CONCEPT', 'ANY', 'Cualquier concepto', 'Regla aplicable sin condicion de concepto fiscal.', true, false, 'APP', '2026-09', 10),
    ('FISCAL_CONCEPT', 'AGRICULTURAL_UNPROCESSED', 'Productos agricolas sin procesamiento', 'Compra de productos agricolas sin procesamiento.', true, false, 'APP', '2026-09', 20),
    ('FISCAL_CONCEPT', 'COFFEE_PARCHMENT_CHERRY', 'Cafe pergamino o cereza', 'Compra de cafe pergamino o cereza.', true, false, 'APP', '2026-09', 30),
    ('FISCAL_CONCEPT', 'GOLD_INTERNATIONAL_TRADING', 'Oro por sociedad de comercializacion internacional', 'Compra de oro por sociedad de comercializacion internacional.', true, false, 'APP', '2026-09', 40),
    ('FISCAL_CONCEPT', 'SERVICE_GENERAL_DECLARANT', 'Servicio general de declarante', 'Pago o gasto por servicio general de declarante.', true, false, 'APP', '2026-09', 50),
    ('FISCAL_CONCEPT', 'REAL_ESTATE_LEASE', 'Arrendamiento de inmueble', 'Pago o gasto por arrendamiento de inmueble.', true, false, 'APP', '2026-09', 60),
    ('FISCAL_CONCEPT', 'CARGO_TRANSPORT', 'Transporte de carga', 'Pago o gasto por transporte de carga.', true, false, 'APP', '2026-09', 70)
ON CONFLICT (catalog_code, item_code) DO UPDATE SET
    label = EXCLUDED.label,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    regulatory = EXCLUDED.regulatory,
    source = EXCLUDED.source,
    source_version = EXCLUDED.source_version,
    sort_order = EXCLUDED.sort_order;
