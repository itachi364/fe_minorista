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

Decision de migracion fisica:

- En local se permite iniciar con un unico contenedor PostgreSQL y una base o schema por microservicio.
- Cada microservicio debe tener sus propias migraciones Flyway y no debe modificar tablas de otro bounded context.
- Las referencias entre microservicios se guardaran como UUID externos, no como llaves foraneas cruzadas entre servicios.
- La separacion a instancias PostgreSQL independientes podra hacerse despues de estabilizar contratos y pruebas E2E.

## Entidades principales

### Tenant y seguridad

- `tenant.company`
- `identity.user_account`
- `identity.role`
- `identity.user_company`

#### `tenant.company`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador de empresa/tenant. |
| `legal_name` | varchar(180) | Si | Razon social. |
| `trade_name` | varchar(180) | No | Nombre comercial. |
| `identification_type_id` | uuid | Si | Tipo de identificacion usado por catalogo oficial. |
| `identification_number` | varchar(30) | Si | Numero de identificacion. |
| `verification_digit` | varchar(2) | No | Digito de verificacion cuando aplique. |
| `email` | varchar(180) | Si | Correo administrativo principal. |
| `status` | varchar(20) | Si | `ACTIVE` o `SUSPENDED`. |
| `created_at` | timestamptz | Si | Fecha de creacion. |
| `updated_at` | timestamptz | Si | Fecha de ultima actualizacion. |

Restricciones:

- `unique(identification_type_id, identification_number)`.
- `status in ('ACTIVE', 'SUSPENDED')`.

### Catalogos globales

- `catalog.pais`
- `catalog.tipodocumento`
- `catalog.impuesto`
- `accounting.puc_account_template`

### Catalogos configurables

- `catalog.metodo_pago`
- `catalog.tipo_gasto`
- `catalog.parametros`
- `catalog.categoria`
- `catalog.producto` como compatibilidad temporal mientras se depura el codigo legacy reemplazado por `inventory-service`.

Regla de migracion TASK-033:

- `pais`, `tipodocumento` e `impuesto` se tratan como catalogos globales iniciales.
- `metodo_pago`, `tipo_gasto`, `parametros`, `categoria` y `producto` incluyen `company_id` nullable para preparar aislamiento por empresa sin romper endpoints legacy durante la extraccion.
- `producto` se mantiene temporalmente en `catalog-service` solo por compatibilidad; el ownership funcional nuevo queda en `inventory-service` desde TASK-034.

### Terceros

- `thirdparty.cliente`
- `thirdparty.proveedor`
- `thirdparty.third_party` como modelo objetivo consolidado.
- `thirdparty.third_party_role` como roles cliente/proveedor por empresa.

Regla de migracion TASK-033:

- `thirdparty.cliente` y `thirdparty.proveedor` incluyen `company_id` nullable y constraint unico por `(company_id, id_tipo_documento, numero_documento)`.
- La implementacion conserva endpoints legacy durante la extraccion fisica. La obligatoriedad estricta de `X-Company-Id` se endurecera cuando los contratos `/api/v1` queden estabilizados.

Modelo objetivo:

- `thirdparty.third_party` consolida identidad fiscal de clientes y proveedores.
- `thirdparty.third_party_role` permite que el mismo tercero sea `CUSTOMER`, `SUPPLIER` o ambos sin duplicar documento.
- Para NIT se calcula automaticamente `verification_digit`; para otros documentos queda nulo.
- Campos clave: `company_id`, `person_type`, `identification_type_code`, `identification_number`, `verification_digit`, `full_name`, `business_name`, `trade_name`, `email`, `phone`, `address`, `municipality_code`, `tax_responsibilities`, `active`.
- Restriccion objetivo: `unique(company_id, identification_type_code, identification_number)`.

Estado TASK-047:

- `thirdparty.third_party` y `thirdparty.third_party_role` quedan creadas por Flyway en `thirdparty-service`.
- El DV NIT se calcula en dominio y se persiste como snapshot fiscal.
- Las tablas legacy `thirdparty.cliente` y `thirdparty.proveedor` se mantienen hasta ejecutar la migracion legacy completa.

### Inventario

- `inventory.product`
- `inventory.stock_balance`
- `inventory.inventory_movement`
- `inventory.purchase`
- `inventory.purchase_line`
- `inventory.service_supply_reference`

Estado TASK-034:

- `inventory-service` queda implementado fisicamente en `services/inventory-service`.
- `inventory.product` reemplaza el ownership funcional de productos inventariables; `catalog.producto` queda solo como compatibilidad legacy temporal.
- `inventory.purchase` e `inventory.purchase_line` reemplazan el modelo legacy de compras para el flujo nuevo.
- `inventory.stock_balance` mantiene stock simple por empresa/producto.
- `inventory.inventory_movement` mantiene kardex inmutable por empresa/producto y documento origen.
- TASK-048 amplia `inventory.product` para soportar bienes fisicos, servicios e insumos mediante `item_type`, `sale_enabled`, `purchase_enabled` y `stock_tracked`.
- TASK-048 agrega `inventory.service_supply_reference` como referencia informativa entre servicios e insumos sin movimientos automaticos.

#### `inventory.product`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador del producto inventariable. |
| `company_id` | uuid | Si | Empresa propietaria del producto. |
| `sku` | varchar(80) | Si | Codigo interno unico por empresa. |
| `barcode` | varchar(80) | No | Codigo de barras unico por empresa cuando exista. |
| `name` | varchar(180) | Si | Nombre comercial del producto. |
| `description` | varchar(500) | No | Descripcion operativa. |
| `item_type` | varchar(30) | Si | `PHYSICAL_GOOD`, `SERVICE` o `SUPPLY`. |
| `sale_enabled` | boolean | Si | Indica si el item puede venderse/facturarse. |
| `purchase_enabled` | boolean | Si | Indica si el item puede comprarse. |
| `stock_tracked` | boolean | Si | Indica si el item afecta stock y kardex. |
| `sale_price` | numeric(19,2) | Si | Precio de venta base. |
| `cost` | numeric(19,2) | Si | Costo vigente inicial. |
| `active` | boolean | Si | Indica si el producto esta disponible para operar. |
| `created_at` | timestamptz | Si | Fecha de creacion. |
| `updated_at` | timestamptz | Si | Fecha de actualizacion. |

Restricciones:

- `unique(company_id, sku)`.
- `unique(company_id, barcode)` cuando exista codigo de barras.
- `item_type in ('PHYSICAL_GOOD', 'SERVICE', 'SUPPLY')`.
- `SERVICE` no puede tener `stock_tracked=true`.
- El stock inicial y los movimientos solo aplican a items con `stock_tracked=true`.

#### `inventory.stock_balance`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `company_id` | uuid | Si | Empresa propietaria del saldo. |
| `product_id` | uuid | Si | Producto inventariable. |
| `current_stock` | numeric(19,4) | Si | Stock actual. |
| `reserved_stock` | numeric(19,4) | Si | Stock reservado; inicia en cero. |
| `average_cost` | numeric(19,2) | Si | Costo vigente usado para trazabilidad simple. |
| `updated_at` | timestamptz | Si | Fecha de ultima actualizacion. |

Restricciones:

- Llave primaria compuesta `(company_id, product_id)`.
- `current_stock >= 0`.
- `reserved_stock >= 0`.

#### `inventory.inventory_movement`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador del movimiento. |
| `company_id` | uuid | Si | Empresa propietaria del movimiento. |
| `product_id` | uuid | Si | Producto afectado. |
| `movement_type` | varchar(30) | Si | `PURCHASE_IN`, `SALE_OUT`, `RETURN_IN`, `ADJUSTMENT_IN`, `ADJUSTMENT_OUT`, `CONSUMPTION_OUT`, `WASTE_OUT`. |
| `quantity` | numeric(19,4) | Si | Cantidad afectada. |
| `unit_cost` | numeric(19,2) | Si | Costo unitario asociado al movimiento. |
| `previous_stock` | numeric(19,4) | Si | Stock antes del movimiento. |
| `resulting_stock` | numeric(19,4) | Si | Stock despues del movimiento. |
| `source_document_type` | varchar(40) | Si | `PURCHASE`, `SALE`, `RETURN`, `ADJUSTMENT`, `INITIAL_STOCK`, `MANUAL_SUPPLY_CONSUMPTION`, `MANUAL_SUPPLY_WASTE`. |
| `source_document_id` | uuid | Si | Documento origen logico. |
| `idempotency_key` | varchar(120) | Si | Clave de idempotencia del comando. |
| `created_by` | uuid | No | Usuario o proceso que origino el movimiento. |
| `reason` | varchar(300) | No | Motivo operativo requerido en consumos, desperdicios y ajustes manuales. |
| `movement_at` | timestamptz | Si | Fecha del movimiento. |

Restricciones:

- `unique(company_id, source_document_type, source_document_id, movement_type, idempotency_key)`.
- `quantity > 0`.
- `unit_cost >= 0`.
- `reason` obligatorio cuando `movement_type` es `CONSUMPTION_OUT` o `WASTE_OUT`.

#### `inventory.purchase` y `inventory.purchase_line`

- Una compra nace en `PENDING`.
- Al confirmarse pasa a `CONFIRMED` y genera movimientos `PURCHASE_IN`.
- La confirmacion es idempotente por compra y linea, usando la clave de idempotencia de la compra.
- `purchase_line` guarda producto, cantidad, costo unitario, subtotal, impuesto y total.
- `purchase.payment_condition` acepta `CASH` o `CREDIT`; si es `CREDIT`, `purchase.due_date` es obligatorio.
- La contabilizacion y CxP de compras se invoca best-effort contra `accounting-service` cuando `ACCOUNTING_SERVICE_URL` esta configurado; NATS reemplazara esa llamada en TASK-045.

#### `inventory.service_supply_reference`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador de la referencia. |
| `company_id` | uuid | Si | Empresa propietaria. |
| `service_product_id` | uuid | Si | Item tipo `SERVICE`. |
| `supply_product_id` | uuid | Si | Item tipo `SUPPLY` o item con stock usado como insumo. |
| `notes` | varchar(300) | No | Observacion operativa. |
| `active` | boolean | Si | Estado de la referencia. |
| `created_at` | timestamptz | Si | Fecha de creacion. |

Reglas:

- Esta tabla no genera movimientos automaticos.
- Sirve para sugerir insumos frecuentes de un servicio.
- El consumo real debe registrarse manualmente mediante `inventory.inventory_movement` en TASK-050.
- `service_product_id` debe apuntar a un item `SERVICE`.
- `supply_product_id` debe apuntar a un item con stock controlado, normalmente `SUPPLY`.

### Gastos y cuentas por pagar

Modelo fisico inicial en `accounting-service`, siguiendo el prefijo de tablas contables existente:

- `accounting_expense`
- `accounting_accounts_payable`
- `accounting_accounts_payable_payment`

#### `accounting_expense`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador del gasto. |
| `company_id` | uuid | Si | Empresa propietaria. |
| `supplier_id` | uuid | No | Proveedor/tercero asociado. |
| `expense_date` | date | Si | Fecha del gasto. |
| `concept` | varchar(250) | Si | Concepto operativo. |
| `subtotal` | numeric(38,2) | Si | Base sin impuesto. |
| `tax_total` | numeric(38,2) | Si | IVA/impuesto descontable. |
| `total` | numeric(38,2) | Si | Total del gasto. |
| `payment_condition` | varchar(20) | Si | `CASH` o `CREDIT`. |
| `due_date` | date | No | Obligatorio si es credito. |
| `status` | varchar(20) | Si | `PENDING` o `CONFIRMED`. |
| `idempotency_key` | varchar(120) | Si | Idempotencia de creacion. |

#### `accounting_accounts_payable`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador de la CxP. |
| `company_id` | uuid | Si | Empresa propietaria. |
| `supplier_id` | uuid | No | Proveedor/tercero asociado. |
| `source_type` | varchar(40) | Si | `PURCHASE` o `EXPENSE`. |
| `source_id` | uuid | Si | Documento origen. |
| `issue_date` | date | Si | Fecha de origen. |
| `due_date` | date | Si | Fecha de vencimiento. |
| `total_amount` | numeric(38,2) | Si | Valor original. |
| `paid_amount` | numeric(38,2) | Si | Valor pagado acumulado. |
| `status` | varchar(20) | Si | `OPEN`, `PARTIALLY_PAID`, `PAID`. |

#### `accounting_accounts_payable_payment`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador del pago. |
| `company_id` | uuid | Si | Empresa propietaria. |
| `accounts_payable_id` | uuid | Si | CxP pagada. |
| `payment_date` | date | Si | Fecha del pago. |
| `amount` | numeric(38,2) | Si | Valor pagado. |
| `payment_method` | varchar(80) | Si | Metodo operativo usado. |
| `reference` | varchar(120) | No | Referencia bancaria/externa. |
| `created_by` | uuid | No | Usuario que registro el pago. |

Reglas:

- Un gasto confirmado no afecta stock.
- Una compra o gasto a credito crea cuenta por pagar.
- Un pago parcial disminuye saldo y conserva trazabilidad.
- Todo registro se aisla por `company_id` y se contabiliza mediante reglas PUC parametrizables.


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

Estado TASK-035:

- `billing-service` fisico crea sus propias tablas `billing.sale`, `billing.sale_line` y `billing.electronic_document`.
- `billing.sale` registra venta POS/factura base, totales calculados e idempotencia.
- `billing.sale_line` registra producto, cantidad, precio, descuento, impuesto y total por linea.
- `billing.electronic_document` registra documento POS mock emitido en confirmacion, CUDE/QR simulado, estado fiscal y estado del proveedor.
- La numeracion autorizada real y resoluciones migradas desde legacy se conectaran en una tarea posterior; en este corte se usa secuencia local mock para pruebas funcionales.

Estado TASK-049:

- `billing.sale_line` conserva snapshot del item vendido: `product_sku`, `product_name`, `item_type` y `stock_tracked`.
- Las ventas pueden mezclar `PHYSICAL_GOOD` y `SERVICE`.
- La afectacion de inventario posterior solo se ejecuta para lineas con `stock_tracked=true`.
- Los servicios no generan consumo automatico de insumos ni kardex.

Campos minimos adicionales para orquestacion:

- `billing.sale.status`.
- `billing.sale.payment_method_id`.
- `billing.sale.customer_id`.
- `billing.electronic_document.inventory_applied_at`.
- `billing.electronic_document.accounting_applied_at`.
- `billing.electronic_document.idempotency_key`.
- `billing.sale_line.item_type`.
- `billing.sale_line.stock_tracked`.

### Proveedor tecnologico DIAN

- `dian_provider.provider_configuration`
- `dian_provider.provider_submission`
- `dian_provider.provider_response`

Estado TASK-036:

- `dian-provider-service` fisico crea `dian_provider.provider_submission`.
- La tabla registra empresa, documento, tipo de documento, clave de idempotencia, tracking ID, estado mock, CUFE/CUDE, QR, error seguro, fecha, request y response seguros.
- `unique(company_id, document_id, document_type, idempotency_key)` evita duplicar envios por reintento.
- La configuracion del proveedor real, certificados, credenciales y respuestas oficiales quedan pendientes hasta seleccionar proveedor tecnologico.

### Contabilidad

- `accounting.account`
- `accounting.accounting_entry`
- `accounting.accounting_entry_line`

### Auditoria

- `audit.audit_event`

## Politica de migracion legacy

Antes de eliminar tablas publicas legacy se debe construir una matriz de reemplazo:

| Tabla legacy | Bounded context destino | Tabla destino | Estado |
|---|---|---|---|
| `roles`, `usuarios` | `identity-service` futuro | pendiente | mantener; autenticacion/autorizacion no esta migrada |
| `auditoria`, `registro_accesos` | `audit-service` e `identity-service` | `audit.audit_event`, tablas identity futuras | `audit.audit_event` migrado en TASK-042; mantener legacy hasta migrar/respaldar datos |
| `tipodocumento`, `pais`, `impuesto`, `metodo_pago`, `parametros`, `categoria`, `tipo_gasto` | `catalog-service` | `catalog.*` | migrado fisicamente; eliminar solo despues de migracion/respaldo de datos |
| `producto` | `inventory-service` y compatibilidad `catalog-service` | `inventory.product`, `inventory.stock_balance`, `catalog.producto` temporal | migrado funcionalmente para inventario; resolver ownership final antes de limpiar |
| `cliente` | `thirdparty-service` | `thirdparty.cliente` | migrado fisicamente; pendiente endurecer `X-Company-Id` obligatorio en rutas compatibles |
| `proveedor` | `thirdparty-service` | `thirdparty.proveedor` | migrado fisicamente; pendiente endurecer `X-Company-Id` obligatorio en rutas compatibles |
| `compra`, `detalle_compra` | `inventory-service` | `inventory.purchase`, `inventory.purchase_line`, `inventory.inventory_movement` | migrado funcionalmente; limpiar historicos solo con plan aprobado |
| `gastos`, `detalle_gasto` | `accounting-service` inicialmente; `expenses/procurement-service` solo si se aprueba despues | `accounting.expense`, `accounting.accounts_payable` objetivo | mantener hasta implementar gastos/cuentas por pagar y migrar datos |
| `factura`, `detalle_factura` | `billing-service` | `billing.sale`, `billing.sale_line`, `billing.electronic_document` | parcial; POS nuevo cubierto, factura electronica completa e historicos pendientes |
| `billing_issuer_profile`, `billing_numbering_resolution` | `billing-service` | `billing.issuer_profile`, `billing.numbering_resolution` | migrado funcionalmente en TASK-041; mantener tablas legacy hasta migrar/respaldar datos |
| `billing_electronic_pos_document`, `billing_electronic_pos_document_line` | `billing-service` | `billing.sale`, `billing.sale_line`, `billing.electronic_document` | parcial; mantener hasta cerrar POS directo y numeracion real |
| `billing_provider_submission` | `dian-provider-service` y `billing-service` | `dian_provider.provider_submission`, `billing.electronic_document` | reemplazado para mock; migrar trazas utiles antes de eliminar |
| `billing_electronic_document_trace_event`, `billing_fiscal_audit_event` | `billing-service`/`audit-service` | `audit.audit_event` | mantener; falta integrar productores y migrar/respaldar datos historicos |
| `accounting_account`, `accounting_rule`, `accounting_rule_line`, `accounting_entry`, `accounting_entry_line` public legacy | `accounting-service` | `accounting.accounting_account`, `accounting.accounting_rule`, `accounting.accounting_rule_line`, `accounting.accounting_entry`, `accounting.accounting_entry_line` | reemplazado funcionalmente; eliminar duplicados solo despues de confirmar datos |

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
