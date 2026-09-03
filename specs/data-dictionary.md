# Data Dictionary

## Convenciones

- `id`: identificador primario tipo UUID o `bigint`, a decidir antes de migraciones.
- `company_id`: identificador de empresa/tenant. Obligatorio en tablas de negocio.
- `created_at`, `updated_at`: fecha/hora en UTC.
- `created_by`, `updated_by`: usuario que crea o modifica.
- `status`: estado funcional explicito.
- Montos: `numeric(19,2)`.
- Porcentajes: `numeric(7,4)`.
- Payloads externos: `jsonb`.

## tenant.company

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador de empresa. |
| legal_name | varchar(200) | Si | Razon social. |
| trade_name | varchar(200) | No | Nombre comercial. |
| identification_type_code | integer | Si | Codigo DIAN de tipo de documento. |
| identification_number | varchar(30) | Si | NIT o identificacion. |
| verification_digit | varchar(2) | No | Digito de verificacion. |
| email | varchar(150) | No | Correo principal. |
| phone | varchar(50) | No | Telefono. |
| address | varchar(250) | No | Direccion. |
| status | varchar(30) | Si | ACTIVE, INACTIVE, SUSPENDED. |

## identity.user_account

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador de usuario. |
| username | varchar(80) | Si | Nombre de usuario. |
| email | varchar(150) | Si | Correo unico. |
| password_hash | varchar(255) | Si | Hash de contrasena, nunca texto plano. |
| cognito_subject | varchar(120) | No | Claim `sub` de Cognito para autenticacion productiva; unico cuando existe. |
| status | varchar(30) | Si | ACTIVE, INACTIVE, LOCKED. |

## identity.role

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador del rol. |
| name | varchar(80) | Si | Nombre del rol. |
| description | varchar(250) | No | Descripcion. |

## identity.user_company

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| user_id | ref | Si | Usuario. |
| company_id | ref | Si | Empresa. |
| role_id | ref | Si | Rol dentro de la empresa. |
| status | varchar(30) | Si | ACTIVE, INACTIVE. |

## bff.secure_sessions

Estado: implementada por `bff-service` en `V001__create_bff_secure_sessions.sql`.

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | varchar(128) | Si | Hash SHA-256 Base64URL del identificador opaco enviado en cookie; no es el valor de cookie en claro. |
| session_type | varchar(32) | Si | `OAUTH_ATTEMPT` o `USER_SESSION`. |
| nonce | bytea | Si | Nonce AES-GCM usado para descifrar el payload. |
| encrypted_payload | bytea | Si | Payload cifrado server-side. Para `OAUTH_ATTEMPT` contiene state, nonce y code verifier temporal; para `USER_SESSION` contiene usuario, claims minimos, token interno y tokens Cognito. |
| expires_at | timestamptz | Si | Expiracion de sesion. |
| created_at | timestamptz | Si | Fecha de creacion. |

Reglas:

- La SPA nunca recibe `encrypted_payload`, tokens Cognito, refresh tokens ni token interno.
- La revocacion elimina la fila de `USER_SESSION`; el consumo de intento OAuth elimina la fila `OAUTH_ATTEMPT`.
- Las sesiones vencidas se ignoran y se eliminan en lectura.
- Auditoria y logs solo pueden registrar `id`, `user_id`, `status`, fechas y correlation ID, nunca token/cookie/CSRF en claro.

## catalog.catalog_definition

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| catalog_code | varchar(80) | Si | Codigo tecnico del catalogo en ingles, por ejemplo `DIAN_DOCUMENT_TYPE`. |
| name | varchar(160) | Si | Nombre visible del catalogo. |
| description | varchar(500) | No | Descripcion funcional. |
| regulatory | boolean | Si | Indica si el catalogo depende de normativa. |
| company_configurable | boolean | Si | Indica si la empresa puede activar/inactivar valores. |
| global_editable_by_root | boolean | Si | Indica si ROOT puede crear o editar valores globales. |
| active | boolean | Si | Estado del catalogo. |

## catalog.catalog_item

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| catalog_code | varchar(80) | Si | Codigo tecnico del catalogo. |
| item_code | varchar(80) | Si | Codigo tecnico o normativo del item. |
| label | varchar(220) | Si | Texto visible para el usuario. |
| description | varchar(500) | No | Significado funcional o normativo. |
| active | boolean | Si | Estado global del item. |
| regulatory | boolean | Si | Indica si el item depende de normativa. |
| source | varchar(120) | No | Fuente del catalogo, por ejemplo DIAN/DANE/migracion. |
| source_version | varchar(80) | No | Version o fecha de la fuente. |
| sort_order | integer | Si | Orden estable de presentacion. |

## catalog.company_catalog_item_setting

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| company_id | uuid | Si | Empresa que parametriza el item. |
| catalog_code | varchar(80) | Si | Codigo tecnico del catalogo. |
| item_code | varchar(80) | Si | Codigo tecnico o normativo del item. |
| active | boolean | Si | Estado del item para esa empresa. |

## catalog.department

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| department_code | varchar(10) | Si | Codigo DANE/DIVIPOLA del departamento. |
| department_name | varchar(160) | Si | Nombre del departamento visible en UI. |
| active | boolean | Si | Estado. |
| source | varchar(120) | No | Fuente del dato. |
| source_version | varchar(80) | No | Version o fecha de la fuente. |
| sort_order | integer | Si | Orden de presentacion. |

## catalog.municipality

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| municipality_code | varchar(10) | Si | Codigo DANE/DIVIPOLA del municipio o ciudad. |
| department_code | varchar(10) | Si | Departamento asociado. |
| municipality_name | varchar(160) | Si | Nombre visible en UI. |
| active | boolean | Si | Estado. |
| source | varchar(120) | No | Fuente del dato. |
| source_version | varchar(80) | No | Version o fecha de la fuente. |
| sort_order | integer | Si | Orden de presentacion. |

## thirdparty.customer

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador del cliente. |
| company_id | ref | Si | Empresa propietaria del dato. |
| identification_type_code | integer | Si | Codigo DIAN de tipo de documento. |
| identification_number | varchar(30) | Si | Numero de documento. |
| verification_digit | varchar(2) | No | Digito de verificacion. |
| name | varchar(200) | Si | Nombre o razon social. |
| email | varchar(150) | No | Correo. |
| phone | varchar(50) | No | Telefono. |
| address | varchar(250) | No | Direccion. |
| tax_regime | varchar(80) | No | Regimen/responsabilidad fiscal si aplica. |
| status | varchar(30) | Si | ACTIVE, INACTIVE. |

## thirdparty.supplier

Campos equivalentes a `thirdparty.customer`, orientados a proveedor.

## thirdparty.third_party

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador del tercero fiscal. |
| company_id | ref | Si | Empresa propietaria del tercero. |
| person_type | varchar(20) | Si | NATURAL, JURIDICA. |
| identification_type_code | integer | Si | Codigo DIAN de tipo de documento. |
| identification_number | varchar(30) | Si | Numero base del documento sin digito de verificacion separado. |
| verification_digit | varchar(2) | No | Digito de verificacion calculado automaticamente solo para NIT. |
| full_name | varchar(220) | No | Nombre completo para persona natural. |
| business_name | varchar(220) | No | Razon social para persona juridica. |
| trade_name | varchar(220) | No | Nombre comercial. |
| email | varchar(150) | No | Correo de contacto. |
| phone | varchar(50) | No | Telefono. |
| address | varchar(250) | No | Direccion. |
| municipality_code | varchar(20) | No | Codigo municipio DIAN/DANE cuando aplique. |
| tax_regime | varchar(30) | No | ORDINARIO, SIMPLE, RESPONSABLE_IVA, NO_RESPONSABLE_IVA. |
| active | boolean | Si | Estado. |

## thirdparty.third_party_tax_responsibility

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| third_party_id | ref | Si | Tercero fiscal. |
| tax_responsibility_code | varchar(20) | Si | O-13, O-15, O-23, O-47 o R-99-PN. |

## thirdparty.third_party_role

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| third_party_id | ref | Si | Tercero fiscal. |
| role | varchar(30) | Si | CUSTOMER, SUPPLIER. |
| active | boolean | Si | Estado del rol. |

## inventory.category

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| name | varchar(120) | Si | Nombre de categoria. |
| description | varchar(250) | No | Descripcion. |
| active | boolean | Si | Estado. |

## inventory.product

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| category_id | ref | Si | Categoria. |
| barcode | varchar(80) | No | Codigo de barras unico por empresa si existe. |
| sku | varchar(80) | No | Codigo interno. |
| name | varchar(200) | Si | Nombre. |
| description | varchar(500) | No | Descripcion. |
| item_type | varchar(30) | Si | PHYSICAL_GOOD, SERVICE, SUPPLY. |
| sale_enabled | boolean | Si | Permite vender o facturar el item. |
| purchase_enabled | boolean | Si | Permite comprar el item. |
| stock_tracked | boolean | Si | Permite afectar stock/kardex. |
| sale_price | numeric(19,2) | Si | Precio de venta base. |
| tax_type_id | ref | No | Tipo de impuesto principal. |
| active | boolean | Si | Estado. |

## inventory.stock_balance

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| product_id | ref | Si | Producto. |
| current_stock | numeric(19,4) | Si | Existencia actual. |
| reserved_stock | numeric(19,4) | Si | Stock reservado, inicialmente 0. |

## inventory.inventory_movement

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| product_id | ref | Si | Producto. |
| movement_type | varchar(40) | Si | PURCHASE_IN, SALE_OUT, RETURN_IN, ADJUSTMENT_IN, ADJUSTMENT_OUT, CONSUMPTION_OUT, WASTE_OUT. |
| quantity | numeric(19,4) | Si | Cantidad movida. |
| previous_stock | numeric(19,4) | Si | Stock antes del movimiento. |
| resulting_stock | numeric(19,4) | Si | Stock despues del movimiento. |
| source_document_type | varchar(40) | Si | SALE, PURCHASE, RETURN, ADJUSTMENT, INITIAL_STOCK, MANUAL_SUPPLY_CONSUMPTION, MANUAL_SUPPLY_WASTE. |
| source_document_id | uuid/bigint | Si | Documento origen. |
| reason | varchar(300) | No | Motivo obligatorio para CONSUMPTION_OUT y WASTE_OUT. |
| movement_at | timestamp | Si | Fecha del movimiento. |

## inventory.service_supply_reference

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| service_product_id | ref | Si | Item tipo SERVICE. |
| supply_product_id | ref | Si | Item tipo SUPPLY o bien controlado usado como insumo. |
| notes | varchar(300) | No | Observacion operativa. |
| active | boolean | Si | Estado. |
| created_at | timestamp | Si | Fecha de creacion. |

Reglas TASK-048:

- `service_product_id` debe pertenecer a la misma empresa y ser `SERVICE`.
- `supply_product_id` debe pertenecer a la misma empresa y ser un item con `stock_tracked=true`.
- La referencia no crea stock, no descuenta insumos y no genera kardex.

## inventory.purchase

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| supplier_id | ref | Si | Proveedor. |
| purchase_date | timestamp | Si | Fecha de compra. |
| subtotal | numeric(38,2) | Si | Subtotal. |
| tax_total | numeric(19,2) | Si | Impuestos. |
| total | numeric(19,2) | Si | Total. |
| status | varchar(30) | Si | DRAFT, CONFIRMED, CANCELLED. |

## inventory.purchase_line

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| purchase_id | ref | Si | Compra. |
| product_id | ref | No | Producto historico asociado, opcional por compatibilidad. |
| description | varchar(300) | Si | Concepto documental de la factura de compra. |
| quantity | numeric(19,4) | Si | Cantidad. |
| unit_price | numeric(19,2) | Si | Precio unitario. |
| tax_amount | numeric(19,2) | Si | Impuesto. |
| line_total | numeric(19,2) | Si | Total linea. |

## accounting_expense

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| supplier_id | ref | No | Proveedor. |
| expense_date | date | Si | Fecha del gasto. |
| concept | varchar(250) | Si | Concepto del gasto. |
| subtotal | numeric(19,2) | Si | Subtotal. |
| tax_total | numeric(38,2) | Si | Impuestos. |
| total | numeric(38,2) | Si | Total. |
| payment_condition | varchar(30) | Si | CASH, CREDIT. |
| due_date | date | No | Obligatorio si payment_condition=CREDIT. |
| evidence_url | varchar(500) | No | Evidencia o soporte externo. |
| status | varchar(30) | Si | PENDING, CONFIRMED. |
| idempotency_key | varchar(120) | Si | Clave de idempotencia. |

## accounting_accounts_payable

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| supplier_id | ref | No | Proveedor. |
| source_type | varchar(40) | Si | PURCHASE, EXPENSE. |
| source_id | uuid/bigint | Si | Documento origen. |
| issue_date | date | Si | Fecha origen. |
| due_date | date | Si | Fecha de vencimiento. |
| total_amount | numeric(38,2) | Si | Valor inicial. |
| paid_amount | numeric(38,2) | Si | Valor pagado acumulado. |
| balance | derivado | Si | Saldo pendiente calculado como total_amount - paid_amount. |
| status | varchar(30) | Si | OPEN, PARTIALLY_PAID, PAID. |

## accounting_accounts_payable_payment

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| accounts_payable_id | ref | Si | Cuenta por pagar. |
| payment_date | date | Si | Fecha de pago. |
| amount | numeric(38,2) | Si | Valor pagado. |
| payment_method | varchar(80) | Si | Medio de pago operativo. |
| reference | varchar(120) | No | Referencia del pago. |
| created_by | ref | No | Usuario que registro el pago. |

## billing.issuer_profile

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa emisora. |
| legal_name | varchar(200) | Si | Razon social fiscal. |
| nit | varchar(30) | Si | NIT. |
| verification_digit | varchar(2) | Si | Digito de verificacion. |
| tax_responsibilities | jsonb | No | Responsabilidades fiscales. |
| municipality_code | varchar(20) | No | Codigo municipio DIAN/DANE si aplica. |
| address | varchar(250) | No | Direccion fiscal. |
| active | boolean | Si | Perfil activo. |

## billing.numbering_resolution

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| document_type | varchar(40) | Si | ELECTRONIC_INVOICE, ELECTRONIC_POS, CREDIT_NOTE, DEBIT_NOTE. |
| resolution_number | varchar(80) | Si | Numero de resolucion. |
| prefix | varchar(20) | No | Prefijo autorizado. |
| from_number | bigint | Si | Numero inicial. |
| to_number | bigint | Si | Numero final. |
| current_number | bigint | Si | Ultimo numero usado o siguiente, segun decision tecnica. |
| valid_from | date | Si | Inicio de vigencia. |
| valid_to | date | Si | Fin de vigencia. |
| environment | varchar(30) | Si | TEST, PRODUCTION. |
| active | boolean | Si | Estado. |

## billing.sale

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador de venta. |
| company_id | ref | Si | Empresa. |
| customer_id | ref | No | Cliente/adquirente. Puede ser consumidor final si normativa lo permite. |
| payment_method_code | varchar(30) | Si | CASH, DEBIT_CARD, CREDIT_CARD, BREB_KEY, BANK_TRANSFER, VIRTUAL_WALLET. |
| virtual_wallet_code | varchar(50) | No | Billetera colombiana; obligatorio solo con VIRTUAL_WALLET. |
| payment_method_id | uuid | No | Columna legacy transitoria sin uso funcional nuevo. |
| sale_channel | varchar(40) | Si | POS, ELECTRONIC_INVOICE, OTHER. |
| sale_at | timestamp | Si | Fecha de venta. |
| subtotal | numeric(19,2) | Si | Subtotal. |
| discount_total | numeric(19,2) | Si | Descuentos. |
| tax_total | numeric(19,2) | Si | Impuestos. |
| total | numeric(19,2) | Si | Total. |
| status | varchar(30) | Si | DRAFT, CONFIRMED, INVOICED, CANCELLED. |

## billing.sale_line

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| sale_id | ref | Si | Venta. |
| product_id | ref | Si | Producto. |
| product_sku | varchar(80) | No | SKU del producto al momento de crear la venta. |
| product_name | varchar(160) | No | Nombre del producto/servicio al momento de crear la venta. |
| item_type | varchar(30) | Si | PHYSICAL_GOOD, SERVICE, SUPPLY. |
| stock_tracked | boolean | Si | Snapshot que indica si la linea afecta inventario. |
| quantity | numeric(19,4) | Si | Cantidad vendida. |
| unit_price | numeric(19,2) | Si | Precio unitario. |
| discount_amount | numeric(19,2) | Si | Descuento linea. |
| tax_amount | numeric(19,2) | Si | Impuesto linea. |
| line_total | numeric(19,2) | Si | Total linea. |

Reglas TASK-049:

- `stock_tracked=true` habilita validacion de disponibilidad y movimiento `SALE_OUT`.
- `stock_tracked=false`, normalmente servicios, no descuenta stock ni insumos automaticamente.
- El snapshot se toma desde `inventory-service` para conservar trazabilidad fiscal/operativa.

## billing.electronic_document

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| sale_id | ref | No | Venta origen. |
| document_type | varchar(40) | Si | ELECTRONIC_INVOICE, ELECTRONIC_POS, CREDIT_NOTE, DEBIT_NOTE, POS_ADJUSTMENT_NOTE. |
| buyer_name | varchar(200) | No | Nombre o razon social del adquirente cuando aplique. |
| buyer_identification_type_code | integer | No | Codigo DIAN de tipo de documento del adquirente cuando aplique. |
| buyer_document_number | varchar(40) | No | Numero de documento del adquirente cuando aplique. |
| prefix | varchar(20) | No | Prefijo. |
| number | bigint | Si | Numero fiscal. |
| issue_at | timestamp | Si | Fecha de emision. |
| cufe_cude | varchar(200) | No | CUFE o CUDE. |
| qr_content | text | No | Contenido QR. |
| xml_content | text | No | XML o referencia normalizada cuando aplique. |
| graphic_representation_content | text | No | Representacion grafica o referencia normalizada cuando aplique. |
| error_code | varchar(80) | No | Codigo de error seguro si existe rechazo/fallo. |
| error_message | varchar(500) | No | Mensaje seguro si existe rechazo/fallo. |
| subtotal | numeric(19,2) | Si | Subtotal. |
| tax_total | numeric(19,2) | Si | Impuestos. |
| total | numeric(19,2) | Si | Total. |
| provider_status | varchar(40) | Si | Estado normalizado del proveedor. |
| status | varchar(40) | Si | Estado interno del documento. |

## billing.electronic_document_trace_event

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| electronic_document_id | ref | Si | Documento electronico. |
| previous_status | varchar(40) | Si | Estado anterior. |
| new_status | varchar(40) | Si | Estado nuevo. |
| action | varchar(80) | Si | Accion fiscal. |
| result | varchar(40) | Si | SUCCESS, REJECTED, FAILED. |
| detail | varchar(500) | No | Detalle seguro. |
| user_id | ref | No | Usuario que origina la operacion. |
| occurred_at | timestamp | Si | Fecha del evento. |

## billing.electronic_document_line

Campos equivalentes a `billing.sale_line`, asociados a `electronic_document_id` para preservar snapshot fiscal.

## billing.electronic_document_tax

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| electronic_document_id | ref | Si | Documento electronico. |
| tax_code | varchar(40) | Si | Codigo de impuesto. |
| tax_name | varchar(100) | Si | Nombre. |
| tax_rate | numeric(7,4) | Si | Porcentaje. |
| taxable_amount | numeric(19,2) | Si | Base gravable. |
| tax_amount | numeric(19,2) | Si | Valor impuesto. |

## billing.electronic_document_artifact

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| electronic_document_id | ref | Si | Documento. |
| artifact_type | varchar(40) | Si | XML, PDF, QR, ATTACHED_DOCUMENT, PROVIDER_RESPONSE. |
| storage_uri | varchar(500) | No | Ubicacion del archivo. |
| content_hash | varchar(128) | No | Hash para integridad. |
| metadata | jsonb | No | Metadata tecnica. |

## billing.fiscal_adjustment_note

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| referenced_document_id | ref | Si | Factura electronica validada que origina la nota. |
| document_type | varchar(40) | Si | CREDIT_NOTE, DEBIT_NOTE. |
| reason | varchar(500) | Si | Motivo fiscal seguro. |
| subtotal | numeric(19,2) | Si | Subtotal de la nota. |
| tax_total | numeric(19,2) | Si | Impuestos de la nota. |
| total | numeric(19,2) | Si | Total de la nota. |
| status | varchar(40) | Si | Estado interno inicial DRAFT. |
| created_by | ref | No | Usuario que crea la nota. |
| created_at | timestamp | Si | Fecha de creacion. |

## billing.pos_adjustment_note

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| referenced_document_id | ref | Si | POS electronico emitido que origina la nota de ajuste. |
| adjustment_type | varchar(40) | Si | CANCELLATION, CORRECTION. |
| reason | varchar(500) | Si | Motivo fiscal seguro. |
| prefix | varchar(20) | Si | Prefijo fiscal propio de la nota de ajuste. |
| number | bigint | Si | Numero fiscal propio de la nota de ajuste. |
| subtotal | numeric(19,2) | Si | Subtotal de la nota. |
| tax_total | numeric(19,2) | Si | Impuestos de la nota. |
| total | numeric(19,2) | Si | Total de la nota. |
| status | varchar(40) | Si | Estado interno inicial NUMBER_ASSIGNED. |
| created_by | ref | No | Usuario que crea la nota. |
| created_at | timestamp | Si | Fecha de creacion. |

## dian_provider.dian_company_configuration

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| mode | varchar(20) | Si | MOCK o REAL. |
| environment | varchar(20) | Si | TEST o PRODUCTION. |
| software_id | varchar(120) | No | Identificador de software configurado por la empresa ante DIAN, si aplica. |
| software_pin_secret_ref | varchar(500) | No | Referencia segura al PIN tecnico; nunca valor real. |
| technical_key_secret_ref | varchar(500) | No | Referencia segura a clave tecnica; nunca valor real. |
| certificate_secret_ref | varchar(500) | No | Referencia segura al certificado digital de la empresa; nunca certificado real. |
| certificate_alias | varchar(180) | No | Alias funcional visible del certificado. |
| certificate_fingerprint | varchar(180) | No | Huella criptografica para identificar certificado sin exponerlo. |
| certificate_expires_at | timestamptz | No | Fecha de vencimiento del certificado. |
| service_base_url | varchar(500) | No | URL DIAN no sensible para pruebas o produccion. |
| test_set_id | varchar(120) | No | Identificador del set de pruebas/habilitacion. |
| accepted_responsibility | boolean | Si | Confirma que la empresa asume habilitacion/certificacion DIAN. |
| status | varchar(30) | Si | DRAFT, READY_FOR_TEST, TESTED, ACTIVE, INACTIVE. |
| last_test_status | varchar(30) | Si | NOT_TESTED, SUCCESS, FAILED. |
| last_test_at | timestamptz | No | Fecha de ultima prueba controlada. |
| last_test_message | varchar(500) | No | Resultado funcional de la ultima prueba sin datos sensibles. |
| updated_by | ref | No | Usuario que ejecuto la ultima mutacion. |
| created_at | timestamptz | Si | Fecha de creacion. |
| updated_at | timestamptz | Si | Fecha de actualizacion. |

Reglas:

- El software no presta servicio de proveedor tecnologico DIAN; esta tabla modela configuracion tecnica por empresa facturadora.
- Secretos, certificados, PIN y claves viven en gestor de secretos. Esta tabla solo guarda referencias y metadata no sensible.
- `company_id` no puede ser nulo ni compartirse entre empresas.
- Toda mutacion debe auditarse sin registrar valores secretos.

## dian_provider.provider_submission

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| electronic_document_id | ref | Si | Documento electronico. |
| provider_configuration_id | ref | Si | Configuracion DIAN usada. |
| tracking_id | varchar(200) | No | Identificador tecnico de la conexion DIAN o mock. |
| request_payload | jsonb | No | Payload normalizado, sin secretos. |
| response_payload | jsonb | No | Respuesta normalizada. |
| status | varchar(40) | Si | SENT, ACCEPTED, REJECTED, FAILED, TIMEOUT. |
| submitted_at | timestamp | Si | Fecha envio. |

## accounting.puc_account_template

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| code | varchar(20) | Si | Codigo PUC. |
| name | varchar(200) | Si | Nombre cuenta. |
| level | integer | Si | Nivel contable. |
| nature | varchar(20) | Si | DEBIT, CREDIT. |
| parent_code | varchar(20) | No | Cuenta padre. |
| active | boolean | Si | Estado. |

## accounting.account

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| code | varchar(20) | Si | Codigo PUC usado por la empresa. |
| name | varchar(200) | Si | Nombre. |
| account_category | varchar(40) | Si | ASSET, LIABILITY, EQUITY, INCOME, EXPENSE, COST_OF_SALES, PRODUCTION_COST, MEMORANDUM_DEBIT, MEMORANDUM_CREDIT. |
| level | varchar(40) | Si | CLASS, GROUP, ACCOUNT, SUBACCOUNT, AUXILIARY. |
| nature | varchar(20) | Si | DEBIT, CREDIT. |
| parent_account_id | ref | No | Cuenta padre. |
| active | boolean | Si | Estado. |

## accounting.accounting_entry

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| entry_date | date | Si | Fecha contable. |
| description | varchar(300) | Si | Descripcion. |
| source_type | varchar(40) | Si | SALE, PURCHASE, EXPENSE, CREDIT_NOTE, ADJUSTMENT. |
| source_id | uuid/bigint | Si | Documento origen. |
| status | varchar(30) | Si | DRAFT, POSTED, CANCELLED. |

## accounting.accounting_entry_line

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| accounting_entry_id | ref | Si | Asiento. |
| account_id | ref | Si | Cuenta contable. |
| thirdparty_id | uuid/bigint | No | Tercero relacionado. |
| debit_amount | numeric(19,2) | Si | Debito. |
| credit_amount | numeric(19,2) | Si | Credito. |
| description | varchar(300) | No | Detalle linea. |

## audit.audit_event

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | No | Empresa, si aplica. |
| user_id | ref | No | Usuario. |
| event_type | varchar(80) | Si | Tipo de evento. |
| resource_type | varchar(80) | Si | Tipo de recurso. |
| resource_id | varchar(80) | No | Identificador recurso. |
| result | varchar(40) | Si | SUCCESS, FAILURE. |
| detail | jsonb | No | Detalle sin secretos. |
| occurred_at | timestamp | Si | Fecha/hora. |

## Event Outbox/Inbox por microservicio

Tablas introducidas en TASK-062 lote 1 para preparar mensajeria AWS sin broker self-hosted. Cada tabla pertenece al esquema del servicio productor o consumidor.

### `billing.outbox_event`, `inventory.outbox_event`, `accounting_outbox_event`

| Campo | Tipo | Obligatorio | Descripcion |
|---|---|---|---|
| event_id | uuid | Si | Identificador global idempotente del evento. |
| event_type | varchar(120) | Si | Nombre canonico del evento. |
| event_version | integer | Si | Version del contrato del evento. |
| occurred_at | timestamptz | Si | Fecha/hora de ocurrencia de negocio. |
| company_id | uuid | Si | Empresa propietaria del evento. |
| aggregate_type | varchar(80) | Si | Tipo de agregado origen. |
| aggregate_id | uuid | Si | Identificador del agregado origen. |
| producer | varchar(120) | Si | Microservicio productor. |
| correlation_id | varchar(120) | No | Correlacion tecnica propagada. |
| idempotency_key | varchar(180) | No | Llave funcional para deduplicacion. |
| payload_json | jsonb | Si | Payload canonico sin secretos. |
| status | varchar(30) | Si | Estado de publicacion local: `PENDING`, `PUBLISHED` o `FAILED`. |
| publish_attempts | integer | Si | Intentos de publicacion del dispatcher. |
| last_error | text | No | Error tecnico seguro del ultimo intento. |
| published_at | timestamptz | No | Fecha/hora de publicacion exitosa. |
| created_at | timestamptz | Si | Fecha/hora de persistencia en Outbox. |

### `billing.inbox_event`, `inventory.inbox_event`, `accounting_inbox_event`

| Campo | Tipo | Obligatorio | Descripcion |
|---|---|---|---|
| id | uuid | Si | Identificador local del registro Inbox. |
| event_id | uuid | Si | Evento consumido. |
| event_type | varchar(120) | Si | Tipo canonico consumido. |
| company_id | uuid | Si | Empresa del evento. |
| consumer | varchar(120) | Si | Consumidor que materializo o descarto idempotentemente el evento. |
| processed_at | timestamptz | Si | Fecha/hora de procesamiento. |

Regla: `event_id + consumer` debe ser unico para impedir reprocesamiento no idempotente.

### `reporting.reporting_inbox_event`

| Campo | Tipo | Obligatorio | Descripcion |
|---|---|---|---|
| id | uuid | Si | Identificador local del registro Inbox. |
| event_id | uuid | Si | Evento consumido. |
| event_type | varchar(120) | Si | Tipo canonico consumido. |
| company_id | uuid | Si | Empresa del evento. |
| consumer_name | varchar(120) | Si | Consumidor que materializo o descarto idempotentemente el evento. |
| processed_at | timestamptz | Si | Fecha/hora de procesamiento. |

### `reporting.reporting_event_projection`

| Campo | Tipo | Obligatorio | Descripcion |
|---|---|---|---|
| id | uuid | Si | Identificador de la proyeccion. |
| event_id | uuid | Si | Evento canonico origen. |
| event_type | varchar(120) | Si | Tipo de evento materializado. |
| company_id | uuid | Si | Empresa propietaria. |
| occurred_at | timestamptz | Si | Fecha/hora de ocurrencia del evento. |
| period_date | date | Si | Fecha de periodo usada para filtros de reporte. |
| aggregate_type | varchar(80) | Si | Tipo de agregado origen. |
| aggregate_id | uuid | Si | Identificador del agregado origen. |
| source_service | varchar(120) | Si | Servicio productor. |
| status | varchar(80) | No | Estado derivado del payload, si aplica. |
| amount | numeric(19,2) | No | Monto derivado del evento, si aplica. |
| correlation_id | varchar(120) | No | Correlacion tecnica propagada. |
| payload_json | jsonb | Si | Payload canonico completo para reconstruccion/diagnostico de reportes. |
| created_at | timestamptz | Si | Fecha/hora de materializacion. |
## Catalogos versionados

### `catalog.catalog_definition`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| catalog_code | varchar(80) | Si | Codigo tecnico del catalogo en ingles, por ejemplo `PAYMENT_METHOD`. |
| label | varchar(180) | Si | Nombre visible en espanol para la UI. |
| description | varchar(300) | No | Descripcion funcional del catalogo. |
| regulatory | boolean | Si | Indica si el catalogo es regulatorio/oficial. |
| company_configurable | boolean | Si | Indica si una empresa puede configurar activacion o extensiones permitidas. |
| global_editable_by_root | boolean | Si | Indica si ROOT puede crear, editar o inactivar items globales desde UI. |
| active | boolean | Si | Catalogo disponible para consumo y administracion. |
| sort_order | integer | Si | Orden de presentacion en el selector de catalogos. |

### `catalog.catalog_item`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| catalog_code | varchar(80) | Si | Codigo del catalogo, por ejemplo `PAYMENT_METHOD`. |
| item_code | varchar(80) | Si | Codigo del item dentro del catalogo. |
| label | varchar(180) | Si | Texto visible para usuario. |
| description | varchar(300) | No | Descripcion funcional o normativa. |
| active | boolean | Si | Disponibilidad global del item. |
| regulatory | boolean | Si | Indica si proviene de catalogo oficial/regulatorio. |
| source | varchar(80) | Si | Fuente del catalogo. |
| source_version | varchar(40) | Si | Version/corte de la fuente. |
| valid_from | date | No | Inicio de vigencia normativa. |
| valid_to | date | No | Fin de vigencia normativa. |
| sort_order | integer | Si | Orden de presentacion. |

### `catalog.company_catalog_item_setting`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| company_id | uuid | Si | Empresa que configura el item. |
| catalog_code | varchar(80) | Si | Catalogo global. |
| item_code | varchar(80) | Si | Item global. |
| enabled | boolean | Si | Item habilitado o inhabilitado para la empresa. |
| updated_at | timestamptz | Si | Fecha de ultima modificacion. |

## payroll.payroll_settings

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| company_id | uuid | Si | Empresa propietaria de la configuracion. |
| electronic_payroll_enabled | boolean | Si | Habilita nomina electronica opcional mock. |
| provider_mode | varchar(30) | Si | MOCK o conector real futuro aprobado. |
| updated_at | timestamptz | Si | Fecha de ultima modificacion. |

## payroll.worker

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del trabajador o persona pagada. |
| company_id | uuid | Si | Empresa propietaria. |
| identification_type_code | smallint | Si | Codigo DIAN de tipo de documento. |
| identification_number | varchar(40) | Si | Numero de documento. |
| verification_digit | smallint | No | Digito de verificacion cuando aplique. |
| full_name | varchar(180) | Si | Nombre completo. |
| worker_classification | varchar(40) | Si | FORMAL_EMPLOYEE, WORKER_BY_DAYS, DAILY_VERBAL_PAYMENT, INDEPENDENT_CONTRACTOR o UNCLASSIFIED_OPERATIONAL_PAYMENT. |
| active | boolean | Si | Estado. |
| created_at | timestamptz | Si | Fecha de creacion. |

## payroll.contract

Tabla planificada para fase posterior. Todavia no existe en Flyway.

## payroll.daily_labor_payment

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del pago diario. |
| company_id | uuid | Si | Empresa propietaria. |
| worker_id | uuid | Si | Persona pagada. |
| work_date | date | Si | Fecha del trabajo realizado. |
| activity_description | varchar(300) | Si | Actividad realizada. |
| agreed_amount | numeric(19,2) | Si | Valor acordado. |
| paid_amount | numeric(19,2) | Si | Valor pagado. |
| payment_method_code | varchar(40) | Si | Metodo de pago usado. |
| legal_notice_accepted | boolean | Si | Confirmacion auditada de advertencia legal. |
| notes | varchar(500) | No | Observaciones del acuerdo verbal o pago. |
| created_at | timestamptz | Si | Fecha de registro. |

## payroll.electronic_payroll_document

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del soporte electronico de nomina. |
| company_id | uuid | Si | Empresa propietaria. |
| daily_labor_payment_id | uuid | Si | Pago diario origen. |
| cune | varchar(120) | Si | CUNE simulado o real futuro. |
| status | varchar(30) | Si | ACCEPTED, REJECTED o estados futuros del proveedor. |
| provider_response | varchar(500) | No | Respuesta segura del mock/proveedor. |
| created_at | timestamptz | Si | Fecha de generacion. |

## Politica de datos iniciales

- El unico seed funcional permitido para pruebas locales iniciales es el usuario `ROOT`.
- Los catalogos se cargan en base de datos mediante migraciones o modulo administrativo.
- No se permiten datos demo de negocio como fuente de formularios frontend.

### `catalog.department`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| department_code | varchar(2) | Si | Codigo DANE/DIVIPOLA del departamento. |
| department_name | varchar(120) | Si | Nombre del departamento. |
| active | boolean | Si | Departamento disponible para seleccion. |
| source | varchar(80) | Si | Fuente de datos. |
| source_version | varchar(40) | Si | Version/corte de la fuente. |
| sort_order | integer | Si | Orden de presentacion. |

### `catalog.municipality`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| municipality_code | varchar(5) | Si | Codigo DANE/DIVIPOLA del municipio o ciudad. |
| department_code | varchar(2) | Si | Departamento asociado. |
| municipality_name | varchar(160) | Si | Nombre del municipio o ciudad. |
| active | boolean | Si | Municipio disponible para seleccion. |
| source | varchar(80) | Si | Fuente de datos. |
| source_version | varchar(40) | Si | Version/corte de la fuente. |
| sort_order | integer | Si | Orden de presentacion dentro del departamento. |

## Extensiones TASK-089

### `inventory.product` columnas fiscales

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| tax_category_code | varchar(40) | Si | Categoria fiscal del impuesto de venta del producto. |
| tax_code | varchar(80) | Si | Codigo tecnico del item en catalogo `SALES_TAX`. |
| tax_label | varchar(180) | Si | Etiqueta visible en espanol usada como snapshot. |
| tax_rate | numeric(7,4) | Si | Tarifa porcentual usada para calcular la linea fiscal. |

### `billing.final_consumer_profile`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del perfil. |
| company_id | uuid | No | Empresa propietaria del override; nulo para perfil global. |
| profile_code | varchar(40) | Si | Codigo funcional; inicialmente `FINAL_CONSUMER`. |
| identification_type_code | integer | Si | Codigo DIAN de tipo de documento parametrizado para consumidor final. |
| identification_number | varchar(30) | Si | Numero parametrizado; seed local `222222222222`. |
| display_name | varchar(180) | Si | Nombre a usar en documento, por defecto `Consumidor final`. |
| active | boolean | Si | Perfil habilitado. |
| source | varchar(80) | Si | Fuente normativa/operativa. |
| source_version | varchar(40) | Si | Version/corte de fuente. |
| updated_at | timestamptz | Si | Fecha de ultima modificacion. |

## Extensiones TASK-153 a TASK-163

Estado: implementado por `dian-provider-service` mediante migracion Flyway V003 para trazas tecnicas, artefactos y validaciones del flujo DIAN configurable por empresa.

### `dian_provider.dian_submission_event`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del evento tecnico DIAN. |
| company_id | uuid | Si | Empresa propietaria del documento y configuracion DIAN. |
| submission_id | uuid | Si | Envio DIAN asociado. |
| document_id | uuid | Si | Documento fiscal de negocio asociado. |
| event_type | varchar(40) | Si | XML_BUILT, SIGNED, VALIDATED, TRANSMITTED, ACCEPTED, REJECTED, RETRY_SCHEDULED o FAILED. |
| status | varchar(20) | Si | SUCCESS, FAILURE o PENDING. |
| dian_code | varchar(80) | No | Codigo DIAN o tecnico sanitizado. |
| dian_message | varchar(500) | No | Mensaje DIAN sanitizado, sin payload completo ni secretos. |
| correlation_id | varchar(120) | Si | Correlacion transversal. |
| created_at | timestamptz | Si | Fecha del evento. |

### `dian_provider.dian_submission_artifact`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del artefacto tecnico. |
| company_id | uuid | Si | Empresa propietaria. |
| submission_id | uuid | Si | Envio DIAN asociado. |
| document_id | uuid | Si | Documento fiscal asociado. |
| artifact_type | varchar(40) | Si | UNSIGNED_XML, SIGNED_XML, ATTACHED_DOCUMENT, ZIP, QR, GRAPHIC_REPRESENTATION, APPLICATION_RESPONSE o DIAN_RESPONSE. |
| storage_bucket_reference | varchar(180) | No | Alias o referencia privada de bucket/storage. |
| storage_key | varchar(500) | Si | Key privada cifrada o referencia interna; no es URL publica. |
| content_type | varchar(120) | Si | Tipo MIME validado. |
| file_name | varchar(220) | Si | Nombre sugerido para descarga. |
| content_hash | varchar(120) | Si | Hash para integridad/trazabilidad. |
| size_bytes | bigint | No | Tamano del artefacto. |
| created_at | timestamptz | Si | Fecha de generacion. |
| created_by | uuid | No | Usuario o proceso que origino el artefacto. |

### `dian_provider.dian_technical_validation_result`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del resultado de validacion. |
| company_id | uuid | Si | Empresa propietaria. |
| submission_id | uuid | Si | Envio DIAN asociado. |
| document_id | uuid | Si | Documento fiscal asociado. |
| validation_type | varchar(40) | Si | XSD, SCHEMATRON, CODE_LIST o SIGNATURE. |
| result | varchar(20) | Si | PASSED, FAILED o SKIPPED. |
| rule_code | varchar(120) | No | Regla tecnica o codigo de validacion. |
| message | varchar(500) | No | Mensaje sanitizado. |
| source_version | varchar(80) | Si | Version/anexo/fuente tecnica usada. |
| validated_at | timestamptz | Si | Fecha de validacion. |

## Extensiones TASK-179 a TASK-189

### `tenant.company_branding`

Tabla implementada por Flyway `V005__create_company_branding.sql` para branding empresarial por empresa.

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| company_id | uuid | Si | Empresa propietaria del branding; PK/FK hacia `tenant.company`. |
| display_name | varchar(180) | No | Nombre visual opcional para UI. |
| primary_color | varchar(20) | No | Color primario aprobado para tema visual. |
| accent_color | varchar(20) | No | Color secundario aprobado para tema visual. |
| main_logo_storage_key | varchar(500) | No | Referencia segura del logo principal en storage. |
| header_logo_storage_key | varchar(500) | No | Referencia segura del logo de encabezado. |
| login_logo_storage_key | varchar(500) | No | Referencia segura del logo de login. |
| favicon_storage_key | varchar(500) | No | Referencia segura del favicon empresarial. |
| main_logo_content_type | varchar(80) | No | MIME validado del logo principal. |
| header_logo_content_type | varchar(80) | No | MIME validado del logo de encabezado. |
| login_logo_content_type | varchar(80) | No | MIME validado del logo de login. |
| favicon_content_type | varchar(80) | No | MIME validado del favicon. |
| main_logo_hash | varchar(120) | No | Hash del archivo para trazabilidad/cache. |
| header_logo_hash | varchar(120) | No | Hash del archivo para trazabilidad/cache. |
| login_logo_hash | varchar(120) | No | Hash del archivo para trazabilidad/cache. |
| favicon_hash | varchar(120) | No | Hash del archivo para trazabilidad/cache. |
| updated_by | uuid | No | Usuario que actualizo el branding. |
| updated_at | timestamptz | Si | Fecha de ultima actualizacion. |

### `reporting.report_definition`

Catalogo backend objetivo de reportes avanzados. En la implementacion actual el catalogo se resuelve desde codigo/configuracion de `reporting-service`; no existe tabla `report_definition` en Flyway hasta justificar administracion dinamica persistida.

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| code | varchar(80) | Si | Codigo tecnico del reporte, por ejemplo `SALES_BY_SELLER`. |
| label | varchar(180) | Si | Nombre visible en espanol. |
| description | varchar(500) | No | Descripcion funcional del reporte. |
| required_modules | jsonb | Si | Modulos de licencia requeridos. |
| required_permissions | jsonb | Si | Permisos RBAC requeridos. |
| date_range_required | boolean | Si | Indica si el rango de fechas es obligatorio. |
| allowed_chart_types | jsonb | Si | Tipos de grafico permitidos: `TABLE`, `BAR`, `LINE`, `PIE`, `KPI`. |
| export_formats | jsonb | Si | Formatos habilitados: `CSV`, `XLSX`, `PDF` cuando aplique. |
| active | boolean | Si | Disponibilidad del reporte. |

### `reporting.report_execution`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador de ejecucion. |
| company_id | uuid | Si | Empresa propietaria de la consulta. |
| report_code | varchar(80) | Si | Reporte solicitado. |
| requested_by | uuid | Si | Usuario que ejecuto el reporte. |
| requested_at | timestamptz | Si | Fecha/hora de solicitud. |
| from_date | date | No | Fecha inicial del reporte. |
| to_date | date | No | Fecha final del reporte. |
| filters_json | jsonb | Si | Filtros normalizados sin datos sensibles. |
| chart_type | varchar(20) | Si | Tipo de visualizacion solicitada. |
| status | varchar(40) | Si | `SUCCESS`, `FAILED` o `VALIDATION_ERROR`. |
| correlation_id | varchar(120) | No | Identificador de correlacion. |

### `reporting.report_export_job`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del job de exportacion. |
| company_id | uuid | Si | Empresa propietaria. |
| requested_by_user_id | uuid | No | Usuario solicitante. |
| report_code | varchar(80) | Si | Reporte exportado. |
| format | varchar(12) | Si | Formato solicitado, por ejemplo `CSV` o `XLSX`. |
| chart_type | varchar(20) | Si | Tipo de visualizacion solicitado. |
| from_date | date | No | Fecha inicial del reporte. |
| to_date | date | No | Fecha final del reporte. |
| filters_json | text | Si | Filtros normalizados sin secretos. |
| notify_by_email | boolean | Si | Indica si se debe notificar disponibilidad. |
| status | varchar(20) | Si | `PENDING`, `PROCESSING`, `READY`, `FAILED`, `EXPIRED` o `REVOKED`. |
| requested_at | timestamptz | Si | Fecha/hora de solicitud. |
| started_at | timestamptz | No | Fecha/hora de inicio del procesamiento. |
| completed_at | timestamptz | No | Fecha/hora de finalizacion. |
| expires_at | timestamptz | Si | Fecha/hora de expiracion del archivo/job. |
| token_hash | varchar(128) | No | Hash del token intermediado; no almacena token plano. |
| token_expires_at | timestamptz | No | Expiracion del token intermediado. |
| storage_key | varchar(500) | No | Referencia segura del archivo generado. |
| content_type | varchar(120) | No | Tipo MIME del archivo. |
| filename | varchar(220) | No | Nombre de descarga sugerido. |
| file_size | bigint | No | Tamano del archivo generado. |
| failure_message | varchar(500) | No | Mensaje de fallo sanitizado. |
| notification_status | varchar(20) | Si | Estado de notificacion, por defecto `NOT_REQUESTED`. |
| notification_message | varchar(500) | No | Mensaje seguro asociado a la notificacion. |
| download_attempts | integer | Si | Conteo de intentos de descarga. |
| last_downloaded_at | timestamptz | No | Fecha/hora de ultima descarga. |
| created_at | timestamptz | Si | Fecha/hora de creacion del registro. |
| updated_at | timestamptz | Si | Fecha/hora de ultima actualizacion. |

### `reporting.report_export_download_attempt`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del intento. |
| job_id | uuid | Si | Job de exportacion asociado. |
| company_id | uuid | Si | Empresa propietaria. |
| attempted_at | timestamptz | Si | Fecha/hora del intento. |
| result | varchar(20) | Si | Resultado del intento. |
| detail | varchar(300) | No | Detalle sanitizado. |

### `billing.fiscal_document_artifact`

Tabla objetivo para artefactos POS/documento fiscal persistidos en `billing-service`. En la implementacion actual los artefactos fiscales tecnicos se conservan principalmente desde `dian-provider-service` y las salidas imprimibles se generan/controlan desde el flujo de documento; esta tabla queda como extension si se requiere almacenamiento fiscal propio en billing.

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del artefacto. |
| company_id | uuid | Si | Empresa propietaria. |
| document_id | uuid | Si | Documento fiscal asociado. |
| artifact_type | varchar(40) | Si | `PRINTABLE_HTML`, `XML`, `JSON_METADATA`, `QR` o `PDF`. |
| storage_key | varchar(500) | Si | Referencia segura del archivo. |
| content_type | varchar(120) | Si | MIME del artefacto. |
| file_name | varchar(220) | No | Nombre sugerido. |
| content_hash | varchar(120) | Si | Hash para integridad. |
| generated_at | timestamptz | Si | Fecha/hora de generacion. |
| generated_by | uuid | No | Usuario o proceso generador. |
| active | boolean | Si | Indica si el artefacto esta vigente. |

### `billing.pos_print_job`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador de solicitud de impresion. |
| company_id | uuid | Si | Empresa propietaria. |
| document_id | uuid | Si | Documento POS asociado. |
| paper_width_mm | integer | Si | Ancho del papel, inicialmente 58 u 80. |
| strategy | varchar(40) | Si | Estrategia inicial `WEB_PRINT`. |
| status | varchar(40) | Si | `REQUESTED`, `OPENED`, `PRINTED`, `FAILED` o `CANCELLED`. |
| requested_by | uuid | Si | Usuario que solicito impresion/reimpresion. |
| requested_at | timestamptz | Si | Fecha/hora de solicitud. |
| printed_at | timestamptz | No | Fecha/hora informada de impresion. |
| error_message | varchar(500) | No | Error sanitizado si falla. |
| correlation_id | varchar(120) | No | Correlacion tecnica. |

## Extensiones TASK-250 a TASK-258

### `inventory.product` mantenimiento operativo

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| active | boolean | Si | Indica si el producto esta disponible para operaciones futuras. Inactivo no se elimina. |
| updated_at | timestamptz | Si | Fecha de ultima actualizacion de datos maestros. |

Reglas:

- `active=false` excluye el producto de ventas nuevas, pero no de reportes historicos.
- `sku` y `barcode` deben ser unicos por empresa.
- Cambios de nombre, precio, impuesto o costo no reescriben snapshots de ventas/documentos existentes.

### `tenant.company_file_asset`

Tabla objetivo para metadata de archivos empresariales y evidencias documentales.

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del archivo. |
| company_id | uuid | Si | Empresa propietaria. |
| category | varchar(60) | Si | Categoria tecnica: `INVOICE`, `LOGO`, `BACKGROUND`, `PURCHASE_EVIDENCE`, `EXPENSE_EVIDENCE` u `OTHER`. |
| storage_key | varchar(700) | Si | Referencia privada del objeto; no se expone como URL publica permanente. |
| original_filename | varchar(220) | Si | Nombre original informado por el cliente. |
| content_type | varchar(120) | Si | MIME validado. |
| file_size | bigint | Si | Tamano del archivo. |
| content_hash | varchar(120) | Si | Hash para integridad y trazabilidad. |
| uploaded_by | uuid | No | Usuario que cargo el archivo. |
| uploaded_at | timestamptz | Si | Fecha de carga. |

### Evidencia en compras y gastos

| Campo logico | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| evidence_type | varchar(20) | Si | `NONE`, `PDF` o `URL`. |
| evidence_file_asset_id | uuid | No | Referencia a `tenant.company_file_asset` cuando `evidence_type=PDF`. |
| evidence_url | varchar(700) | No | URL `http/https` cuando `evidence_type=URL`. |

Reglas:

- Evidencia es opcional.
- Para PDF se permite un unico archivo validado por soporte.
- Para URL no se descarga automaticamente el contenido; solo se guarda referencia validada.
- Los campos total-only de compras/gastos no exponen IVA ni subtotal al usuario.

## Extensiones TASK-261 a TASK-272

Estado: diccionario objetivo documentado; pendiente de implementacion.

### `tenant.company_readiness_snapshot`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del diagnostico de preparacion. |
| company_id | uuid | Si | Empresa evaluada. |
| overall_status | varchar(20) | Si | Estado consolidado: `READY`, `WARNING` o `BLOCKED`. |
| evaluated_at | timestamptz | Si | Fecha/hora de evaluacion. |
| evaluated_by | uuid | No | Usuario que solicito el diagnostico, si aplica. |
| correlation_id | varchar(120) | No | Correlacion tecnica de la evaluacion. |

### `tenant.company_readiness_check`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del check. |
| snapshot_id | uuid | Si | Diagnostico al que pertenece. |
| check_code | varchar(80) | Si | Codigo funcional del prerequisito. |
| module_code | varchar(80) | Si | Modulo responsable de la accion correctiva. |
| status | varchar(20) | Si | `READY`, `WARNING` o `BLOCKED`. |
| blocking | boolean | Si | Indica si impide operar. |
| action_code | varchar(80) | No | Accion sugerida para la SPA. |
| message | varchar(500) | Si | Mensaje funcional sanitizado. |

### `reporting.report_dataset_cache`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del cache. |
| company_id | uuid | Si | Empresa propietaria. |
| report_code | varchar(80) | Si | Codigo de reporte normalizado. |
| cache_key | varchar(200) | Si | Hash/clave de filtros y parametros. |
| normalized_dataset | json/jsonb | Si | Dataset normalizado para UI/exportacion. |
| created_at | timestamptz | Si | Fecha de creacion. |
| expires_at | timestamptz | No | Fecha de expiracion del cache. |

### `platform.business_health_snapshot`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del snapshot funcional. |
| company_id | uuid | No | Empresa evaluada; nulo para estado global. |
| sales_status | varchar(20) | Si | Estado funcional de ventas. |
| dian_status | varchar(20) | Si | Estado funcional de DIAN/mock. |
| storage_status | varchar(20) | Si | Estado funcional de almacenamiento. |
| reporting_status | varchar(20) | Si | Estado funcional de reportes. |
| evaluated_at | timestamptz | Si | Fecha/hora de evaluacion. |

### `platform.service_health_event`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| id | uuid | Si | Identificador del evento de salud. |
| company_id | uuid | No | Empresa asociada cuando el evento es funcional por empresa. |
| service_name | varchar(80) | Si | Servicio o integracion afectada. |
| event_type | varchar(80) | Si | Tipo de evento: latencia, error, degradacion, recuperacion. |
| status | varchar(20) | Si | Estado reportado. |
| correlation_id | varchar(120) | No | Correlacion tecnica. |
| occurred_at | timestamptz | Si | Fecha/hora del evento. |

### Extension `tenant.company_file_asset`

| Campo | Tipo | Requerido | Descripcion |
|---|---|---:|---|
| scan_status | varchar(20) | No | Estado de validacion/antimalware: `PENDING`, `CLEAN`, `REJECTED` o `SKIPPED`. |
| retention_until | timestamptz | No | Fecha hasta la cual debe conservarse el archivo. |
| download_policy | varchar(40) | No | Politica de descarga: intermediada, prefirmada corta o interna. |
| last_downloaded_at | timestamptz | No | Ultima descarga registrada. |
