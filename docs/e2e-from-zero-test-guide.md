# Prueba E2E Docker desde cero

Esta guia valida el flujo local multi-contenedor desde una empresa nueva hasta una venta POS aceptada por el proveedor DIAN mock, con descuento de inventario y asiento contable generado.

No usa credenciales reales, certificados DIAN ni datos productivos.

## Servicios involucrados

| Componente | Puerto local | Rol en el flujo |
| --- | ---: | --- |
| `postgres` | `15432` o `${POSTGRES_HOST_PORT}` | Persistencia local |
| `tenant-service` | `8084` | Crea empresa/tenant |
| `catalog-service` | `8085` | Crea tipo de documento legacy requerido por terceros |
| `thirdparty-service` | `8086` | Crea cliente y proveedor de referencia |
| `inventory-service` | `8087` | Crea producto, stock inicial, kardex y salida por venta |
| `dian-provider-service` | `8089` | Simula aceptacion DIAN |
| `accounting-service` | `8090` | Crea cuentas PUC, regla y asiento contable |
| `billing-service` | `8088` | Crea venta, confirma POS y orquesta efectos posteriores |

`legacy-monolith` no participa en esta prueba. `audit-service` participa como microservicio fisico y recibe desde `billing-service` el evento fiscal `ELECTRONIC_DOCUMENT`/`SALE`/`CONFIRM_SALE` cuando la venta POS queda confirmada.

## Limitaciones conocidas

- `thirdparty-service` y parte de `catalog-service` conservan endpoints legacy sin `X-Company-Id`; se incluyen para completar el escenario funcional, pero no prueban aislamiento multiempresa.
- `billing-service` crea emisor y resolucion POS reales para la corrida local; la integracion DIAN sigue en modo mock y no valida anexos tecnicos oficiales.
- El aislamiento por `company_id` se verifica en servicios que ya lo implementan: `tenant-service`, `inventory-service`, `billing-service`, `dian-provider-service` y `accounting-service`.
- La integracion DIAN real queda fuera de alcance; `DIAN_PROVIDER_MODE=mock` y `DIAN_MOCK_DEFAULT_STATUS=ACCEPTED`.

## 1. Preparar entorno

Revise que `.env` tenga valores locales seguros similares a:

```text
POSTGRES_DB=facturaelectronica
POSTGRES_USER=factura_user
POSTGRES_PASSWORD=change_me
POSTGRES_HOST_PORT=15432
DIAN_PROVIDER_MODE=mock
DIAN_MOCK_DEFAULT_STATUS=ACCEPTED
```

Levante los servicios requeridos:

```powershell
docker compose up -d postgres tenant-service catalog-service thirdparty-service inventory-service accounting-service dian-provider-service audit-service billing-service
docker compose ps
```

Docker Compose no encadena microservicios con `depends_on`; solo PostgreSQL es dependencia de arranque. El script E2E valida la salud de cada servicio antes de invocar el flujo.

Healthchecks esperados:

```powershell
curl.exe -s http://localhost:8084/actuator/health
curl.exe -s http://localhost:8085/actuator/health
curl.exe -s http://localhost:8086/actuator/health
curl.exe -s http://localhost:8087/actuator/health
curl.exe -s http://localhost:8088/actuator/health
curl.exe -s http://localhost:8089/actuator/health
curl.exe -s http://localhost:8090/actuator/health
```

Cada respuesta debe incluir:

```json
{"status":"UP"}
```

## 2. Ejecutar prueba automatizada

El script crea datos con sufijo unico en cada ejecucion. No borra volumenes ni limpia tablas por defecto.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\e2e-from-zero.ps1
```

Para que el script levante contenedores antes de probar:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\e2e-from-zero.ps1 -StartContainers
```

Resultado esperado al final:

```text
E2E flow completed successfully.
```

## 3. Flujo validado por el script

1. Crea una empresa activa en `tenant-service`.
2. Crea un tipo de documento en `catalog-service`.
3. Crea un cliente y un proveedor en `thirdparty-service`.
4. Configura emisor y resolucion POS en `billing-service`.
5. Crea cuentas PUC basicas en `accounting-service`:
   - `1105` Caja.
   - `4135` Comercio al por mayor y al por menor.
   - `2408` IVA generado.
6. Crea regla contable `SALE_CONFIRMED` + `SALE`:
   - Debito `1105` por `TOTAL`.
   - Credito `4135` por `SUBTOTAL`.
   - Credito `2408` por `TAX_TOTAL`.
7. Crea producto con costo, precio y stock inicial en `inventory-service`.
8. Crea una venta POS en `billing-service`.
9. Confirma la venta.
10. `billing-service` asigna prefijo/consecutivo desde la resolucion configurada y envia el documento a `dian-provider-service`.
11. Si el proveedor mock acepta, se registran:
    - `SALE_OUT` en inventario.
    - asiento contable balanceado en contabilidad.
    - marcas `inventoryAppliedAt` y `accountingAppliedAt` en el documento electronico.
12. Verifica consulta de proveedor por `trackingId`.
13. Verifica que otra empresa no pueda consultar el producto, venta, envio o cuentas de la primera.

## 4. Consultas PostgreSQL

Use PgAdmin, Navicat o `psql`. Reemplace los valores por los IDs impresos por el script.

```sql
select id, legal_name, identification_number, status
from tenant.company
order by created_at desc
limit 5;

select id, company_id, sku, name, sale_price, cost, current_stock
from inventory.product
order by created_at desc
limit 5;

select product_id, company_id, quantity_on_hand
from inventory.stock_balance
order by updated_at desc
limit 5;

select id, company_id, product_id, movement_type, quantity, previous_stock, resulting_stock, source_document_type, source_document_id
from inventory.inventory_movement
order by movement_at desc
limit 10;

select id, company_id, status, subtotal, tax_total, total
from billing.sale
order by created_at desc
limit 5;

select id, company_id, legal_name, nit, active
from billing.issuer_profile
order by id desc
limit 5;

select id, company_id, document_type, resolution_number, prefix, from_number, to_number, current_number, environment, active
from billing.numbering_resolution
order by valid_to desc
limit 5;

select id, company_id, sale_id, prefix, document_number, status, provider_status, provider_tracking_id, inventory_applied_at, accounting_applied_at
from billing.electronic_document
order by issued_at desc
limit 5;

select id, company_id, document_id, document_type, tracking_id, status, cufe_cude
from dian_provider.provider_submission
order by created_at desc
limit 5;

select id, company_id, code, name, category, nature
from accounting.accounting_account
order by code asc;

select id, company_id, source_type, source_id, debit_total, credit_total, status
from accounting.accounting_entry
order by entry_date desc
limit 5;

select line.id, line.account_code, line.debit_amount, line.credit_amount, line.description
from accounting.accounting_entry_line line
join accounting.accounting_entry entry on entry.id = line.entry_id
order by line.id desc
limit 10;
```

## 5. Datos esperados

Para la venta de prueba:

```text
Stock inicial: 10
Cantidad vendida: 2
Stock final esperado: 8
Subtotal esperado: 30000.00
IVA 19% esperado: 5700.00
Total esperado: 35700.00
Estado venta esperado: CONFIRMED
Estado documento esperado: VALIDATED
Estado proveedor esperado: ACCEPTED
Prefijo esperado: POS
Asiento esperado: debitos 35700.00, creditos 35700.00
```

## 6. Reset local opcional

Esto borra los datos locales del volumen PostgreSQL. Ejecutelo solo si quiere empezar con base limpia.

```powershell
docker compose down -v
docker compose up -d postgres tenant-service catalog-service thirdparty-service inventory-service accounting-service dian-provider-service audit-service billing-service
```
