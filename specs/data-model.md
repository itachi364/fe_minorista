# Data Model: Multiempresa, facturacion/POS, inventario y contabilidad

## Motor recomendado

PostgreSQL se mantiene como motor principal.

Razones:

- Soporte ACID para numeracion fiscal, inventario y contabilidad.
- Integridad referencial mediante llaves foraneas y constraints.
- Indices compuestos para aislamiento multiempresa.
- `jsonb` para respuestas del proveedor tecnologico DIAN, trazas tecnicas y payloads normalizados.
- Buen soporte para migraciones versionadas.
- Consultas relacionales necesarias para libro diario, libro mayor, kardex, ventas y reportes fiscales.

No se recomienda usar NoSQL como base principal del dominio fiscal/contable. Puede evaluarse mas adelante para logs, busqueda o eventos, pero no para la fuente de verdad transaccional.

## Estrategia multiempresa

La aplicacion sera vendida a multiples negocios. Cada empresa solo debe ver y operar su informacion.

Decision:

- Usar `company_id` como columna obligatoria en todas las tablas de datos del negocio.
- Usar catologos globales sin `company_id` cuando representen datos oficiales o compartidos: PUC base, paises, tipos de documento DIAN, codigos DIAN, formas/medios de pago oficiales.
- Usar catologos por empresa cuando el negocio los configure: productos, clientes, proveedores, bodegas, resoluciones, impuestos activos, parametros de emision.
- Agregar indices y constraints compuestos por `company_id` para impedir duplicados dentro de una empresa y permitir datos equivalentes en empresas distintas.

Regla de seguridad:

- Ninguna consulta de negocio debe ejecutarse sin filtro de `company_id`, salvo consultas de catalogos globales.

## Schemas sugeridos en PostgreSQL

Fase inicial:

- `identity`
- `tenant`
- `catalog`
- `thirdparty`
- `inventory`
- `billing`
- `dian_provider`
- `accounting`
- `audit`

Fase microservicios:

- Cada microservicio puede mantener su propio schema o base de datos.
- Los IDs externos entre servicios deben tratarse como referencias logicas, no como FK cruzadas entre bases separadas.

## Entidades principales

### Tenant y seguridad

- `tenant.company`
- `identity.user_account`
- `identity.role`
- `identity.user_company`

### Catalogos globales

- `catalog.country`
- `catalog.identification_type`
- `catalog.tax_type`
- `catalog.payment_method`
- `accounting.puc_account_template`

### Terceros

- `thirdparty.customer`
- `thirdparty.supplier`

### Inventario

- `inventory.category`
- `inventory.product`
- `inventory.stock_balance`
- `inventory.inventory_movement`
- `inventory.purchase`
- `inventory.purchase_line`

### Facturacion/POS

- `billing.issuer_profile`
- `billing.numbering_resolution`
- `billing.number_sequence`
- `billing.sale`
- `billing.sale_line`
- `billing.electronic_document`
- `billing.electronic_document_line`
- `billing.electronic_document_tax`
- `billing.electronic_document_artifact`
- `billing.adjustment_document`

### Proveedor tecnologico DIAN

- `dian_provider.provider_configuration`
- `dian_provider.provider_submission`
- `dian_provider.provider_response`

### Contabilidad

- `accounting.account`
- `accounting.accounting_entry`
- `accounting.accounting_entry_line`

### Auditoria

- `audit.audit_event`

## Relaciones principales

- Una `company` tiene muchos usuarios mediante `user_company`.
- Una `company` tiene clientes, proveedores, productos, resoluciones, ventas, documentos electronicos, movimientos de inventario y asientos contables.
- Una venta POS genera un documento electronico.
- Un documento electronico puede tener multiples lineas, impuestos, artefactos y envios al proveedor.
- Una venta facturada descuenta inventario mediante movimientos.
- Una compra confirmada incrementa inventario mediante movimientos.
- Un documento fiscal confirmado o validado genera asiento contable.
- Una cuenta contable por empresa puede originarse desde el PUC base.

## Reglas de modelado

- Todas las tablas transaccionales deben incluir:
  - `id`
  - `company_id`
  - `created_at`
  - `updated_at`
  - `created_by`
  - `updated_by`
- Las tablas fiscales deben incluir estados explicitos.
- Las tablas de documentos electronicos deben guardar identificadores fiscales: prefijo, numero, CUFE/CUDE, QR, estado proveedor, ambiente y tipo de documento.
- Los totales monetarios deben usar `numeric(19, 2)` salvo que el anexo tecnico o calculos tributarios exijan mayor precision.
- Porcentajes deben usar `numeric(7, 4)` o superior.
- Campos libres del proveedor tecnologico deben usar `jsonb`, manteniendo tambien columnas normalizadas para busqueda.

## Constraints e indices recomendados

### Multiempresa

- `unique(company_id, document_type, document_number)` en clientes y proveedores.
- `unique(company_id, barcode)` en productos cuando aplique.
- `unique(company_id, prefix, number, document_type)` en documentos electronicos.
- `unique(company_id, resolution_number, prefix, document_type)` en resoluciones.
- `index(company_id, status)` en documentos, ventas y movimientos.
- `index(company_id, created_at)` en tablas transaccionales.

### Inventario

- `check(quantity > 0)` en lineas de venta/compra.
- `check(current_stock >= 0)` en `stock_balance`, salvo decision futura para permitir negativos.
- `unique(company_id, product_id)` en `stock_balance`.

### Contabilidad

- `check(debit_amount >= 0)`.
- `check(credit_amount >= 0)`.
- Validacion de partida doble en capa de dominio/caso de uso antes de persistir.
- `unique(company_id, account_code)` en cuentas por empresa.

## Politica transaccional inicial

Venta POS/factura:

1. Crear venta.
2. Validar stock.
3. Crear documento fiscal.
4. Emitir mediante proveedor tecnologico.
5. Si el estado fiscal es aceptado/validado o confirmado segun politica definida:
   - Descontar inventario.
   - Registrar asiento contable.

Compra:

1. Crear compra.
2. Confirmar compra.
3. Aumentar stock.
4. Registrar asiento contable.

## Preguntas abiertas

- Proveedor tecnologico DIAN especifico.
- Politica final de afectacion de inventario: al crear venta, al emitir, al validar proveedor o al recibir pago.
- Bodegas multiples o unica bodega inicial.
- Manejo de caja y cierres POS.
- Plan de cuentas PUC inicial completo o subconjunto por actividad economica.
