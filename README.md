# Factura Electronica Minorista

Backend Java/Spring Boot para una plataforma multiempresa de facturacion electronica colombiana, POS electronico, inventario simple y contabilidad basica con PUC colombiano.

El proyecto migro desde una estructura legacy CRUD hacia Clean Architecture por bounded contexts. Actualmente usa una estructura Maven multi-modulo con microservicios fisicos activos en `services/*`; el codigo del monolito legacy fue removido del repositorio en TASK-059 y las tablas `public.*` legacy se conservan temporalmente solo para auditoria/migracion de datos.

## Estado Actual

- Arquitectura Clean Architecture implementada por modulos.
- PostgreSQL con migraciones Flyway versionadas.
- Docker Compose local para PostgreSQL, `bff-service`, frontend SPA, `tenant-service`, `catalog-service`, `thirdparty-service`, `inventory-service`, `billing-service`, `dian-provider-service`, `accounting-service`, `audit-service` y `payroll-service`.
- POS electronico con emisor/resolucion fiscal persistidos en `billing-service`, proveedor DIAN mock configurable como microservicio HTTP y efectos posteriores idempotentes sobre inventario/contabilidad.
- Persistencia JPA y endpoints REST para billing/POS, accounting, audit y nomina.
- Limpieza legacy en curso: monolito removido, catalogos/terceros legacy de microservicios retirados mediante migraciones nuevas y datos historicos `public.*` preservados hasta migracion aprobada.
- Suite multi-modulo activa validada con `tenant-service`, `catalog-service`, `thirdparty-service`, `inventory-service`, `billing-service`, `dian-provider-service`, `accounting-service`, `audit-service` y `payroll-service`.

## Alcance Funcional

- Catalogos versionados: tipos de documento DIAN, responsabilidades fiscales, regimenes tributarios, metodos de pago, billeteras virtuales y DIVIPOLA por departamentos/municipios.
- Terceros: clientes y proveedores.
- Inventario: productos multiempresa, costos, compras, stock simple, movimientos y kardex.
- Billing/POS: emisor, resoluciones, emision POS electronico, consulta y envio a proveedor DIAN mock.
- Contabilidad: cuentas PUC por empresa, reglas contables configurables, asientos `POSTED`, libro diario y libro mayor.
- Nomina: configuracion por empresa, trabajadores, pagos diarios verbales, documento soporte electronico mock opcional y contabilizacion base de pagos diarios.
- Reportes: ventas, inventario, gastos, cuentas por cobrar, cuentas por pagar, libro diario y libro mayor.
- Errores API: contrato estandar con `timestamp`, `status`, `code`, `message`, `correlationId` y `details`.
- Observabilidad HTTP: correlation ID por request y logs estructurados de inicio/fin.

## Arquitectura

La estructura objetivo por microservicio es:

```text
services/<service>
  src/main/java/.../<service>
    domain/
    application/
    infrastructure/
    interfaces/
```

Estructura actual:

- `services/bff-service`: frontera publica para la SPA y API Gateway.
- `apps/facturaelectronica-web`: SPA React/Vite para pruebas funcionales.
- `services/tenant-service`: microservicio fisico para empresas/tenants.
- `services/catalog-service`: microservicio fisico para catalogos oficiales y configurables.
- `services/thirdparty-service`: microservicio fisico para clientes/proveedores.
- `services/inventory-service`: microservicio fisico para productos, costos, stock, compras y kardex.
- `services/billing-service`: microservicio fisico para ventas POS, emisor fiscal, resoluciones, numeracion fiscal y emision electronica mock.
- `services/dian-provider-service`: microservicio fisico para mock DIAN y futura integracion real.
- `services/accounting-service`: microservicio fisico para PUC, reglas contables, asientos, libro diario y mayor.
- `services/audit-service`: microservicio fisico para auditoria fiscal y tecnica.
- `services/payroll-service`: microservicio fisico para trabajadores, pagos diarios verbales y nomina electronica mock opcional.


La unidad de despliegue objetivo es un artefacto/contenedor por microservicio, no uno por endpoint individual.

## Stack Tecnico

- Java 17
- Spring Boot 3.5.14
- Spring Web MVC
- Spring WebFlux
- Spring Data JPA
- Spring Validation
- Spring Actuator
- Springdoc OpenAPI UI
- PostgreSQL
- Flyway
- Maven Wrapper
- Docker Compose
- Node.js 20 y npm 11 para la SPA local
- JUnit 5, Mockito y AssertJ

## Requisitos

- Java 17.
- Docker Desktop o Docker Engine con Docker Compose.
- PostgreSQL local o contenedor PostgreSQL del proyecto.
- Git.
- PowerShell en Windows para los comandos mostrados.

## Variables De Entorno

El archivo seguro de referencia es `.env.example`.

Variables principales:

```text
POSTGRES_DB=facturaelectronica
POSTGRES_USER=factura_user
POSTGRES_PASSWORD=change_me
POSTGRES_HOST_PORT=5432

TENANT_SERVICE_PORT=8084
CATALOG_SERVICE_PORT=8085
THIRDPARTY_SERVICE_PORT=8086

DB_URL=jdbc:postgresql://postgres:5432/facturaelectronica
DB_USERNAME=factura_user
DB_PASSWORD=change_me

TENANT_DB_URL=jdbc:postgresql://postgres:5432/facturaelectronica
TENANT_DB_USERNAME=factura_user
TENANT_DB_PASSWORD=change_me

CATALOG_DB_URL=jdbc:postgresql://postgres:5432/facturaelectronica
CATALOG_DB_USERNAME=factura_user
CATALOG_DB_PASSWORD=change_me

THIRDPARTY_DB_URL=jdbc:postgresql://postgres:5432/facturaelectronica
THIRDPARTY_DB_USERNAME=factura_user
THIRDPARTY_DB_PASSWORD=change_me

INVENTORY_DB_URL=jdbc:postgresql://postgres:5432/facturaelectronica
INVENTORY_DB_USERNAME=factura_user
INVENTORY_DB_PASSWORD=change_me
INVENTORY_SERVICE_URL=http://inventory-service:8087

BILLING_DB_URL=jdbc:postgresql://postgres:5432/facturaelectronica
BILLING_DB_USERNAME=factura_user
BILLING_DB_PASSWORD=change_me
DIAN_PROVIDER_SERVICE_URL=http://dian-provider-service:8089
ACCOUNTING_SERVICE_URL=http://accounting-service:8090
AUDIT_SERVICE_URL=http://audit-service:8091

DIAN_PROVIDER_DB_URL=jdbc:postgresql://postgres:5432/facturaelectronica
DIAN_PROVIDER_DB_USERNAME=factura_user
DIAN_PROVIDER_DB_PASSWORD=change_me

ACCOUNTING_DB_URL=jdbc:postgresql://postgres:5432/facturaelectronica
ACCOUNTING_DB_USERNAME=factura_user
ACCOUNTING_DB_PASSWORD=change_me

AUDIT_DB_URL=jdbc:postgresql://postgres:5432/facturaelectronica
AUDIT_DB_USERNAME=factura_user
AUDIT_DB_PASSWORD=change_me

JPA_SHOW_SQL=false

DIAN_PROVIDER_MODE=mock
DIAN_MOCK_DEFAULT_STATUS=ACCEPTED
DIAN_MOCK_ERROR_CODE=
DIAN_MOCK_ERROR_MESSAGE=
```

Para ejecucion local fuera de Docker, usa una URL como:

```text
DB_URL=jdbc:postgresql://localhost:15432/facturaelectronica
```

No se deben versionar `.env`, certificados, API keys ni credenciales reales.



## Identidad Y Roles

El modelo objetivo aprobado para identidad usa RBAC modular:

- `ROOT` es un usuario global de plataforma, no pertenece a ninguna empresa y no depende de licencia empresarial.
- `ROOT` registra empresas contratantes, gestiona licencias y entrega el administrador inicial de cada empresa.
- Todos los roles distintos de `ROOT` son roles por empresa y se aislan por `company_id`.
- Cada empresa puede crear roles propios y asignar permisos modulares a sus usuarios.
- Un administrador empresarial no puede crear ni asignar roles con permisos iguales, superiores o no poseidos por el mismo.
- Los permisos `GLOBAL_*` son exclusivos de `ROOT`.
- El frontend debe ocultar modulos no permitidos, pero la autorizacion real siempre debe validarse en backend.
Credenciales ROOT locales dummy para pruebas Docker:

```text
Usuario: root@example.com
Password: RootDemo#2026!
```

Estas credenciales se controlan con `IDENTITY_ROOT_USER_*` y no deben usarse en produccion.

ROOT puede crear empresas contratantes desde la SPA. Al crear una empresa, el `companyId` retornado queda como empresa activa y permite crear el administrador inicial con email, nombre completo, password inicial y rol empresarial `OWNER`.

## Frontend Y BFF

El frontend inicial vive en:

```text
apps/facturaelectronica-web
```

La SPA consume solamente el BFF por `/api/v1`. En desarrollo Vite usa proxy hacia `BFF_SERVICE_PORT`.

El flujo operativo actual inicia con login desde la UI. Sin sesion activa solo se muestra la pantalla de login; los menus y formularios no se renderizan. La SPA llama `POST /api/v1/auth/login`, consulta `GET /api/v1/me/companies`, selecciona una empresa autorizada y valida internamente su licencia con `GET /api/v1/companies/{companyId}/license/validation?action=CREATE_TRANSACTION`.

Despues del login exitoso con licencia activa, la SPA muestra un shell operativo profesional con sidebar, panel superior de sesion/empresa y formularios de empresa, terceros, inventario, configuracion fiscal, venta POS/factura y reportes con campos editables. El JSON de request se arma al enviar cada formulario y se envia al BFF con `Authorization`, `X-Company-Id`, `X-Correlation-Id` e `Idempotency-Key` cuando aplica. El campo `companyId` ya no se digita manualmente en la UI operativa; proviene de las empresas asociadas al usuario autenticado. Si la licencia no esta activa, la UI muestra un modal informativo, limpia la sesion automaticamente y vuelve al login. El encabezado autenticado incluye `Cerrar sesion`.

La UI operativa no muestra paneles permanentes de JSON tecnico. Cada accion usa un modal de proceso/exito/error y los detalles de trazabilidad se consultan en el modulo `Logs`, visible para `ROOT`, administradores de empresa y usuarios con permiso de auditoria.

Ejecutar BFF fuera de Docker:

```powershell
$env:BFF_SERVICE_PORT='8083'
$env:TENANT_SERVICE_URL='http://localhost:8084'
$env:IDENTITY_SERVICE_URL='http://localhost:8092'
$env:CATALOG_SERVICE_URL='http://localhost:8085'
$env:THIRDPARTY_SERVICE_URL='http://localhost:8086'
$env:INVENTORY_SERVICE_URL='http://localhost:8087'
$env:BILLING_SERVICE_URL='http://localhost:8088'
$env:ACCOUNTING_SERVICE_URL='http://localhost:8090'
$env:AUDIT_SERVICE_URL='http://localhost:8091'
.\mvnw.cmd -pl services/bff-service spring-boot:run
```

Ejecutar SPA fuera de Docker:

```powershell
cd apps/facturaelectronica-web
npm install
npm run dev
```

URLs locales por defecto:

```text
BFF: http://localhost:8083
SPA: http://localhost:5173
```

## Ejecucion Local Sin Docker

Primero asegurese de tener PostgreSQL disponible y las variables de entorno configuradas.

El monolito legacy fue removido. La ejecucion local activa se realiza unicamente sobre microservicios fisicos.

Ejecutar `tenant-service` fuera de Docker:

```powershell
$env:TENANT_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:TENANT_DB_USERNAME='factura_user'
$env:TENANT_DB_PASSWORD='change_me'
.\mvnw.cmd -pl services/tenant-service spring-boot:run
```

Ejecutar `catalog-service` fuera de Docker:

```powershell
$env:CATALOG_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:CATALOG_DB_USERNAME='factura_user'
$env:CATALOG_DB_PASSWORD='change_me'
.\mvnw.cmd -pl services/catalog-service spring-boot:run
```

Ejecutar `thirdparty-service` fuera de Docker:

```powershell
$env:THIRDPARTY_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:THIRDPARTY_DB_USERNAME='factura_user'
$env:THIRDPARTY_DB_PASSWORD='change_me'
.\mvnw.cmd -pl services/thirdparty-service spring-boot:run
```

Ejecutar `inventory-service` fuera de Docker:

```powershell
$env:INVENTORY_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:INVENTORY_DB_USERNAME='factura_user'
$env:INVENTORY_DB_PASSWORD='change_me'
.\mvnw.cmd -pl services/inventory-service spring-boot:run
```

Ejecutar `billing-service` fuera de Docker:

```powershell
$env:BILLING_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:BILLING_DB_USERNAME='factura_user'
$env:BILLING_DB_PASSWORD='change_me'
$env:INVENTORY_SERVICE_URL='http://localhost:8087'
$env:DIAN_PROVIDER_SERVICE_URL='http://localhost:8089'
.\mvnw.cmd -pl services/billing-service spring-boot:run
```

Ejecutar `dian-provider-service` fuera de Docker:

```powershell
$env:DIAN_PROVIDER_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:DIAN_PROVIDER_DB_USERNAME='factura_user'
$env:DIAN_PROVIDER_DB_PASSWORD='change_me'
$env:DIAN_PROVIDER_MODE='mock'
$env:DIAN_MOCK_DEFAULT_STATUS='ACCEPTED'
.\mvnw.cmd -pl services/dian-provider-service spring-boot:run
```

`tenant-service` inicia en:

```text
http://localhost:8084
```

`catalog-service` inicia en:

```text
http://localhost:8085
```

`thirdparty-service` inicia en:

```text
http://localhost:8086
```

`inventory-service` inicia en:

```text
http://localhost:8087
```

`billing-service` inicia en:

```text
http://localhost:8088
```

`dian-provider-service` inicia en:

```text
http://localhost:8089
```

## Ejecucion Con Docker Compose

El proyecto incluye `docker-compose.yml` con:

- `postgres`: `postgres:16-alpine`.
- `bff-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/bff-service clean spring-boot:run`.
- `frontend`: `node:20-alpine`, ejecutando `npm install && npm run dev`.
- `tenant-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/tenant-service clean spring-boot:run`.
- `catalog-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/catalog-service clean spring-boot:run`.
- `thirdparty-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/thirdparty-service clean spring-boot:run`.
- `inventory-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/inventory-service clean spring-boot:run`.
- `billing-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/billing-service clean spring-boot:run`.
- `dian-provider-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/dian-provider-service clean spring-boot:run`.
- `accounting-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/accounting-service clean spring-boot:run`.
- `audit-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/audit-service clean spring-boot:run`.
- `payroll-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/payroll-service clean spring-boot:run`.

Politica de arranque local:

- Los microservicios no tienen `depends_on` hacia otros microservicios.
- La unica dependencia de arranque declarada para servicios de aplicacion es `postgres`.
- Las llamadas REST entre servicios ocurren en runtime; si un servicio par no esta disponible, el contenedor llamador debe permanecer iniciado y el caso de uso debe fallar de forma controlada.
- La prueba E2E espera la salud de cada servicio requerido antes de ejecutar el flujo completo.

Crear `.env` desde `.env.example` y ajustar puertos si es necesario. En esta maquina se uso PostgreSQL en el puerto host `15432` porque `5432` y `5433` estaban ocupados o reservados.

Levantar todo:

```powershell
docker compose up -d
```

Levantar solo PostgreSQL:

```powershell
docker compose up -d postgres
```

Ver logs:

```powershell
docker compose logs -f bff-service
docker compose logs -f frontend
docker compose logs -f tenant-service
docker compose logs -f catalog-service
docker compose logs -f thirdparty-service
docker compose logs -f inventory-service
docker compose logs -f billing-service
docker compose logs -f dian-provider-service
docker compose logs -f accounting-service
docker compose logs -f audit-service
docker compose logs -f payroll-service
docker compose logs -f postgres
```

Ver estado:

```powershell
docker compose ps
```

Apagar contenedores:

```powershell
docker compose down
```

Apagar y eliminar volumen local de PostgreSQL:

```powershell
docker compose down -v
```

## Base De Datos

Motor seleccionado: PostgreSQL.

Las migraciones activas se ejecutan con Flyway desde cada microservicio:

```text
services/tenant-service/src/main/resources/db/migration
services/catalog-service/src/main/resources/db/migration
services/thirdparty-service/src/main/resources/db/migration
services/inventory-service/src/main/resources/db/migration
services/billing-service/src/main/resources/db/migration
services/dian-provider-service/src/main/resources/db/migration
services/accounting-service/src/main/resources/db/migration
services/payroll-service/src/main/resources/db/migration
services/audit-service/src/main/resources/db/migration
```

Las migraciones legacy del monolito ya no forman parte del repositorio activo. Las tablas public.* existentes en bases locales o ambientes previos se conservan hasta ejecutar un plan de migracion/respaldo aprobado.

Migracion de `tenant-service`:

- `services/tenant-service/src/main/resources/db/migration/V001__create_tenant_schema.sql`

Migraciones de servicios extraidos:

- `services/catalog-service/src/main/resources/db/migration/V001__create_catalog_schema.sql`
- `services/thirdparty-service/src/main/resources/db/migration/V001__create_thirdparty_schema.sql`
- `services/inventory-service/src/main/resources/db/migration/V001__create_inventory_schema.sql`
- `services/billing-service/src/main/resources/db/migration/V001__create_billing_sales_schema.sql`
- `services/billing-service/src/main/resources/db/migration/V002__add_post_validation_effect_tracking.sql`
- `services/billing-service/src/main/resources/db/migration/V003__create_billing_fiscal_configuration.sql`
- `services/dian-provider-service/src/main/resources/db/migration/V001__create_dian_provider_schema.sql`
- `services/audit-service/src/main/resources/db/migration/V001__create_audit_schema.sql`


Auditar conteos de tablas legacy y tablas de destino antes de proponer eliminaciones:

```powershell
Get-Content .\scripts\legacy-data-audit.sql | docker compose exec -T postgres psql -U factura_user -d facturaelectronica
```

Este script no elimina ni modifica datos de negocio. Reporta tablas presentes o faltantes y conteos exactos para apoyar la limpieza controlada de `TASK-040`.

Tablas relevantes:

- Billing/POS legacy/public: `billing_issuer_profile`, `billing_numbering_resolution`, `billing_electronic_pos_document`, `billing_provider_submission`, `billing_electronic_document_trace_event`, `billing_fiscal_audit_event`.
- Billing/POS activo: `billing.issuer_profile`, `billing.numbering_resolution`, `billing.sale`, `billing.sale_line`, `billing.electronic_document`.
- Accounting: `accounting_account`, `accounting_rule`, `accounting_rule_line`, `accounting_entry`, `accounting_entry_line`.
- Tenant: `tenant.company`.
- Catalog activo: `catalog.catalog_definition`, `catalog.catalog_item`, `catalog.company_catalog_item_setting`, `catalog.department`, `catalog.municipality`.
- Catalog legacy retirado por TASK-088: `catalog.tipodocumento`, `catalog.pais`, `catalog.impuesto`, `catalog.metodo_pago`, `catalog.tipo_gasto`, `catalog.parametros`, `catalog.categoria`, `catalog.producto`.
- Thirdparty activo: `thirdparty.third_party`, `thirdparty.third_party_role`, `thirdparty.third_party_tax_responsibility`.
- Thirdparty legacy retirado por TASK-088: `thirdparty.cliente`, `thirdparty.proveedor`.
- DIAN provider: `dian_provider.provider_submission`.
- Audit: `audit.audit_event`.

Conexion sugerida en PgAdmin/Navicat:

```text
Host: localhost
Port: 15432
Database: facturaelectronica
User: factura_user
Password: change_me
```

El puerto depende de `POSTGRES_HOST_PORT`.

## Endpoints Principales

Todos los endpoints de negocio usan versionado:

```text
/api/v1
```

Header multiempresa:

```text
X-Company-Id: <uuid>
```

### Billing/POS

- `POST /api/v1/issuers`
- `GET /api/v1/issuers/current`
- `POST /api/v1/numbering-resolutions`
- `GET /api/v1/numbering-resolutions?documentType=&active=`
- `POST /api/v1/electronic-pos`
- `GET /api/v1/electronic-pos/{documentId}`
- `POST /api/v1/electronic-pos/{documentId}/submit`

Para enviar POS al proveedor mock tambien se usa:

```text
Idempotency-Key: <valor-unico>
```

### Accounting

- `POST /api/v1/accounts`
- `GET /api/v1/accounts?code=`
- `POST /api/v1/accounting-rules`
- `POST /api/v1/accounting-entries`
- `GET /api/v1/reports/journal?from=&to=`
- `GET /api/v1/reports/ledger?from=&to=&accountCode=`

`POST /api/v1/accounting-entries` genera asientos `POSTED` inmediatamente desde reglas contables activas por empresa.

### Tenant

`tenant-service` expone en `http://localhost:8084`:

- `POST /api/v1/companies`
- `GET /api/v1/companies/{companyId}`
- `PUT /api/v1/companies/{companyId}/activate`
- `PUT /api/v1/companies/{companyId}/suspend`

### Catalog

`catalog-service` expone en `http://localhost:8085` los endpoints canonicos:

- `GET /api/v1/catalog-definitions`
- `GET /api/v1/catalogs/{catalogCode}/items?includeInactive=`
- `POST /api/v1/catalogs/{catalogCode}/items`
- `PUT /api/v1/catalogs/{catalogCode}/items/{itemCode}`
- `PUT /api/v1/catalogs/{catalogCode}/items/{itemCode}/activation`
- `GET /api/v1/company-catalogs/{catalogCode}/items`
- `POST /api/v1/company-catalogs/{catalogCode}/items`
- `PUT /api/v1/company-catalogs/{catalogCode}/items/{itemCode}/activation`
- `GET /api/v1/departments`
- `GET /api/v1/departments/{departmentCode}/municipalities`

Las rutas legacy de catalogo fueron retiradas en TASK-088. Los consumidores deben usar catalogos versionados o los endpoints duenos del bounded context correspondiente.

### Thirdparty

`thirdparty-service` expone en `http://localhost:8086` los endpoints canonicos:

- `POST /api/v1/third-parties`
- `GET /api/v1/third-parties/{thirdPartyId}`
- `GET /api/v1/third-parties/by-document?identificationTypeCode=&identificationNumber=`
- `PUT /api/v1/third-parties/{thirdPartyId}`
- `PUT /api/v1/third-parties/{thirdPartyId}/activate`
- `PUT /api/v1/third-parties/{thirdPartyId}/deactivate`
- `POST /api/v1/customers`
- `GET /api/v1/customers?active=`
- `POST /api/v1/suppliers`
- `GET /api/v1/suppliers?active=`

Las rutas legacy `/api/clientes` y `/api/proveedores` fueron retiradas en TASK-059 lote 2. Los tipos de documento se reciben como codigo fiscal en el contrato v1 y el DV NIT se calcula en dominio.

### Inventory

`inventory-service` expone en `http://localhost:8087`:

- `POST /api/v1/products`
- `GET /api/v1/products/{productId}`
- `GET /api/v1/products/by-barcode/{barcode}`
- `GET /api/v1/products/{productId}/availability?quantity=`
- `GET /api/v1/products/{productId}/kardex`
- `POST /api/v1/inventory-movements`
- `POST /api/v1/purchases`
- `POST /api/v1/purchases/{purchaseId}/confirm`

Las operaciones de negocio requieren `X-Company-Id`; los movimientos y compras requieren `Idempotency-Key`. Cada producto vendible guarda su impuesto de venta como snapshot configurable desde catalogos (`SALES_TAX`), junto con precio, costo, SKU y codigo de barras para operacion POS con lector USB HID.

### Billing

`billing-service` expone en `http://localhost:8088`:

- `POST /api/v1/sales`
- `POST /api/v1/sales/{saleId}/confirm`
- `GET /api/v1/sales/{saleId}`

La creacion de venta valida disponibilidad contra `inventory-service`. El POS usa siempre canal POS/equivalente electronico; precio e impuesto de cada linea se toman del producto en inventario, no del vendedor. Si el comprador no solicita factura nominada, `billing-service` resuelve el perfil `FINAL_CONSUMER` desde configuracion persistida. La confirmacion envia el POS a `dian-provider-service`, que responde con CUDE/QR mock y estado configurable con `DIAN_MOCK_DEFAULT_STATUS`.

Cuando el proveedor responde `ACCEPTED`, `billing-service`:

- registra `SALE_OUT` en `inventory-service` por cada linea vendida;
- genera un asiento `SALE_CONFIRMED` en `accounting-service`;
- marca `inventoryAppliedAt` y `accountingAppliedAt` en el documento electronico para reintentos idempotentes.

### DIAN Provider

`dian-provider-service` expone en `http://localhost:8089`:

- `POST /api/v1/provider/electronic-pos`
- `POST /api/v1/provider/electronic-invoices`
- `GET /api/v1/provider/submissions/{trackingId}`

Los comandos requieren `Idempotency-Key`. Las consultas requieren `X-Company-Id`. El servicio persiste el envio mock en `dian_provider.provider_submission`.

### Accounting

`accounting-service` expone en `http://localhost:8090`:

- `POST /api/v1/accounts`
- `GET /api/v1/accounts?code=`
- `POST /api/v1/accounting-setup/basic`
- `POST /api/v1/accounting-rules`
- `POST /api/v1/accounting-entries`
- `GET /api/v1/reports/expenses?status=&from=&to=`
- `GET /api/v1/accounts-payable?status=&from=&to=`
- `GET /api/v1/reports/accounts-receivable?status=&from=&to=`
- `GET /api/v1/reports/journal?from=&to=`
- `GET /api/v1/reports/ledger?from=&to=&accountCode=`

Los asientos se generan desde reglas activas por empresa y son idempotentes por `companyId`, `sourceType` y `sourceId`. La plantilla basica crea cuentas PUC iniciales y reglas para ventas, compras, gastos, cuentas por cobrar, cuentas por pagar y pago diario de nomina.

### Payroll

`payroll-service` expone en `http://localhost:8093`:

- `GET /api/v1/payroll/settings`
- `PUT /api/v1/payroll/settings`
- `GET /api/v1/payroll/workers`
- `POST /api/v1/payroll/workers`
- `GET /api/v1/payroll/daily-payments`
- `POST /api/v1/payroll/daily-payments`
- `GET /api/v1/payroll/electronic-documents`
- `POST /api/v1/payroll/electronic-documents`

La nomina electronica es configurable por empresa. Si `electronicPayrollEnabled=false`, la empresa puede registrar nomina interna, pero no emitir documento soporte electronico mock. Los pagos diarios verbales se registran con advertencia legal y clasificacion laboral/contractual; no se tratan automaticamente como exentos de obligaciones laborales. Al registrar un pago diario, `payroll-service` intenta crear el asiento contable `PAYROLL_DAILY_PAYMENT_REGISTERED` en `accounting-service` de forma best-effort usando `ACCOUNTING_SERVICE_URL`; si contabilidad no esta disponible, el pago ya persistido no se revierte.

### Audit

`audit-service` expone en `http://localhost:8091`:

- `POST /api/v1/audit-events`
- `GET /api/v1/audit-events?resourceType=&resourceId=&from=&to=&userId=`

Los eventos requieren `X-Company-Id` y almacenan detalle seguro sin secretos en `audit.audit_event`.

El BFF propaga `X-User-Id` y registra auditoria best-effort para mutaciones `POST`, `PUT`, `PATCH` y `DELETE` que pasen por `/api/v1/**`. Para creacion de empresas sin `X-Company-Id`, toma el `id` de la empresa creada y registra la auditoria contra esa empresa. `catalog-service` tambien registra eventos especificos para crear, actualizar, activar e inactivar catalogos globales o configuracion empresarial. La falla de auditoria no detiene la operacion principal en el flujo sincrono local.

`billing-service` registra eventos canonicos en Outbox al confirmar una venta POS/factura y obtener resultado del proveedor DIAN mock. `inventory-service` registra `InventoryMovementRegistered` al crear movimientos y `accounting-service` registra `AccountingEntryPosted` al postear asientos. La entrega hacia EventBridge/SQS ya cuenta con dispatcher condicional, consumidor Lambda `audit-event-writer-lambda` para auditoria, consumidor Lambda `inventory-sale-effect-lambda` para descontar stock desde `SaleConfirmed`, consumidor Lambda `accounting-sale-entry-lambda` para generar asientos contables de forma idempotente, consumidor `provider-submission-retry-lambda` para reintentos tecnicos de proveedor y consumidor `reporting-projection-lambda` para proyecciones de reportes.

### Eventing AWS

El dispatcher Outbox hacia EventBridge esta apagado por defecto para desarrollo local:

```text
EVENTING_EVENTBRIDGE_ENABLED=false
EVENTING_EVENT_BUS_NAME=facturaelectronica-dev-events
EVENTING_OUTBOX_BATCH_SIZE=25
EVENTING_OUTBOX_MAX_ATTEMPTS=5
```

Cuando `EVENTING_EVENTBRIDGE_ENABLED=true`, los servicios productores `billing-service`, `inventory-service` y `accounting-service` leen eventos `PENDING`/`FAILED` de su Outbox, publican a EventBridge usando `detailType=eventType` y marcan `PUBLISHED` o `FAILED` sin detener el microservicio.

Lambdas iniciales:

- `audit-event-writer-lambda`: consume `audit-events` y persiste auditoria central con Inbox.
- `inventory-sale-effect-lambda`: consume `inventory-effects`, procesa `SaleConfirmed`, descuenta lineas `stockTracked=true` y evita duplicados con la clave de idempotencia de la venta.
- `accounting-sale-entry-lambda`: consume `accounting-effects`, procesa `SaleConfirmed`, aplica reglas `SALE_CONFIRMED`/`SALE` y crea asientos contables sin duplicar ventas ya contabilizadas.
- `provider-submission-retry-lambda`: consume `provider-retries`, reintenta fallas tecnicas `FAILED` contra el proveedor DIAN mock y republica eventos si la validacion queda aceptada.
- `reporting-projection-lambda`: consume `reporting-projections` y materializa eventos de ventas, documentos, inventario y contabilidad en `reporting.reporting_event_projection`.

## Guia De Pruebas Docker

La guia E2E desde cero para microservicios, con empresa nueva, inventario, venta POS, proveedor DIAN mock, asiento contable, auditoria central, nomina minima con pago diario verbal, documento soporte mock, consultas SQL y checklist AC-024/AC-031/AC-032/AC-035 esta en:

En Windows el script usa `127.0.0.1` por defecto para evitar bloqueos de resolucion de `localhost`.

```text
docs/e2e-from-zero-test-guide.md
```

La guia legacy del monolito con seed local se conserva como referencia transitoria:

```text
docs/local-docker-test-guide.md
```

## Proveedor DIAN Mock

Mientras no existan proveedor tecnologico real, certificados y credenciales, la plataforma usa `dian-provider-service` en modo mock.

Variables:

```text
DIAN_PROVIDER_MODE=mock
DIAN_MOCK_DEFAULT_STATUS=ACCEPTED
```

Valores soportados para `DIAN_MOCK_DEFAULT_STATUS`:

- `ACCEPTED`
- `REJECTED`
- `FAILED`

Esta simulacion no reemplaza la integracion real con proveedor tecnologico DIAN ni valida cumplimiento final de anexos tecnicos.

## Pruebas

Ejecutar suite completa:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:DB_USERNAME='factura_user'
$env:DB_PASSWORD='change_me'
$env:DIAN_PROVIDER_MODE='mock'
.\mvnw.cmd test
```

Ejecutar prueba enfocada de un microservicio activo usando -pl services/<servicio> y -Dtest=<ClaseTest> cuando aplique.

Ejecutar pruebas de `tenant-service`:

```powershell
$env:TENANT_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:TENANT_DB_USERNAME='factura_user'
$env:TENANT_DB_PASSWORD='change_me'
.\mvnw.cmd -pl services/tenant-service test
```

Ejecutar pruebas de `catalog-service`:

```powershell
$env:CATALOG_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:CATALOG_DB_USERNAME='factura_user'
$env:CATALOG_DB_PASSWORD='change_me'
.\mvnw.cmd -pl services/catalog-service test
```

Ejecutar pruebas de `thirdparty-service`:

```powershell
$env:THIRDPARTY_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:THIRDPARTY_DB_USERNAME='factura_user'
$env:THIRDPARTY_DB_PASSWORD='change_me'
.\mvnw.cmd -pl services/thirdparty-service test
```

Ejecutar pruebas de `inventory-service`:

```powershell
$env:INVENTORY_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:INVENTORY_DB_USERNAME='factura_user'
$env:INVENTORY_DB_PASSWORD='change_me'
.\mvnw.cmd -pl services/inventory-service test
```

Ejecutar pruebas de `billing-service`:

```powershell
$env:BILLING_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:BILLING_DB_USERNAME='factura_user'
$env:BILLING_DB_PASSWORD='change_me'
$env:INVENTORY_SERVICE_URL='http://localhost:8087'
$env:DIAN_PROVIDER_SERVICE_URL='http://localhost:8089'
$env:ACCOUNTING_SERVICE_URL='http://localhost:8090'
.\mvnw.cmd -pl services/billing-service test
```

Ejecutar pruebas de `dian-provider-service`:

```powershell
$env:DIAN_PROVIDER_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:DIAN_PROVIDER_DB_USERNAME='factura_user'
$env:DIAN_PROVIDER_DB_PASSWORD='change_me'
$env:DIAN_PROVIDER_MODE='mock'
.\mvnw.cmd -pl services/dian-provider-service test
```

Ejecutar pruebas de `accounting-service`:

```powershell
$env:ACCOUNTING_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:ACCOUNTING_DB_USERNAME='factura_user'
$env:ACCOUNTING_DB_PASSWORD='change_me'
.\mvnw.cmd -pl services/accounting-service test
```

Ejecutar pruebas de `audit-service`:

```powershell
$env:AUDIT_DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:AUDIT_DB_USERNAME='factura_user'
$env:AUDIT_DB_PASSWORD='change_me'
.\mvnw.cmd -pl services/audit-service test
```

## Especificaciones SDD

La especificacion vive en:

```text
specs/
```

Archivos principales:

- `specs/requirements.md`
- `specs/design.md`
- `specs/tasks.md`
- `specs/api-contract.md`
- `specs/data-model.md`
- `specs/data-dictionary.md`
- `specs/architecture.md`
- `specs/acceptance-criteria.md`
- `specs/use-cases.md`

Toda modificacion funcional debe estar trazada a requisitos, criterios de aceptacion y tareas SDD.

## Seguridad

- No versionar `.env`.
- No versionar certificados DIAN.
- No versionar API keys ni passwords reales.
- El proveedor DIAN real esta pendiente.
- Los errores publicos deben usar mensajes seguros y no exponer stack traces.
- Las variables DIAN reales deben moverse a un gestor de secretos o mecanismo aprobado antes de produccion.

## Infraestructura

Estado actual:

- Docker Compose local disponible.
- PostgreSQL local en contenedor.
- `bff-service`, frontend SPA, `tenant-service`, `catalog-service`, `thirdparty-service`, `inventory-service`, `billing-service`, `dian-provider-service`, `accounting-service` y `audit-service` locales montados como volumen y ejecutados con Maven/npm. Los comandos Compose usan `clean spring-boot:run` para evitar clases obsoletas durante la migracion.
- El monolito legacy fue removido del repositorio activo; Docker Compose solo levanta microservicios fisicos y PostgreSQL.

Pendiente:

- `Dockerfile` productivo multi-stage.
- Terraform/IaC.
- Configuracion cloud.
- Pipeline CI/CD.
- Escaneo de imagenes con Docker Scout, Trivy, Grype o herramienta equivalente.

## Pendientes Relevantes

- Integracion real con proveedor tecnologico DIAN.
- Certificados digitales reales.
- Representacion grafica oficial.
- XML UBL y anexos tecnicos definitivos.
- Seguridad/autenticacion/autorizacion.
- Productores de auditoria restantes desde inventario y contabilidad, identity-service y modulo de gastos fuera del legacy.

## Git

Rama actual usada durante el ultimo push:

```text
master
```

Formato recomendado de commits:

```text
<gitmoji> <type>(<scope>): <descripcion-corta>
```

Ejemplo:

```text
arch: clean backend
```

## Nota Legal Y Contable

La implementacion tecnica debe ser validada antes de produccion contra la normatividad colombiana vigente, los anexos tecnicos DIAN aplicables, el proveedor tecnologico seleccionado y el criterio de un contador publico o asesor tributario.


## Arquitectura cloud AWS objetivo

El target productivo aprobado es 100% cloud en AWS:

- Frontend SPA en Amazon S3 privado servido por CloudFront.
- Entrada publica mediante API Gateway hacia un BFF.
- BFF y microservicios Spring Boot en ECS Fargate.
- Procesos event-driven cortos e idempotentes en Lambda con EventBridge/SQS.
- Persistencia en RDS/Aurora PostgreSQL.
- Secretos, certificados y credenciales en Secrets Manager o Parameter Store.

Docker Compose se mantiene como entorno local de desarrollo y pruebas. La arquitectura productiva documentada usa exclusivamente EventBridge/SQS + Lambda para mensajeria asincrona administrada en AWS.

## Terraform AWS

El esqueleto IaC inicial vive en `infra/aws`.

```powershell
cd infra/aws/envs/dev
terraform init
terraform fmt -recursive -check ..\..
terraform validate
terraform plan -out dev.tfplan
```

No ejecutar `terraform apply` sin aprobacion explicita. El ambiente `dev` crea recursos cloud objetivo con servicios ECS en `desired_count = 0` hasta publicar imagenes productivas en ECR.
