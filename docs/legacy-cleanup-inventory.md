# Inventario de codigo y tablas legacy

## Objetivo

Este documento se actualiza para TASK-059. Su proposito es registrar que codigo legacy fue removido, que endpoints/tablas siguen protegidos por compatibilidad o datos historicos, y que elementos requieren migracion/respaldo antes de cualquier eliminacion destructiva.

Estado documental: este archivo es historico de TASK-059 y no debe usarse como fuente vigente unica. La fuente actual de limpieza aplicada es `specs/legacy-cleanup-audit.md`; la fuente vigente de contratos es `specs/api-contract.md`.

TASK-059 lote 1 elimina el modulo `services/legacy-monolith` del repositorio activo. No elimina tablas ni datos `public.*`; cualquier depuracion de base de datos queda bloqueada hasta plan de migracion/respaldo aprobado.

## Evidencia revisada

Comandos usados para levantar el inventario:

```powershell
rg --files services
rg -n "<module>|artifactId|packaging" pom.xml services\*\pom.xml
rg -n "@(RestController|Controller)|@RequestMapping|@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)" services\legacy-monolith\src\main\java
rg -n "@(RestController|Controller)|@RequestMapping|@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)" services\catalog-service\src\main\java services\thirdparty-service\src\main\java services\inventory-service\src\main\java services\billing-service\src\main\java services\accounting-service\src\main\java services\tenant-service\src\main\java services\dian-provider-service\src\main\java
rg -n "CREATE TABLE|CREATE SCHEMA|CREATE INDEX|ALTER TABLE" services\*\src\main\resources\db\migration
rg -n "legacy-monolith|LEGACY_MONOLITH|audit-service" pom.xml docker-compose.yml .env.example README.md specs docs
```

Resultado estructural:

| Componente | Estado actual | Java main |
| --- | --- | ---: |
| `services/tenant-service` | Microservicio fisico activo | 28 |
| `services/catalog-service` | Microservicio fisico activo con contratos canonicos `/api/v1` | 137 |
| `services/thirdparty-service` | Microservicio fisico activo solo con contratos canonicos `/api/v1` | 72 |
| `services/inventory-service` | Microservicio fisico activo | 70 |
| `services/billing-service` | Microservicio fisico activo para venta POS, configuracion fiscal y orquestacion | 87 |
| `services/dian-provider-service` | Microservicio fisico activo con mock DIAN | 34 |
| `services/accounting-service` | Microservicio fisico activo | 74 |
| `services/audit-service` | Microservicio fisico activo para auditoria fiscal/tecnica | 30 |
| `services/legacy-monolith` | Removido del repositorio activo en TASK-059 lote 1 | 0 |

## Componentes involucrados en el flujo E2E aprobado

La prueba `scripts/e2e-from-zero.ps1` valida el flujo local desde empresa nueva hasta venta POS aceptada:

| Componente | Participa en E2E | Evidencia funcional |
| --- | --- | --- |
| `tenant-service` | Si | Crea empresa, licencia activa y valida acciones licenciadas. |
| `identity-service` | Si | Crea usuario owner, login y membresia por empresa. |
| `catalog-service` | Salud validada; no invoca endpoints legacy en el flujo principal | Contratos activos `/api/v1/catalog-*`; rutas legacy retiradas por limpieza posterior. |
| `thirdparty-service` | Si | Crea cliente y proveedor por `/api/v1/customers` y `/api/v1/suppliers`. |
| `inventory-service` | Si | Crea producto, stock inicial, kardex y `SALE_OUT`. |
| `billing-service` | Si | Configura emisor/resolucion, crea venta, confirma POS y orquesta efectos posteriores. |
| `dian-provider-service` | Si | Registra envio mock aceptado. |
| `accounting-service` | Si | Inicializa plantilla contable basica y genera asiento balanceado. |
| `legacy-monolith` | No | Codigo removido en TASK-059 lote 1; no participa en build, Docker ni E2E. |
| `audit-service` | Si, para confirmacion fiscal de ventas desde `billing-service` | Implementado en TASK-042 y conectado parcialmente en TASK-043. |

## Endpoints legacy y reemplazos

| Area | Endpoint legacy | Reemplazo actual | Estado |
| --- | --- | --- | --- |
| Catalogos | `/api/categorias` | `catalog-service /api/v1/catalogs/*` o `inventory-service /api/v1/products` segun caso | Retirado como contrato legacy activo. |
| Catalogos | `/api/paises` | `catalog-service /api/v1/catalogs/*` | Retirado como contrato legacy activo. |
| Catalogos | `/api/tipos-documento` | `catalog-service /api/v1/catalogs/DIAN_DOCUMENT_TYPE/items` | Retirado como contrato legacy activo. |
| Catalogos | `/api/metodopago` | `catalog-service /api/v1/catalogs/PAYMENT_METHOD/items` | Retirado como contrato legacy activo. |
| Catalogos | `/api/tipogasto` | `catalog-service /api/v1/catalogs/*` y `accounting-service /api/v1/expenses` | Retirado como contrato legacy activo. |
| Catalogos | `/api/impuesto` | `catalog-service /api/v1/catalogs/SALES_TAX/items` | Retirado como contrato legacy activo. |
| Catalogos | `/api/parametros` | `catalog-service /api/v1/catalogs/*` o configuracion del servicio dueno | Retirado como contrato legacy activo. |
| Catalogos | `/api/productos` | `inventory-service /api/v1/products` para item vendible/comprable/inventariable | Retirado como contrato legacy activo. |
| Terceros | `/api/clientes` | `thirdparty-service /api/v1/customers` y `/api/v1/third-parties` | Codigo y endpoint retirados en TASK-059 lote 2; tablas historicas preservadas. |
| Terceros | `/api/proveedores` | `thirdparty-service /api/v1/suppliers` y `/api/v1/third-parties` | Codigo y endpoint retirados en TASK-059 lote 2; tablas historicas preservadas. |
| Inventario | `/api/compras` | `inventory-service /api/v1/purchases` y `/api/v1/inventory-movements` | Reemplazado funcionalmente para flujo nuevo. |
| Gastos | `/api/gastos` | `accounting-service /api/v1/expenses` | Reemplazado funcionalmente para flujo nuevo de gastos; mantener datos legacy hasta auditoria/migracion. |
| Billing legacy | `/api/v1/issuers` | `billing-service /api/v1/issuers` y `/api/v1/issuers/current` | Reemplazado funcionalmente en TASK-041. |
| Billing legacy | `/api/v1/numbering-resolutions` | `billing-service /api/v1/numbering-resolutions` | Reemplazado funcionalmente en TASK-041. |
| Billing legacy | `/api/v1/electronic-pos` | `billing-service /api/v1/sales` cubre venta POS con numeracion fiscal configurada | Migracion parcial: POS directo y artefactos oficiales siguen pendientes. |
| Billing nuevo | `/api/v1/sales` | `billing-service` | Activo. |
| Contabilidad | `/api/v1/accounts`, `/api/v1/accounting-rules`, `/api/v1/accounting-entries`, `/api/v1/reports/*` | `accounting-service` | Reemplazado funcionalmente. |
| Proveedor DIAN mock | Proveedor dummy local del monolito | `dian-provider-service /api/v1/provider/*` | Reemplazado funcionalmente para mock. |
| Auditoria/accesos | `auditoria`, `registro_accesos` | `audit-service /api/v1/audit-events` e `identity.identity_access_audit` | Reemplazado funcionalmente para eventos nuevos; migrar/respaldar datos legacy antes de eliminar. |

## Matriz de tablas legacy

| Tabla legacy | Reemplazo destino | Estado | Accion propuesta para TASK-040 |
| --- | --- | --- | --- |
| `roles` | `identity.company_membership_role` y roles de dominio `identity-service` | Reemplazado funcionalmente para usuarios nuevos | Mantener hasta migrar/respaldar datos legacy. |
| `usuarios` | `identity.user_account`, `identity.company_membership`, `identity.user_session` | Reemplazado funcionalmente para usuarios nuevos | Mantener hasta migrar/respaldar datos legacy. |
| `auditoria` | `audit.audit_event` | Reemplazado funcionalmente para auditoria generica | Mantener hasta migrar/respaldar datos legacy. |
| `registro_accesos` | `identity.identity_access_audit` y `audit.audit_event` segun tipo de evento | Reemplazado funcionalmente para accesos nuevos | Mantener hasta migrar/respaldar historicos. |
| `tipodocumento` | `catalog.catalog_item` con `DIAN_DOCUMENT_TYPE` | Reemplazado y eliminado por limpieza posterior | Ver `specs/legacy-cleanup-audit.md`. |
| `cliente` | `thirdparty.third_party` y `thirdparty.third_party_role` | Codigo/endpoints legacy retirados en TASK-059 lote 2 | Tabla eliminada por TASK-088 con salvaguarda Flyway. |
| `proveedor` | `thirdparty.third_party` y `thirdparty.third_party_role` | Codigo/endpoints legacy retirados en TASK-059 lote 2 | Tabla eliminada por TASK-088 con salvaguarda Flyway. |
| `categoria` | `catalog.categoria` | Reemplazado funcionalmente | Eliminar despues de validar dependencia de productos/catalogo. |
| `producto` | `inventory.product`, `inventory.stock_balance`; `catalog.producto` temporal | Reemplazado para inventario nuevo, compatibilidad parcial en catalogo | Migrar datos utiles y eliminar duplicidad de ownership. |
| `metodo_pago` | `catalog.metodo_pago` | Reemplazado funcionalmente | Eliminar despues de migrar datos requeridos. |
| `tipo_gasto` | `catalog.tipo_gasto` y futuro gastos | Parcial | Mantener mientras gastos no migre. |
| `pais` | `catalog.pais` | Reemplazado funcionalmente | Eliminar despues de migrar datos requeridos. |
| `impuesto` | `catalog.impuesto` | Reemplazado funcionalmente | Eliminar despues de migrar datos requeridos. |
| `parametros` | `catalog.parametros` | Reemplazado funcionalmente | Eliminar despues de migrar datos requeridos. |
| `compra` | `inventory.purchase` | Reemplazado para flujo nuevo | Eliminar despues de migrar datos historicos requeridos. |
| `detalle_compra` | `inventory.purchase_line` e `inventory.inventory_movement` | Reemplazado para flujo nuevo | Eliminar despues de migrar datos historicos requeridos. |
| `gastos` | `accounting.expense` y `accounting.accounting_accounts_payable` cuando aplica | Reemplazado funcionalmente para flujo nuevo | Mantener hasta migrar/respaldar historicos. |
| `detalle_gasto` | `accounting.expense` agregado y asientos/reglas contables | Reemplazado funcionalmente para flujo nuevo | Mantener hasta migrar/respaldar historicos. |
| `factura` | `billing.sale`, `billing.electronic_document` | Parcial: POS nuevo cubierto, factura electronica completa pendiente | Mantener hasta completar factura electronica y migrar historico. |
| `detalle_factura` | `billing.sale_line` | Parcial | Mantener hasta completar factura electronica y migrar historico. |
| `billing_issuer_profile` | `billing.issuer_profile` | Reemplazado funcionalmente en TASK-041 | Mantener hasta migrar/respaldar datos legacy y validar que no existan consumidores del monolito. |
| `billing_numbering_resolution` | `billing.numbering_resolution` | Reemplazado funcionalmente en TASK-041 | Mantener hasta migrar/respaldar datos legacy y validar que no existan consumidores del monolito. |
| `billing_electronic_pos_document` | `billing.sale`, `billing.electronic_document` | Parcial | Mantener hasta cerrar POS directo, artefactos oficiales y migracion historica. |
| `billing_electronic_pos_document_line` | `billing.sale_line` | Parcial | Mantener hasta cerrar POS directo, artefactos oficiales y migracion historica. |
| `billing_provider_submission` | `dian_provider.provider_submission` y `billing.electronic_document` | Reemplazado para mock nuevo | Eliminar despues de migrar trazas utiles. |
| `billing_electronic_document_trace_event` | `billing.electronic_document` parcial y `audit.audit_event` | Parcial | Mantener hasta integrar productores y migrar trazas utiles. |
| `billing_fiscal_audit_event` | `audit.audit_event` | Reemplazado funcionalmente como destino central | Mantener hasta migrar/respaldar datos legacy. |
| `accounting_account` | `accounting.accounting_account` | Reemplazado funcionalmente | Eliminar duplicado legacy/public despues de confirmar datos. |
| `accounting_rule` | `accounting.accounting_rule` | Reemplazado funcionalmente | Eliminar duplicado legacy/public despues de confirmar datos. |
| `accounting_rule_line` | `accounting.accounting_rule_line` | Reemplazado funcionalmente | Eliminar duplicado legacy/public despues de confirmar datos. |
| `accounting_entry` | `accounting.accounting_entry` | Reemplazado funcionalmente | Eliminar duplicado legacy/public despues de confirmar datos. |
| `accounting_entry_line` | `accounting.accounting_entry_line` | Reemplazado funcionalmente | Eliminar duplicado legacy/public despues de confirmar datos. |

## Auditoria de datos previa a eliminacion

La limpieza de tablas legacy debe basarse en conteos reales por ambiente, no en supuestos del codigo.

Script de auditoria:

```text
scripts/legacy-data-audit.sql
```

Ejecutar contra PostgreSQL local:

```powershell
Get-Content .\scripts\legacy-data-audit.sql | docker compose exec -T postgres psql -U factura_user -d facturaelectronica
```

El script reporta:

- Existencia de tablas legacy/public y tablas destino en esquemas de microservicios.
- Conteos exactos de filas para cada tabla presente.
- Estado `MISSING` para tablas esperadas que aun no existan en una base recien creada o parcialmente migrada.

El resultado debe adjuntarse o resumirse antes de aprobar cualquier migracion destructiva de tablas publicas legacy.

## Codigo candidato a eliminar

Estado de eliminacion de codigo:

| Ruta | Estado | Condicion antes de eliminar |
| --- | --- | --- |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/catalog/**` | Eliminado en TASK-059 lote 1 | Reemplazado por `catalog-service`; contratos activos actuales viven en `/api/v1/catalogs*`. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/thirdparty/**` | Eliminado en TASK-059 lote 1 | Reemplazado por `thirdparty-service`; E2E usa `/api/v1/customers` y `/api/v1/suppliers`. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/inventory/**` | Eliminado en TASK-059 lote 1 | Reemplazado por `inventory-service`; historicos `public.compra`/`public.detalle_compra` se preservan. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/accounting/**` | Eliminado en TASK-059 lote 1 | Reemplazado por `accounting-service`; datos public contables se preservan. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/billing/**` | Eliminado en TASK-059 lote 1 | Flujo POS nuevo cubierto por `billing-service`; historicos `public.billing_*` se preservan. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/controller/GastoController.java` y `expenses/**` | Eliminado en TASK-059 lote 1 | Gastos nuevos viven en `accounting-service`; historicos `public.gastos`/`public.detalle_gasto` se preservan. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/models/**`, `DTO/**`, `repository/**`, `mapper/**` | Eliminado en TASK-059 lote 1 | Sin referencias desde microservicios activos. |
| `services/legacy-monolith/src/test/java/**` | Eliminado en TASK-059 lote 1 | La suite activa cubre microservicios fisicos. |
| `services/thirdparty-service/src/main/java/com/msvanegasg/facturaelectronica/controller/ClienteController.java`, `ProveedorController.java` y bloque `Customer`/`Supplier` asociado | Eliminado en TASK-059 lote 2 | Reemplazado por `ThirdPartyController` y modelo `third_party`; E2E usa `/api/v1/customers` y `/api/v1/suppliers`. |

## Codigo que no debe eliminarse todavia

- `catalog-service` ya expone contratos canonicos `/api/v1/catalogs*`. `thirdparty-service` retiro `/api/clientes` y `/api/proveedores` en TASK-059 lote 2; conserva `ThirdPartyController` con `/api/v1`.
- `services/audit-service` ya es microservicio activo; `billing-service` publica confirmaciones fiscales, pero aun falta integrar productores de inventario y contabilidad.
- `services/legacy-monolith` fue eliminado como codigo en TASK-059 lote 1; las brechas de datos historicos se gestionan sobre tablas `public.*`, no reactivando el monolito.
- Las migraciones legacy no deben borrarse sin una estrategia de datos. En Flyway, retirar migraciones ya aplicadas puede romper ambientes existentes.

## Riesgos posteriores a TASK-059 lote 1

1. Gastos nuevos estan en `accounting-service`; falta auditoria/migracion de datos historicos legacy antes de eliminar tablas/clases antiguas.
2. `audit-service` ya recibe eventos de `billing-service`; faltan productores de inventario y contabilidad.
3. Las rutas legacy de catalogo y terceros ya no deben considerarse contratos activos; las pruebas nuevas deben usar BFF y `/api/v1`.
4. Existen tablas duplicadas entre public legacy y esquemas de microservicio; se requiere plan de migracion/respaldo antes de eliminarlas.
5. El codigo `legacy-monolith`, el codigo legacy de terceros y las tablas `thirdparty.cliente/proveedor` ya fueron retirados; para datos historicos restantes `public.*` se requiere plan de migracion, respaldo o eliminacion aprobado.

## Recomendacion posterior a TASK-059 lote 1

Ejecutar la limpieza por lotes pequenos:

1. Retiro de `legacy-monolith` de Docker Compose ejecutado previamente; TASK-059 lote 1 elimina el codigo fuente del modulo.
2. Perfil Maven `legacy-monolith` eliminado en TASK-059 lote 1 porque el modulo ya no existe.
3. Crear auditoria de conteos para tablas legacy y destino antes de borrar datos. Ejecutado en TASK-040 lote 3.
4. Migrar o documentar datos necesarios de tablas publicas reemplazadas.
5. Codigo del monolito eliminado en TASK-059 lote 1; eliminar duplicados de tablas contables public solo con migracion/respaldo aprobado.
6. Mantener auditoria de datos para catalogo/terceros/inventario `public.*` antes de eliminar tablas.
7. Mantener temporalmente billing legacy hasta migrar datos historicos y cerrar POS directo/trazabilidad fiscal.
8. Mantener gastos legacy hasta decidir `expenses-service` o integracion contable.
9. Ejecutar `.\mvnw.cmd test`, `docker compose config` y `.\scripts\e2e-from-zero.ps1`.
