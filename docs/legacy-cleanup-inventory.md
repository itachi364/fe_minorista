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
| `services/billing-service` | Microservicio fisico activo para venta POS y orquestacion | 56 |
| `services/dian-provider-service` | Microservicio fisico activo con mock DIAN | 34 |
| `services/accounting-service` | Microservicio fisico activo | 74 |
| `services/audit-service` | Placeholder sin codigo Java | 0 |
| `services/legacy-monolith` | Modulo transitorio legacy | 430 |

## Componentes involucrados en el flujo E2E aprobado

La prueba `scripts/e2e-from-zero.ps1` valida el flujo local desde empresa nueva hasta venta POS aceptada:

| Componente | Participa en E2E | Evidencia funcional |
| --- | --- | --- |
| `tenant-service` | Si | Crea empresa y aisla por `company_id`. |
| `catalog-service` | Si | Crea tipo de documento requerido por terceros. |
| `thirdparty-service` | Si | Crea cliente y proveedor. |
| `inventory-service` | Si | Crea producto, stock inicial, kardex y `SALE_OUT`. |
| `billing-service` | Si | Crea venta, confirma POS y orquesta efectos posteriores. |
| `dian-provider-service` | Si | Registra envio mock aceptado. |
| `accounting-service` | Si | Crea PUC basico, regla y asiento balanceado. |
| `legacy-monolith` | No | No es requerido por la prueba E2E. |
| `audit-service` | No | Aun no tiene implementacion. |

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
| Billing legacy | `/api/v1/issuers` | Solo existe en `legacy-monolith` | Migrar a `billing-service`. |
| Billing legacy | `/api/v1/numbering-resolutions` | Solo existe en `legacy-monolith` | Migrar a `billing-service`. |
| Billing legacy | `/api/v1/electronic-pos` | `billing-service /api/v1/sales` cubre venta POS; configuracion completa pendiente | Migracion parcial. |
| Billing nuevo | `/api/v1/sales` | `billing-service` | Activo. |
| Contabilidad | `/api/v1/accounts`, `/api/v1/accounting-rules`, `/api/v1/accounting-entries`, `/api/v1/reports/*` | `accounting-service` | Reemplazado funcionalmente. |
| Proveedor DIAN mock | Proveedor dummy local del monolito | `dian-provider-service /api/v1/provider/*` | Reemplazado funcionalmente para mock. |
| Auditoria | `auditoria`, `registro_accesos` sin API nueva | `audit-service` pendiente | Migrar antes de eliminar. |

## Matriz de tablas legacy

| Tabla legacy | Reemplazo destino | Estado | Accion propuesta para TASK-040 |
| --- | --- | --- | --- |
| `roles` | `identity-service` futuro | Pendiente | Mantener. |
| `usuarios` | `identity-service` futuro | Pendiente | Mantener. |
| `auditoria` | `audit-service.audit_event` futuro | Pendiente | Mantener y migrar. |
| `registro_accesos` | `audit-service` o `identity-service` futuro | Pendiente | Mantener y migrar. |
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
| `billing_issuer_profile` | Pendiente en `billing-service` | Pendiente | Migrar a esquema `billing` antes de eliminar. |
| `billing_numbering_resolution` | Pendiente en `billing-service` | Pendiente | Migrar a esquema `billing` antes de eliminar. |
| `billing_electronic_pos_document` | `billing.sale`, `billing.electronic_document` | Parcial | Mantener hasta cerrar emisor/resolucion/POS directo. |
| `billing_electronic_pos_document_line` | `billing.sale_line` | Parcial | Mantener hasta cerrar emisor/resolucion/POS directo. |
| `billing_provider_submission` | `dian_provider.provider_submission` y `billing.electronic_document` | Reemplazado para mock nuevo | Eliminar despues de migrar trazas utiles. |
| `billing_electronic_document_trace_event` | `billing.electronic_document` parcial; audit/eventos pendiente | Parcial | Mantener hasta definir auditoria/trazabilidad persistida. |
| `billing_fiscal_audit_event` | `audit-service` futuro | Pendiente | Mantener y migrar. |
| `accounting_account` | `accounting.accounting_account` | Reemplazado funcionalmente | Eliminar duplicado legacy/public despues de confirmar datos. |
| `accounting_rule` | `accounting.accounting_rule` | Reemplazado funcionalmente | Eliminar duplicado legacy/public despues de confirmar datos. |
| `accounting_rule_line` | `accounting.accounting_rule_line` | Reemplazado funcionalmente | Eliminar duplicado legacy/public despues de confirmar datos. |
| `accounting_entry` | `accounting.accounting_entry` | Reemplazado funcionalmente | Eliminar duplicado legacy/public despues de confirmar datos. |
| `accounting_entry_line` | `accounting.accounting_entry_line` | Reemplazado funcionalmente | Eliminar duplicado legacy/public despues de confirmar datos. |

## Codigo candidato a eliminar

Estos elementos son candidatos, no eliminaciones aprobadas:

| Ruta | Estado | Condicion antes de eliminar |
| --- | --- | --- |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/catalog/**` | Reemplazado por `catalog-service` | Mantener rutas compatibles en `catalog-service` y ejecutar suite completa. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/thirdparty/**` | Reemplazado por `thirdparty-service` | Endurecer aislamiento por empresa o documentar compatibilidad legacy. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/inventory/**` | Reemplazado por `inventory-service` para compras/stock/kardex | Verificar migracion de historicos de compra. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/accounting/**` | Reemplazado por `accounting-service` | Verificar que no existan datos contables utiles en public schema. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/billing/**` | Parcial | Migrar emisor, resolucion, POS directo, trazabilidad y auditoria fiscal a `billing-service`/`audit-service`. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/controller/GastoController.java` y `expenses/**` | Pendiente | Crear bounded context fisico de gastos o mover responsabilidad a contabilidad aprobada. |
| `services/legacy-monolith/src/main/java/com/msvanegasg/facturaelectronica/models/**`, `DTO/**`, `repository/**`, `mapper/**` | Legacy interno del monolito | Eliminar junto con el modulo o por lotes, nunca antes de validar compilacion. |
| `services/legacy-monolith/src/test/java/**` | Pruebas del monolito transitorio | Remover cuando el modulo salga del reactor Maven. |

## Codigo que no debe eliminarse todavia

- `catalog-service` y `thirdparty-service` mantienen controladores con rutas legacy compatibles. Aunque el nombre del paquete sea `controller`, forman parte del microservicio fisico actual y sus pruebas cubren compatibilidad.
- `services/audit-service/pom.xml` es placeholder aprobado en la estrategia de microservicios; no es codigo muerto, pero necesita implementacion posterior.
- `services/legacy-monolith` completo no debe eliminarse en un solo paso hasta cerrar las brechas de gastos, emisor/resolucion, auditoria e historicos.
- Las migraciones legacy no deben borrarse sin una estrategia de datos. En Flyway, retirar migraciones ya aplicadas puede romper ambientes existentes.

## Riesgos antes de TASK-040

1. `billing-service` fisico aun no expone configuracion real de emisor y resoluciones.
2. `audit-service` no tiene implementacion; auditoria fiscal y registro de accesos siguen pendientes.
3. Gastos no tiene microservicio fisico independiente ni decision final de bounded context.
4. Algunas rutas compatibles de catalogo y terceros todavia no exigen `X-Company-Id`.
5. Existen tablas duplicadas entre public legacy y esquemas de microservicio; se requiere plan de migracion/respaldo antes de eliminarlas.
6. `legacy-monolith` sigue listado en el reactor Maven y en Docker Compose; retirarlo exige actualizar `pom.xml`, `docker-compose.yml`, README y pruebas.

## Recomendacion para TASK-040

Ejecutar la limpieza por lotes pequenos:

1. Retirar `legacy-monolith` de Docker Compose solo si la prueba E2E pasa sin levantarlo.
2. Migrar o documentar datos necesarios de tablas publicas reemplazadas.
3. Eliminar duplicados contables del monolito y validar `accounting-service`.
4. Eliminar duplicados de catalogo/terceros/inventario del monolito si no quedan referencias.
5. Mantener temporalmente billing legacy hasta implementar emisor/resolucion en `billing-service`.
6. Mantener gastos legacy hasta decidir `expenses-service` o integracion contable.
7. Ejecutar `.\mvnw.cmd test`, `docker compose config` y `.\scripts\e2e-from-zero.ps1`.

