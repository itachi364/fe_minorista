# NexoFiscal

NexoFiscal es una plataforma modular para gestionar ventas, facturacion fiscal configurable por empresa, inventario, terceros, contabilidad operativa, nomina, reportes y administracion multiempresa.

El repositorio contiene una SPA React, un BFF Spring Boot, microservicios de dominio, lambdas de proyeccion o procesamiento asincrono, migraciones Flyway, despliegue local con Docker Compose y configuracion de calidad con SonarQube.

## Arquitectura General

La aplicacion esta organizada en servicios autonomos comunicados por HTTP interno y eventos operativos:

- `apps/facturaelectronica-web`: frontend React/Vite.
- `services/bff-service`: puerta de entrada para la SPA, sesion, CSRF y orquestacion.
- `services/tenant-service`: empresas contratantes, branding, licencias y alcance multiempresa.
- `services/identity-service`: usuarios, roles, permisos y PIN operacional.
- `services/catalog-service`: catalogos parametrizables almacenados en base de datos.
- `services/thirdparty-service`: clientes, proveedores y terceros empresariales.
- `services/inventory-service`: items, stock, movimientos de inventario y registro documental de compras.
- `services/billing-service`: ventas, cierre fiscal, documentos fiscales y consecutivos.
- `services/dian-provider-service`: integracion fiscal configurable por empresa hacia DIAN o modo mock local.
- `services/accounting-service`: plan de cuentas, reglas contables, egresos, deudores y asientos.
- `services/payroll-service`: empleados, periodos y pagos de nomina.
- `services/reporting-service`: reportes operativos y exportaciones.
- `services/audit-service`: auditoria funcional y tecnica.
- `services/*-lambda`: procesos asincronos para auditoria, inventario, contabilidad, reintentos DIAN y reporteria.

## Estructura Del Repositorio

```text
.
|-- apps/
|   `-- facturaelectronica-web/
|-- services/
|   |-- bff-service/
|   |-- tenant-service/
|   |-- identity-service/
|   |-- catalog-service/
|   |-- thirdparty-service/
|   |-- inventory-service/
|   |-- billing-service/
|   |-- dian-provider-service/
|   |-- accounting-service/
|   |-- payroll-service/
|   |-- audit-service/
|   |-- reporting-service/
|   `-- *-lambda/
|-- specs/
|-- infra/
|-- scripts/
|-- docker-compose.yml
|-- pom.xml
`-- sonar-project.properties
```

## Stack Tecnico

- Backend: Java 17, Spring Boot 3.5, Spring Web, Spring Security, Spring Data JPA, Bean Validation, OpenAPI.
- Frontend: React 19, TypeScript, Vite 7, i18next, React Testing Library, Vitest.
- Base de datos: PostgreSQL con migraciones Flyway por servicio.
- Contenedores: Docker y Docker Compose.
- Calidad: Maven, JaCoCo, Vitest Coverage, SonarQube local.
- Infraestructura: Terraform y artefactos AWS para despliegues productivos.

## Requisitos Locales

- JDK 17.
- Maven Wrapper incluido en el repositorio.
- Node.js compatible con Vite 7.
- Docker Desktop o Docker Engine con Compose.
- PostgreSQL si se ejecutan servicios fuera de Docker.
- SonarQube local opcional en `http://localhost:9000`.

## Configuracion

1. Crea el archivo local de entorno desde la plantilla:

```powershell
Copy-Item .env.example .env
```

2. Ajusta puertos, credenciales locales y banderas de integracion segun el modo de ejecucion.

3. No versionar `.env`, certificados, tokens, llaves privadas ni contrasenas reales.

La integracion DIAN real se configura por empresa mediante referencias seguras a secretos. En local se puede usar el modo mock para validar el flujo completo sin enviar documentos a entidades externas.

## Ejecucion Con Docker Compose

Levantar todo el entorno local:

```powershell
docker compose up -d --build
```

Ver estado de contenedores:

```powershell
docker compose ps
```

Consultar logs del BFF:

```powershell
docker compose logs -f bff-service
```

Detener el entorno:

```powershell
docker compose down
```

Servicios locales principales:

| Componente | URL local |
|---|---|
| Frontend | `http://localhost:5173` |
| BFF | `http://localhost:8083` |
| Tenant | `http://localhost:8084` |
| Catalog | `http://localhost:8085` |
| Thirdparty | `http://localhost:8086` |
| Inventory | `http://localhost:8087` |
| Billing | `http://localhost:8088` |
| DIAN Provider | `http://localhost:8089` |
| Accounting | `http://localhost:8090` |
| Audit | `http://localhost:8091` |
| Identity | `http://localhost:8092` |
| Reporting | `http://localhost:8094` |

## Observabilidad Local

Levantar Prometheus y Grafana sobre el entorno Docker local:

```powershell
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d prometheus grafana
```

Servicios:

| Componente | URL local |
|---|---|
| Prometheus | `http://localhost:9090` |
| Grafana | `http://localhost:3001` |

Los microservicios no DIAN exponen metricas en `/actuator/prometheus`. Grafana incluye un dashboard base para revisar trafico HTTP, memoria y errores por aplicacion.

## Ejecucion Local Por Servicio

Compilar todos los modulos backend:

```powershell
.\mvnw.cmd clean package
```

Ejecutar un servicio concreto:

```powershell
.\mvnw.cmd -pl services/bff-service spring-boot:run
```

Instalar dependencias del frontend:

```powershell
cd apps/facturaelectronica-web
npm install
```

Ejecutar la SPA:

```powershell
npm run dev
```

Generar build productivo del frontend:

```powershell
npm run build
```

## Pruebas

Ejecutar pruebas backend:

```powershell
.\mvnw.cmd test
```

Ejecutar pruebas de un modulo:

```powershell
.\mvnw.cmd -pl services/billing-service test
```

Ejecutar pruebas frontend:

```powershell
cd apps/facturaelectronica-web
npm test
```

Ejecutar cobertura frontend:

```powershell
npm run coverage
```

## SonarQube

Con SonarQube levantado en `http://localhost:9000`, define un token local y ejecuta el analisis:

```powershell
$env:SONAR_TOKEN="token-local"
.\scripts\sonar-local.ps1
```

La configuracion base esta en `sonar-project.properties` e integra fuentes backend, frontend, infraestructura, cobertura JaCoCo y cobertura LCOV.

## Infraestructura AWS

El esqueleto de infraestructura vive en `infra/aws`. Para validar formato y definicion sin crear recursos:

```powershell
cd infra/aws/envs/dev
terraform init -backend=false
terraform fmt -recursive -check ..\..
terraform validate
```

La arquitectura objetivo usa frontend en S3/CloudFront, BFF y microservicios privados en ECS/Fargate, PostgreSQL administrado, Secrets Manager/KMS, CloudWatch, EventBridge/SQS y lambdas para procesos asincronos.

## API Y Swagger

Cada microservicio Spring Boot expone OpenAPI cuando esta levantado:

```text
http://localhost:<puerto>/v3/api-docs
http://localhost:<puerto>/swagger-ui.html
```

El consumo normal desde la SPA se realiza por el BFF en `http://localhost:8083`.

## Datos Y Migraciones

Cada servicio mantiene sus migraciones en:

```text
services/<servicio>/src/main/resources/db/migration
```

Flyway crea y evoluciona las tablas al iniciar el servicio. Los catalogos funcionales viven en base de datos y el entorno local mantiene solo los datos minimos necesarios para validar acceso inicial y flujos operativos.

## Seguridad

- La SPA no debe registrar credenciales, tokens, certificados ni datos sensibles en consola.
- Las credenciales viajan por TLS en despliegues reales; cifrado adicional en payload solo aplica con un modelo formal de llaves.
- La sesion web se protege desde el BFF con cookies, CSRF, validaciones de permisos y correlacion de errores.
- La configuracion DIAN real, certificados y secretos por empresa deben guardarse en un gestor de secretos.
- Los archivos empresariales se guardan por empresa/categoria; las descargas usan enlaces temporales y en produccion deben usar storage privado cifrado.
- Los errores publicos deben ser claros para el usuario y no exponer trazas internas.

## Documentacion Tecnica

La documentacion de especificacion vive en `specs/`:

- `requirements.md`: requisitos funcionales, reglas y criterios.
- `design.md`: arquitectura funcional, flujos, decisiones y evidencias tecnicas.
- `api-contract.md`: contratos HTTP y eventos.
- `database-design.md`: persistencia y migraciones.
- `infrastructure.md`: ejecucion local, calidad, seguridad e infraestructura.
- `diagrams/`: diagramas Mermaid de arquitectura, secuencia y modelo de datos.

El README se mantiene como guia practica del repositorio para instalacion, ejecucion, validacion y orientacion tecnica.
