DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM thirdparty.cliente WHERE company_id IS NOT NULL) THEN
        RAISE EXCEPTION 'Cannot drop thirdparty.cliente because it contains company-scoped legacy data';
    END IF;

    IF EXISTS (SELECT 1 FROM thirdparty.proveedor WHERE company_id IS NOT NULL) THEN
        RAISE EXCEPTION 'Cannot drop thirdparty.proveedor because it contains company-scoped legacy data';
    END IF;
END $$;

DROP TABLE IF EXISTS thirdparty.cliente;
DROP TABLE IF EXISTS thirdparty.proveedor;
