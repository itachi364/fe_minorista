ALTER TABLE catalog.tipodocumento DROP CONSTRAINT IF EXISTS ck_catalog_tipodocumento_codigo;

DELETE FROM catalog.tipodocumento
WHERE codigo NOT IN (11, 12, 13, 21, 22, 31, 41, 42, 43, 47, 48);

INSERT INTO catalog.tipodocumento (codigo, nombre, descripcion, activo) VALUES
    (11, 'Registro civil de nacimiento', 'Documento DIAN codigo 11', true),
    (12, 'Tarjeta de identidad', 'Documento DIAN codigo 12', true),
    (13, 'Cedula de ciudadania', 'Documento DIAN codigo 13', true),
    (21, 'Tarjeta de extranjeria', 'Documento DIAN codigo 21', true),
    (22, 'Cedula de extranjeria', 'Documento DIAN codigo 22', true),
    (31, 'NIT', 'Documento DIAN codigo 31', true),
    (41, 'Pasaporte', 'Documento DIAN codigo 41', true),
    (42, 'Tipo de documento extranjero', 'Documento DIAN codigo 42', true),
    (43, 'Sin identificacion exterior o uso DIAN', 'Documento DIAN codigo 43', true),
    (47, 'Permiso Especial de Permanencia', 'Documento DIAN codigo 47', true),
    (48, 'Permiso por Proteccion Temporal', 'Documento DIAN codigo 48', true)
ON CONFLICT (codigo) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    activo = EXCLUDED.activo;

ALTER TABLE catalog.tipodocumento
    ALTER COLUMN codigo TYPE INTEGER USING codigo::INTEGER;

ALTER TABLE catalog.tipodocumento ADD CONSTRAINT ck_catalog_tipodocumento_codigo
    CHECK (codigo IN (11, 12, 13, 21, 22, 31, 41, 42, 43, 47, 48));