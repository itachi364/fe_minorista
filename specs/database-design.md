# Database Design

Este documento consolida decisiones de persistencia para los modulos nuevos y complementa `specs/data-model.md` y `specs/data-dictionary.md`.

## Principios

- PostgreSQL es la fuente de verdad transaccional.
- Cada microservicio es dueno de su schema y sus migraciones Flyway.
- Los datos de negocio deben vivir en base de datos, no en frontend.
- La SPA no debe incluir `initialState` con empresas, terceros, productos, ventas, resoluciones, usuarios empresariales ni catalogos.
- El unico seed funcional permitido para pruebas locales iniciales es el usuario global `ROOT`.
- Los catalogos oficiales y operativos se cargan por migraciones o modulo administrativo, y se consumen via BFF/catalog-service.

## Schemas

- `tenant`: empresas y licencias.
- `identity`: usuarios, roles, permisos y sesiones.
- `catalog`: catalogos globales, configuracion por empresa, departamentos y municipios.
- `thirdparty`: clientes/proveedores consolidados.
- `inventory`: productos, stock, compras y kardex.
- `billing`: ventas, documentos electronicos y consumidor final parametrizable.
- `accounting`: PUC, reglas, asientos, cuentas por cobrar/pagar, ingresos, egresos, costos y activos.
- `audit`: eventos de auditoria.
- `payroll`: trabajadores, contratos, pagos diarios, liquidaciones y nomina electronica opcional.

## Catalogos DB-only

Catalogos que deben existir en `catalog.catalog_definition` y `catalog.catalog_item`:

- `THIRD_PARTY_ROLE`
- `PERSON_TYPE`
- `ITEM_TYPE`
- `DIAN_DOCUMENT_TYPE`
- `TAX_RESPONSIBILITY`
- `TAX_REGIME`
- `SALES_TAX`
- `PAYMENT_METHOD`
- `VIRTUAL_WALLET`
- `FISCAL_DOCUMENT_TYPE`
- `FISCAL_ENVIRONMENT`
- `PAYROLL_CONTRACT_TYPE`
- `PAYROLL_WORKER_CLASSIFICATION`
- `PAYROLL_PAYMENT_FREQUENCY`
- `PAYROLL_EARNING_TYPE`
- `PAYROLL_DEDUCTION_TYPE`

DIVIPOLA se mantiene en tablas relacionales:

- `catalog.department`
- `catalog.municipality`

## Nomina

Tablas implementadas:

- `payroll.payroll_settings`
- `payroll.worker`
- `payroll.daily_labor_payment`
- `payroll.electronic_payroll_document`

Tablas objetivo futuras:

- `payroll.contract`
- `payroll.payroll_period`
- `payroll.payroll_settlement`
- `payroll.payroll_settlement_line`

Reglas:

- `payroll_settings.electronic_payroll_enabled=false` bloquea la generacion de documento soporte electronico mock.
- Los pagos diarios verbales exigen `legal_notice_accepted=true`.
- `INDEPENDENT_CONTRACTOR` se contabiliza como egreso/proveedor o gasto operativo.
- Todas las tablas de nomina incluyen `company_id` y auditoria.

## Inventario Y Consumo De Insumos

- Los servicios facturables se almacenan en `inventory.product` con `item_type='SERVICE'`, `sale_enabled=true` y `stock_tracked=false`.
- Los insumos controlados se almacenan en `inventory.product` con `item_type='SUPPLY'` y `stock_tracked=true`.
- La relacion sugerida servicio-insumo usa `inventory.service_supply_reference`; no representa una receta automatica ni descuenta stock por si sola.
- El consumo asistido confirmado por usuario reutiliza `inventory.inventory_movement` con `movement_type='CONSUMPTION_OUT'` y `source_document_type='MANUAL_SUPPLY_CONSUMPTION'`.
- No se requiere tabla nueva para TASK-113; la trazabilidad se conserva con `source_document_id`, `idempotency_key`, `reason`, `created_by` y kardex.

## Contabilidad

El modelo contable debe cubrir:

- Ingresos.
- Egresos.
- Costos de operacion.
- Activos.
- Cuentas por cobrar.
- Cuentas por pagar.
- Asientos de nomina y pagos diarios.

Si una regla PUC no existe, el comando falla con error estructurado y auditoria.

## Datos iniciales

Permitido:

- Usuario `ROOT` dummy para pruebas locales cuando la variable de entorno lo habilite.
- Catalogos regulatorios/operativos mediante migraciones controladas.

No permitido:

- Empresas demo.
- Administradores empresariales demo.
- Terceros demo.
- Productos demo.
- Resoluciones demo.
- Ventas demo.
- Catalogos hardcodeados en frontend.

## Licenciamiento

Tabla existente:

- `tenant.company_license`

Campos principales:

- `company_id`: empresa contratante.
- `plan_code`: nombre comercial o plantilla base (`BASIC`, `POS`, `FULL`, `CUSTOM`).
- `status`: `ACTIVE`, `SUSPENDED`, `EXPIRED`, `CANCELLED`.
- `valid_from`, `valid_to`: vigencia comercial.
- `max_users`, `max_monthly_documents`: limites opcionales.
- `enabled_modules`: arreglo de codigos tecnicos de modulos contratados.

Reglas:

- `enabled_modules` usa codigos en ingles para contrato tecnico, pero la UI muestra etiquetas en espanol.
- Licencias existentes sin `enabled_modules` deben migrarse a arreglo vacio o valor explicito segun migracion aprobada; una licencia sin modulos no habilita operacion empresarial.
- ROOT no depende de `tenant.company_license`.

## Productizacion operativa

### Datos requeridos por el E2E

La prueba desde cero debe crear datos en las tablas activas de cada bounded context:

- `tenant.company` y `tenant.company_license`.
- `identity.user_account`, roles, permisos y membresias empresariales.
- `catalog.catalog_definition`, `catalog.catalog_item`, `catalog.department`, `catalog.municipality`.
- `thirdparty.third_party`.
- `inventory.product`, `inventory.inventory_movement` y tablas de compra/entrada cuando existan.
- `billing.sale`, lineas de venta, documento fiscal, submission mock y configuracion de consumidor final.
- `accounting.account`, reglas contables y asientos/comprobantes.
- `audit.audit_event`.

### Compras e inventario

Si el flujo de compra no esta completamente modelado, se debe introducir o completar un modelo transaccional con:

- Cabecera de compra por empresa y proveedor.
- Lineas de compra con producto/insumo, cantidad, costo unitario, impuesto y subtotal.
- Estado de compra (`DRAFT`, `CONFIRMED`, `CANCELLED`).
- Movimiento de inventario idempotente al confirmar.
- Cuenta por pagar o egreso segun medio de pago/configuracion.
- Asiento contable balanceado segun regla PUC empresarial.

### Servicios con insumos

- `inventory.service_supply_reference` conserva referencias sugeridas entre servicio e insumos.
- El consumo real queda en `inventory.inventory_movement` con `CONSUMPTION_OUT`, `source_document_id`, `source_document_type`, `reason`, `idempotency_key` y usuario.
- No se deben crear descuentos automaticos por receta sin confirmacion explicita.

### Reportes y licencias

- Los reportes se calculan desde tablas activas de contabilidad, inventario, billing e identidad.
- El uso de licencia mensual se calcula desde documentos fiscales emitidos en `billing` y usuarios activos vinculados en `identity`.
- Las proyecciones event-driven futuras deben poder reconstruirse desde los datos canonicos persistidos.

### Depuracion futura

Ninguna tabla legacy debe eliminarse hasta que:

- Exista flujo equivalente activo.
- La tabla no tenga referencias en JPA, repositorios, SQL, migraciones posteriores o scripts.
- Los datos esten vacios o migrados/respaldados.
- Flyway valide sobre base limpia y base local actual.
