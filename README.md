# Factura Electronica Minorista

Backend Java/Spring Boot para una plataforma multiempresa de facturacion electronica colombiana, POS electronico, inventario simple y contabilidad basica con PUC colombiano.

El proyecto esta migrando desde una estructura legacy CRUD hacia Clean Architecture por bounded contexts, manteniendo una estrategia de monolito modular como paso intermedio antes de una posible extraccion a microservicios fisicos.

## Estado Actual

- Arquitectura Clean Architecture implementada por modulos.
- PostgreSQL con migraciones Flyway versionadas.
- Docker Compose local para PostgreSQL y aplicacion.
- POS electronico con proveedor DIAN mock configurable.
- Persistencia JPA y endpoints REST para billing/POS y accounting.
- Refactor de modulos legacy hacia bounded contexts.
- Suite completa validada recientemente: `242 tests, 0 fallos`.

## Alcance Funcional

- Catalogos: categorias, productos, impuestos, paises, parametros, metodos de pago, tipos de documento y tipos de gasto.
- Terceros: clientes y proveedores.
- Inventario: compras, stock simple y movimientos de inventario a nivel de dominio.
- Billing/POS: emisor, resoluciones, emision POS electronico, consulta y envio a proveedor DIAN mock.
- Contabilidad: cuentas PUC por empresa, reglas contables configurables, asientos `POSTED`, libro diario y libro mayor.
- Errores API: contrato estandar con `timestamp`, `status`, `code`, `message`, `correlationId` y `details`.

## Arquitectura

Cada bounded context sigue esta estructura:

```text
<context>
  domain/
    model/
  application/
    dto/
    port/in/
    port/out/
    usecase/
  infrastructure/
    config/
    persistence/
    provider/
    system/
  interfaces/
    rest/
    rest/dto/
```

Bounded contexts principales:

- `catalog`
- `thirdparty`
- `inventory`
- `expenses`
- `billing`
- `accounting`

Los paquetes legacy `DTO`, `mapper`, `models`, `repository`, `service` y `validator` fueron limpiados durante la migracion aprobada.

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

DB_URL=jdbc:postgresql://postgres:5432/facturaelectronica
DB_USERNAME=factura_user
DB_PASSWORD=change_me

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

En PowerShell:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:15432/facturaelectronica'
$env:DB_USERNAME='factura_user'
$env:DB_PASSWORD='change_me'
$env:DIAN_PROVIDER_MODE='mock'
.\mvnw.cmd spring-boot:run
```

La aplicacion inicia en:

```text
http://localhost:8083
```

Healthcheck:

```text
http://localhost:8083/actuator/health
```

Swagger UI:

```text
http://localhost:8083/swagger-ui.html
```

## Ejecucion Con Docker Compose

El proyecto incluye `docker-compose.yml` con:

- `postgres`: `postgres:16-alpine`.
- `app`: `eclipse-temurin:17-jdk`, ejecutando `./mvnw spring-boot:run`.

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
docker compose logs -f app
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

Las migraciones se ejecutan con Flyway desde:

```text
src/main/resources/db/migration
```

Migraciones actuales:

- `V001__create_legacy_public_schema.sql`
- `V002__create_billing_pos_schema.sql`
- `V003__create_accounting_schema.sql`

Tablas relevantes:

- Billing/POS: `billing_issuer_profile`, `billing_numbering_resolution`, `billing_electronic_pos_document`, `billing_provider_submission`, `billing_electronic_document_trace_event`, `billing_fiscal_audit_event`.
- Accounting: `accounting_account`, `accounting_rule`, `accounting_rule_line`, `accounting_entry`, `accounting_entry_line`.

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
- `POST /api/v1/numbering-resolutions`
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

## Proveedor DIAN Mock

Mientras no existan proveedor tecnologico real, certificados y credenciales, la aplicacion usa un adaptador local dummy.

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
.\mvnw.cmd "-Dtest=AccountingControllerTest" test
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
- Aplicacion local montada como volumen y ejecutada con Maven Wrapper.

Pendiente:

- `Dockerfile` productivo multi-stage.
- Terraform/IaC.
- Configuracion cloud.
- Pipeline CI/CD.
- Escaneo de imagenes con Docker Scout, Trivy, Grype o herramienta equivalente.

## Pendientes Relevantes

- `TASK-025`: auditoria fiscal.
- `TASK-026`: correlation ID y logs estructurados.
- `TASK-030`: seed local y guia de pruebas Docker.
- Integracion real con proveedor tecnologico DIAN.
- Certificados digitales reales.
- Representacion grafica oficial.
- XML UBL y anexos tecnicos definitivos.
- Descuento automatico de inventario despues de validacion fiscal.
- Asiento contable automatico conectado al flujo POS/billing.
- Seguridad/autenticacion/autorizacion.

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
