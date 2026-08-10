CREATE TABLE IF NOT EXISTS catalog.catalog_definition (
    catalog_code VARCHAR(80) PRIMARY KEY,
    label VARCHAR(180) NOT NULL,
    description VARCHAR(300),
    regulatory BOOLEAN NOT NULL DEFAULT false,
    company_configurable BOOLEAN NOT NULL DEFAULT false,
    global_editable_by_root BOOLEAN NOT NULL DEFAULT false,
    active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER NOT NULL DEFAULT 0
);

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
    ('DIAN_DOCUMENT_TYPE', 'Tipos de documento DIAN', 'Tipos de identificacion usados en Colombia para documentos fiscales.', true, false, false, true, 10),
    ('TAX_RESPONSIBILITY', 'Responsabilidades fiscales', 'Responsabilidades fiscales usadas para facturacion electronica.', true, false, false, true, 20),
    ('TAX_REGIME', 'Regimenes tributarios', 'Regimen tributario aplicable al tercero o emisor.', true, false, false, true, 30),
    ('PAYMENT_METHOD', 'Metodos de pago', 'Opciones de pago disponibles para ventas.', false, true, true, true, 40),
    ('VIRTUAL_WALLET', 'Billeteras virtuales', 'Billeteras virtuales disponibles para pagos en Colombia.', false, true, true, true, 50),
    ('FISCAL_DOCUMENT_TYPE', 'Tipos de documento fiscal', 'Tipos de documento electronico gestionados por la plataforma.', true, false, false, true, 60),
    ('FISCAL_ENVIRONMENT', 'Ambientes fiscales', 'Ambientes de operacion fiscal para pruebas o produccion.', true, false, false, true, 70)
ON CONFLICT (catalog_code) DO UPDATE SET
    label = EXCLUDED.label,
    description = EXCLUDED.description,
    regulatory = EXCLUDED.regulatory,
    company_configurable = EXCLUDED.company_configurable,
    global_editable_by_root = EXCLUDED.global_editable_by_root,
    active = EXCLUDED.active,
    sort_order = EXCLUDED.sort_order;
