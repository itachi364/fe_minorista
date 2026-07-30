# API Contract: Microservicios

## Objetivo

Definir contratos iniciales entre microservicios para una plataforma multiempresa de facturacion electronica, POS electronico, inventario y contabilidad.

Estos contratos son la base para futuros archivos OpenAPI por servicio.

## Convenciones generales

- Versionado HTTP: `/api/v1`.
- Formato: JSON UTF-8.
- Fechas: ISO-8601.
- Montos: decimal string o numero JSON con precision controlada por backend.
- IDs: `uuid` recomendado para nuevas tablas y contratos.
- Autenticacion externa: `Authorization: Bearer <token>`.
- Aislamiento multiempresa: `X-Company-Id` obligatorio en APIs de negocio.
- Trazabilidad: `X-Correlation-Id` obligatorio o generado por gateway.
- Idempotencia: `Idempotency-Key` obligatorio en emision fiscal, movimientos de inventario y contabilizacion.
- Unidad de despliegue: un artefacto y contenedor por microservicio/bounded context, no por endpoint individual.
- Comunicacion inicial: REST sincrono entre microservicios; eventos internos quedan como contrato para una fase posterior.

## Microservicios fisicos objetivo

| Microservicio | Responsabilidad principal | Artefacto objetivo |
|---|---|---|
| `tenant-service` | Empresas y estado del tenant | `services/tenant-service` |
| `identity-service` | Usuarios, roles y permisos | `services/identity-service` |
| `catalog-service` | Catalogos oficiales y configurables | `services/catalog-service` |
| `thirdparty-service` | Clientes y proveedores | `services/thirdparty-service` |
| `inventory-service` | Productos, costos, compras, stock y kardex | `services/inventory-service` |
| `billing-service` | Ventas, POS, factura electronica, notas y numeracion | `services/billing-service` |
| `dian-provider-service` | Mock DIAN y futura integracion con proveedor real | `services/dian-provider-service` |
| `accounting-service` | PUC, reglas, asientos, libro diario y mayor | `services/accounting-service` |
| `audit-service` | Auditoria fiscal y tecnica | `services/audit-service` |

## Headers obligatorios

| Header | Requerido | Aplica | Descripcion |
|---|---:|---|---|
| `Authorization` | Si | APIs expuestas | Token de usuario o servicio. |
| `X-Company-Id` | Si | Datos de negocio | Tenant/empresa propietaria de la operacion. |
| `X-Correlation-Id` | Si | Todas | Trazabilidad entre servicios. |
| `Idempotency-Key` | Si | Comandos criticos | Evita duplicados por reintentos. |

## Error estandar

```json
{
  "timestamp": "2026-05-11T22:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "La solicitud no cumple las reglas de validacion.",
  "correlationId": "7b6f1c88-7bb9-40a0-93c0-7df2e50975ef",
  "details": [
    {
      "field": "lines[0].quantity",
      "message": "La cantidad debe ser mayor a cero."
    }
  ]
}
```

Codigos base:

- `VALIDATION_ERROR`
- `BUSINESS_RULE_VIOLATION`
- `RESOURCE_NOT_FOUND`
- `DUPLICATE_RESOURCE`
- `UNAUTHORIZED`
- `FORBIDDEN`
- `TENANT_ACCESS_DENIED`
- `EXTERNAL_PROVIDER_ERROR`
- `IDEMPOTENCY_CONFLICT`
- `INTERNAL_ERROR`

## bff-service

Responsabilidad: frontera publica consumida por la SPA y expuesta por API Gateway en produccion.

Estado TASK-063:

- Microservicio fisico inicial implementado en `services/bff-service`.
- No tiene persistencia ni reglas fiscales propias.
- Expone `/api/v1/**` solo para rutas aprobadas y enruta hacia el microservicio dueno del contrato.
- Propaga `Authorization`, `X-Company-Id`, `X-Correlation-Id`, `Idempotency-Key`, `Content-Type` y `Accept`.
- Filtra cabeceras de respuesta a `Content-Type` y `X-Correlation-Id`.
- Rechaza rutas internas no publicas, por ejemplo `/api/v1/provider/**`.

Reglas:

- El frontend debe consumir siempre el BFF.
- Los microservicios internos no deben exponerse al navegador.
- El BFF no debe implementar reglas de negocio que pertenezcan a billing, inventory, accounting, tenant, identity, catalog, thirdparty o audit.
- Un fallo de servicio interno debe responder como error publico estructurado sin stack trace ni detalles de infraestructura.
- Estado TASK-065: `/api/v1/auth/**` y `/api/v1/me/**` se enrutan a `identity-service`; `/api/v1/companies/{companyId}/license/**` se enruta a `tenant-service`; `/api/v1/companies/{companyId}/memberships`, `/api/v1/companies/{companyId}/users/{userId}/roles` y `/api/v1/companies/{companyId}/permissions` se enrutan a `identity-service`.
## tenant-service

Responsabilidad: empresas, configuracion multiempresa y estado del tenant.

### Endpoints

- `POST /api/v1/companies`
- `GET /api/v1/companies/{companyId}`
- `PUT /api/v1/companies/{companyId}`
- `PUT /api/v1/companies/{companyId}/activate`
- `PUT /api/v1/companies/{companyId}/suspend`

### CompanyResponse

### CompanyRequest

```json
{
  "legalName": "Mi Empresa SAS",
  "tradeName": "Mi Tienda",
  "identificationTypeId": "uuid",
  "identificationNumber": "900123456",
  "verificationDigit": "7",
  "email": "admin@example.com"
}
```

### CompanyResponse

```json
{
  "id": "uuid",
  "legalName": "Mi Empresa SAS",
  "tradeName": "Mi Tienda",
  "identificationTypeId": "uuid",
  "identificationNumber": "900123456",
  "verificationDigit": "7",
  "email": "admin@example.com",
  "status": "ACTIVE"
}
```

## identity-service

Responsabilidad: usuarios, autenticacion, sesiones, permisos efectivos, roles globales de plataforma, roles por empresa y auditoria de acceso.

Estado TASK-056:

- Microservicio fisico implementado en `services/identity-service`.
- Persistencia propia bajo schema `identity`.
- Login con token opaco Bearer, token persistido como hash y expiracion configurable.
- Passwords persistidos con hash PBKDF2, nunca en texto plano.
- Modelo actual con roles fijos por empresa: `OWNER`, `ADMIN`, `CASHIER`, `ACCOUNTANT`, `AUDITOR`.
- Estado TASK-072: `POST /api/v1/auth/login` incluye `globalRoles` en la respuesta. Cuando contiene `ROOT`, el cliente puede iniciar flujo global sin `company_id`, membresia empresarial ni licencia empresarial.
- Estado TASK-073: `ROOT` puede crear usuario con `POST /api/v1/users` y asignar `OWNER` como administrador inicial mediante `POST /api/v1/companies/{companyId}/memberships`, sin membresia previa ni licencia empresarial.

Objetivo TASK-068/TASK-069:

- `ROOT` es global, no tiene `company_id`, no depende de licencia empresarial y administra empresas contratantes, licencias, usuarios root y administradores iniciales.
- Todo rol distinto de `ROOT` pertenece a una empresa y se aisla por `company_id`.
- Los nombres de roles empresariales son configurables por cada empresa.
- Los permisos son modulares y persistidos; el sistema no debe depender de nombres de roles hardcodeados para autorizar acciones.
- Permisos `GLOBAL_*` son exclusivos de `ROOT`.
- Un actor solo puede crear, editar o asignar roles con permisos estrictamente menores que sus permisos efectivos.
- Se registra auditoria interna para login, creacion de usuario, creacion/edicion de rol, asignacion de permisos y asignacion de roles.

### Endpoints actuales

- `POST /api/v1/auth/login`
- `POST /api/v1/users`
- `GET /api/v1/me`
- `GET /api/v1/me/companies`
- `POST /api/v1/companies/{companyId}/memberships`
- `POST /api/v1/companies/{companyId}/users/{userId}/roles`
- `PUT /api/v1/companies/{companyId}/memberships/{membershipId}/roles`
- `GET /api/v1/companies/{companyId}/permissions?userId=`

### Endpoints objetivo RBAC modular

Root global:

- `POST /api/v1/platform/root-users`
- `POST /api/v1/platform/companies`
- `POST /api/v1/platform/companies/{companyId}/admin`
- `GET /api/v1/platform/permissions`
- `GET /api/v1/platform/audit-events?from=&to=&userId=`

Roles y usuarios por empresa:

- `GET /api/v1/companies/{companyId}/roles`
- `POST /api/v1/companies/{companyId}/roles`
- `GET /api/v1/companies/{companyId}/roles/{roleId}`
- `PUT /api/v1/companies/{companyId}/roles/{roleId}`
- `PUT /api/v1/companies/{companyId}/roles/{roleId}/deactivate`
- `GET /api/v1/companies/{companyId}/permissions/catalog`
- `POST /api/v1/companies/{companyId}/users`
- `POST /api/v1/companies/{companyId}/users/{userId}/role-assignments`
- `DELETE /api/v1/companies/{companyId}/users/{userId}/role-assignments/{roleId}`
- `GET /api/v1/companies/{companyId}/users/{userId}/effective-permissions`

Ejemplo `POST /api/v1/companies/{companyId}/roles`:

```json
{
  "name": "Vendedor POS",
  "description": "Puede vender y emitir POS electronico",
  "permissionCodes": ["SALES_CREATE", "FISCAL_DOCUMENTS_ISSUE", "REPORTS_VIEW"]
}
```

Reglas:

- Un usuario puede pertenecer a varias empresas, pero sus roles empresariales se calculan por `companyId`.
- Todo token permite determinar usuario autenticado, alcance global cuando aplique, empresas autorizadas y permisos efectivos por empresa.
- `ROOT` puede crear la empresa contratante y el administrador inicial.
- El administrador empresarial puede crear roles y usuarios dentro de su empresa si tiene `COMPANY_ROLES_MANAGE` y/o `COMPANY_USERS_MANAGE`.
- El backend rechaza cualquier rol empresarial que contenga permisos `GLOBAL_*`.
- El backend rechaza delegar permisos iguales, superiores o no poseidos por el actor.
- La licencia empresarial se valida al crear usuarios o roles empresariales cuando la politica comercial lo requiera; `ROOT` no consume licencia para entrar al panel global.

## catalog-service

Responsabilidad: catalogos globales y catalogos configurables por empresa.

Estado TASK-033:

- Microservicio fisico implementado en `services/catalog-service`.
- Contratos legacy compatibles mantenidos durante extraccion: `/api/categorias`, `/api/paises`, `/api/tipos-documento`, `/api/metodos-pago`, `/api/parametros`, `/api/tipos-gasto`, `/api/impuestos` y `/api/productos`.
- Los contratos `/api/v1/catalogs/*` siguen siendo objetivo del contrato estable posterior.

### Endpoints

- `GET /api/v1/catalogs/identification-types`
- `GET /api/v1/catalogs/countries`
- `GET /api/v1/catalogs/tax-types`
- `GET /api/v1/catalogs/payment-methods`
- `GET /api/v1/catalogs/puc-accounts`
- `GET /api/v1/company-catalogs/taxes`
- `POST /api/v1/company-catalogs/taxes`

Regla:

- Los catalogos oficiales pueden omitirse de `X-Company-Id`.
- Los catalogos configurables por empresa requieren `X-Company-Id`.

## thirdparty-service

Responsabilidad: clientes y proveedores aislados por empresa.

Estado TASK-033:

- Microservicio fisico implementado en `services/thirdparty-service`.
- TASK-059 lote 2 retira contratos legacy `/api/clientes` y `/api/proveedores`; el contrato canonico queda en `/api/v1`.
- `thirdparty-service` ya no consulta `catalog-service` en runtime para crear terceros; recibe `identificationTypeCode` en el contrato v1 y calcula DV NIT en dominio.
- TASK-047 agrega el modelo fiscal unificado `/api/v1/third-parties`, con roles cliente/proveedor y DV NIT automatico.
- Los contratos `/api/v1/customers` y `/api/v1/suppliers` existen como vistas por rol sobre el modelo fiscal unificado.

### Terceros fiscales

- `POST /api/v1/third-parties`
- `GET /api/v1/third-parties/{thirdPartyId}`
- `GET /api/v1/third-parties/by-document?identificationTypeCode=&identificationNumber=`
- `PUT /api/v1/third-parties/{thirdPartyId}`
- `PUT /api/v1/third-parties/{thirdPartyId}/activate`
- `PUT /api/v1/third-parties/{thirdPartyId}/deactivate`

### Clientes v1

- `POST /api/v1/customers`
- `GET /api/v1/customers?active=`

### Proveedores v1

- `POST /api/v1/suppliers`
- `GET /api/v1/suppliers?active=`

### Clientes/proveedores legacy compatibles

Retirados en TASK-059 lote 2. Los consumidores deben usar `/api/v1/customers`, `/api/v1/suppliers` o `/api/v1/third-parties`.

### ThirdPartyRequest

```json
{
  "role": "CUSTOMER",
  "personType": "JURIDICA",
  "identificationTypeCode": "NIT",
  "identificationNumber": "900123456",
  "fullName": null,
  "businessName": "Cliente Prueba SAS",
  "tradeName": "Cliente Prueba",
  "email": "cliente@example.com",
  "phone": "3000000000",
  "address": "Calle 1 # 2-3",
  "municipalityCode": "11001",
  "taxResponsibilities": ["O-13"],
  "taxRegime": "RESPONSABLE_IVA"
}
```

Reglas:

- `identificationTypeId + identificationNumber` es unico por empresa.
- `identificationTypeCode=NIT` calcula `verificationDigit` automaticamente; el request no debe tratar el DV como dato libre.
- Para tipos de documento distintos a NIT, `verificationDigit` debe quedar nulo o vacio.
- `personType` debe ser `NATURAL` o `JURIDICA`.
- Un tercero puede tener rol `CUSTOMER`, `SUPPLIER` o `BOTH`.
- Ninguna consulta puede retornar terceros de otra empresa.

## inventory-service

Responsabilidad: productos, stock simple, compras, movimientos y kardex.

Estado TASK-034:

- Microservicio fisico implementado en `services/inventory-service`.
- Endpoints `/api/v1` implementados para productos, disponibilidad, kardex, movimientos, compras y confirmacion de compras.
- Las operaciones implementadas requieren `X-Company-Id`.
- `Idempotency-Key` es obligatorio en movimientos y compras; en creacion de producto se usa cuando se registra stock inicial.
- Las rutas de busqueda/listado, actualizacion y desactivacion quedan como contrato objetivo posterior.

Estado TASK-048:

- `POST /api/v1/products` acepta `itemType`, `saleEnabled`, `purchaseEnabled` y `stockTracked`.
- Si `itemType` se omite, se conserva compatibilidad y se crea `PHYSICAL_GOOD` con venta, compra y stock habilitados.
- `SERVICE` es facturable, pero no puede tener stock automatico ni stock inicial.
- `POST /api/v1/service-supply-references` y `GET /api/v1/products/{serviceProductId}/supply-references` permiten registrar insumos sugeridos para servicios sin generar movimientos de kardex.
- Compras y movimientos solo afectan items con `stockTracked=true`.

### Productos

- `POST /api/v1/products`
- `GET /api/v1/products/{productId}`
- `GET /api/v1/products/{productId}/availability?quantity=`
- `GET /api/v1/products/{productId}/kardex`
- `GET /api/v1/products/{serviceProductId}/supply-references`

### Compras

- `POST /api/v1/purchases`
- `POST /api/v1/purchases/{purchaseId}/confirm`

### Movimientos

- `POST /api/v1/inventory-movements`
- Kardex por producto: `GET /api/v1/products/{productId}/kardex`

### Referencias servicio-insumo

- `POST /api/v1/service-supply-references`
- `GET /api/v1/products/{serviceProductId}/supply-references`

### ProductRequest

```json
{
  "sku": "SKU-001",
  "barcode": "7701234567890",
  "name": "Cafe 500g",
  "description": "Bolsa de cafe",
  "itemType": "PHYSICAL_GOOD",
  "saleEnabled": true,
  "purchaseEnabled": true,
  "stockTracked": true,
  "salePrice": 15000,
  "cost": 9000,
  "initialStock": 10
}
```

### ProductResponse

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "sku": "SKU-001",
  "barcode": "7701234567890",
  "name": "Cafe 500g",
  "description": "Bolsa de cafe",
  "itemType": "PHYSICAL_GOOD",
  "saleEnabled": true,
  "purchaseEnabled": true,
  "stockTracked": true,
  "salePrice": 15000,
  "cost": 9000,
  "active": true,
  "currentStock": 10,
  "createdAt": "2026-05-20T10:00:00Z",
  "updatedAt": "2026-05-20T10:00:00Z"
}
```

### ProductAvailabilityResponse

```json
{
  "companyId": "uuid",
  "productId": "uuid",
  "requestedQuantity": 2,
  "availableQuantity": 10,
  "available": true
}
```

### InventoryMovementRequest

```json
{
  "productId": "uuid",
  "movementType": "CONSUMPTION_OUT",
  "quantity": 4,
  "unitCost": 9000,
  "sourceDocumentType": "MANUAL_SUPPLY_CONSUMPTION",
  "sourceDocumentId": "uuid",
  "reason": "Consumo operativo de insumo usado en servicios"
}
```

### ServiceSupplyReferenceRequest

```json
{
  "serviceProductId": "uuid",
  "supplyProductId": "uuid",
  "notes": "Insumo sugerido para manicura; el consumo real se registra manualmente."
}
```

### ServiceSupplyReferenceResponse

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "serviceProductId": "uuid",
  "supplyProductId": "uuid",
  "notes": "Insumo sugerido para manicura; el consumo real se registra manualmente.",
  "active": true,
  "createdAt": "2026-05-20T10:00:00Z"
}
```

### PurchaseRequest

```json
{
  "supplierId": "uuid",
  "subtotal": 90000,
  "taxTotal": 17100,
  "total": 107100,
  "paymentCondition": "CREDIT",
  "dueDate": "2026-06-20",
  "evidenceUrl": "https://example.local/evidence.pdf",
  "lines": [
    {
      "productId": "uuid",
      "quantity": 10,
      "unitCost": 9000,
      "subtotal": 90000,
      "tax": 17100,
      "total": 107100
    }
  ]
}
```

Reglas:

- Una venta facturada descuenta stock.
- Una compra confirmada aumenta stock.
- Una compra a credito requiere `dueDate`.
- Si `ACCOUNTING_SERVICE_URL` esta configurado, `inventory-service` intenta contabilizar la compra confirmada y crear CxP en `accounting-service` de forma best-effort; el fallo de contabilidad no revierte stock ni confirmacion.
- Stock negativo no esta permitido en la fase inicial.
- Todo movimiento debe referenciar documento origen.
- Los tipos de item objetivo son `PHYSICAL_GOOD`, `SERVICE` y `SUPPLY`.
- Un `SERVICE` puede facturarse, pero no descuenta insumos automaticamente.
- Las referencias servicio-insumo son informativas y no generan kardex.
- `initialStock` solo se acepta cuando `stockTracked=true`.
- La disponibilidad de un `SERVICE` sin stock se considera disponible para venta; la venta final se ajusta en `billing-service`.
- Los movimientos manuales de insumos incluyen `CONSUMPTION_OUT` y `WASTE_OUT`.
- `CONSUMPTION_OUT` debe usar `sourceDocumentType=MANUAL_SUPPLY_CONSUMPTION`.
- `WASTE_OUT` debe usar `sourceDocumentType=MANUAL_SUPPLY_WASTE`.
- `reason` es obligatorio para `CONSUMPTION_OUT` y `WASTE_OUT`.
- Los gastos sin inventario no deben crear movimientos de stock.

## billing-service

Responsabilidad: ventas POS, facturacion electronica, documento equivalente POS, notas, numeracion y estados fiscales.

Estado TASK-041:

- Microservicio fisico inicial implementado en `services/billing-service`.
- Endpoints implementados: `POST /api/v1/issuers`, `GET /api/v1/issuers/current`, `POST /api/v1/numbering-resolutions`, `GET /api/v1/numbering-resolutions`, `POST /api/v1/sales`, `POST /api/v1/sales/{saleId}/confirm` y `GET /api/v1/sales/{saleId}`.
- La creacion de venta valida disponibilidad contra `inventory-service` usando `GET /api/v1/products/{productId}/availability`.
- La confirmacion exige emisor activo y resolucion vigente para `ELECTRONIC_POS` en ambiente `TEST`.
- La confirmacion asigna prefijo y consecutivo desde `billing.numbering_resolution`, genera documento electronico POS y consume `dian-provider-service` por HTTP.
- Si el proveedor acepta, aplica automaticamente `SALE_OUT` y asiento contable idempotente.

Estado TASK-049:

- La venta soporta lineas mixtas de bienes fisicos y servicios.
- Cada linea guarda snapshot de `productSku`, `productName`, `itemType` y `stockTracked` obtenido desde `inventory-service`.
- La disponibilidad de inventario se valida solo cuando `stockTracked=true`.
- La confirmacion aplica `SALE_OUT` solo para lineas con `stockTracked=true`.
- Las lineas tipo `SERVICE` no consumen insumos automaticamente.

### Emisor

- `POST /api/v1/issuers`
- `GET /api/v1/issuers/current`
- `PUT /api/v1/issuers/{issuerId}` pendiente

### Resoluciones

- `POST /api/v1/numbering-resolutions`
- `GET /api/v1/numbering-resolutions?documentType=&active=`
- `GET /api/v1/numbering-resolutions/{resolutionId}` pendiente
- `PUT /api/v1/numbering-resolutions/{resolutionId}/activate` pendiente
- `PUT /api/v1/numbering-resolutions/{resolutionId}/deactivate` pendiente

### Ventas POS

- `POST /api/v1/sales`
- `POST /api/v1/sales/{saleId}/confirm`
- `GET /api/v1/sales/{saleId}`
- `GET /api/v1/sales?status=&from=&to=`

### Documento equivalente electronico POS

- `POST /api/v1/electronic-pos`
- `POST /api/v1/electronic-pos/{documentId}/submit`
- `GET /api/v1/electronic-pos/{documentId}`
- `POST /api/v1/electronic-pos/{documentId}/adjustment-notes`

### Factura electronica

- `POST /api/v1/electronic-invoices`
- `POST /api/v1/electronic-invoices/{documentId}/issue`
- `GET /api/v1/electronic-invoices/{documentId}`
- `GET /api/v1/electronic-invoices?status=&customerId=&from=&to=&prefix=&number=`
- `GET /api/v1/electronic-invoices/{documentId}/artifacts`
- `GET /api/v1/electronic-invoices/{documentId}/events`

### Notas

- `POST /api/v1/credit-notes`
- `POST /api/v1/debit-notes`
- `GET /api/v1/credit-notes/{noteId}`
- `GET /api/v1/debit-notes/{noteId}`

### SaleRequest

### IssuerProfileRequest

```json
{
  "legalName": "ACME SAS",
  "nit": "900123456",
  "verificationDigit": "7",
  "taxResponsibilities": ["O-13"],
  "municipalityCode": "11001",
  "address": "Calle 1 # 2-3"
}
```

### SaleLineResponse

```json
{
  "id": "uuid",
  "productId": "uuid",
  "productSku": "SERV-1",
  "productName": "Manicura",
  "itemType": "SERVICE",
  "stockTracked": false,
  "quantity": 1,
  "unitPrice": 35000,
  "discountAmount": 0,
  "taxCode": "IVA_19",
  "taxRate": 19,
  "subtotal": 35000,
  "taxAmount": 6650,
  "total": 41650
}
```

### NumberingResolutionRequest

```json
{
  "documentType": "ELECTRONIC_POS",
  "resolutionNumber": "18760000001",
  "prefix": "POS",
  "fromNumber": 100,
  "toNumber": 200,
  "validFrom": "2026-01-01",
  "validTo": "2026-12-31",
  "environment": "TEST"
}
```

```json
{
  "customerId": "uuid",
  "saleChannel": "POS",
  "paymentMethodId": "uuid",
  "items": [
    {
      "productId": "uuid",
      "quantity": 2,
      "unitPrice": 15000,
      "discountAmount": 0,
      "taxCode": "IVA_19",
      "taxRate": 19
    }
  ]
}
```

### ElectronicDocumentResponse

```json
{
  "id": "uuid",
  "documentType": "ELECTRONIC_POS",
  "prefix": "POS",
  "number": 123,
  "cufeCude": "abc123",
  "qrContent": "https://...",
  "subtotal": 30000,
  "taxTotal": 5700,
  "total": 35700,
  "status": "VALIDATED",
  "providerStatus": "ACCEPTED",
  "inventoryAppliedAt": "2026-05-19T10:01:00Z",
  "accountingAppliedAt": "2026-05-19T10:01:01Z"
}
```

### ElectronicPosRequest

```json
{
  "saleId": "uuid",
  "buyerName": "Consumidor Final",
  "buyerDocumentType": "CC",
  "buyerDocumentNumber": "123456789",
  "documentDate": "2026-05-15",
  "environment": "TEST",
  "lines": [
    {
      "productId": "uuid",
      "quantity": 2,
      "unitPrice": 15000,
      "discountAmount": 0,
      "taxCode": "IVA_19",
      "taxRate": 19
    }
  ]
}
```

### SubmitElectronicDocumentResponse

```json
{
  "documentId": "uuid",
  "providerSubmissionId": "mock-submission-id",
  "providerStatus": "ACCEPTED",
  "documentStatus": "VALIDATED",
  "cufeCude": "mock-cude",
  "qrContent": "mock-qr-content"
}
```

Reglas:

- `X-Company-Id` define la empresa emisora.
- La numeracion fiscal debe ser idempotente y no reutilizable.
- Confirmar una venta fiscal exige emisor activo y resolucion vigente con numeros disponibles.
- El POS genera documento electronico a partir de la venta.
- La afectacion de inventario ocurre cuando la venta/documento llegue al estado aprobado por la politica transaccional.
- Si el proveedor devuelve `ACCEPTED`, `billing-service` aplica `SALE_OUT` en `inventory-service` y genera asiento `SALE_CONFIRMED` en `accounting-service`.
- `SALE_OUT` solo se aplica a lineas con `stockTracked=true`; los servicios no generan consumo automatico de insumos.
- El snapshot de linea fiscal/operativa se toma desde `inventory-service` al crear la venta.
- Los campos `inventoryAppliedAt` y `accountingAppliedAt` evidencian la aplicacion idempotente de efectos posteriores.
- Reintentar `POST /api/v1/sales/{saleId}/confirm` no debe duplicar documento, movimientos de inventario ni asiento contable.
- En ambiente local, `POST /api/v1/electronic-pos/{documentId}/submit` usa proveedor DIAN mock sin llamadas externas.

## dian-provider-service

Responsabilidad: encapsular integracion con proveedor tecnologico DIAN.

Estado TASK-036:

- Microservicio fisico implementado en `services/dian-provider-service`.
- Modo local soportado: `DIAN_PROVIDER_MODE=mock`.
- Persistencia de envios mock en `dian_provider.provider_submission`.
- `billing-service` consume `POST /api/v1/provider/electronic-pos` por HTTP usando `DIAN_PROVIDER_SERVICE_URL`.
- `GET /api/v1/provider/submissions/{trackingId}` permite consultar el resultado mock persistido y requiere `X-Company-Id`.

### Endpoints internos

- `POST /api/v1/provider/electronic-invoices`
- `POST /api/v1/provider/electronic-pos`
- `POST /api/v1/provider/credit-notes`
- `POST /api/v1/provider/debit-notes`
- `GET /api/v1/provider/submissions/{trackingId}`

### ProviderSubmissionRequest

```json
{
  "companyId": "uuid",
  "documentId": "uuid",
  "documentType": "ELECTRONIC_INVOICE",
  "payload": {},
  "idempotencyKey": "uuid"
}
```

### ProviderSubmissionResponse

```json
{
  "trackingId": "provider-tracking-id",
  "status": "ACCEPTED",
  "cufeCude": "abc123",
  "qrContent": "https://...",
  "artifacts": [
    {
      "type": "XML",
      "storageUri": "s3://bucket/path.xml",
      "contentHash": "sha256"
    }
  ],
  "rawResponse": {}
}
```

Reglas:

- No guardar secretos en payloads ni logs.
- Todo error externo debe mapearse a `EXTERNAL_PROVIDER_ERROR` con detalle seguro.
- La integracion real queda pendiente hasta seleccionar proveedor tecnologico.
- En desarrollo local se usa el microservicio mock sin llamadas externas y solo sirve para probar el flujo interno.
- Variables locales del mock:
  - `DIAN_PROVIDER_MODE=mock`.
  - `DIAN_MOCK_DEFAULT_STATUS=ACCEPTED|REJECTED|FAILED`.
  - `DIAN_MOCK_ERROR_CODE`.
  - `DIAN_MOCK_ERROR_MESSAGE`.
- En esta version, `DIAN_PROVIDER_MODE` solo acepta `mock`; un valor distinto debe fallar explicitamente hasta implementar el adaptador real.

## accounting-service

Responsabilidad: PUC, cuentas por empresa, asientos, libro diario y libro mayor.

Estado TASK-037:

- Microservicio fisico implementado en `services/accounting-service`.
- Persistencia propia bajo schema `accounting`.
- `POST /api/v1/accounting-entries` es idempotente por `companyId`, `sourceType` y `sourceId`; si el asiento ya existe, retorna el asiento existente.

### Configuracion base contable

- `POST /api/v1/accounting-setup/basic`

Crea o reactiva una plantilla contable minima para pruebas operativas de pequenas empresas en Colombia. La plantilla no sustituye el PUC oficial completo ni una parametrizacion contable profesional; deja cuentas y reglas editables por empresa.

Respuesta:

```json
{
  "companyId": "uuid",
  "templateName": "BASIC_COLOMBIA_SMALL_BUSINESS",
  "accounts": [
    {"code": "1105", "name": "Caja", "active": true}
  ],
  "rules": [
    {"eventType": "SALE_CONFIRMED", "sourceType": "SALE", "active": true}
  ]
}
```

Reglas:

- Crea las cuentas base `1105`, `1110`, `1305`, `1435`, `2205`, `2408`, `4135` y `5135` si no existen para la empresa.
- Si una cuenta base existe inactiva, la reactiva sin duplicar codigo.
- Reemplaza las reglas activas de venta, compra, gasto, pago de cuenta por pagar y recaudo de cuenta por cobrar, dejando la regla previa inactiva para conservar historial.

### Cuentas

- `POST /api/v1/accounts`
- `GET /api/v1/accounts?code=`
- `GET /api/v1/accounts?active=`

### Reglas contables

- `POST /api/v1/accounting-rules`
- `PUT /api/v1/accounting-rules/active`
- `POST /api/v1/accounting-rules/{eventType}/deactivate`
- `GET /api/v1/accounting-rules?eventType=&active=`

### Asientos

- `POST /api/v1/accounting-entries`

### Reportes

- `GET /api/v1/reports/journal?from=&to=`
- `GET /api/v1/reports/ledger?from=&to=&accountCode=`

### AccountingRuleRequest

```json
{
  "eventType": "SALE_CONFIRMED",
  "sourceType": "SALE",
  "name": "Venta POS",
  "lines": [
    {
      "accountCode": "1105",
      "side": "DEBIT",
      "amountType": "TOTAL",
      "description": "Caja"
    },
    {
      "accountCode": "4135",
      "side": "CREDIT",
      "amountType": "SUBTOTAL",
      "description": "Ingresos"
    }
  ]
}
```

### AccountingEntryRequest

```json
{
  "eventType": "SALE_CONFIRMED",
  "sourceType": "SALE",
  "sourceId": "uuid",
  "entryDate": "2026-05-11",
  "description": "Venta POS 123",
  "thirdpartyId": "uuid",
  "subtotal": 30000,
  "taxTotal": 5700,
  "total": 35700
}
```

Reglas:

- Todo asiento posteado debe cumplir partida doble.
- En la implementacion local actual, `POST /api/v1/accounting-entries` genera un asiento `POSTED` inmediatamente desde reglas contables activas por empresa; el flujo draft/post-by-id queda pendiente hasta que se apruebe un modelo de borradores contables.
- Reintentar la contabilizacion de una misma venta no debe crear un segundo asiento.
- Las cuentas deben derivar del PUC colombiano o de una configuracion aprobada por empresa.
- TASK-053 agrega configuracion base contable editable, consulta de cuentas por empresa/filtro activo, consulta de reglas por empresa/evento/estado, reemplazo de regla activa y desactivacion explicita de la regla activa.
- `PUT /api/v1/accounting-rules/active` desactiva la regla activa previa para el evento y crea una nueva regla activa; `POST /api/v1/accounting-rules` conserva la validacion de no duplicar regla activa.
- Un documento fiscal validado debe poder rastrearse hasta su asiento contable.

### Cuentas por cobrar

- `POST /api/v1/accounts-receivable`
- `GET /api/v1/accounts-receivable?status=&customerId=&from=&to=`
- `POST /api/v1/accounts-receivable/{receivableId}/payments`
- `GET /api/v1/reports/accounts-receivable?status=&customerId=&from=&to=`

### AccountsReceivableRequest

```json
{
  "customerId": "uuid",
  "sourceType": "SALE",
  "sourceId": "uuid",
  "issueDate": "2026-05-11",
  "dueDate": "2026-06-10",
  "totalAmount": 35700,
  "idempotencyKey": "sale-credit-001"
}
```

### ReceivablePaymentRequest

```json
{
  "paymentDate": "2026-05-20",
  "amount": 10000,
  "paymentMethod": "BANK_TRANSFER",
  "reference": "TRX-001"
}
```

Reglas:

- Todos los endpoints requieren `X-Company-Id`.
- `sourceType` debe identificar el documento origen aprobado: `SALE`, `ELECTRONIC_INVOICE`, `ELECTRONIC_POS` u `OPENING_BALANCE`.
- `totalAmount` debe ser mayor que cero y `dueDate` no debe ser anterior a `issueDate`.
- Un pago no puede exceder el saldo pendiente.
- El reporte de cuentas por cobrar debe consultar el modelo nuevo `accounting_accounts_receivable`, no tablas legacy ni solo saldos contables agregados.

## audit-service

Responsabilidad: auditoria tecnica y fiscal.

Estado TASK-042:

- Microservicio fisico implementado en `services/audit-service`.
- Persistencia propia bajo schema `audit`.
- Registra eventos de auditoria fiscal/tecnica por empresa mediante `X-Company-Id`.
- Permite consultar eventos por recurso, rango de fechas y usuario.
- TASK-043 conecta `billing-service` como primer productor automatico para eventos de confirmacion de venta y documento electronico.
- La integracion automatica desde `inventory-service` y `accounting-service` queda para lotes posteriores.

### Endpoints

- `POST /api/v1/audit-events`
- `GET /api/v1/audit-events?resourceType=&resourceId=&from=&to=&userId=`

### AuditEventRequest

```json
{
  "userId": "uuid",
  "eventType": "ELECTRONIC_DOCUMENT",
  "resourceType": "SALE",
  "resourceId": "sale-1",
  "action": "CONFIRM_SALE",
  "result": "SUCCESS",
  "detail": "{\"documentStatus\":\"VALIDATED\",\"providerStatus\":\"ACCEPTED\"}"
}
```

### AuditEventResponse

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "userId": "uuid",
  "eventType": "ELECTRONIC_DOCUMENT",
  "resourceType": "SALE",
  "resourceId": "sale-1",
  "action": "CONFIRM_SALE",
  "result": "SUCCESS",
  "detail": "{\"documentStatus\":\"VALIDATED\",\"providerStatus\":\"ACCEPTED\"}",
  "occurredAt": "2026-05-20T10:00:00Z"
}
```

Reglas:

- No registrar secretos ni datos sensibles innecesarios.
- Operaciones fiscales, cambios de resolucion, emision, anulacion, ajustes e integraciones externas son auditables.
- `detail` debe contener detalle seguro, sin certificados, API keys, tokens, credenciales, payloads completos del proveedor ni datos sensibles innecesarios.
- Ninguna consulta debe retornar eventos de otra empresa.
- `billing-service` publica `ELECTRONIC_DOCUMENT`/`SALE`/`CONFIRM_SALE` despues de confirmar una venta nueva. El resultado es `SUCCESS` cuando el documento queda `VALIDATED`; de lo contrario es `FAILURE`.
- La publicacion inicial desde `billing-service` es sincrona y best-effort para no revertir una emision fiscal ya persistida; el patron outbox/inbox queda pendiente para endurecer garantias de entrega.

## Contratos objetivo pendientes antes de limpieza legacy

Estos contratos deben estabilizarse antes de eliminar codigo o tablas legacy asociadas.

### thirdparty-service: ThirdPartyResponse

Implementado en TASK-047 para `/api/v1/third-parties`, `/api/v1/customers` y `/api/v1/suppliers`.

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "roles": ["CUSTOMER", "SUPPLIER"],
  "personType": "JURIDICA",
  "identificationTypeCode": "NIT",
  "identificationNumber": "900123456",
  "verificationDigit": "7",
  "fullName": null,
  "businessName": "Cliente Proveedor SAS",
  "tradeName": "Cliente Proveedor",
  "email": "contacto@example.com",
  "phone": "3000000000",
  "address": "Calle 1 # 2-3",
  "municipalityCode": "11001",
  "taxResponsibilities": ["O-13"],
  "active": true
}
```

### inventory-service: items de servicio e insumos

- `POST /api/v1/products` debe aceptar `itemType`, `saleEnabled`, `purchaseEnabled` y `stockTracked`.
- `POST /api/v1/service-supply-references` crea una referencia informativa entre un servicio y un insumo.
- `GET /api/v1/products/{serviceId}/supply-references` lista insumos sugeridos.
- `POST /api/v1/inventory-movements` debe aceptar `CONSUMPTION_OUT` y `WASTE_OUT`.

Regla: crear o confirmar una venta de servicio no invoca automaticamente `CONSUMPTION_OUT`.

### inventory-service/accounting-service: compras, gastos y cuentas por pagar

- `POST /api/v1/purchases`
- `POST /api/v1/purchases/{purchaseId}/confirm`
- `POST /api/v1/expenses`
- `POST /api/v1/expenses/{expenseId}/confirm`
- `POST /api/v1/accounts-payable`
- `GET /api/v1/accounts-payable?status=&supplierId=&from=&to=`
- `POST /api/v1/accounts-payable/{payableId}/payments`

### ExpenseRequest

```json
{
  "supplierId": "uuid",
  "expenseDate": "2026-05-20",
  "concept": "Servicio publico energia",
  "subtotal": 100000,
  "taxTotal": 19000,
  "total": 119000,
  "paymentCondition": "CREDIT",
  "dueDate": "2026-06-20",
  "evidenceUrl": "https://example.local/evidence.pdf"
}
```

### AccountsPayableRequest

```json
{
  "supplierId": "uuid",
  "sourceType": "PURCHASE",
  "sourceId": "uuid",
  "issueDate": "2026-05-20",
  "dueDate": "2026-06-20",
  "totalAmount": 107100
}
```

### PayablePaymentRequest

```json
{
  "paymentDate": "2026-05-25",
  "amount": 40000,
  "paymentMethod": "BANK_TRANSFER",
  "reference": "TRX-1"
}
```

Reglas:

- Un gasto confirmado no afecta stock.
- Un gasto o compra a credito crea una cuenta por pagar asociada a `sourceType` y `sourceId`.
- Un pago parcial reduce `balance` y deja estado `PARTIALLY_PAID`; un pago total deja estado `PAID`.
- La contabilizacion depende de reglas PUC parametrizadas por empresa; si la regla no existe, el comando contable falla de forma explicita.

### identity-service: roles y permisos

- `POST /api/v1/users`
- `POST /api/v1/auth/login`
- `GET /api/v1/me`
- `GET /api/v1/me/companies`
- `POST /api/v1/companies/{companyId}/memberships`
- `POST /api/v1/companies/{companyId}/users/{userId}/roles`
- `PUT /api/v1/companies/{companyId}/memberships/{membershipId}/roles`
- `GET /api/v1/companies/{companyId}/permissions?userId=`

Roles minimos: `OWNER`, `ADMIN`, `CASHIER`, `ACCOUNTANT`, `AUDITOR`.

Permisos iniciales: `USERS_MANAGE`, `ROLES_MANAGE`, `SALES_CREATE`, `FISCAL_DOCUMENTS_ISSUE`, `INVENTORY_MANAGE`, `ACCOUNTING_MANAGE`, `REPORTS_VIEW`, `AUDIT_VIEW`, `LICENSE_MANAGE`.

#### CreateUserRequest

```json
{
  "email": "owner@example.com",
  "fullName": "Owner User",
  "password": "secret123"
}
```

#### LoginResponse

```json
{
  "userId": "uuid",
  "email": "owner@example.com",
  "fullName": "Owner User",
  "tokenType": "Bearer",
  "accessToken": "opaque-token",
  "expiresAt": "2026-07-16T22:00:00Z"
}
```

#### MembershipRequest

```json
{
  "userId": "uuid",
  "roles": ["OWNER"]
}
```

#### CompanyAccessResponse

```json
{
  "companyId": "uuid",
  "roles": ["OWNER"],
  "permissions": ["ROLES_MANAGE", "REPORTS_VIEW"]
}
```
### bff-service

Responsabilidad: frontera publica consumida por la SPA y expuesta por API Gateway en produccion.

Estado TASK-063:

- Microservicio fisico inicial implementado en `services/bff-service`.
- No tiene persistencia ni reglas fiscales propias.
- Expone `/api/v1/**` solo para rutas aprobadas y enruta hacia el microservicio dueno del contrato.
- Propaga `Authorization`, `X-Company-Id`, `X-Correlation-Id`, `Idempotency-Key`, `Content-Type` y `Accept`.
- Filtra cabeceras de respuesta a `Content-Type` y `X-Correlation-Id`.
- Rechaza rutas internas no publicas, por ejemplo `/api/v1/provider/**`.

Reglas:

- El frontend debe consumir siempre el BFF.
- Los microservicios internos no deben exponerse al navegador.
- El BFF no debe implementar reglas de negocio que pertenezcan a billing, inventory, accounting, tenant, identity, catalog, thirdparty o audit.
- Un fallo de servicio interno debe responder como error publico estructurado sin stack trace ni detalles de infraestructura.
- Estado TASK-065: `/api/v1/auth/**` y `/api/v1/me/**` se enrutan a `identity-service`; `/api/v1/companies/{companyId}/license/**` se enruta a `tenant-service`; `/api/v1/companies/{companyId}/memberships`, `/api/v1/companies/{companyId}/users/{userId}/roles` y `/api/v1/companies/{companyId}/permissions` se enrutan a `identity-service`.
## tenant-service: licenciamiento

- `POST /api/v1/companies/{companyId}/license`
- `GET /api/v1/companies/{companyId}/license`
- `PUT /api/v1/companies/{companyId}/license/suspend`
- `PUT /api/v1/companies/{companyId}/license/activate`
- `GET /api/v1/companies/{companyId}/license/validation?action=ISSUE_FISCAL_DOCUMENT`

Acciones iniciales de validacion: `CREATE_TRANSACTION`, `ISSUE_FISCAL_DOCUMENT`, `CREATE_USER`.
Los servicios consumidores deben evaluar este contrato antes de comandos de negocio que creen usuarios, transacciones o documentos fiscales.
Si `allowed=false`, el servicio consumidor debe bloquear el comando con error estructurado usando `reasonCode` y `message`.
Las consultas, exportaciones y acciones administrativas de recuperacion no requieren validacion de licencia en esta fase.
Estado TASK-058 licencia consumidores:

- `billing-service` valida licencia contra `tenant-service` antes de crear una venta nueva (`CREATE_TRANSACTION`) y antes de confirmar/emision fiscal (`ISSUE_FISCAL_DOCUMENT`).
- `identity-service` valida licencia contra `tenant-service` antes de crear o actualizar una membresia/roles por empresa mediante accion `CREATE_USER`.
- La creacion global de usuario en `identity-service` no consume licencia porque todavia no pertenece a una empresa; el control aplica al asociarlo a una empresa.
- Los servicios consumidores usan `TENANT_SERVICE_URL` como configuracion externa y no dependen de otros contenedores para arrancar.

### CompanyLicenseRequest

```json
{
  "planCode": "SMALL_BUSINESS",
  "validFrom": "2026-05-01",
  "validTo": "2027-05-01",
  "maxUsers": 5,
  "maxMonthlyDocuments": 1000
}
```

### CompanyLicenseResponse

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "planCode": "SMALL_BUSINESS",
  "status": "ACTIVE",
  "validFrom": "2026-05-01",
  "validTo": "2027-05-01",
  "maxUsers": 5,
  "maxMonthlyDocuments": 1000,
  "createdAt": "2026-05-19T10:00:00Z",
  "updatedAt": "2026-05-19T10:00:00Z"
}
```

### CompanyLicenseValidationResponse

```json
{
  "companyId": "uuid",
  "action": "ISSUE_FISCAL_DOCUMENT",
  "allowed": false,
  "status": "SUSPENDED",
  "reasonCode": "LICENSE_SUSPENDED",
  "message": "La licencia de la empresa esta suspendida."
}
```
### Reportes minimos por servicio dueno del dato

TASK-054 implementa los reportes minimos como endpoints de lectura en los servicios duenos del dato.
El `reporting-service` fisico queda diferido hasta implementar Outbox/Inbox, eventos AWS y proyecciones.

#### billing-service

- `GET /api/v1/reports/sales?status=&from=&to=`
- `GET /api/v1/reports/electronic-documents?documentType=&status=&customerId=&from=&to=&prefix=&number=&cufeCude=`

#### inventory-service

- `GET /api/v1/reports/inventory-stock?active=`
- `GET /api/v1/reports/kardex?productId=&from=&to=`
- `GET /api/v1/reports/purchases?status=&supplierId=&from=&to=`

#### accounting-service

- `GET /api/v1/reports/expenses?status=&supplierId=&from=&to=`
- `GET /api/v1/accounts-payable?status=&supplierId=&from=&to=`
- `GET /api/v1/reports/journal?from=&to=`
- `GET /api/v1/reports/ledger?from=&to=&accountCode=`
- `GET /api/v1/reports/trial-balance?from=&to=&accountCode=`
- `GET /api/v1/reports/accounts-receivable?status=&customerId=&from=&to=`

#### Estado TASK-055

- `GET /api/v1/reports/accounts-receivable?status=&customerId=&from=&to=` consulta el agregado de cartera por cobrar persistido en `accounting-service`.

## Eventos internos propuestos

Los eventos pueden implementarse inicialmente como llamadas sincrones y luego migrar a mensajeria.

### `SaleConfirmed`

Publicado por: `billing-service`.

Consumidores:

- `inventory-service`
- `accounting-service`

```json
{
  "eventId": "uuid",
  "companyId": "uuid",
  "saleId": "uuid",
  "documentId": "uuid",
  "occurredAt": "2026-05-11T22:00:00Z",
  "items": [
    {
      "productId": "uuid",
      "quantity": 2
    }
  ],
  "total": 35700
}
```

### `ElectronicDocumentValidated`

Publicado por: `billing-service`.

Consumidores:

- `inventory-service`
- `accounting-service`
- `audit-service`

### `InventoryMovementRegistered`

Publicado por: `inventory-service`.

Consumidores:

- `accounting-service`
- `audit-service`

### `AccountingEntryPosted`

Publicado por: `accounting-service`.

Consumidores:

- `reporting-service`
- `audit-service`

## Contract tests futuros

Cuando los servicios existan fisicamente, cada contrato debe tener:

- Pruebas de consumidor para requests.
- Pruebas de proveedor para responses.
- Validacion de headers obligatorios.
- Validacion de errores estandar.
- Validacion de aislamiento por `X-Company-Id`.

## Pendientes

- Definir proveedor tecnologico DIAN especifico.
- Definir si la primera implementacion sera monolito modular o servicios fisicos separados.
- Definir broker de eventos si se decide asincronia.
- Definir OpenAPI formal por servicio despues de estabilizar DTOs.

### Estado TASK-052: documentos fiscales, consultas y notas

Implementacion local agregada:

- `GET /api/v1/sales?status=&from=&to=` lista ventas por empresa, estado y rango de fechas.
- `POST /api/v1/electronic-pos?saleId=` confirma la venta indicada y retorna la venta con documento POS.
- `POST /api/v1/electronic-pos/{documentId}/submit` retorna el estado persistido del documento POS; el envio real ya ocurre al confirmar la venta.
- `GET /api/v1/electronic-pos/{documentId}` consulta documento POS por empresa.
- `POST /api/v1/electronic-invoices?saleId=` confirma la venta indicada y retorna la venta con factura electronica.
- `POST /api/v1/electronic-invoices/{documentId}/issue` retorna el estado persistido de la factura; el envio real ya ocurre al confirmar la venta.
- `GET /api/v1/electronic-invoices/{documentId}` consulta factura por empresa.
- `GET /api/v1/electronic-invoices?status=&customerId=&from=&to=&prefix=&number=&cufeCude=` consulta facturas por filtros fiscales.
- `GET /api/v1/electronic-invoices/{documentId}/artifacts` retorna artefactos mock `XML`, `PDF` y `QR` para pruebas funcionales.
- `GET /api/v1/electronic-invoices/{documentId}/events` retorna evento fiscal mock de envio/estado del proveedor.
- `POST /api/v1/credit-notes` crea y envia una nota credito con numeracion propia.
- `GET /api/v1/credit-notes/{noteId}` consulta nota credito.
- `POST /api/v1/debit-notes` crea y envia una nota debito con numeracion propia.
- `GET /api/v1/debit-notes/{noteId}` consulta nota debito.
- `POST /api/v1/electronic-pos/{documentId}/adjustment-notes` crea y envia una nota de ajuste POS con numeracion propia.
- `dian-provider-service` agrega `POST /api/v1/provider/credit-notes`, `POST /api/v1/provider/debit-notes` y `POST /api/v1/provider/pos-adjustment-notes`.

### FiscalNoteRequest

```json
{
  "originalDocumentId": "uuid",
  "adjustmentKind": "CANCELLATION",
  "reason": "Correccion de valor facturado",
  "subtotal": 10000,
  "taxTotal": 1900,
  "total": 11900
}
```

Reglas:

- `adjustmentKind` solo aplica para `POS_ADJUSTMENT_NOTE`; si se omite en nota de ajuste POS, se toma `CORRECTION`.
- Nota credito y nota debito requieren una factura electronica original `VALIDATED` de la misma empresa.
- Nota de ajuste POS requiere un documento `ELECTRONIC_POS` original `VALIDATED` de la misma empresa.
- Cada nota usa resolucion y consecutivo propio segun `documentType`: `CREDIT_NOTE`, `DEBIT_NOTE` o `POS_ADJUSTMENT_NOTE`.
- La respuesta mock aceptada deja `status=VALIDATED`, `providerStatus=ACCEPTED`, CUFE/CUDE y QR simulados.
- La idempotencia se conserva por `companyId` e `Idempotency-Key`.


## BFF public API boundary

El frontend objetivo consume solo el `bff-service` expuesto por API Gateway. Los microservicios internos no se publican directamente al navegador. El BFF debe:

- Propagar `Authorization`, `X-Company-Id`, `X-Correlation-Id` e `Idempotency-Key`.
- Agregar respuestas de varios servicios cuando el flujo de pantalla lo requiera.
- Normalizar errores publicos sin exponer detalles internos.
- Mantener contratos publicos versionados independientes de contratos internos.

## Event envelope target

Los eventos productivos se publicaran hacia EventBridge/SQS usando un envelope comun versionado. TASK-062 lote 1 ya persiste este envelope en Outbox local de productores:

```json
{
  "eventId": "uuid",
  "eventType": "SaleConfirmed",
  "eventVersion": 1,
  "occurredAt": "2026-07-21T00:00:00Z",
  "companyId": "uuid",
  "aggregateType": "SALE",
  "aggregateId": "uuid",
  "producer": "billing-service",
  "correlationId": "string",
  "idempotencyKey": "string",
  "payload": {}
}
```

Eventos canonicos iniciales:

- `SaleConfirmed`: productor `billing-service`, agregado `SALE`; payload con `saleId`, `documentId`, `documentType`, `status`, `total`, `issuedAt`, items e indicadores de efectos de inventario/contabilidad.
- `ElectronicDocumentValidated`: productor `billing-service`, agregado `ELECTRONIC_DOCUMENT`; payload con documento, tipo, prefijo, numero, CUFE/CUDE, estado de proveedor y total.
- `AuditEventRequested`: productor del servicio que origina la accion; payload con `eventType`, `resourceType`, `resourceId`, `action`, `result`, `userId` opcional y `detail` seguro.
- `ProviderSubmissionFailed`: productor `billing-service`, agregado `SALE`; payload con `saleId`, `documentId`, `documentType`, `documentIdempotencyKey`, `providerStatus=FAILED`, totales, lineas y datos de error tecnico. Solo `FAILED` entra a retry automatico; `REJECTED` requiere correccion manual.
- `InventoryMovementRegistered`: productor `inventory-service`, agregado `INVENTORY_MOVEMENT`; payload con movimiento, producto, referencia, cantidad, costo unitario y stock resultante.
- `AccountingEntryPosted`: productor `accounting-service`, agregado `ACCOUNTING_ENTRY`; payload con asiento, origen, fecha, descripcion, total debito y total credito.

Consumidores Lambda deben persistir Inbox o estado equivalente antes de materializar efectos no idempotentes. Los eventos deben conservar `companyId`, `correlationId` e `idempotencyKey`; `payload` no debe contener secretos ni datos de autenticacion.

Consumidores iniciales TASK-062:

- `audit-event-writer-lambda`: `AuditEventRequested` -> `audit.audit_event`.
- `inventory-sale-effect-lambda`: `SaleConfirmed` -> `inventory.stock_balance` y `inventory.inventory_movement`.
- `accounting-sale-entry-lambda`: `SaleConfirmed` -> `accounting.accounting_entry` y `accounting.accounting_outbox_event`.
- `provider-submission-retry-lambda`: `ProviderSubmissionFailed`/`ProviderSubmissionPending` -> reintento tecnico de documento en `billing.electronic_document`.
- `reporting-projection-lambda`: `SaleConfirmed`, `ElectronicDocumentValidated`, `InventoryMovementRegistered`, `AccountingEntryPosted` -> `reporting.reporting_event_projection`.

## EventBridge delivery mapping

El dispatcher Outbox publica cada evento canonico a EventBridge con:

- `eventBusName`: `EVENTING_EVENT_BUS_NAME`.
- `source`: `producer` del envelope, por ejemplo `billing-service`.
- `detailType`: `eventType`, por ejemplo `SaleConfirmed`.
- `detail`: envelope canonico JSON con `payload` como objeto JSON.

Regla de error: si AWS SDK retorna `failedEntryCount > 0`, el evento queda `FAILED` en Outbox con `publishAttempts` incrementado y `lastError` seguro. No se deben registrar secretos ni credenciales en `lastError`.
