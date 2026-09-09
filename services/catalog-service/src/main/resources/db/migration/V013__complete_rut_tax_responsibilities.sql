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
    ('TAX_RESPONSIBILITY', 'O-07', 'O-07 - Retencion en la fuente a titulo de renta', 'Responsabilidad fiscal DIAN', true, true, 'DIAN', 'RUT-2026-09', 5),
    ('TAX_RESPONSIBILITY', 'O-48', 'O-48 - Responsable del impuesto sobre las ventas IVA', 'Responsabilidad fiscal DIAN', true, true, 'DIAN', 'RUT-2026-09', 45),
    ('TAX_RESPONSIBILITY', 'O-49', 'O-49 - No responsable de IVA', 'Responsabilidad fiscal DIAN', true, true, 'DIAN', 'RUT-2026-09', 50),
    ('TAX_RESPONSIBILITY', 'O-52', 'O-52 - Facturador electronico', 'Responsabilidad fiscal DIAN', true, true, 'DIAN', 'RUT-2026-09', 55),
    ('TAX_RESPONSIBILITY', 'O-53', 'O-53 - Persona juridica no responsable de IVA', 'Responsabilidad fiscal DIAN', true, true, 'DIAN', 'RUT-2026-09', 60),
    ('TAX_RESPONSIBILITY', 'O-59', 'O-59 - Autorretencion especial renta', 'Responsabilidad fiscal DIAN', true, true, 'DIAN', 'RUT-2026-09', 65)
ON CONFLICT (catalog_code, item_code) DO UPDATE SET
    label = EXCLUDED.label,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    regulatory = EXCLUDED.regulatory,
    source = EXCLUDED.source,
    source_version = EXCLUDED.source_version,
    sort_order = EXCLUDED.sort_order;
