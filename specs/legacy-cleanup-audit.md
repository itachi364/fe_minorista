# Auditoria de limpieza legacy

## Contexto

Documento de trabajo para `TASK-088`. La auditoria inicial ya fue ejecutada y la eliminacion aprobada por el usuario se aplico mediante migraciones Flyway nuevas, sin reescribir migraciones ya aplicadas.

Fecha local de auditoria: 2026-08-10.

## Validaciones ejecutadas

- `docker compose ps`: PostgreSQL y microservicios principales estaban arriba y saludables.
- Consulta exacta de conteos sobre PostgreSQL local en Docker.
- Busqueda de referencias en Java y migraciones con `rg`.
- `./mvnw.cmd test`: reactor completo en verde antes de la eliminacion.
- `npm run test` y `npm run build` en `apps/facturaelectronica-web`: en verde.
- `./mvnw.cmd -pl services/catalog-service test`: en verde despues de retirar runtime legacy de catalogo.
- `./mvnw.cmd -pl services/thirdparty-service test`: en verde despues de retirar tablas legacy de terceros.

## Matriz preliminar

| Tabla | Filas exactas | Referencias actuales | Uso E2E observado | Decision preliminar |
|---|---:|---|---|---|
| `catalog.catalog_definition` | 7 | `CatalogDefinitionJpaEntity`, `VersionedCatalogController` | Modulo `Catalogos` | EN_USO |
| `catalog.catalog_item` | 43 | `CatalogItemJpaEntity`, `runtimeCatalogs.js` via BFF | Formularios fiscales, terceros y POS | EN_USO |
| `catalog.company_catalog_item_setting` | 1 | `CompanyCatalogItemSettingJpaEntity`, `company-catalogs` | Configuracion por empresa | EN_USO |
| `catalog.department` | 33 | `DepartmentJpaEntity`, `MunicipalityFields` via BFF | Selectores por departamento | EN_USO |
| `catalog.municipality` | 1122 | `MunicipalityJpaEntity`, `MunicipalityFields` via BFF | Selectores de municipio/DIVIPOLA | EN_USO |
| `catalog.tipodocumento` | 11 | Runtime legacy retirado | Migrado a `catalog.catalog_item` como `DIAN_DOCUMENT_TYPE` | ELIMINADA |
| `catalog.categoria` | 0 | Runtime legacy retirado | Reemplazado por `inventory.product`/catalogos versionados cuando aplique | ELIMINADA |
| `catalog.producto` | 0 | Runtime legacy retirado | Reemplazado por `inventory.product` | ELIMINADA |
| `catalog.metodo_pago` | 0 | Runtime legacy retirado | Reemplazado por `catalog.catalog_item` | ELIMINADA |
| `catalog.tipo_gasto` | 0 | Runtime legacy retirado | No participa en flujo nuevo actual | ELIMINADA |
| `catalog.pais` | 0 | Runtime legacy retirado | Reemplazado por catalogos/DIVIPOLA cuando aplique | ELIMINADA |
| `catalog.impuesto` | 0 | Runtime legacy retirado | Reemplazado por catalogos versionados y reglas fiscales | ELIMINADA |
| `catalog.parametros` | 0 | Runtime legacy retirado | Reemplazado por configuracion versionada/servicios duenos | ELIMINADA |
| `thirdparty.third_party` | 0 | `ThirdPartyJpaEntity`, `ThirdPartyController` | Flujo nuevo de terceros/clientes | EN_USO |
| `thirdparty.third_party_role` | 0 | `ThirdPartyPersistenceAdapter` | Roles CUSTOMER/SUPPLIER del modelo nuevo | EN_USO |
| `thirdparty.third_party_tax_responsibility` | 0 | `ThirdPartyPersistenceAdapter` | Responsabilidades fiscales multiples | EN_USO |
| `thirdparty.cliente` | 11 | Sin runtime activo | No usado por flujo nuevo; filas locales E2E/dummy sin `company_id` real | ELIMINADA |
| `thirdparty.proveedor` | 11 | Sin runtime activo | No usado por flujo nuevo; filas locales E2E/dummy sin `company_id` real | ELIMINADA |
| `billing.sale` | 141 | `SaleJpaEntity`, reportes, POS | Venta POS | EN_USO |
| `billing.sale_line` | N/D en conteo exacto parcial | `SaleLineJpaEntity`, POS, lambdas | Venta POS e inventario | EN_USO |
| `billing.electronic_document` | 77 | `ElectronicDocumentJpaEntity`, proveedor mock/retry | Factura/POS electronico mock | EN_USO |

## Conclusiones

- El runtime legacy de catalogo fue retirado: controladores, DTOs, puertos, casos de uso, entidades, repositorios, mappers y pruebas asociadas.
- `catalog.tipodocumento` se uso como fuente de migracion hacia `catalog.catalog_item` antes de eliminar la tabla.
- Las tablas legacy vacias de catalogo se eliminaron mediante `services/catalog-service/src/main/resources/db/migration/V005__drop_legacy_catalog_tables.sql`.
- `thirdparty.cliente` y `thirdparty.proveedor` se eliminaron mediante `services/thirdparty-service/src/main/resources/db/migration/V005__drop_legacy_thirdparty_tables.sql`; la migracion aborta si encuentra datos legacy con `company_id` no nulo.
- Las migraciones Flyway historicas `V001/V002` no se reescriben para evitar romper checksums de bases existentes; la limpieza se expresa como migraciones nuevas.

## Siguiente paso propuesto

1. Ejecutar suite completa del reactor.
2. Ejecutar pruebas y build del frontend.
3. Validar con Docker que las tablas eliminadas ya no existen y que los endpoints v1 siguen operativos.
4. Ejecutar E2E desde cero.
