# Inventario de codigo y tablas legacy

## Objetivo

Este documento cubre la TASK-039. Su proposito es identificar que codigo, endpoints, modulos y tablas legacy ya tienen reemplazo en microservicios fisicos, que elementos aun deben migrarse y que puede proponerse para eliminacion en TASK-040.

No elimina codigo ni datos. Cualquier eliminacion queda bloqueada hasta aprobacion explicita de TASK-040.

## Evidencia revisada

Comandos usados para levantar el inventario:

```powershell
rg --files services
rg -n "<module>|artifactId|packaging" pom.xml services\*\pom.xml
rg -n "@(RestController|Controller)|@RequestMapping|@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)" services\legacy-monolith\src\main\java
rg -n "@(RestController|Controller)|@RequestMapping|@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)" services\catalog-service\src\main\java services\thirdparty-service\src\main\java services\inventory-service\src\main\java services\billing-service\src\main\java services\accounting-service\src\main\java services\tenant-service\src\main\java services\dian-provider-service\src\main\java
rg -n "CREATE TABLE|CREATE SCHEMA|CREATE INDEX|ALTER TABLE" services\*\src\main\resources\db\migration
rg -n "legacy-monolith|LEGACY_MONOLITH|audit-service" pom.xml docker-compose.yml .env.example README.md specs
```

Resultado estructural:

| Componente | Estado actual | Java main |
| --- | --- | ---: |
| `services/tenant-service` | Microservicio fisico activo | 28 |
| `services/catalog-service` | Microservicio fisico activo con rutas legacy compatibles | 137 |
| `services/thirdparty-service` | Microservicio fisico activo con rutas legacy compatibles | 72 |
| `services/inventory-service` | Microservicio fisico activo | 70 |
| `services/billing-service` | Microservicio fisico activo para venta POS, configuracion fiscal y orquestacion | 87 |
| `services/dian-provider-service` | Microservicio fisico activo con mock DIAN | 34 |
| `services/accounting-service` | Microservicio fisico activo | 74 |
| `services/audit-service` | Microservicio fisico activo para auditoria fiscal/tecnica | 30 |
| `services/legacy-monolith` | Modulo transitorio legacy, fuera del reactor Maven activo por defecto | 430 |

## Componentes involucrados en el flujo E2E aprobado

La prueba `scripts/e2e-from-zero.ps1` valida el flujo local desde empresa nueva hasta venta POS aceptada:

| Componente | Participa en E2E | Evidencia funcional |
| --- | --- | --- |
| `tenant-service` | Si | Crea empresa y aisla por `company_id`. |
| `catalog-service` | Si | Crea tipo de documento requerido por terceros. |
| `thirdparty-service` | Si | Crea cliente y proveedor. |
| `inventory-service` | Si | Crea producto, stock inicial, kardex y `SALE_OUT`. |
| `billing-service` | Si | Configura emisor/resolucion, crea venta, confirma POS y orquesta efectos posteriores. |
| `dian-provider-service` | Si | Registra envio mock aceptado. |
| `accounting-service` | Si | Crea PUC basico, regla y asiento balanceado. |
| `legacy-monolith` | No | No es requerido por la prueba E2E; retirado de Docker Compose en TASK-040 lote 1. |
| `audit-service` | No | Implementado en TASK-042; aun no recibe eventos automaticos del flujo E2E. |

## Endpoints legacy y reemplazos

| Area | Endpoint legacy | Reemplazo actual | Estado |
| --- | --- | --- | --- |
| Catalogos | `/api/categorias` | `catalog-service` mantiene ruta compatible | Mantener temporal hasta crear contrato `/api/v1`. |
| Catalogos | `/api/paises` | `catalog-service` mantiene ruta compatible | Mantener temporal. |
| Catalogos | `/api/tipos-documento` | `catalog-service` mantiene ruta compatible | Mantener temporal. |
| Catalogos | `/api/metodopago` | `catalog-service` mantiene ruta compatible | Mantener temporal. |
| Catalogos | `/api/tipogasto` | `catalog-service` mantiene ruta compatible | Mantener temporal. |
| Catalogos | `/api/impuesto` | `catalog-service` mantiene ruta compatible | Mantener temporal. |
| Catalogos | `/api/parametros` | `catalog-service` mantiene ruta compatible | Mantener temporal. |
| Catalogos | `/api/productos` | `catalog-service` compatible e `inventory-service /api/v1/products` para stock real | Migracion parcial: producto inventariable ya vive en inventario; catalogo conserva compatibilidad. |
| Terceros | `/api/clientes` | `thirdparty-service` mantiene ruta compatible | Mantener temporal hasta endurecer `X-Company-Id`. |
| Terceros | `/api/proveedores` | `thirdparty-service` mantiene ruta compatible | Mantener temporal hasta endurecer `X-Company-Id`. |
| Inventario | `/api/compras` | `inventory-service /api/v1/purchases` y `/api/v1/inventory-movements` | Reemplazado funcionalmente para flujo nuevo. |
| Gastos | `/api/gastos` | No existe microservicio fisico de gastos | Migrar antes de eliminar. |
| Billing legacy | `/api/v1/issuers` | `billing-service /api/v1/issuers` y `/api/v1/issuers/current` | Reemplazado funcionalmente en TASK-041. |
| Billing legacy | `/api/v1/numbering-resolutions` | `billing-service /api/v1/numbering-resolutions` | Reemplazado funcionalmente en TASK-041. |
| Billing legacy | `/api/v1/electronic-pos` | `billing-service /api/v1/sales` cubre venta POS con numeracion fiscal configurada | Migracion parcial: POS directo y artefactos oficiales siguen pendientes. |
| Billing nuevo | `/api/v1/sales` | `billing-service` | Activo. |
| Contabilidad | `/api/v1/accounts`, `/api/v1/accounting-rules`, `/api/v1/accounting-entries`, `/api/v1/reports/*` | `accounting-service` | Reemplazado funcionalmente. |
| Proveedor DIAN mock | Proveedor dummy local del monolito | `dian-provider-service /api/v1/provider/*` | Reemplazado funcionalmente para mock. |
| Auditoria | `auditoria`, `registro_accesos` sin API nueva | `audit-service /api/v1/audit-events` | Migrar/respaldar datos antes de eliminar legacy. |

## Matriz de tablas legacy

| Tabla legacy | Reemplazo destino | Estado | Accion propuesta para TASK-040 |
| --- | --- | --- | --- |
| `roles` | `identity-service` futuro | Pendiente | Mantener. |
| `usuarios` | `identity-service` futuro | Pendiente | Mantener. |
| `auditoria` | `audit.audit_event` | Reemplazado funcionalmente para auditoria generica | Mantener hasta migrar/respaldar datos legacy. |
| `registro_accesos` | `audit.audit_event` o `identity-service` futuro | Parcial | Mantener hasta decidir ownership de accesos/identity y migrar datos. |
| `tipodocumento` | `catalog.tipodocumento` | Reemplazado por catalogo fisico | Eliminar solo despues de migrar/respaldar datos requeridos. |
| `cliente` | `thirdparty.cliente` | Reemplazado funcionalmente | Eliminar solo despues de verificar datos y endurecer aislamiento por empresa. |
| `proveedor` | `thirdparty.proveedor` | Reemplazado funcionalmente | Eliminar solo despues de verificar datos y endurecer aislamiento por empresa. |
| `categoria` | `catalog.categoria` | Reemplazado funcionalmente | Eliminar despues de validar dependencia de productos/catalogo. |
| `producto` | `inventory.product`, `inventory.stock_balance`; `catalog.producto` temporal | Reemplazado para inventario nuevo, compatibilidad parcial en catalogo | Migrar datos utiles y eliminar duplicidad de ownership. |
| `metodo_pago` | `catalog.metodo_pago` | Reemplazado funcionalmente | Eliminar despues de migrar datos requeridos. |
| `tipo_gasto` | `catalog.tipo_gasto` y futuro gastos | Parcial | Mantener mientras gastos no migre. |
| `pais` | `catalog.pais` | Reemplazado funcionalmente | Eliminar despues de migrar datos requeridos. |
| `impuesto` | `catalog.impuesto` | Reemplazado funcionalmente | Eliminar despues de migrar datos requeridos. |
| `parametros` | `catalog.parametros` | Reemplazado funcionalmente | Eliminar despues de migrar datos requeridos. |
| `compra` | `inventory.purchase` | Reemplazado para flujo nuevo | Eliminar despues de migrar datos historicos requeridos. |
| `detalle_compra` | `inventory.purchase_line` e `inventory.inventory_movement` | Reemplazado para flujo nuevo | Eliminar despues de migrar datos historicos requeridos. |
| `gastos` | Pendiente `expenses-service` o `accounting-service` | Pendiente | Mantener y decidir bounded context. |
| `detalle_gasto` | Pendiente `expenses-service` o `accounting-service` | Pendiente | Mantener y decidir bounded context. |
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

Estos elementos son candidatos, no eliminaciones aprobadas:

| Ruta | Estado | Condicion antes de eliminar |
| --- | --- | --- |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/catalog/**` | Reemplazado por `catalog-service` | Mantener rutas compatibles en `catalog-service` y ejecutar suite completa. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/thirdparty/**` | Reemplazado por `thirdparty-service` | Endurecer aislamiento por empresa o documentar compatibilidad legacy. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/inventory/**` | Reemplazado por `inventory-service` para compras/stock/kardex | Verificar migracion de historicos de compra. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/accounting/**` | Reemplazado por `accounting-service` | Verificar que no existan datos contables utiles en public schema. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/billing/**` | Parcial | Emisor/resolucion migrados a `billing-service` y auditoria central a `audit-service`; falta cerrar POS directo, productores de eventos y migracion historica. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/controller/GastoController.java` y `expenses/**` | Pendiente | Crear bounded context fisico de gastos o mover responsabilidad a contabilidad aprobada. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/models/**`, `DTO/**`, `repository/**`, `mapper/**` | Legacy interno del monolito | Eliminar junto con el modulo o por lotes, nunca antes de validar compilacion. |
| `services/legacy-monolith/src/test/java/**` | Pruebas del monolito transitorio | Remover cuando el modulo salga del reactor Maven. |

## Codigo que no debe eliminarse todavia

- `catalog-service` y `thirdparty-service` mantienen controladores con rutas legacy compatibles. Aunque el nombre del paquete sea `controller`, forman parte del microservicio fisico actual y sus pruebas cubren compatibilidad.
- `services/audit-service` ya es microservicio activo; no es codigo muerto, pero necesita integracion automatica desde los productores de eventos.
- `services/legacy-monolith` completo no debe eliminarse en un solo paso hasta cerrar las brechas de gastos, identity/accesos e historicos.
- Las migraciones legacy no deben borrarse sin una estrategia de datos. En Flyway, retirar migraciones ya aplicadas puede romper ambientes existentes.

## Riesgos antes de TASK-040

1. Gastos no tiene microservicio fisico independiente ni decision final de bounded context.
2. `audit-service` ya existe, pero los productores de eventos aun no estan conectados automaticamente.
3. Algunas rutas compatibles de catalogo y terceros todavia no exigen `X-Company-Id`.
4. Existen tablas duplicadas entre public legacy y esquemas de microservicio; se requiere plan de migracion/respaldo antes de eliminarlas.
5. `legacy-monolith` ya no hace parte del reactor Maven activo por defecto; retirarlo del repositorio exige cerrar gastos, identity/accesos, historicos, README y pruebas.

## Recomendacion para TASK-040

Ejecutar la limpieza por lotes pequenos:

1. Retirar `legacy-monolith` de Docker Compose solo si la prueba E2E pasa sin levantarlo. Ejecutado en TASK-040 lote 1; los servicios activos arrancan con `clean spring-boot:run` para evitar clases obsoletas en `target`.
2. Sacar `legacy-monolith` del reactor Maven activo y dejarlo disponible solo bajo perfil `legacy-monolith`. Ejecutado en TASK-040 lote 2.
3. Crear auditoria de conteos para tablas legacy y destino antes de borrar datos. Ejecutado en TASK-040 lote 3.
4. Migrar o documentar datos necesarios de tablas publicas reemplazadas.
5. Eliminar duplicados contables del monolito y validar `accounting-service`.
6. Eliminar duplicados de catalogo/terceros/inventario del monolito si no quedan referencias.
7. Mantener temporalmente billing legacy hasta migrar datos historicos y cerrar POS directo/trazabilidad fiscal.
8. Mantener gastos legacy hasta decidir `expenses-service` o integracion contable.
9. Ejecutar `.\mvnw.cmd test`, `docker compose config` y `.\scripts\e2e-from-zero.ps1`.
