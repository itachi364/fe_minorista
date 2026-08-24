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

## Actualizacion TASK-178 - 2026-08-19

### Repositorio

- No existe `services/legacy-monolith` ni carpeta raiz `src/` activa.
- No se encontraron imports activos hacia paquetes legacy `DTO`, `mapper`, `models`, `repository`, `service` o `validator` dentro de `services` ni `apps`.
- Las rutas legacy `/api/clientes`, `/api/proveedores`, `/api/categorias`, `/api/productos`, `/api/metodopago`, `/api/tipos-documento`, `/api/impuesto`, `/api/paises`, `/api/parametros` y `/api/tipogasto` no aparecen como endpoints activos en controladores.
- Los artefactos ignorados `.github/java-upgrade`, `.github/modernize`, `.idea`, `.settings`, `target/`, `services/*/target` y `apps/facturaelectronica-web/dist` son candidatos de limpieza local porque no estan rastreados por Git.

### Base local PostgreSQL

Consulta ejecutada:

```sql
select table_schema, table_name
from information_schema.tables
where table_type='BASE TABLE'
  and table_schema in ('public','catalog','thirdparty','inventory','billing','accounting','identity','tenant','audit','dian_provider','payroll')
order by table_schema, table_name;
```

Tablas `public.*` vacias candidatas a eliminacion segura por script idempotente:

| Tabla | Filas |
|---|---:|
| `auditoria` | 0 |
| `accounting_entry` | 0 |
| `accounting_entry_line` | 0 |
| `compra` | 0 |
| `detalle_compra` | 0 |
| `detalle_factura` | 0 |
| `detalle_gasto` | 0 |
| `factura` | 0 |
| `gastos` | 0 |
| `parametros` | 0 |
| `registro_accesos` | 0 |
| `roles` | 0 |
| `usuarios` | 0 |

Tablas `public.*` con datos, no eliminables sin migracion, respaldo o descarte aprobado:

| Tabla | Filas |
|---|---:|
| `accounting_account` | 3 |
| `accounting_rule` | 1 |
| `accounting_rule_line` | 3 |
| `billing_electronic_document_trace_event` | 1 |
| `billing_electronic_pos_document` | 1 |
| `billing_electronic_pos_document_line` | 1 |
| `billing_fiscal_audit_event` | 1 |
| `billing_issuer_profile` | 4 |
| `billing_numbering_resolution` | 4 |
| `billing_provider_submission` | 1 |
| `categoria` | 1 |
| `cliente` | 1 |
| `impuesto` | 1 |
| `metodo_pago` | 1 |
| `pais` | 1 |
| `producto` | 2 |
| `proveedor` | 1 |
| `tipo_gasto` | 1 |
| `tipodocumento` | 2 |

### Decision

- Se crea `scripts/db/drop-empty-legacy-public-tables.sql` para eliminar solo tablas `public.*` vacias.
- Las tablas `public.*` con datos quedan bloqueadas para una siguiente decision de migracion, respaldo o descarte.
- No se reescriben migraciones Flyway existentes; Context7/Flyway confirma que el historial aplicado se valida contra migraciones locales y checksums.

## Siguiente paso propuesto

1. Ejecutar suite completa del reactor.
2. Ejecutar pruebas y build del frontend.
3. Validar con Docker que las tablas eliminadas ya no existen y que los endpoints v1 siguen operativos.
4. Ejecutar E2E desde cero.
