# Infrastructure: AWS cloud target

## Decision vigente

La infraestructura productiva se define con Terraform y servicios administrados AWS. No se contempla despliegue on-premise ni brokers self-hosted.

## Ambiente inicial

`infra/aws/envs/dev` compone el primer ambiente cloud de desarrollo con modulos reutilizables.

## Modulos Terraform

- `network`: VPC, subnets publicas/privadas, rutas, NAT opcional y security groups base.
- `database`: RDS PostgreSQL 16 con storage cifrado, backups, Secrets Manager gestionado por AWS y acceso privado.
- `ecs`: ECS Fargate, ECR por servicio, CloudWatch Logs, IAM roles, Cloud Map privado, ALB interno y servicios con `desired_count = 0` inicial.
- `api`: API Gateway HTTP API con VPC Link hacia el ALB interno del BFF.
- `frontend`: S3 privado, CloudFront y Origin Access Control.
- `messaging`: EventBridge custom bus, SQS queues, DLQ, reglas y policies para eventos iniciales.
- `event_consumers`: Lambdas event-driven conectadas a SQS con fallos parciales, VPC privada e IAM minimo.
- `secrets`: Secrets Manager para secretos de aplicacion sin valores versionados.

## Servicios ECS iniciales

- `bff-service`
- `tenant-service`
- `identity-service`
- `catalog-service`
- `thirdparty-service`
- `inventory-service`
- `billing-service`
- `dian-provider-service`
- `accounting-service`
- `audit-service`

Los servicios quedan con `desired_count = 0` hasta que existan imagenes publicadas en ECR, Dockerfiles productivos y pipeline de despliegue.

## Mensajeria inicial

El modulo `messaging` crea rutas base para:

- `audit-events`
- `inventory-effects`
- `accounting-effects`
- `reporting-projections`
- `provider-retries`

TASK-062 se implementa por lotes: el lote 1 registra contratos canonicos y Outbox/Inbox local en productores; los siguientes lotes agregaran publicadores hacia EventBridge/SQS, Lambdas consumidoras, reintentos e idempotencia operativa.

## Lambdas event-driven

`event_consumers` declara consumidores Lambda iniciales para eventos SQS generados por EventBridge.

- `audit-event-writer-lambda`: fuente `audit-events`, handler `com.msvanegasg.facturaelectronica.auditlambda.AuditEventWriterHandler::handleRequest`, Inbox `audit_inbox_event`, materializa `AuditEventRequested` en `audit_event`.
- `inventory-sale-effect-lambda`: fuente `inventory-effects`, handler `com.msvanegasg.facturaelectronica.inventorylambda.InventorySaleEffectHandler::handleRequest`, Inbox `inventory.inbox_event`, materializa `SaleConfirmed` como movimientos `SALE_OUT` idempotentes.
- `accounting-sale-entry-lambda`: fuente `accounting-effects`, handler `com.msvanegasg.facturaelectronica.accountinglambda.AccountingSaleEntryHandler::handleRequest`, Inbox `accounting_inbox_event`, materializa `SaleConfirmed` como asientos `SALE_CONFIRMED`/`SALE` idempotentes.
- `provider-submission-retry-lambda`: fuente `provider-retries`, handler `com.msvanegasg.facturaelectronica.providerlambda.ProviderSubmissionRetryHandler::handleRequest`, reintenta fallas tecnicas de proveedor sin bloquear `billing-service`.
- `reporting-projection-lambda`: fuente `reporting-projections`, handler `com.msvanegasg.facturaelectronica.reportinglambda.ReportingProjectionHandler::handleRequest`, Inbox `reporting.reporting_inbox_event`, materializa proyecciones en `reporting.reporting_event_projection`.
- Runtime: Java 17.
- Fallos parciales: `function_response_types = ["ReportBatchItemFailures"]`.
- Secretos: las Lambdas reciben el ARN del secreto de base de datos y leen el password desde Secrets Manager en runtime.
- Activacion: si `lambda_artifact_bucket` esta vacio, Terraform no crea la Lambda para evitar applies con artefactos inexistentes.


## Gestion de conexiones PostgreSQL

Cada microservicio Spring Boot declara un pool Hikari explicito para evitar depender de defaults y para controlar el numero total de conexiones contra PostgreSQL.

Configuracion local/test inicial:

- `DB_POOL_MAX_SIZE=3`
- `DB_POOL_MIN_IDLE=0`
- `DB_CONNECTION_TIMEOUT_MS=10000`
- `DB_IDLE_TIMEOUT_MS=60000`
- `DB_MAX_LIFETIME_MS=300000`

Cada servicio puede sobrescribir estos valores con variables propias como `BILLING_DB_POOL_MAX_SIZE`, `INVENTORY_DB_POOL_MAX_SIZE` o `AUDIT_DB_POOL_MAX_SIZE`.

Para AWS productivo, ECS Fargate y Lambdas event-driven deben conectarse a RDS PostgreSQL mediante RDS Proxy o una capa administrada equivalente. Las Lambdas no deben abrir conexiones directas sin control al endpoint primario de RDS porque su escalado concurrente puede agotar conexiones rapidamente.

La capacidad final del pool por servicio debe dimensionarse con metricas reales: concurrencia por servicio, latencia de queries, `DatabaseConnections`, saturacion de Hikari y tiempos de espera de conexion.
## Seguridad

- No se versionan secretos reales.
- RDS no es publico.
- CloudFront accede a S3 mediante OAC.
- API Gateway entra al BFF por VPC Link.
- Microservicios y base de datos usan subnets privadas.
- Secrets Manager aloja credenciales y certificados cuando existan.

## Pendientes tecnicos

- Agregar job CI para ejecutar `terraform fmt`, `terraform init`, `terraform validate` y `terraform plan` automaticamente.
- Agregar backend remoto de Terraform state con S3 + DynamoDB lock antes de trabajo colaborativo real.
- Definir estrategia de NAT gateway o VPC endpoints para egress privado de ECS hacia ECR, CloudWatch Logs, Secrets Manager y otros servicios AWS.
- Crear Dockerfiles productivos multi-stage por microservicio.
- Crear pipeline para build, scan, push a ECR y despliegue ECS.
- Agregar HTTPS/custom domain con ACM y Route 53 cuando exista dominio.
- Agregar WAF, alarmas, dashboards y presupuestos.

## Context7 evidence

- Library/tool: Spring Boot 3.5 (`/websites/spring_io_spring-boot_3_5`).
- Topic consulted: HikariCP datasource pool properties and environment variable relaxed binding.
- Relevant finding: Spring Boot permite ajustar propiedades especificas del pool mediante prefijos de datasource/Hikari y mapear variables de entorno a propiedades con relaxed binding.
- Decision impact: Se parametrizan `spring.datasource.hikari.*` en cada microservicio y se exponen variables por ambiente/servicio en Docker Compose.
- Library/tool: Terraform AWS Provider (`/hashicorp/terraform-provider-aws`).
- Topic consulted: ECS Fargate, Lambda, API Gateway, SQS/EventBridge y recursos administrados en Terraform.
- Relevant finding: El provider soporta recursos para ECS Fargate, API Gateway, Lambda, SQS event source mappings y recursos administrados AWS necesarios para el target.
- Decision impact: Se crea IaC modular en Terraform para cloud AWS sin scripts imperativos.
## Validacion TASK-061

- `terraform version`: Terraform v1.15.8 en Windows amd64.
- `terraform fmt -recursive -check infra/aws`: exitoso.
- `terraform init -backend=false`: exitoso con provider `hashicorp/aws` v6.55.0.
- `terraform validate`: exitoso sin warnings.
- `terraform plan -refresh=false -out dev.tfplan`: exitoso, `Plan: 118 to add, 0 to change, 0 to destroy`.
- Revision de plan contra referencias legacy/on-premise/NATS: sin hallazgos.
- No se ejecuto `terraform apply`.
## Avance TASK-062

- Modulo compartido `platform-eventing` creado para envelope canonico, tipos de evento y puerto de publicacion.
- `billing-service`, `inventory-service` y `accounting-service` tienen migraciones Flyway para Outbox/Inbox local.
- Los productores iniciales escriben eventos en Outbox dentro del flujo transaccional local.
- Validacion parcial: `./mvnw.cmd -pl services/platform-eventing,services/billing-service,services/inventory-service,services/accounting-service -am test` exitoso.
- Dispatcher Outbox hacia EventBridge implementado en productores iniciales y deshabilitado por defecto con `EVENTING_EVENTBRIDGE_ENABLED=false`.
- Implementados consumidores Lambda de auditoria, inventario, contabilidad, reintentos de proveedor y proyecciones de reportes con Inbox/estado idempotente y SQS partial batch response.
- Validacion actual: Terraform `fmt`/`validate` exitosos, suite Maven completa con 326 tests verdes y E2E Docker desde cero exitoso con `scripts/e2e-from-zero.ps1 -StartContainers`.
- Library/tool: AWS Lambda Java Support Libraries (`/aws/aws-lambda-java-libs`).
- Topic consulted: SQS event handling with Java Lambda and `aws-lambda-java-events`.
- Relevant finding: `SQSEvent` is the supported Java event model for SQS-triggered Lambda handlers; `aws-lambda-java-events` 3.16.0 provides the event objects.
- Decision impact: `audit-event-writer-lambda`, `inventory-sale-effect-lambda`, `accounting-sale-entry-lambda`, `provider-submission-retry-lambda` and `reporting-projection-lambda` implement Java 17 SQS handlers and return `SQSBatchResponse` so failed records can be retried without replaying the full batch.

## TASK-063 entorno local BFF/SPA

Docker Compose agrega `bff-service` en el puerto `BFF_SERVICE_PORT` y `frontend` en `FRONTEND_PORT`. `bff-service` no declara `depends_on` hacia microservicios de negocio; sus dependencias REST son de runtime. El servicio `frontend` usa Node 20 y proxy Vite hacia `bff-service` dentro de la red Compose.

La arquitectura productiva se mantiene alineada con el target AWS: SPA estatica en S3/CloudFront, API Gateway hacia BFF en ECS Fargate y microservicios internos privados.

## TASK-142 a TASK-143 infraestructura productiva objetivo

- Terraform es la fuente de verdad para infraestructura cloud.
- La SPA se publica como artefacto estatico en S3 privado con distribucion CloudFront y OAC.
- API Gateway o ALB publico enruta unicamente hacia el BFF; los microservicios permanecen privados dentro de la VPC.
- BFF y microservicios de negocio se despliegan en ECS Fargate con logs en CloudWatch, secretos desde Secrets Manager y discovery interno.
- PostgreSQL productivo vive en RDS privado, preferiblemente con RDS Proxy para proteger conexiones de ECS/Lambda.
- Procesos event-driven transversales usan EventBridge/SQS + Lambda con DLQ, reintentos e idempotencia.
- No se incorpora NATS, RabbitMQ ni broker self-hosted para produccion.
- Las tareas locales Docker Compose siguen existiendo solo como entorno de desarrollo y E2E, no como arquitectura productiva.
