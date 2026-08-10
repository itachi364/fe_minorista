INSERT INTO catalog.catalog_definition (
    catalog_code,
    label,
    description,
    regulatory,
    company_configurable,
    global_editable_by_root,
    active,
    sort_order
) VALUES
    ('SALES_TAX', 'Impuestos de venta', 'Impuestos aplicables a productos, servicios e insumos vendibles en Colombia.', true, true, true, true, 35)
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
    ('SALES_TAX', 'IVA_19', 'IVA 19%', 'category=IVA;rate=19;Impuesto sobre las ventas tarifa general.', true, true, 'DIAN/Gobierno Colombia', '2026-08', 10),
    ('SALES_TAX', 'IVA_5', 'IVA 5%', 'category=IVA;rate=5;Impuesto sobre las ventas tarifa diferencial.', true, true, 'DIAN/Gobierno Colombia', '2026-08', 20),
    ('SALES_TAX', 'IVA_0', 'IVA 0%', 'category=IVA;rate=0;Bien o servicio gravado a tarifa cero cuando aplique.', true, true, 'DIAN/Gobierno Colombia', '2026-08', 30),
    ('SALES_TAX', 'EXEMPT', 'Exento', 'category=EXEMPT;rate=0;Operacion exenta segun clasificacion fiscal aplicable.', true, true, 'DIAN/Gobierno Colombia', '2026-08', 40),
    ('SALES_TAX', 'EXCLUDED', 'Excluido', 'category=EXCLUDED;rate=0;Operacion excluida del impuesto cuando aplique.', true, true, 'DIAN/Gobierno Colombia', '2026-08', 50),
    ('SALES_TAX', 'INC_8', 'Impuesto nacional al consumo 8%', 'category=INC;rate=8;Impuesto nacional al consumo cuando aplique.', true, true, 'DIAN/Gobierno Colombia', '2026-08', 60)
ON CONFLICT (catalog_code, item_code) DO UPDATE SET
    label = EXCLUDED.label,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    regulatory = EXCLUDED.regulatory,
    source = EXCLUDED.source,
    source_version = EXCLUDED.source_version,
    sort_order = EXCLUDED.sort_order;
