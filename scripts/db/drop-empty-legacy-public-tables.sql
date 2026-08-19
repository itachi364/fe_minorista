-- TASK-167: Safe cleanup for empty legacy tables in the public schema.
-- This script is intentionally operational because active microservices own
-- their own schemas and no service runs Flyway against public anymore.
--
-- It only drops a table when it exists and has zero rows. Tables with data are
-- preserved for migration, backup, or explicit discard approval.

\set ON_ERROR_STOP on

DO $$
DECLARE
    legacy_table text;
    row_count bigint;
BEGIN
    FOREACH legacy_table IN ARRAY ARRAY[
        'auditoria',
        'accounting_entry_line',
        'accounting_entry',
        'detalle_compra',
        'detalle_factura',
        'detalle_gasto',
        'compra',
        'factura',
        'gastos',
        'parametros',
        'registro_accesos',
        'usuarios',
        'roles'
    ]
    LOOP
        IF to_regclass(format('public.%I', legacy_table)) IS NULL THEN
            RAISE NOTICE 'Skipping %.%: table does not exist.', 'public', legacy_table;
        ELSE
            EXECUTE format('SELECT count(*) FROM public.%I', legacy_table) INTO row_count;

            IF row_count = 0 THEN
                EXECUTE format('DROP TABLE public.%I', legacy_table);
                RAISE NOTICE 'Dropped empty legacy table public.%', legacy_table;
            ELSE
                RAISE NOTICE 'Preserved legacy table public.% because it has % row(s).', legacy_table, row_count;
            END IF;
        END IF;
    END LOOP;
END $$;
