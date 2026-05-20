# Guia local Docker y datos demo

Esta guia valida el estado actual del backend en Docker con PostgreSQL, proveedor DIAN mock, POS electronico y contabilidad. No usa credenciales reales ni certificados DIAN.

## Datos fijos

```text
Base URL: http://localhost:8083
Company ID: 11111111-1111-1111-1111-111111111111
PostgreSQL host: localhost
PostgreSQL port: 15432
Database: facturaelectronica
User: factura_user
Password: change_me
Proveedor DIAN: mock local
```

## 1. Levantar servicios

```powershell
docker compose up -d
docker compose ps
docker compose logs -f legacy-monolith
```

Healthcheck:

```powershell
curl.exe -s http://localhost:8083/actuator/health
```

Resultado esperado:

```json
{"status":"UP"}
```

## 2. Cargar seed local

El seed esta fuera de Flyway para evitar que datos dummy entren automaticamente a otros ambientes.

```powershell
Get-Content .\services\legacy-monolith\src\main\resources\db\seed\local-demo-seed.sql | docker compose exec -T postgres psql -U factura_user -d facturaelectronica
```

El script es idempotente: puede ejecutarse varias veces sin duplicar los registros definidos por ID o codigo.

## 3. Variables para las pruebas API

```powershell
$BaseUrl = 'http://localhost:8083'
$CompanyId = '11111111-1111-1111-1111-111111111111'
$CorrelationId = [guid]::NewGuid().ToString()
```

## 4. Emitir POS electronico

```powershell
$posBody = @'
{
  "saleId": "66666666-6666-6666-6666-666666666666",
  "buyerName": "Cliente Demo",
  "buyerDocumentType": "CC",
  "buyerDocumentNumber": "10101010",
  "documentDate": "2026-05-19",
  "environment": "TEST",
  "lines": [
    {
      "productId": "77777777-7777-7777-7777-777777777777",
      "quantity": 2,
      "unitPrice": 15000.00,
      "discountAmount": 0.00,
      "taxCode": "IVA",
      "taxRate": 19.00
    }
  ]
}
'@

$pos = curl.exe -s -X POST "$BaseUrl/api/v1/electronic-pos" `
  -H "Content-Type: application/json" `
  -H "X-Company-Id: $CompanyId" `
  -H "X-Correlation-Id: $CorrelationId" `
  -d $posBody | ConvertFrom-Json

$DocumentId = $pos.id
$pos
```

Resultado esperado:

- `status`: `NUMBER_ASSIGNED`.
- `prefix`: `POS`.
- `number`: consecutivo asignado desde la resolucion demo.
- `subtotal`: `30000.00`.
- `taxTotal`: `5700.00`.
- `total`: `35700.00`.

## 5. Consultar POS persistido

```powershell
curl.exe -s -X GET "$BaseUrl/api/v1/electronic-pos/$DocumentId" `
  -H "X-Company-Id: $CompanyId" `
  -H "X-Correlation-Id: $CorrelationId"
```

## 6. Enviar POS al proveedor DIAN mock

```powershell
$IdempotencyKey = "demo-pos-$DocumentId"

$submission = curl.exe -s -X POST "$BaseUrl/api/v1/electronic-pos/$DocumentId/submit" `
  -H "X-Company-Id: $CompanyId" `
  -H "X-Correlation-Id: $CorrelationId" `
  -H "Idempotency-Key: $IdempotencyKey" | ConvertFrom-Json

$submission
```

Resultado esperado con `DIAN_MOCK_DEFAULT_STATUS=ACCEPTED`:

- `providerStatus`: `ACCEPTED`.
- `documentStatus`: `VALIDATED`.
- `providerSubmissionId`: valor `DUMMY-SUBMISSION-*`.
- `cufeCude`: valor dummy para validar persistencia.

## 7. Generar asiento contable de la venta

```powershell
$entryBody = @"
{
  "eventType": "SALE_CONFIRMED",
  "sourceType": "SALE",
  "sourceId": "$DocumentId",
  "entryDate": "2026-05-19",
  "description": "Venta POS demo $DocumentId",
  "thirdpartyId": "88888888-8888-8888-8888-888888888888",
  "subtotal": 30000.00,
  "taxTotal": 5700.00,
  "total": 35700.00
}
"@

$entry = curl.exe -s -X POST "$BaseUrl/api/v1/accounting-entries" `
  -H "Content-Type: application/json" `
  -H "X-Company-Id: $CompanyId" `
  -H "X-Correlation-Id: $CorrelationId" `
  -d $entryBody | ConvertFrom-Json

$entry
```

Resultado esperado:

- `status`: `POSTED`.
- `debitTotal`: `35700.00`.
- `creditTotal`: `35700.00`.
- Lineas:
  - Debito `110505` por total.
  - Credito `413505` por subtotal.
  - Credito `240805` por IVA.

## 8. Consultar libro diario y mayor

```powershell
curl.exe -s -X GET "$BaseUrl/api/v1/reports/journal?from=2026-05-01&to=2026-05-31" `
  -H "X-Company-Id: $CompanyId" `
  -H "X-Correlation-Id: $CorrelationId"
```

```powershell
curl.exe -s -X GET "$BaseUrl/api/v1/reports/ledger?from=2026-05-01&to=2026-05-31" `
  -H "X-Company-Id: $CompanyId" `
  -H "X-Correlation-Id: $CorrelationId"
```

## 9. Ver data en PostgreSQL

Desde PgAdmin o Navicat:

```sql
SELECT id, company_id, prefix, document_number, status, subtotal, tax_total, total
FROM billing_electronic_pos_document
WHERE company_id = '11111111-1111-1111-1111-111111111111'
ORDER BY issue_at DESC;

SELECT document_id, status, provider_submission_id, cufe_cude, submitted_at
FROM billing_provider_submission
WHERE company_id = '11111111-1111-1111-1111-111111111111'
ORDER BY submitted_at DESC;

SELECT code, name, category, level, nature, active
FROM accounting_account
WHERE company_id = '11111111-1111-1111-1111-111111111111'
ORDER BY code;

SELECT e.id, e.entry_date, e.description, e.debit_total, e.credit_total, l.account_code, l.debit_amount, l.credit_amount
FROM accounting_entry e
JOIN accounting_entry_line l ON l.entry_id = e.id
WHERE e.company_id = '11111111-1111-1111-1111-111111111111'
ORDER BY e.entry_date DESC, l.line_order;
```

## 10. Ver logs con correlation ID

```powershell
docker compose logs legacy-monolith | Select-String "correlationId="
```

Debe verse `event=http_request_start` y `event=http_request_end` con el mismo `correlationId` enviado en los headers.

## 11. Probar rechazo DIAN mock

Cambiar temporalmente en `.env`:

```text
DIAN_MOCK_DEFAULT_STATUS=REJECTED
DIAN_MOCK_ERROR_CODE=DEMO_REJECTED
DIAN_MOCK_ERROR_MESSAGE=Documento rechazado por prueba local
```

Reiniciar el monolito transitorio:

```powershell
docker compose up -d legacy-monolith
```

Emitir un nuevo POS y enviarlo al mock. Resultado esperado:

- `providerStatus`: `REJECTED`.
- `documentStatus`: `REJECTED`.
- Error publico seguro sin secretos.

## Checklist AC-024

- [ ] `docker compose ps` muestra `postgres` healthy y `legacy-monolith` arriba.
- [ ] `/actuator/health` responde `UP`.
- [ ] El seed local se ejecuta sin errores.
- [ ] Se emite un POS electronico por API.
- [ ] Se consulta el POS por API.
- [ ] Se envia el POS al proveedor DIAN mock.
- [ ] PostgreSQL muestra documento, envio de proveedor, cuentas PUC y asiento contable.
- [ ] Libro diario y libro mayor responden por API.
- [ ] Logs de `legacy-monolith` incluyen `correlationId=`.

## Limitaciones locales

- El proveedor DIAN es mock; no firma, no envia a DIAN y no valida anexos tecnicos oficiales.
- No hay certificados reales ni credenciales productivas.
- El POS usa CUDE/QR/XML dummy para validar flujo y persistencia.
- El descuento automatico de inventario conectado al flujo POS sigue pendiente.
- La contabilizacion automatica conectada al flujo POS sigue pendiente; en esta guia se dispara por endpoint contable.
