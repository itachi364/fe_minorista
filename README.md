# Factura Electronica Minorista

Backend Java/Spring Boot para una plataforma multiempresa de facturacion electronica colombiana, POS electronico, inventario simple y contabilidad basica con PUC colombiano.

El proyecto esta migrando desde una estructura legacy CRUD hacia Clean Architecture por bounded contexts. Actualmente usa una estructura Maven multi-modulo: los microservicios fisicos activos viven en `services/*` y `services/legacy-monolith` queda como modulo transitorio de referencia, excluido del reactor Maven por defecto y disponible solo mediante perfil explicito.

## Estado Actual

- Arquitectura Clean Architecture implementada por modulos.
- PostgreSQL con migraciones Flyway versionadas.
- Docker Compose local para PostgreSQL, `tenant-service`, `catalog-service`, `thirdparty-service`, `inventory-service`, `billing-service`, `dian-provider-service`, `accounting-service` y `audit-service`.
- POS electronico con emisor/resolucion fiscal persistidos en `billing-service`, proveedor DIAN mock configurable como microservicio HTTP y efectos posteriores idempotentes sobre inventario/contabilidad.
- Persistencia JPA y endpoints REST para billing/POS, accounting y audit.
- Refactor de modulos legacy hacia bounded contexts.
- Suite multi-modulo activa validada con `tenant-service`, `catalog-service`, `thirdparty-service`, `inventory-service`, `billing-service`, `dian-provider-service`, `accounting-service` y `audit-service`.

## Alcance Funcional

- Catalogos: categorias, productos, impuestos, paises, parametros, metodos de pago, tipos de documento y tipos de gasto.
- Terceros: clientes y proveedores.
- Inventario: productos multiempresa, costos, compras, stock simple, movimientos y kardex.
- Billing/POS: emisor, resoluciones, emision POS electronico, consulta y envio a proveedor DIAN mock.
- Contabilidad: cuentas PUC por empresa, reglas contables configurables, asientos `POSTED`, libro diario y libro mayor.
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

- `services/legacy-monolith`: modulo transitorio de referencia; no se levanta en Docker Compose local por defecto ni compila en el reactor Maven activo.
- `services/tenant-service`: microservicio fisico para empresas/tenants.
- `services/catalog-service`: microservicio fisico para catalogos oficiales y configurables.
- `services/thirdparty-service`: microservicio fisico para clientes/proveedores.
- `services/inventory-service`: microservicio fisico para productos, costos, stock, compras y kardex.
- `services/billing-service`: microservicio fisico para ventas POS, emisor fiscal, resoluciones, numeracion fiscal y emision electronica mock.
- `services/dian-provider-service`: microservicio fisico para mock DIAN y futura integracion real.
- `services/accounting-service`: microservicio fisico para PUC, reglas contables, asientos, libro diario y mayor.
- `services/audit-service`: microservicio fisico para auditoria fiscal y tecnica.

Bounded contexts presentes dentro del monolito transitorio:

- `catalog`
- `thirdparty`
- `inventory`
- `expenses`
- `billing`
- `accounting`

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
CATALOG_SERVICE_URL=http://catalog-service:8085

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

## Ejecucion Local Sin Docker

Primero asegurese de tener PostgreSQL disponible y las variables de entorno configuradas.

El monolito transitorio no hace parte de la ejecucion local activa. Para ejecutarlo bajo demanda use el perfil Maven explicito:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:DB_USERNAME='factura_user'
$env:DB_PASSWORD='change_me'
$env:DIAN_PROVIDER_MODE='mock'
.\mvnw.cmd -Plegacy-monolith -pl services/legacy-monolith spring-boot:run
```

Nota: `legacy-monolith` se mantiene solo como modulo transitorio y no participa en el flujo Docker E2E activo.

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
$env:CATALOG_SERVICE_URL='http://localhost:8085'
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
- `tenant-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/tenant-service clean spring-boot:run`.
- `catalog-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/catalog-service clean spring-boot:run`.
- `thirdparty-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/thirdparty-service clean spring-boot:run`.
- `inventory-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/inventory-service clean spring-boot:run`.
- `billing-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/billing-service clean spring-boot:run`.
- `dian-provider-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/dian-provider-service clean spring-boot:run`.
- `accounting-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/accounting-service clean spring-boot:run`.
- `audit-service`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw -pl services/audit-service clean spring-boot:run`.

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
docker compose logs -f tenant-service
docker compose logs -f catalog-service
docker compose logs -f thirdparty-service
docker compose logs -f inventory-service
docker compose logs -f billing-service
docker compose logs -f dian-provider-service
docker compose logs -f accounting-service
docker compose logs -f audit-service
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
services/audit-service/src/main/resources/db/migration
```

Migraciones legacy de referencia, fuera del reactor activo por defecto:

- `V001__create_legacy_public_schema.sql`
- `V002__create_billing_pos_schema.sql`
- `V003__create_accounting_schema.sql`

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

Seed local legacy opcional, no usado por el flujo E2E activo:

```text
services/legacy-monolith/src/main/resources/db/seed/local-demo-seed.sql
```

Aplicarlo manualmente:

```powershell
Get-Content .\services\legacy-monolith\src\main\resources\db\seed\local-demo-seed.sql | docker compose exec -T postgres psql -U factura_user -d facturaelectronica
```

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
- Catalog: `catalog.tipodocumento`, `catalog.pais`, `catalog.impuesto`, `catalog.metodo_pago`, `catalog.tipo_gasto`, `catalog.parametros`, `catalog.categoria`, `catalog.producto`.
- Thirdparty: `thirdparty.cliente`, `thirdparty.proveedor`.
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

`catalog-service` expone en `http://localhost:8085` los endpoints legacy compatibles:

- `/api/categorias`
- `/api/paises`
- `/api/tipos-documento`
- `/api/metodos-pago`
- `/api/parametros`
- `/api/tipos-gasto`
- `/api/impuestos`
- `/api/productos`

### Thirdparty

`thirdparty-service` expone en `http://localhost:8086` los endpoints legacy compatibles:

- `/api/clientes`
- `/api/proveedores`

`thirdparty-service` consulta tipos de documento en `catalog-service` por REST usando `CATALOG_SERVICE_URL`.

### Inventory

`inventory-service` expone en `http://localhost:8087`:

- `POST /api/v1/products`
- `GET /api/v1/products/{productId}`
- `GET /api/v1/products/{productId}/availability?quantity=`
- `GET /api/v1/products/{productId}/kardex`
- `POST /api/v1/inventory-movements`
- `POST /api/v1/purchases`
- `POST /api/v1/purchases/{purchaseId}/confirm`

Las operaciones de negocio requieren `X-Company-Id`; los movimientos y compras requieren `Idempotency-Key`.

### Billing

`billing-service` expone en `http://localhost:8088`:

- `POST /api/v1/sales`
- `POST /api/v1/sales/{saleId}/confirm`
- `GET /api/v1/sales/{saleId}`

La creacion de venta valida disponibilidad contra `inventory-service`. La confirmacion envia el POS a `dian-provider-service`, que responde con CUDE/QR mock y estado configurable con `DIAN_MOCK_DEFAULT_STATUS`.

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
- `POST /api/v1/accounting-rules`
- `POST /api/v1/accounting-entries`
- `GET /api/v1/reports/journal?from=&to=`
- `GET /api/v1/reports/ledger?from=&to=&accountCode=`

Los asientos se generan desde reglas activas por empresa y son idempotentes por `companyId`, `sourceType` y `sourceId`.

### Audit

`audit-service` expone en `http://localhost:8091`:

- `POST /api/v1/audit-events`
- `GET /api/v1/audit-events?resourceType=&resourceId=&from=&to=&userId=`

Los eventos requieren `X-Company-Id` y almacenan detalle seguro sin secretos en `audit.audit_event`.

`billing-service` publica automaticamente un evento `ELECTRONIC_DOCUMENT`/`SALE`/`CONFIRM_SALE` cuando confirma una venta POS/factura y obtiene resultado del proveedor DIAN mock. Los productores de inventario y contabilidad quedan pendientes para un lote posterior.

## Guia De Pruebas Docker

La guia E2E desde cero para microservicios, con empresa nueva, inventario, venta POS, proveedor DIAN mock, asiento contable, auditoria central, consultas SQL y checklist AC-024/AC-031/AC-032/AC-035 esta en:

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

Ejecutar prueba enfocada:

```powershell
.\mvnw.cmd -Plegacy-monolith -pl services/legacy-monolith "-Dtest=AccountingControllerTest" test
```

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
$env:CATALOG_SERVICE_URL='http://localhost:8085'
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
- `tenant-service`, `catalog-service`, `thirdparty-service`, `inventory-service`, `billing-service`, `dian-provider-service`, `accounting-service` y `audit-service` locales montados como volumen y ejecutados con Maven Wrapper. Los comandos Compose usan `clean spring-boot:run` para evitar clases obsoletas durante la migracion.
- `legacy-monolith` se conserva como referencia transitoria bajo perfil Maven `legacy-monolith`, pero no se despliega por defecto en Compose ni compila en el reactor activo.

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
🏗️ arch: clean backend
```

## Nota Legal Y Contable

La implementacion tecnica debe ser validada antes de produccion contra la normatividad colombiana vigente, los anexos tecnicos DIAN aplicables, el proveedor tecnologico seleccionado y el criterio de un contador publico o asesor tributario.
