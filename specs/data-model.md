# Data Model: Multiempresa, facturacion/POS, inventario y contabilidad

> Estado SDD: documento historico/transitorio. La fuente vigente para persistencia es `specs/database-design.md` y `specs/data-dictionary.md`. Este archivo se conserva como contexto de evolucion y matriz legacy, pero cualquier diferencia con esos documentos debe resolverse a favor de `database-design.md`/`data-dictionary.md`.

## Motor recomendado

PostgreSQL se mantiene como motor principal.

Razones:

- Soporte ACID para numeracion fiscal, inventario y contabilidad.
- Integridad referencial mediante llaves foraneas y constraints.
- Indices compuestos para aislamiento multiempresa.
- `jsonb` para respuestas de la conexion DIAN configurada por empresa, trazas tecnicas y payloads normalizados.
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
- `reporting`

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
- `identity.company_membership`
- `identity.company_membership_role`
- `identity.user_session`
- `identity.identity_access_audit`

#### `tenant.company`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador de empresa/tenant. |
| `legal_name` | varchar(180) | Si | Razon social. |
| `trade_name` | varchar(180) | No | Nombre comercial. |
| `identification_type_code` | integer | Si | Codigo DIAN de tipo de documento de identificacion. |
| `identification_number` | varchar(30) | Si | Numero de identificacion. |
| `verification_digit` | varchar(2) | No | Digito de verificacion cuando aplique. |
| `email` | varchar(180) | Si | Correo administrativo principal. |
| `status` | varchar(20) | Si | `ACTIVE` o `SUSPENDED`. |
| `created_at` | timestamptz | Si | Fecha de creacion. |
| `updated_at` | timestamptz | Si | Fecha de ultima actualizacion. |

Restricciones:

- `unique(identification_type_code, identification_number)`.
- `identification_type_code in (11, 12, 13, 21, 22, 31, 41, 42, 43, 47, 48)`.
- `status in ('ACTIVE', 'SUSPENDED')`.

#### `tenant.company_license`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador de licencia. |
| `company_id` | uuid | Si | Empresa propietaria de la licencia. |
| `plan_code` | varchar(60) | Si | Codigo comercial del plan contratado. |
| `status` | varchar(20) | Si | `ACTIVE`, `SUSPENDED`, `EXPIRED` o `CANCELLED`. |
| `valid_from` | date | Si | Fecha inicial de vigencia. |
| `valid_to` | date | Si | Fecha final de vigencia. |
| `max_users` | integer | No | Limite de usuarios permitidos por plan. |
| `max_monthly_documents` | integer | No | Limite mensual de documentos permitidos por plan. |
| `created_at` | timestamptz | Si | Fecha de creacion. |
| `updated_at` | timestamptz | Si | Fecha de ultima actualizacion. |

Restricciones:

- `unique(company_id)`.
- `foreign key(company_id) references tenant.company(id)`.
- `status in ('ACTIVE', 'SUSPENDED', 'EXPIRED', 'CANCELLED')`.
- `valid_to >= valid_from`.
- `max_users` y `max_monthly_documents` deben ser nulos o mayores que cero.
#### `identity.user_account`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador del usuario. |
| `email` | varchar(180) | Si | Correo normalizado en minuscula. |
| `full_name` | varchar(180) | Si | Nombre completo. |
| `password_hash` | varchar(500) | Si | Hash PBKDF2 del password. |
| `status` | varchar(20) | Si | `ACTIVE` o `INACTIVE`. |
| `created_at` | timestamptz | Si | Fecha de creacion. |
| `updated_at` | timestamptz | Si | Fecha de ultima actualizacion. |

Restricciones: `unique(email)`.

#### `identity.company_membership`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador de membresia. |
| `company_id` | uuid | Si | Empresa/tenant. |
| `user_id` | uuid | Si | Usuario miembro. |
| `active` | boolean | Si | Estado de la membresia. |
| `created_at` | timestamptz | Si | Fecha de creacion. |
| `updated_at` | timestamptz | Si | Fecha de ultima actualizacion. |

Restricciones: `unique(company_id, user_id)`.

#### `identity.company_membership_role`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `membership_id` | uuid | Si | Membresia asociada. |
| `role` | varchar(40) | Si | Rol asignado. |

Roles validos: `OWNER`, `ADMIN`, `CASHIER`, `ACCOUNTANT`, `AUDITOR`.

#### `identity.user_session`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador de sesion. |
| `user_id` | uuid | Si | Usuario autenticado. |
| `token_hash` | varchar(120) | Si | Hash SHA-256 del token opaco. |
| `expires_at` | timestamptz | Si | Fecha de expiracion. |
| `created_at` | timestamptz | Si | Fecha de creacion. |
| `revoked_at` | timestamptz | No | Fecha de revocacion futura. |

Restricciones: `unique(token_hash)`.

#### `identity.identity_access_audit`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador del evento de acceso. |
| `company_id` | uuid | No | Empresa relacionada cuando aplique. |
| `user_id` | uuid | No | Usuario relacionado cuando aplique. |
| `action` | varchar(80) | Si | Accion ejecutada. |
| `resource_type` | varchar(80) | Si | Tipo de recurso. |
| `resource_id` | varchar(120) | No | Recurso asociado. |
| `result` | varchar(40) | Si | `SUCCESS` o `FAILURE`. |
| `detail` | varchar(500) | No | Detalle seguro sin secretos. |
| `occurred_at` | timestamptz | Si | Fecha del evento. |

### Catalogos globales y configurables

- `catalog.catalog_definition`
- `catalog.catalog_item`
- `catalog.company_catalog_item_setting`
- `catalog.department`
- `catalog.municipality`
- `accounting.puc_account_template`

Estado TASK-088:

- Los catalogos legacy `catalog.pais`, `catalog.tipodocumento`, `catalog.impuesto`, `catalog.metodo_pago`, `catalog.tipo_gasto`, `catalog.parametros`, `catalog.categoria` y `catalog.producto` fueron retirados.
- `catalog.tipodocumento` se uso como fuente para poblar `catalog.catalog_item` con `catalog_code='DIAN_DOCUMENT_TYPE'` antes de eliminar la tabla.
- Los metodos de pago, responsabilidades fiscales, regimenes tributarios, tipos de documento DIAN, billeteras virtuales y otros catalogos parametrizables viven en `catalog.catalog_item`.
- DIVIPOLA se consulta desde `catalog.department` y `catalog.municipality`.
- Los productos de negocio viven en `inventory.product`; `catalog-service` ya no conserva ownership temporal de productos.

### Terceros

- `thirdparty.third_party` como modelo consolidado activo.
- `thirdparty.third_party_role` como roles cliente/proveedor por empresa.
- `thirdparty.third_party_tax_responsibility` como responsabilidades fiscales multiples por tercero.

Regla de migracion TASK-033:

- TASK-059 lote 2 retira endpoints legacy de terceros; el acceso runtime queda en `/api/v1` con `X-Company-Id` obligatorio.

Modelo objetivo:

- `thirdparty.third_party` consolida identidad fiscal de clientes y proveedores.
- `thirdparty.third_party_role` permite que el mismo tercero sea `CUSTOMER`, `SUPPLIER` o ambos sin duplicar documento.
- Para `identification_type_code=31` (NIT) se calcula automaticamente `verification_digit`; para otros documentos queda nulo.
- Campos clave: `company_id`, `person_type`, `identification_type_code`, `identification_number`, `verification_digit`, `full_name`, `business_name`, `trade_name`, `email`, `phone`, `address`, `municipality_code`, `tax_regime`, `active`.
- `thirdparty.third_party_tax_responsibility` conserva responsabilidades fiscales DIAN por tercero con codigos `O-13`, `O-15`, `O-23`, `O-47` o `R-99-PN`.
- Restriccion objetivo: `unique(company_id, identification_type_code, identification_number)`.

Estado TASK-047:

- `thirdparty.third_party` y `thirdparty.third_party_role` quedan creadas por Flyway en `thirdparty-service`.
- El DV NIT se calcula en dominio cuando `identification_type_code=31` y se persiste como snapshot fiscal.
- Las tablas legacy `thirdparty.cliente` y `thirdparty.proveedor` fueron retiradas en TASK-088; la migracion de limpieza aborta si detecta datos legacy con `company_id` no nulo.

### Inventario

- `inventory.product`
- `inventory.stock_balance`
- `inventory.inventory_movement`
- `inventory.purchase`
- `inventory.purchase_line`
- `inventory.service_supply_reference`

Estado TASK-034:

- `inventory-service` queda implementado fisicamente en `services/inventory-service`.
- `inventory.product` reemplaza el ownership funcional de productos inventariables.
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
- Al confirmarse pasa a `CONFIRMED` y no genera movimientos `PURCHASE_IN`.
- La confirmacion es idempotente por compra y dispara contabilidad/cuenta por pagar cuando aplica.
- `purchase_line` guarda concepto, cantidad, costo unitario, subtotal, impuesto y total; `product_id` queda opcional solo por compatibilidad historica.
- `purchase.payment_condition` acepta `CASH` o `CREDIT`; si es `CREDIT`, `purchase.due_date` es obligatorio.
- La contabilizacion y CxP de compras se invoca contra `accounting-service` con evento `PURCHASE_CONFIRMED` cuando `ACCOUNTING_SERVICE_URL` esta configurado; Outbox/Inbox con EventBridge/SQS y Lambdas reemplazara esa llamada en la tarea event-driven aprobada.

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


### Cuentas por cobrar

Modelo fisico implementado en `accounting-service`, siguiendo el prefijo de tablas contables existente:

- `accounting_accounts_receivable`
- `accounting_accounts_receivable_payment`

#### `accounting_accounts_receivable`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador de la CxC. |
| `company_id` | uuid | Si | Empresa propietaria. |
| `customer_id` | uuid | Si | Cliente/tercero asociado. |
| `source_type` | varchar(40) | Si | `SALE`, `ELECTRONIC_INVOICE`, `ELECTRONIC_POS` o `OPENING_BALANCE`. |
| `source_id` | uuid | Si | Documento origen o registro inicial aprobado. |
| `issue_date` | date | Si | Fecha de origen. |
| `due_date` | date | Si | Fecha de vencimiento. |
| `total_amount` | numeric(38,2) | Si | Valor original. |
| `paid_amount` | numeric(38,2) | Si | Valor pagado acumulado. |
| `status` | varchar(20) | Si | `OPEN`, `PARTIALLY_PAID`, `PAID`, `OVERDUE`, `CANCELLED`. |
| `idempotency_key` | varchar(120) | Si | Idempotencia de creacion por empresa. |

#### `accounting_accounts_receivable_payment`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador del recaudo. |
| `company_id` | uuid | Si | Empresa propietaria. |
| `accounts_receivable_id` | uuid | Si | CxC pagada. |
| `payment_date` | date | Si | Fecha del recaudo. |
| `amount` | numeric(38,2) | Si | Valor recaudado. |
| `payment_method` | varchar(80) | Si | Metodo operativo usado. |
| `reference` | varchar(120) | No | Referencia bancaria/externa. |
| `created_by` | uuid | No | Usuario que registro el recaudo. |

Reglas:

- Una venta/documento a credito crea cuenta por cobrar cuando el documento queda efectivo segun politica fiscal/contable.
- Un pago parcial disminuye saldo y conserva trazabilidad.
- No se permiten pagos que excedan el saldo pendiente.
- Todo registro se aisla por `company_id` y se contabiliza mediante reglas PUC parametrizables.

### Facturacion/POS

- `billing.issuer_profile`
- `billing.numbering_resolution`
- `billing.sale`
- `billing.sale_line`
- `billing.electronic_document`
- `billing.electronic_document_line`
- `billing.electronic_document_tax`
- `billing.fiscal_note`
- Artefactos fiscales tecnicos gestionados por `dian-provider-service`; almacenamiento propio en billing queda como extension documentada.

Estado TASK-035:

- `billing-service` fisico crea sus propias tablas `billing.sale`, `billing.sale_line` y `billing.electronic_document`.
- `billing.sale` registra venta POS/factura base, totales calculados e idempotencia.
- `billing.sale_line` registra producto, cantidad, precio, descuento, impuesto y total por linea.
- `billing.electronic_document` registra documento POS mock emitido en confirmacion, CUDE/QR simulado, estado fiscal y estado del proveedor.
- La numeracion fiscal se resuelve desde `billing.numbering_resolution`, que conserva `from_number`, `to_number` y `current_number`, con una resolucion activa por empresa, tipo documental y ambiente. El modo mock local puede emitir documentos de prueba, pero no reemplaza la configuracion DIAN real de cada empresa.

Estado TASK-049:

- `billing.sale_line` conserva snapshot del item vendido: `product_sku`, `product_name`, `item_type` y `stock_tracked`.
- Las ventas pueden mezclar `PHYSICAL_GOOD` y `SERVICE`.
- La afectacion de inventario posterior solo se ejecuta para lineas con `stock_tracked=true`.
- Los servicios no generan consumo automatico de insumos ni kardex.

Campos minimos adicionales para orquestacion:

- `billing.sale.status`.
- `billing.sale.payment_method_code`.
- `billing.sale.virtual_wallet_code`.
- `billing.sale.payment_method_id` queda como columna legacy transitoria sin uso funcional nuevo.
- `billing.sale.customer_id`.
- `billing.electronic_document.inventory_applied_at`.
- `billing.electronic_document.accounting_applied_at`.
- `billing.electronic_document.idempotency_key`.
- `billing.sale_line.item_type`.
- `billing.sale_line.stock_tracked`.

### Conector DIAN parametrizable por empresa

- `dian_provider.provider_configuration`
- `dian_provider.provider_submission`
- `dian_provider.provider_response`
- `dian_provider.dian_submission_event` objetivo TASK-159.
- `dian_provider.dian_submission_artifact` objetivo TASK-161.
- `dian_provider.dian_technical_validation_result` objetivo TASK-157.

Estado TASK-036:

- `dian-provider-service` fisico crea `dian_provider.provider_submission`.
- La tabla registra empresa, documento, tipo de documento, clave de idempotencia, tracking ID, estado mock, CUFE/CUDE, QR, error seguro, fecha, request y response seguros.
- `unique(company_id, document_id, document_type, idempotency_key)` evita duplicar envios por reintento.
- La configuracion DIAN real, referencias de certificados/credenciales, respuestas oficiales, validaciones tecnicas y artefactos quedan implementadas/documentadas como configuracion parametrizable por empresa en Fase 20 TASK-145 a TASK-163. Estado actualizado 2026-09-03: el transporte SOAP WCF DIAN y la carga de certificado `.p12/.pfx` quedan pendientes en TASK-273 a TASK-276. Cada empresa es responsable de su habilitacion/certificacion DIAN; la plataforma no presta servicio de proveedor tecnologico.

Extensiones SOAP objetivo:

- `dian_company_configuration.wsdl_url`: URL WSDL o singleWsdl por ambiente, no sensible.
- `dian_company_configuration.certificate_file_type`: `P12` o `PFX`, derivado del archivo validado.
- `provider_submission.soap_operation`: operacion DIAN ejecutada.
- `provider_submission.zip_key`: tracking/ZipKey retornado por DIAN.
- `provider_submission.application_response_reference`: referencia privada a ApplicationResponse almacenada como artefacto.

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
| `roles`, `usuarios` | `identity-service` | reemplazado por RBAC modular | conservar solo datos migrados o respaldados; no usar tablas legacy para login/RBAC nuevo |
| `auditoria`, `registro_accesos` | `audit-service` e `identity-service` | `audit.audit_event`, tablas identity futuras | `audit.audit_event` migrado en TASK-042; mantener legacy hasta migrar/respaldar datos |
| `tipodocumento`, `pais`, `impuesto`, `metodo_pago`, `parametros`, `categoria`, `tipo_gasto` | `catalog-service` | `catalog.catalog_definition`, `catalog.catalog_item`, `catalog.department`, `catalog.municipality` | runtime legacy retirado y tablas eliminadas en TASK-088; `tipodocumento` migrado a `DIAN_DOCUMENT_TYPE` |
| `producto` | `inventory-service` | `inventory.product`, `inventory.stock_balance` | runtime legacy retirado y tabla `catalog.producto` eliminada en TASK-088 |
| `cliente` | `thirdparty-service` | `thirdparty.third_party` y `thirdparty.third_party_role` | codigo/endpoints legacy retirados en TASK-059 lote 2; tabla `thirdparty.cliente` eliminada en TASK-088 con salvaguarda `company_id` |
| `proveedor` | `thirdparty-service` | `thirdparty.third_party` y `thirdparty.third_party_role` | codigo/endpoints legacy retirados en TASK-059 lote 2; tabla `thirdparty.proveedor` eliminada en TASK-088 con salvaguarda `company_id` |
| `compra`, `detalle_compra` | `inventory-service` | `inventory.purchase`, `inventory.purchase_line` | migrado funcionalmente como compra documental; no crea movimientos de inventario |
| `gastos`, `detalle_gasto` | `accounting-service` inicialmente; `expenses/procurement-service` solo si se aprueba despues | `accounting.expense`, `accounting.accounts_payable` objetivo | mantener hasta implementar gastos/cuentas por pagar y migrar datos |
| `factura`, `detalle_factura` | `billing-service` | `billing.sale`, `billing.sale_line`, `billing.electronic_document` | parcial; POS nuevo cubierto, factura electronica completa e historicos pendientes |
| `billing_issuer_profile`, `billing_numbering_resolution` | `billing-service` | `billing.issuer_profile`, `billing.numbering_resolution` | migrado funcionalmente en TASK-041; mantener tablas legacy hasta migrar/respaldar datos |
| `billing_electronic_pos_document`, `billing_electronic_pos_document_line` | `billing-service` | `billing.sale`, `billing.sale_line`, `billing.electronic_document` | parcial; mantener hasta cerrar POS directo y numeracion real |
| `billing_provider_submission` | `dian-provider-service` y `billing-service` | `dian_provider.provider_submission`, `billing.electronic_document` | reemplazado para mock; migrar trazas utiles antes de eliminar |
| `billing_electronic_document_trace_event`, `billing_fiscal_audit_event` | `billing-service`/`audit-service` | `audit.audit_event`, `billing.outbox_event` | reemplazado para eventos nuevos; migrar/respaldar datos historicos si existen antes de eliminar |
| `accounting_account`, `accounting_rule`, `accounting_rule_line`, `accounting_entry`, `accounting_entry_line` public legacy | `accounting-service` | `accounting.accounting_account`, `accounting.accounting_rule`, `accounting.accounting_rule_line`, `accounting.accounting_entry`, `accounting.accounting_entry_line` | reemplazado funcionalmente; eliminar duplicados solo despues de confirmar datos |

## Relaciones principales

- Una `company` tiene muchos usuarios mediante `user_company`.
- Una `company` tiene clientes, proveedores, productos, resoluciones, ventas, documentos electronicos, movimientos de inventario y asientos contables.
- Una venta POS genera un documento electronico.
- Un documento electronico puede tener multiples lineas, impuestos, artefactos, envios DIAN y eventos/validaciones tecnicas del conector.
- Una venta facturada descuenta inventario mediante movimientos.
- Una compra documental confirmada no incrementa inventario; las entradas fisicas se registran como movimientos desde `Inventario`.
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
- Campos libres de la conexion DIAN deben usar `jsonb`, manteniendo tambien columnas normalizadas para busqueda.

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
4. Emitir mediante la configuracion/conexion DIAN activa de la empresa.
5. Si el estado fiscal es aceptado/validado o confirmado segun politica definida:
   - Descontar inventario.
   - Registrar asiento contable.

Compra:

1. Crear compra.
2. Confirmar compra.
3. Aumentar stock.
4. Registrar asiento contable.

## Preguntas abiertas

- Validacion productiva con certificado real, fixtures oficiales y habilitacion DIAN por empresa antes de operacion comercial.
- Politica final de afectacion de inventario: al crear venta, al emitir, al validar proveedor o al recibir pago.
- Bodegas multiples o unica bodega inicial.
- Manejo de caja y cierres POS.
- Plan de cuentas PUC inicial completo o subconjunto por actividad economica.

### `billing.fiscal_note`

Tabla agregada en TASK-052 para notas fiscales persistidas.

| Columna | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| `id` | uuid | Si | Identificador de la nota. |
| `company_id` | uuid | Si | Empresa propietaria. |
| `original_document_id` | uuid | Si | Documento electronico original referenciado. |
| `note_type` | varchar(40) | Si | `CREDIT_NOTE`, `DEBIT_NOTE`, `POS_ADJUSTMENT_NOTE`. |
| `adjustment_kind` | varchar(30) | No | `CANCELLATION` o `CORRECTION`, solo para POS. |
| `status` | varchar(30) | Si | Estado fiscal normalizado. |
| `provider_status` | varchar(30) | Si | Estado del proveedor mock. |
| `reason` | text | Si | Motivo de la nota. |
| `prefix` | varchar(10) | Si | Prefijo fiscal asignado. |
| `document_number` | bigint | Si | Consecutivo fiscal propio. |
| `cufe_cude` | varchar(200) | Si | Identificador fiscal simulado. |
| `qr_content` | text | Si | QR simulado. |
| `subtotal`, `tax_total`, `total` | numeric(19,2) | Si | Valores fiscales de la nota. |
| `provider_tracking_id` | varchar(120) | No | Tracking mock. |
| `provider_error_code`, `provider_error_message` | varchar/text | No | Error seguro si el proveedor rechaza o falla. |
| `idempotency_key` | varchar(120) | Si | Idempotencia por empresa. |
| `issued_at` | timestamptz | Si | Fecha de emision mock. |

Restricciones principales: FK a `billing.electronic_document(id)`, unicidad por `(company_id, prefix, document_number)` y por `(company_id, idempotency_key)`.

### Modelo objetivo TASK-068/TASK-069: RBAC modular

El modelo actual de roles fijos se conserva hasta implementar la migracion. El objetivo aprobado es reemplazar `identity.company_membership_role.role` como enum fijo por roles configurables y permisos persistidos.

#### `identity.global_user_role`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `user_id` | uuid | Si | Usuario global. |
| `role_code` | varchar(40) | Si | Rol global; inicialmente solo `ROOT`. |
| `created_at` | timestamptz | Si | Fecha de asignacion. |

Restricciones:

- `primary key(user_id, role_code)`.
- `role_code in ('ROOT')` en la primera version.
- Un usuario con `ROOT` no requiere membresia empresarial para ingresar al panel global.

#### `identity.permission_catalog`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `code` | varchar(80) | Si | Codigo unico del permiso. |
| `scope` | varchar(20) | Si | `GLOBAL` o `COMPANY`. |
| `module` | varchar(60) | Si | Dominio funcional: plataforma, ventas, inventario, contabilidad, reportes, auditoria, usuarios. |
| `description` | varchar(250) | Si | Descripcion visible para administradores. |
| `active` | boolean | Si | Permiso disponible para asignacion. |

Restricciones:

- `primary key(code)`.
- Permisos con prefijo `GLOBAL_` deben tener `scope='GLOBAL'`.
- Permisos globales solo pueden asociarse a `ROOT`.

#### `identity.company_role`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador del rol empresarial. |
| `company_id` | uuid | Si | Empresa propietaria del rol. |
| `name` | varchar(100) | Si | Nombre del rol definido por la empresa. |
| `description` | varchar(250) | No | Descripcion del rol. |
| `system_seed` | boolean | Si | Indica si fue creado como plantilla inicial del sistema. |
| `active` | boolean | Si | Estado del rol. |
| `created_by` | uuid | No | Usuario que creo el rol. |
| `created_at` | timestamptz | Si | Fecha de creacion. |
| `updated_at` | timestamptz | Si | Fecha de ultima actualizacion. |

Restricciones:

- `unique(company_id, lower(name))`.
- Todo rol empresarial tiene `company_id` obligatorio.
- No existe rol empresarial global compartido entre empresas.

#### `identity.company_role_permission`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `role_id` | uuid | Si | Rol empresarial. |
| `permission_code` | varchar(80) | Si | Permiso empresarial asignado. |

Restricciones:

- `primary key(role_id, permission_code)`.
- `permission_code` debe existir en `identity.permission_catalog` con `scope='COMPANY'`.
- Prohibido asignar permisos `GLOBAL_*` a roles empresariales.

#### `identity.company_user_role_assignment`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `company_id` | uuid | Si | Empresa donde aplica la asignacion. |
| `user_id` | uuid | Si | Usuario asignado. |
| `role_id` | uuid | Si | Rol empresarial asignado. |
| `assigned_by` | uuid | No | Actor que asigno el rol. |
| `assigned_at` | timestamptz | Si | Fecha de asignacion. |
| `revoked_at` | timestamptz | No | Fecha de revocacion. |

Restricciones:

- `unique(company_id, user_id, role_id)` para asignaciones activas.
- `role_id` debe pertenecer al mismo `company_id`.
- El actor debe tener permisos efectivos estrictamente superiores al conjunto que delega.

## Modelo de catalogos versionados

#### `catalog.catalog_definition`

Define los catalogos disponibles para administracion y consumo de UI.

Campos clave: `catalog_code`, `label`, `description`, `regulatory`, `company_configurable`, `global_editable_by_root`, `active`, `sort_order`.

#### `catalog.catalog_item`

Catalogos oficiales y operativos globales por `catalog_code` e `item_code`.

Campos clave: `catalog_code`, `item_code`, `label`, `description`, `active`, `regulatory`, `source`, `source_version`, `valid_from`, `valid_to`, `sort_order`.

#### `catalog.company_catalog_item_setting`

Overlay empresarial para activar o inactivar items sin editar codigos oficiales ni duplicar catalogos globales.

Campos clave: `company_id`, `catalog_code`, `item_code`, `enabled`, `updated_at`.

Restricciones:

- `primary key(company_id, catalog_code, item_code)`.
- FK compuesta hacia `catalog.catalog_item(catalog_code, item_code)`.

#### `catalog.department`

Departamentos DIVIPOLA/DANE.

Campos clave: `department_code`, `department_name`, `active`, `source`, `source_version`, `sort_order`.

#### `catalog.municipality`

Municipios/ciudades DIVIPOLA/DANE relacionados con departamento.

Campos clave: `municipality_code`, `department_code`, `municipality_name`, `active`, `source`, `source_version`, `sort_order`.

Restricciones:

- `municipality.department_code` referencia `department.department_code`.
- La UI debe consultar municipios por departamento para evitar cargar todo DIVIPOLA como lista plana.

## Matriz de auditoria para limpieza de tablas

TASK-088 debe producir una matriz verificable antes de cualquier eliminacion.

Columnas minimas:

| Campo | Descripcion |
|---|---|
| schema_name | Esquema PostgreSQL donde vive la tabla. |
| table_name | Nombre de la tabla auditada. |
| owner_service | Microservicio o bounded context dueno actual. |
| flyway_origin | Migracion o script que creo la tabla. |
| jpa_references | Entidades, repositorios o queries que la referencian. |
| endpoint_references | Endpoints o casos de uso que dependen de la tabla. |
| row_count | Conteo de filas en la base local antes de decidir. |
| e2e_used | Indica si participa en el flujo E2E actual. |
| decision | `EN_USO`, `LEGACY_CON_DATOS`, `LEGACY_SIN_USO`, `PENDIENTE_MIGRACION` o `CANDIDATA_A_ELIMINAR`. |
| action | Mantener, migrar, respaldar, eliminar o revisar. |

## Extensiones TASK-089

### `inventory.product`

Se agregan columnas fiscales para que el producto sea la fuente tributaria de la linea POS:

- `tax_category_code`: categoria fiscal, por ejemplo `IVA`, `INC`, `EXEMPT` o `EXCLUDED`.
- `tax_code`: codigo tecnico del impuesto dentro del catalogo `SALES_TAX`.
- `tax_label`: etiqueta visible congelada para snapshot operativo.
- `tax_rate`: tarifa porcentual aplicada al producto al momento de crear la venta.

### `billing.final_consumer_profile`

Configuracion fiscal para comprador no identificado:

- `id`: uuid.
- `company_id`: uuid nullable; nulo representa perfil global.
- `profile_code`: `FINAL_CONSUMER`.
- `identification_type_code`: codigo DIAN parametrizable.
- `identification_number`: numero parametrizable; seed local inicial `222222222222`.
- `display_name`: etiqueta visible para documento, por defecto `Consumidor final`.
- `active`: estado.
- `source`, `source_version`: fuente normativa/operativa.
- `updated_at`: ultima modificacion.

Regla: este perfil no representa un tercero de negocio y no se inserta en `thirdparty.third_party`.

## Extensiones TASK-094 a TASK-112

### Politica de datos iniciales

- El unico seed funcional permitido para pruebas locales iniciales es `identity.user_account` con rol global `ROOT`.
- Los catalogos regulatorios y operativos se cargan mediante migraciones Flyway de `catalog-service`, no desde archivos frontend.
- Empresas, administradores, terceros, productos, resoluciones, ventas y datos de nomina deben crearse por API o scripts E2E.
- La SPA no debe persistir ni importar datos demo como fuente de formularios.

### Catalogos requeridos adicionales

`catalog.catalog_definition` y `catalog.catalog_item` deben incluir, como minimo:

- `THIRD_PARTY_ROLE`
- `PERSON_TYPE`
- `ITEM_TYPE`
- `PAYROLL_CONTRACT_TYPE`
- `PAYROLL_WORKER_CLASSIFICATION`
- `PAYROLL_PAYMENT_FREQUENCY`
- `PAYROLL_EARNING_TYPE`
- `PAYROLL_DEDUCTION_TYPE`

### Nomina

Schema objetivo: `payroll`.

Tablas principales:

- `payroll.payroll_settings`
- `payroll.worker`
- `payroll.daily_labor_payment`
- `payroll.electronic_payroll_document`

Tablas futuras planificadas:

- `payroll.contract`
- `payroll.payroll_period`
- `payroll.payroll_settlement`
- `payroll.payroll_settlement_line`

#### `payroll.payroll_settings`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `company_id` | uuid | Si | Empresa propietaria. |
| `electronic_payroll_enabled` | boolean | Si | Habilita documento soporte de nomina electronica mock. |
| `provider_mode` | varchar(30) | Si | `MOCK` o futuro proveedor real aprobado. |
| `updated_at` | timestamptz | Si | Fecha de ultima actualizacion. |

#### `payroll.worker`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador del trabajador/persona. |
| `company_id` | uuid | Si | Empresa propietaria. |
| `identification_type_code` | smallint | Si | Codigo DIAN de identificacion. |
| `identification_number` | varchar(40) | Si | Numero de documento. |
| `verification_digit` | smallint | No | DV si aplica. |
| `full_name` | varchar(180) | Si | Nombre completo. |
| `worker_classification` | varchar(40) | Si | `FORMAL_EMPLOYEE`, `WORKER_BY_DAYS`, `DAILY_VERBAL_PAYMENT`, `INDEPENDENT_CONTRACTOR`, `UNCLASSIFIED_OPERATIONAL_PAYMENT`. |
| `active` | boolean | Si | Estado. |
| `created_at` | timestamptz | Si | Fecha de creacion. |

#### `payroll.daily_labor_payment`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador del pago. |
| `company_id` | uuid | Si | Empresa propietaria. |
| `worker_id` | uuid | Si | Persona pagada. |
| `work_date` | date | Si | Fecha del trabajo. |
| `activity_description` | varchar(300) | Si | Actividad realizada. |
| `agreed_amount` | numeric(19,2) | Si | Valor acordado. |
| `paid_amount` | numeric(19,2) | Si | Valor pagado. |
| `payment_method_code` | varchar(40) | Si | Metodo de pago. |
| `legal_notice_accepted` | boolean | Si | Confirmacion de advertencia legal. |
| `notes` | varchar(500) | No | Observaciones. |
| `created_at` | timestamptz | Si | Fecha de registro. |

#### `payroll.electronic_payroll_document`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| `id` | uuid | Si | Identificador. |
| `company_id` | uuid | Si | Empresa propietaria. |
| `daily_labor_payment_id` | uuid | Si | Pago diario origen. |
| `cune` | varchar(120) | Si | CUNE simulado o real futuro. |
| `status` | varchar(30) | Si | `ACCEPTED`, `REJECTED` o estados futuros del proveedor. |
| `provider_response` | varchar(500) | No | Respuesta segura. |
| `created_at` | timestamptz | Si | Fecha de generacion. |

### Contabilidad ampliada

`accounting-service` debe ampliar sus reportes y reglas para:

- Ingresos por ventas/documentos.
- Egresos por compras, gastos, pagos a proveedores y contratistas.
- Costos de operacion, incluyendo inventario consumido y pagos de personal.
- Activos y movimientos de activos basicos.
- Cuentas por cobrar y por pagar.
- Asientos derivados de nomina y pagos diarios.

## Extensiones TASK-250 a TASK-258

### Producto como maestro mutable con historico estable

`inventory.product` conserva el estado vigente del item. Puede actualizarse o inactivarse, pero los hechos historicos no deben depender de volver a leer el maestro actual para reconstruir lo ocurrido.

Reglas:

- Ventas y documentos fiscales guardan snapshot de producto, impuesto, precio y totales en sus lineas.
- Reportes historicos usan snapshots o joins tolerantes a `active=false`.
- Productos inactivos no se ofrecen para nuevas ventas POS.
- El barcode existente en mantenimiento identifica el producto y activa modo actualizacion.

### Compra documental total-only

`inventory.purchase` representa factura o documento de proveedor para control administrativo/financiero. No incrementa inventario.

Modelo logico:

- Encabezado: proveedor, fecha, concepto, condicion de pago, vencimiento opcional, total y evidencia opcional.
- Lineas opcionales: descripcion y total por concepto si se requiere desglose administrativo.
- No hay cantidad, costo unitario, subtotal ni IVA como datos capturados por el usuario.

### Gasto operativo total-only

`accounting.accounting_expense` representa egresos del negocio. El usuario captura total no discriminado; si el backend conserva subtotal/IVA por compatibilidad, se derivan internamente como subtotal igual al total e IVA cero.

### Archivo empresarial

`tenant.company_file_asset` centraliza metadata de archivos empresariales. El storage fisico puede ser local/S3, pero el dominio solo maneja referencias privadas.

Relaciones logicas:

- Compras y gastos pueden referenciar `company_file_asset` como evidencia PDF.
- Branding puede reutilizar la misma estrategia de storage para logos/favicon/fondos cuando se migre desde el storage especifico vigente.
- Reportes y artefactos fiscales pueden usar la misma convencion de prefijos por empresa/categoria.

### QR de comprobante POS

`billing.electronic_document.qr_content` conserva el contenido canonico del QR. La representacion imprimible convierte ese contenido en una imagen QR escaneable.

Reglas:

- Modo mock: contenido construido desde URL base parametrizable de NexoFiscal.
- Modo DIAN real: contenido retornado por DIAN/proveedor.
- El QR no debe depender de valores hardcodeados ni de URLs privadas de storage.

## Extensiones TASK-261 a TASK-272

Estado: modelo objetivo documentado; pendiente de implementacion.

### Readiness empresarial

Modelo logico:

- `company_readiness_snapshot`: resultado consolidado de preparacion funcional por empresa.
- `company_readiness_check`: detalle por prerequisito, modulo y accion sugerida.

Reglas:

- El readiness es un diagnostico derivado; no reemplaza validaciones de dominio.
- Los checks deben poder recalcularse y auditarse.
- No se almacenan secretos, certificados, PIN, tokens ni passwords.

### Readiness contable

Modelo logico:

- Lista derivada de reglas contables faltantes por evento.
- Lista derivada de cuentas PUC sugeridas o requeridas por modulo.
- Severidad funcional: informativa, advertencia o bloqueo.

Reglas:

- El diagnostico no crea cuentas/reglas automaticamente.
- Las acciones correctivas son explicitas y trazables.

### Reportes gerenciales normalizados

Modelo logico:

- `report_dataset_cache`: cache opcional de datasets normalizados por empresa/reporte/filtros.
- `report_export_job`: mantiene jobs pesados y descargas seguras.
- Vistas agregadas futuras para ventas, vendedores, productos, gastos, compras, cartera, cuentas por pagar y flujo de caja.

Reglas:

- La UI y exportaciones consumen datasets normalizados, no JSON transaccional crudo.
- Los reportes historicos deben tolerar productos, terceros o usuarios inactivos mediante snapshots o datos historicos estables.

### Auditoria operativa

Modelo logico:

- Eventos auditables existentes enriquecidos con indices y filtros por empresa, usuario, modulo, resultado, correlation ID y rango de fechas.

Reglas:

- La auditoria visible debe sanitizar payloads.
- Eventos denegados, fallidos y exitosos deben distinguirse claramente.

### Storage y observabilidad

Modelo logico:

- `company_file_asset` puede extenderse con estado de escaneo, retencion y politica de descarga.
- `business_health_snapshot`: estado funcional consolidado para soporte.
- `service_health_event`: eventos de salud/degradacion asociados a servicios o integraciones.

Reglas:

- Los health checks tecnicos se exponen por mecanismo de observabilidad; las tablas funcionales son apoyo para soporte.
- Las URLs prefirmadas no se persisten como historico.
