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

## tenant-service

Responsabilidad: empresas, configuracion multiempresa y estado del tenant.

### Endpoints

- `POST /api/v1/companies`
- `GET /api/v1/companies/{companyId}`
- `PUT /api/v1/companies/{companyId}`
- `PUT /api/v1/companies/{companyId}/activate`
- `PUT /api/v1/companies/{companyId}/suspend`

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

Responsabilidad: usuarios, roles, membresias por empresa y autenticacion.

### Endpoints

- `POST /api/v1/auth/login`
- `POST /api/v1/users`
- `GET /api/v1/users/{userId}`
- `POST /api/v1/companies/{companyId}/users/{userId}/roles`
- `GET /api/v1/me/companies`

Regla:

- Un usuario puede pertenecer a varias empresas.
- Todo token debe permitir determinar empresas autorizadas y roles por empresa.

## catalog-service

Responsabilidad: catalogos globales y catalogos configurables por empresa.

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

### Clientes

- `POST /api/v1/customers`
- `GET /api/v1/customers/{customerId}`
- `GET /api/v1/customers?identificationNumber=&identificationTypeId=&name=&active=`
- `PUT /api/v1/customers/{customerId}`
- `PUT /api/v1/customers/{customerId}/deactivate`
- `PUT /api/v1/customers/{customerId}/activate`

### Proveedores

- `POST /api/v1/suppliers`
- `GET /api/v1/suppliers/{supplierId}`
- `GET /api/v1/suppliers?identificationNumber=&identificationTypeId=&name=&active=`
- `PUT /api/v1/suppliers/{supplierId}`
- `PUT /api/v1/suppliers/{supplierId}/deactivate`
- `PUT /api/v1/suppliers/{supplierId}/activate`

### ThirdPartyRequest

```json
{
  "identificationTypeId": "uuid",
  "identificationNumber": "900123456",
  "verificationDigit": "7",
  "name": "Cliente Prueba",
  "email": "cliente@example.com",
  "phone": "3000000000",
  "address": "Calle 1 # 2-3",
  "taxRegime": "RESPONSABLE_IVA"
}
```

Reglas:

- `identificationTypeId + identificationNumber` es unico por empresa.
- Ninguna consulta puede retornar terceros de otra empresa.

## inventory-service

Responsabilidad: productos, stock simple, compras, movimientos y kardex.

### Productos

- `POST /api/v1/products`
- `GET /api/v1/products/{productId}`
- `GET /api/v1/products?barcode=&sku=&name=&active=`
- `PUT /api/v1/products/{productId}`
- `PUT /api/v1/products/{productId}/deactivate`
- `GET /api/v1/products/{productId}/stock`
- `GET /api/v1/products/{productId}/kardex?from=&to=`

### Compras

- `POST /api/v1/purchases`
- `POST /api/v1/purchases/{purchaseId}/confirm`
- `GET /api/v1/purchases/{purchaseId}`

### Movimientos

- `POST /api/v1/inventory-movements`
- `GET /api/v1/inventory-movements?productId=&sourceType=&from=&to=`

### ProductAvailabilityRequest

```json
{
  "items": [
    {
      "productId": "uuid",
      "quantity": 2
    }
  ]
}
```

### ProductAvailabilityResponse

```json
{
  "available": true,
  "items": [
    {
      "productId": "uuid",
      "requestedQuantity": 2,
      "currentStock": 10,
      "available": true
    }
  ]
}
```

Reglas:

- Una venta facturada descuenta stock.
- Una compra confirmada aumenta stock.
- Stock negativo no esta permitido en la fase inicial.
- Todo movimiento debe referenciar documento origen.

## billing-service

Responsabilidad: ventas POS, facturacion electronica, documento equivalente POS, notas, numeracion y estados fiscales.

### Emisor

- `POST /api/v1/issuers`
- `GET /api/v1/issuers/current`
- `PUT /api/v1/issuers/{issuerId}`

### Resoluciones

- `POST /api/v1/numbering-resolutions`
- `GET /api/v1/numbering-resolutions?documentType=&active=`
- `GET /api/v1/numbering-resolutions/{resolutionId}`
- `PUT /api/v1/numbering-resolutions/{resolutionId}/activate`
- `PUT /api/v1/numbering-resolutions/{resolutionId}/deactivate`

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
      "taxCode": "IVA_19"
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
  "providerStatus": "ACCEPTED"
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
- El POS genera documento electronico a partir de la venta.
- La afectacion de inventario ocurre cuando la venta/documento llegue al estado aprobado por la politica transaccional.
- En ambiente local, `POST /api/v1/electronic-pos/{documentId}/submit` usa proveedor DIAN mock sin llamadas externas.

## dian-provider-service

Responsabilidad: encapsular integracion con proveedor tecnologico DIAN.

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
- La implementacion concreta queda pendiente hasta seleccionar proveedor tecnologico.
- En desarrollo local se usara un adaptador dummy que responde sin llamadas externas y solo sirve para probar el flujo interno.
- Variables locales del mock:
  - `DIAN_PROVIDER_MODE=mock`.
  - `DIAN_MOCK_DEFAULT_STATUS=ACCEPTED|REJECTED|FAILED`.
  - `DIAN_MOCK_ERROR_CODE`.
  - `DIAN_MOCK_ERROR_MESSAGE`.
- En esta version, `DIAN_PROVIDER_MODE` solo acepta `mock`; un valor distinto debe fallar explicitamente hasta implementar el adaptador real.

## accounting-service

Responsabilidad: PUC, cuentas por empresa, asientos, libro diario y libro mayor.

### Cuentas

- `POST /api/v1/accounts`
- `GET /api/v1/accounts?code=`

### Reglas contables

- `POST /api/v1/accounting-rules`

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
- Las cuentas deben derivar del PUC colombiano o de una configuracion aprobada por empresa.
- Un documento fiscal validado debe poder rastrearse hasta su asiento contable.

## audit-service

Responsabilidad: auditoria tecnica y fiscal.

### Endpoints

- `POST /api/v1/audit-events`
- `GET /api/v1/audit-events?resourceType=&resourceId=&from=&to=&userId=`

Reglas:

- No registrar secretos ni datos sensibles innecesarios.
- Operaciones fiscales, cambios de resolucion, emision, anulacion, ajustes e integraciones externas son auditables.

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
