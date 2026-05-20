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
| identification_type_id | ref | Si | Tipo de documento. |
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

## catalog.identification_type

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| dian_code | varchar(20) | Si | Codigo DIAN si aplica. |
| name | varchar(100) | Si | Nombre del tipo de identificacion. |
| applies_to_person | boolean | Si | Aplica a persona natural. |
| applies_to_company | boolean | Si | Aplica a persona juridica. |
| active | boolean | Si | Estado del catalogo. |

## thirdparty.customer

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador del cliente. |
| company_id | ref | Si | Empresa propietaria del dato. |
| identification_type_id | ref | Si | Tipo de documento. |
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

## thirdparty.third_party objetivo

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador del tercero fiscal. |
| company_id | ref | Si | Empresa propietaria del tercero. |
| person_type | varchar(20) | Si | NATURAL, JURIDICA. |
| identification_type_code | varchar(20) | Si | Codigo del tipo de documento, por ejemplo NIT, CC, CE. |
| identification_number | varchar(30) | Si | Numero base del documento sin digito de verificacion separado. |
| verification_digit | varchar(2) | No | Digito de verificacion calculado automaticamente solo para NIT. |
| full_name | varchar(220) | No | Nombre completo para persona natural. |
| business_name | varchar(220) | No | Razon social para persona juridica. |
| trade_name | varchar(220) | No | Nombre comercial. |
| email | varchar(150) | No | Correo de contacto. |
| phone | varchar(50) | No | Telefono. |
| address | varchar(250) | No | Direccion. |
| municipality_code | varchar(20) | No | Codigo municipio DIAN/DANE cuando aplique. |
| tax_responsibilities | jsonb | No | Responsabilidades fiscales. |
| active | boolean | Si | Estado. |

## thirdparty.third_party_role objetivo

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
| source_type | varchar(40) | Si | SALE, PURCHASE, CREDIT_NOTE, ADJUSTMENT. |
| source_id | uuid/bigint | Si | Documento origen. |
| reason | varchar(250) | No | Motivo. |
| movement_at | timestamp | Si | Fecha del movimiento. |

## inventory.service_supply_reference objetivo

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| service_product_id | ref | Si | Item tipo SERVICE. |
| supply_product_id | ref | Si | Item tipo SUPPLY o bien controlado usado como insumo. |
| notes | varchar(300) | No | Observacion operativa. |
| active | boolean | Si | Estado. |

## inventory.purchase

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| supplier_id | ref | Si | Proveedor. |
| purchase_date | timestamp | Si | Fecha de compra. |
| subtotal | numeric(19,2) | Si | Subtotal. |
| tax_total | numeric(19,2) | Si | Impuestos. |
| total | numeric(19,2) | Si | Total. |
| status | varchar(30) | Si | DRAFT, CONFIRMED, CANCELLED. |

## inventory.purchase_line

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| purchase_id | ref | Si | Compra. |
| product_id | ref | Si | Producto. |
| quantity | numeric(19,4) | Si | Cantidad. |
| unit_price | numeric(19,2) | Si | Precio unitario. |
| tax_amount | numeric(19,2) | Si | Impuesto. |
| line_total | numeric(19,2) | Si | Total linea. |

## accounting.expense objetivo

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| supplier_id | ref | Si | Proveedor. |
| expense_date | date | Si | Fecha del gasto. |
| concept | varchar(250) | Si | Concepto del gasto. |
| subtotal | numeric(19,2) | Si | Subtotal. |
| tax_total | numeric(19,2) | Si | Impuestos. |
| total | numeric(19,2) | Si | Total. |
| payment_condition | varchar(30) | Si | CASH, CREDIT. |
| evidence_url | varchar(500) | No | Evidencia o soporte externo. |
| status | varchar(30) | Si | DRAFT, CONFIRMED, CANCELLED. |

## accounting.accounts_payable objetivo

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| supplier_id | ref | Si | Proveedor. |
| source_type | varchar(40) | Si | PURCHASE, EXPENSE. |
| source_id | uuid/bigint | Si | Documento origen. |
| issue_date | date | Si | Fecha origen. |
| due_date | date | No | Fecha de vencimiento. |
| original_amount | numeric(19,2) | Si | Valor inicial. |
| paid_amount | numeric(19,2) | Si | Valor pagado acumulado. |
| balance | numeric(19,2) | Si | Saldo pendiente. |
| status | varchar(30) | Si | OPEN, PARTIALLY_PAID, PAID, CANCELLED. |

## accounting.accounts_payable_payment objetivo

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| accounts_payable_id | ref | Si | Cuenta por pagar. |
| payment_date | date | Si | Fecha de pago. |
| amount | numeric(19,2) | Si | Valor pagado. |
| payment_method_id | ref | No | Medio de pago. |
| accounting_entry_id | ref | No | Asiento contable asociado. |

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
| quantity | numeric(19,4) | Si | Cantidad vendida. |
| unit_price | numeric(19,2) | Si | Precio unitario. |
| discount_amount | numeric(19,2) | Si | Descuento linea. |
| tax_amount | numeric(19,2) | Si | Impuesto linea. |
| line_total | numeric(19,2) | Si | Total linea. |

## billing.electronic_document

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| sale_id | ref | No | Venta origen. |
| document_type | varchar(40) | Si | ELECTRONIC_INVOICE, ELECTRONIC_POS, CREDIT_NOTE, DEBIT_NOTE, POS_ADJUSTMENT_NOTE. |
| buyer_name | varchar(200) | No | Nombre o razon social del adquirente cuando aplique. |
| buyer_document_type | varchar(40) | No | Tipo de documento del adquirente cuando aplique. |
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

## dian_provider.provider_configuration

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| provider_name | varchar(120) | Si | Nombre proveedor tecnologico. |
| base_url | varchar(300) | No | URL base. Debe venir de entorno o configuracion segura. |
| credentials_reference | varchar(300) | No | Referencia a secreto, no secreto real. |
| certificate_reference | varchar(300) | No | Ruta/referencia segura a certificado. |
| environment | varchar(30) | Si | TEST, PRODUCTION. |
| active | boolean | Si | Estado. |

## dian_provider.provider_submission

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| id | uuid/bigint | Si | Identificador. |
| company_id | ref | Si | Empresa. |
| electronic_document_id | ref | Si | Documento electronico. |
| provider_configuration_id | ref | Si | Configuracion usada. |
| tracking_id | varchar(200) | No | Identificador proveedor. |
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
