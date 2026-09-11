# NexoFiscal

NexoFiscal es una plataforma modular para gestionar ventas, facturacion fiscal configurable por empresa, inventario, terceros, contabilidad operativa, nomina, reportes y administracion multiempresa.

El repositorio contiene una SPA React, un BFF Spring Boot, microservicios de dominio, lambdas de proyeccion o procesamiento asincrono, migraciones Flyway, despliegue local con Docker Compose y configuracion de calidad con SonarQube.

El estado verificable de capacidades y limitaciones se mantiene en [`specs/sdd-status.md`](specs/sdd-status.md); el trabajo pendiente ordenado por fases esta en [`specs/roadmap.md`](specs/roadmap.md). La existencia de una especificacion objetivo no implica que la capacidad este implementada.

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

- Backend: Java 17, Spring Boot 3.5.14, Spring Web, Spring Security, Spring Data JPA, Bean Validation, OpenAPI.
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

## Licencias Comerciales

La licencia combina modulos de navegacion con funcionalidades verificables por backend:

- `POS`: operacion comercial basica, clientes, productos, inventario, ventas POS, facturacion electronica, configuracion y documentos fiscales basicos, reportes basicos, catalogos, logs, usuarios y PIN operacional. La plataforma completa automaticamente la plantilla contable minima al cerrar la primera venta si hace falta, sin exponer contabilidad avanzada. No incluye proveedores, compras, gastos, deudores, nomina, reglas fiscales avanzadas ni reportes asincronos.
- `FULL`: todos los modulos y funcionalidades operativas estandar de la plataforma.
- `CUSTOM`: seleccion granular de modulos, funcionalidades y servicios comerciales como reglas, reportes, flujos e integraciones personalizadas.

El contrato de licencia expone `enabledModules` y `enabledFeatures`. La migracion `tenant/V008__add_license_features.sql` convierte planes anteriores a `POS`, `FULL` o `CUSTOM` y deriva funcionalidades para conservar el acceso contratado existente.

En el panel ROOT de licencias, seleccionar una empresa carga automaticamente su licencia y consumo comercial. Si no existe licencia, el formulario se limpia y queda listo para crearla sin reutilizar datos de otra empresa.

La clasificacion de obligacion de facturar se calcula en backend durante la creacion o actualizacion de empresa por ROOT. El RUT se conserva como evidencia privada, los datos se capturan desde catalogos y un cuestionario complementario, y el resultado no es editable. Solo `NOT_OBLIGATED_VERIFIED` permite venta interna no fiscal; codigos de no responsable de IVA o consumo no bastan por si solos. Esta capacidad fue implementada en `TASK-305` y no usa OCR en su primera version.

Los estados nacionales de retencion del perfil empresarial se derivan de las responsabilidades RUT y se muestran como solo lectura. La designacion de ReteICA sigue siendo municipal y explicita. Las excepciones especiales declaradas requieren revision antes de permitir una venta no fiscal (`TASK-306`).

El BFF reenvia cargas multipart sin interpretar sus partes; la validacion y almacenamiento de RUT, soportes y certificados corresponde al microservicio propietario (`TASK-307`).

El set reproducible para validar esta clasificacion desde el panel ROOT se encuentra en `specs/test-data/invoicing-obligation-frontend.md`.

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
| Payroll | `http://localhost:8093` |
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

La configuracion base esta en `sonar-project.properties` e integra fuentes backend, frontend, infraestructura, cobertura JaCoCo y cobertura LCOV. El pipeline `.github/workflows/quality.yml` ejecuta Maven, cobertura/build frontend y Sonar cuando existe `SONAR_TOKEN`. El objetivo SDD para logica de negocio nueva o modificada es 100% de cobertura de ramas; cualquier excepcion debe quedar justificada en la tarea.

## Infraestructura AWS

El esqueleto de infraestructura vive en `infra/aws`. Para validar formato y definicion sin crear recursos:

```powershell
cd infra/aws/envs/dev
terraform init -backend=false
terraform fmt -recursive -check ..\..
terraform validate
```

La arquitectura objetivo usa frontend en S3/CloudFront, BFF y microservicios privados en ECS/Fargate, PostgreSQL administrado, Secrets Manager/KMS, CloudWatch, EventBridge/SQS y lambdas para procesos asincronos.

### AWS Free Preview

Antes del target productivo existe una fase separada, temporal y no productiva (`TASK-317` a `TASK-321`) para la cuenta `883425315805`. El proceso previsto valida por API que la cuenta permanezca `FREE/ACTIVE`, bloquea recursos de costo persistente, ejecuta toda la plataforma en una unica EC2 apagada automaticamente a las cuatro horas y exige una prueba local de memoria antes de crear infraestructura.

No se debe ejecutar `infra/aws/envs/dev` para esta finalidad. El entorno Free Preview prohibe NAT, Fargate, RDS, balanceadores, Route 53, WAF, Secrets Manager, KMS propio y observabilidad administrada. Aun sin cobro externo mientras la cuenta conserve Free Plan, el uso de EC2/EBS y otros servicios puede consumir creditos y cerrar la cuenta al agotarlos o vencer el plan.

El runbook y la decision completa estan en `infra/aws/envs/free-preview/README.md`, `specs/infrastructure.md` y `specs/adr/ADR-003-aws-free-preview-cost-gated.md`. El entorno Terraform, las imagenes y los scripts ya existen y pasaron validacion local con 14 contenedores y 3096,98 MiB; ningun `terraform apply` fue autorizado y no se han creado recursos AWS.

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

El catalogo fiscal de `accounting-service` versiona UVT, conceptos, bases, tarifas, exenciones y fuentes normativas. Las reglas nacionales se actualizan mediante nuevas migraciones; ReteICA se configura por municipio y vigencia. Una operacion que requiera una regla territorial ausente queda bloqueada para evitar retenciones o asientos con tarifas asumidas.

> Advertencia fiscal vigente al 2026-09-10: `TASK-309` registra la suspension provisional de los articulos 2 a 8 del Decreto 572 de 2025 desde el 8 de mayo de 2026 y evita tratarlos como vigentes. Una eventual reactivacion exige registrar primero su providencia o comunicacion oficial y fecha efectiva.

La fase 39 (`TASK-308` a `TASK-315`) esta implementada: gobierno juridico, perfiles efectivos y tipados, calculo por linea, bases AIU/IVA/bruta, acumulaciones por operacion/contrato/dia/mes/ano, paquetes ReteICA bajo demanda, mapeos contables y de presentacion, confirmacion atomica de compras, conciliacion, auxiliar 350, cierres, movimientos reversores, certificados versionados y metricas. Un concepto nacional `REQUIRES_REVIEW` o un municipio sin paquete publicado sigue bloqueado; cerrar la fase tecnica no presume una tarifa sin fuente oficial vigente.

NIIF y tributacion se mantienen separadas: el Decreto 2420 de 2015 orienta reconocimiento y presentacion financiera, no tarifas de retencion. Las cuentas `2205`, `2365`, `2367` y `2368` son una plantilla configurable inspirada en el PUC historico; cada empresa conserva su plan de cuentas y su mapeo contable.

ROOT y los usuarios autorizados registran en el mismo formulario de empresa el regimen, responsabilidades RUT, CIIU y calidades de responsable de IVA, agente retenedor, agente de ReteIVA/ReteICA, gran contribuyente y autorretenedor. Estos datos se guardan para la empresa activa y deben corresponder al RUT y a la orientacion del contador.

La administracion tributaria se encuentra en `Configuracion > Reglas fiscales` y requiere `FISCAL_SETTINGS_MANAGE`. El permiso tambien habilita el perfil fiscal de empresa, emisor, politica, resoluciones y conexion DIAN, pero no autoriza la emision de documentos, que conserva `FISCAL_DOCUMENTS_ISSUE`. El rol `OWNER` inicial recibe ambos permisos y puede delegar el permiso fiscal desde la administracion de roles.

Con una empresa activa, `Reglas fiscales` tambien permite mapear cada tipo de retencion a cuentas activas del plan empresarial, mapear cuentas a rubros de presentacion por grupo NIIF, consultar el catalogo nacional y el auxiliar del Formulario 350, cerrar periodos, revisar la conciliacion y generar o descargar certificados CSV por proveedor. El certificado conserva identidades legales y queda pendiente de notificacion; el correo efectivo corresponde al modulo futuro de TASK-295.

Los campos CIIU y tercero exento usan un unico selector con busqueda interna. Al seleccionar un tercero, la regla cambia a `Exento` y exige cargar su soporte PDF; las opciones se limitan a proveedores activos de la empresa seleccionada.

El catalogo general `CIIU` contiene las clases DANE CIIU Rev. 4 A.C. actualizacion 2022 adoptadas por la DIAN para el RUT y los procesos fiscales. Los terceros juridicos y proveedores pueden seleccionar varias actividades; los clientes exclusivamente naturales no requieren CIIU. El campo historico `ciiuCode` se conserva temporalmente como alias de compatibilidad mientras `ciiuCodes` es la fuente canonica. La CIIU Rev. 5 publicada por DANE en 2026 no se usa fiscalmente hasta que la DIAN formalice su adopcion.

En compras y gastos, `Fecha limite de pago` solo aparece para operaciones a credito porque alimenta la cuenta por pagar. Las operaciones de contado no envian fecha de vencimiento.

### Prueba del motor fiscal desde la aplicacion

Las pruebas validan el flujo tecnico y las reglas publicadas en el ambiente. No certifican conceptos nacionales marcados `REQUIRES_REVIEW` ni municipios sin paquete ReteICA verificado; esos casos deben permanecer bloqueados.

1. En `Configuracion > Configuracion contable`, usa `Completar plantilla basica` si la empresa aun no tiene sus cuentas y reglas iniciales.
2. En `Configuracion > Empresa`, guarda el perfil fiscal de la empresa: regimen, responsabilidades RUT, condiciones de agente, municipio ICA y CIIU.
3. En `Clientes y proveedores`, registra un proveedor con regimen tributario, responsabilidades, municipio y uno o varios CIIU.
4. En `Compras` o `Gastos`, crea un documento indicando proveedor, concepto fiscal, subtotal e IVA. El total se calcula automaticamente.
5. En la tabla de pendientes, usa `Calcular` para ver base, tarifa, valor, decision, regla y razon sin confirmar el documento.
6. Usa `Confirmar`. El backend recalcula con los perfiles persistidos, crea los snapshots y contabiliza la cuenta por pagar por el valor neto cuando la operacion es a credito.
7. En un documento confirmado, usa `Detalle fiscal` para volver a consultar el mismo resultado persistido.

Si una obligacion requiere una regla ausente, por ejemplo ReteICA sin catalogo municipal vigente, la vista previa muestra `BLOCKED` y la confirmacion falla sin crear asiento ni cuenta por pagar.
La plantilla basica contabiliza el neto del proveedor en `2205` y las retenciones en `2365` (retefuente), `2367` (reteIVA) y `2368` (reteICA).

## Seguridad

- La SPA no debe registrar credenciales, tokens, certificados ni datos sensibles en consola.
- Las credenciales viajan por TLS en despliegues reales; cifrado adicional en payload solo aplica con un modelo formal de llaves.
- La sesion web se protege desde el BFF con cookies, CSRF, validaciones de permisos y correlacion de errores.
- La configuracion DIAN real, certificados y secretos por empresa deben guardarse en un gestor de secretos.
- Los archivos empresariales se guardan por empresa/categoria; las descargas usan enlaces temporales y en produccion deben usar storage privado cifrado.
- Las exenciones fiscales dirigidas a un tercero cargan un soporte PDF privado de maximo 5 MB; la regla conserva una referencia interna aislada por empresa.
- Los errores publicos deben ser claros para el usuario y no exponer trazas internas.

## Limitaciones Conocidas

- La conexion DIAN SOAP WCF real, su normalizacion de respuestas y la prueba E2E de habilitacion siguen pendientes; el modo mock no demuestra produccion DIAN.
- El motor fiscal aplica solo reglas cuya vigencia y condiciones puede demostrar. Los conceptos `REQUIRES_REVIEW` y los municipios sin paquete territorial se bloquean y requieren validacion profesional antes de operar.
- El portal de contadores, las contrasenas temporales y los correos operativos siguen especificados pero no implementados.
- La infraestructura AWS productiva esta definida como target; los recursos ECS se mantienen sin cargas productivas. El entorno `free-preview` esta documentado pero no implementado ni desplegado.

## Documentacion Tecnica

La documentacion de especificacion vive en `specs/`:

- `requirements.md`: requisitos funcionales, reglas y criterios.
- `sdd-status.md`: fotografia canonica de capacidades reales, parciales y objetivo.
- `roadmap.md`: fases y dependencias del backlog activo.
- `legal-baseline.md`: fuentes oficiales, estado documental y compuerta de publicacion fiscal.
- `design.md`: arquitectura funcional, flujos, decisiones y evidencias tecnicas.
- `api-contract.md`: contratos HTTP y eventos.
- `database-design.md`: persistencia y migraciones.
- `infrastructure.md`: ejecucion local, calidad, seguridad e infraestructura.
- `diagrams/`: diagramas Mermaid de arquitectura, secuencia y modelo de datos.

El README se mantiene como guia practica del repositorio para instalacion, ejecucion, validacion y orientacion tecnica.
