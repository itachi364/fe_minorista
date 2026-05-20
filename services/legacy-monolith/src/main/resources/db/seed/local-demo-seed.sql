-- Local demo seed for Docker/manual testing.
-- This file is intentionally outside db/migration so Flyway does not apply it in every environment.
-- Demo company id for API headers: 11111111-1111-1111-1111-111111111111

BEGIN;

INSERT INTO tipodocumento (codigo, nombre, descripcion, activo)
SELECT 31, 'NIT', 'Numero de identificacion tributaria', true
WHERE NOT EXISTS (SELECT 1 FROM tipodocumento WHERE codigo = 31);

INSERT INTO tipodocumento (codigo, nombre, descripcion, activo)
SELECT 13, 'Cedula de ciudadania', 'Documento de identificacion personal', true
WHERE NOT EXISTS (SELECT 1 FROM tipodocumento WHERE codigo = 13);

INSERT INTO categoria (id_categoria, nombre, descripcion, activo)
SELECT 1001, 'Demo cafeteria', 'Categoria local para pruebas Docker', true
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE id_categoria = 1001);

INSERT INTO producto (id_producto, nombre, descripcion, precio_base, cantidad_stock, id_categoria, activo, codigo_barras)
SELECT 1001, 'Cafe demo 250g', 'Producto seguro para pruebas locales', 15000.00, 25, 1001, true, 770123450001
WHERE NOT EXISTS (SELECT 1 FROM producto WHERE id_producto = 1001);

INSERT INTO producto (id_producto, nombre, descripcion, precio_base, cantidad_stock, id_categoria, activo, codigo_barras)
SELECT 1002, 'Panela demo 500g', 'Producto seguro para pruebas locales', 8200.00, 40, 1001, true, 770123450002
WHERE NOT EXISTS (SELECT 1 FROM producto WHERE id_producto = 1002);

INSERT INTO metodo_pago (id_metodo_pago, nombre, descripcion, activo)
SELECT 1001, 'Efectivo demo', 'Metodo de pago local para pruebas', true
WHERE NOT EXISTS (SELECT 1 FROM metodo_pago WHERE id_metodo_pago = 1001);

INSERT INTO tipo_gasto (id_tipo_gasto, nombre, descripcion, activo)
SELECT 1001, 'Servicios demo', 'Tipo de gasto local para pruebas', true
WHERE NOT EXISTS (SELECT 1 FROM tipo_gasto WHERE id_tipo_gasto = 1001);

INSERT INTO pais (codigo_pais, nombre, moneda, activo)
SELECT 'CO', 'Colombia', 'COP', true
WHERE NOT EXISTS (SELECT 1 FROM pais WHERE codigo_pais = 'CO');

INSERT INTO impuesto (id_impuesto, nombre, porcentaje, tipo, codigo_pais, activo, descripcion)
SELECT 1001, 'IVA 19 demo', 19.00, 'IVA', 'CO', true, 'Impuesto local para pruebas'
WHERE NOT EXISTS (SELECT 1 FROM impuesto WHERE id_impuesto = 1001);

INSERT INTO cliente (
    id_cliente,
    nombre,
    id_tipo_documento,
    numero_documento,
    digito_verificacion,
    direccion,
    telefono,
    correo_electronico,
    tipo_cliente,
    activo
)
SELECT
    1001,
    'Cliente Demo',
    13,
    10101010,
    null,
    'Calle 1 # 2-03',
    '3000000000',
    'cliente.demo@example.com',
    'NATURAL',
    true
WHERE NOT EXISTS (SELECT 1 FROM cliente WHERE id_cliente = 1001);

INSERT INTO proveedor (
    id_proveedor,
    nombre,
    id_tipo_documento,
    numero_documento,
    digito_verificacion,
    direccion,
    telefono,
    correo_electronico,
    activo
)
SELECT
    1001,
    'Proveedor Demo SAS',
    31,
    900123456,
    1,
    'Carrera 10 # 20-30',
    '6010000000',
    'proveedor.demo@example.com',
    true
WHERE NOT EXISTS (SELECT 1 FROM proveedor WHERE id_proveedor = 1001);

INSERT INTO billing_issuer_profile (
    id,
    company_id,
    legal_name,
    nit,
    verification_digit,
    tax_responsibilities,
    municipality_code,
    address,
    active
)
SELECT
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'Empresa Demo Local SAS',
    '900123456',
    '1',
    'R-99-PN',
    '11001',
    'Calle 1 # 2-03',
    true
WHERE NOT EXISTS (
    SELECT 1 FROM billing_issuer_profile WHERE id = '22222222-2222-2222-2222-222222222222'
);

INSERT INTO billing_numbering_resolution (
    id,
    company_id,
    document_type,
    resolution_number,
    prefix,
    from_number,
    to_number,
    current_number,
    valid_from,
    valid_to,
    environment,
    active
)
SELECT
    '33333333-3333-3333-3333-333333333333',
    '11111111-1111-1111-1111-111111111111',
    'ELECTRONIC_POS',
    'DEMO-POS-LOCAL-001',
    'POS',
    1,
    5000,
    0,
    '2026-01-01',
    '2030-12-31',
    'TEST',
    true
WHERE NOT EXISTS (
    SELECT 1 FROM billing_numbering_resolution WHERE id = '33333333-3333-3333-3333-333333333333'
);

INSERT INTO accounting_account (id, company_id, code, name, category, level, nature, parent_account_id, active)
SELECT
    '00000000-0000-0000-0000-000000110505',
    '11111111-1111-1111-1111-111111111111',
    '110505',
    'Caja general demo',
    'ASSET',
    'AUXILIARY',
    'DEBIT',
    null,
    true
WHERE NOT EXISTS (
    SELECT 1 FROM accounting_account
    WHERE company_id = '11111111-1111-1111-1111-111111111111' AND code = '110505'
);

INSERT INTO accounting_account (id, company_id, code, name, category, level, nature, parent_account_id, active)
SELECT
    '00000000-0000-0000-0000-000000413505',
    '11111111-1111-1111-1111-111111111111',
    '413505',
    'Ingresos por ventas demo',
    'INCOME',
    'AUXILIARY',
    'CREDIT',
    null,
    true
WHERE NOT EXISTS (
    SELECT 1 FROM accounting_account
    WHERE company_id = '11111111-1111-1111-1111-111111111111' AND code = '413505'
);

INSERT INTO accounting_account (id, company_id, code, name, category, level, nature, parent_account_id, active)
SELECT
    '00000000-0000-0000-0000-000000240805',
    '11111111-1111-1111-1111-111111111111',
    '240805',
    'IVA generado demo',
    'LIABILITY',
    'AUXILIARY',
    'CREDIT',
    null,
    true
WHERE NOT EXISTS (
    SELECT 1 FROM accounting_account
    WHERE company_id = '11111111-1111-1111-1111-111111111111' AND code = '240805'
);

INSERT INTO accounting_rule (id, company_id, event_type, source_type, name, active)
SELECT
    '44444444-4444-4444-4444-444444444444',
    '11111111-1111-1111-1111-111111111111',
    'SALE_CONFIRMED',
    'SALE',
    'Venta POS demo',
    true
WHERE NOT EXISTS (
    SELECT 1 FROM accounting_rule
    WHERE company_id = '11111111-1111-1111-1111-111111111111'
      AND event_type = 'SALE_CONFIRMED'
      AND active = true
);

WITH demo_rule AS (
    SELECT id
    FROM accounting_rule
    WHERE company_id = '11111111-1111-1111-1111-111111111111'
      AND event_type = 'SALE_CONFIRMED'
      AND active = true
    ORDER BY id
    LIMIT 1
)
INSERT INTO accounting_rule_line (id, rule_id, line_order, account_code, side, amount_type, description)
SELECT
    '55555555-5555-5555-5555-555555555551',
    demo_rule.id,
    1,
    '110505',
    'DEBIT',
    'TOTAL',
    'Ingreso a caja por venta POS'
FROM demo_rule
WHERE NOT EXISTS (
    SELECT 1 FROM accounting_rule_line WHERE id = '55555555-5555-5555-5555-555555555551'
);

WITH demo_rule AS (
    SELECT id
    FROM accounting_rule
    WHERE company_id = '11111111-1111-1111-1111-111111111111'
      AND event_type = 'SALE_CONFIRMED'
      AND active = true
    ORDER BY id
    LIMIT 1
)
INSERT INTO accounting_rule_line (id, rule_id, line_order, account_code, side, amount_type, description)
SELECT
    '55555555-5555-5555-5555-555555555552',
    demo_rule.id,
    2,
    '413505',
    'CREDIT',
    'SUBTOTAL',
    'Ingreso por venta POS'
FROM demo_rule
WHERE NOT EXISTS (
    SELECT 1 FROM accounting_rule_line WHERE id = '55555555-5555-5555-5555-555555555552'
);

WITH demo_rule AS (
    SELECT id
    FROM accounting_rule
    WHERE company_id = '11111111-1111-1111-1111-111111111111'
      AND event_type = 'SALE_CONFIRMED'
      AND active = true
    ORDER BY id
    LIMIT 1
)
INSERT INTO accounting_rule_line (id, rule_id, line_order, account_code, side, amount_type, description)
SELECT
    '55555555-5555-5555-5555-555555555553',
    demo_rule.id,
    3,
    '240805',
    'CREDIT',
    'TAX_TOTAL',
    'IVA generado por venta POS'
FROM demo_rule
WHERE NOT EXISTS (
    SELECT 1 FROM accounting_rule_line WHERE id = '55555555-5555-5555-5555-555555555553'
);

COMMIT;
