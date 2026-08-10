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
)
SELECT
    'DIAN_DOCUMENT_TYPE',
    CAST(codigo AS VARCHAR),
    nombre,
    descripcion,
    activo,
    TRUE,
    'LEGACY_TIPODOCUMENTO',
    'legacy-cleanup-2026-08',
    codigo
FROM catalog.tipodocumento
ON CONFLICT (catalog_code, item_code) DO UPDATE SET
    label = EXCLUDED.label,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    regulatory = TRUE,
    source = EXCLUDED.source,
    source_version = EXCLUDED.source_version,
    sort_order = EXCLUDED.sort_order;

DROP TABLE IF EXISTS catalog.producto;
DROP TABLE IF EXISTS catalog.impuesto;
DROP TABLE IF EXISTS catalog.parametros;
DROP TABLE IF EXISTS catalog.metodo_pago;
DROP TABLE IF EXISTS catalog.tipo_gasto;
DROP TABLE IF EXISTS catalog.categoria;
DROP TABLE IF EXISTS catalog.pais;
DROP TABLE IF EXISTS catalog.tipodocumento;
