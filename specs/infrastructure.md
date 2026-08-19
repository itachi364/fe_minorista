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

Modulo objetivo pendiente:

- `auth`: Amazon Cognito User Pool, App Client OAuth code flow + PKCE, dominio administrado/custom domain, MFA y politicas de password. Esta definido por TASK-153 a TASK-160, pero `infra/aws/modules/auth` todavia no existe en el repositorio.

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
- `payroll-service`

Los servicios quedan con `desired_count = 0` hasta que existan imagenes publicadas en ECR, Dockerfiles productivos y pipeline de despliegue.

Estado aclarado: no existe un `reporting-service` ECS implementado. Los reportes operativos actuales se exponen desde `billing-service`, `inventory-service`, `accounting-service` y `bff-service`; las proyecciones event-driven viven en `reporting-projection-lambda`.

## Mensajeria inicial

El modulo `messaging` crea rutas base para:

- `audit-events`
- `inventory-effects`
- `accounting-effects`
- `reporting-projections`
- `provider-retries`

TASK-062/TASK-143 implementan contratos canonicos, Outbox/Inbox local, dispatcher condicional hacia EventBridge/SQS y consumidores Lambda iniciales para auditoria, inventario, contabilidad, reintentos DIAN mock y proyecciones de reportes.

## Lambdas event-driven

`event_consumers` declara consumidores Lambda iniciales para eventos SQS generados por EventBridge.

- `audit-event-writer-lambda`: fuente `audit-events`, handler `com.msvanegasg.facturaelectronica.auditlambda.AuditEventWriterHandler::handleRequest`, Inbox `audit_inbox_event`, materializa `AuditEventRequested` en `audit_event`.
- `inventory-sale-effect-lambda`: fuente `inventory-effects`, handler `com.msvanegasg.facturaelectronica.inventorylambda.InventorySaleEffectHandler::handleRequest`, Inbox `inventory.inbox_event`, materializa `SaleConfirmed` como movimientos `SALE_OUT` idempotentes.
- `accounting-sale-entry-lambda`: fuente `accounting-effects`, handler `com.msvanegasg.facturaelectronica.accountinglambda.AccountingSaleEntryHandler::handleRequest`, Inbox `accounting_inbox_event`, materializa `SaleConfirmed` como asientos `SALE_CONFIRMED`/`SALE` idempotentes.
- `provider-submission-retry-lambda`: fuente `provider-retries`, handler `com.msvanegasg.facturaelectronica.providerlambda.ProviderSubmissionRetryHandler::handleRequest`, reintenta fallas tecnicas de conexion DIAN sin bloquear `billing-service`.
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
- Los certificados/credenciales DIAN se almacenan por empresa, nunca como secreto global compartido para todos los tenants.
- Rutas de secretos recomendadas: `/facturaelectronica/{env}/companies/{companyId}/dian/certificate`, `/software-pin` y `/technical-key`, con IAM minimo por servicio.
- La base de datos guarda solamente referencias, alias, huellas, vencimientos y estados de configuracion DIAN.
- Cognito administra credenciales productivas, MFA y flujos OAuth. La SPA no debe recibir tokens Cognito.
- CloudFront/BFF deben emitir headers HSTS, CSP, `X-Content-Type-Options`, proteccion anti-frame y `Referrer-Policy`.
- BFF debe emitir cookies `HttpOnly`, `Secure`, `SameSite=Lax` o `Strict`.
- El rol IAM del BFF puede crear secretos por empresa solo bajo prefijos controlados del ambiente, con `secretsmanager:CreateSecret`, `PutSecretValue`, `TagResource`, `DescribeSecret`, `GetSecretValue` y permisos KMS condicionados a Secrets Manager.
- La creacion runtime de secretos por empresa debe ser idempotente y auditable; Terraform crea las policies/KMS/base, no un secreto por tenant.

## Pendientes tecnicos

- Agregar job CI para ejecutar `terraform fmt`, `terraform init`, `terraform validate` y `terraform plan` automaticamente.
- Agregar backend remoto de Terraform state con S3 + DynamoDB lock antes de trabajo colaborativo real.
- Definir estrategia de NAT gateway o VPC endpoints para egress privado de ECS hacia ECR, CloudWatch Logs, Secrets Manager y otros servicios AWS.
- Crear Dockerfiles productivos multi-stage por microservicio.
- Crear pipeline para build, scan, push a ECR y despliegue ECS.
- Agregar HTTPS/custom domain con ACM y Route 53 cuando exista dominio.
- Agregar WAF, alarmas, dashboards y presupuestos.
- Definir KMS key policy y rotacion para secretos DIAN por empresa antes de habilitar emision real.
- Agregar modulo Terraform `auth` para Cognito User Pool/App Client con Authorization Code + PKCE, MFA para administradores y revocacion de tokens.
- Agregar CloudFront Function/Response Headers Policy para security headers.
- Definir si el store productivo de sesiones BFF usa PostgreSQL cifrado, DynamoDB con TTL o ElastiCache cifrado; PostgreSQL queda como opcion inicial por simplicidad operacional.
- Formalizar runbooks productivos: investigacion de DLQ, rotacion de secretos, restauracion RDS, reintentos DIAN, vencimiento de licencias y respuesta ante incidentes de seguridad.

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

### Alcance cerrado por TASK-142

TASK-142 documenta y valida la base Terraform para un despliegue AWS 100% cloud. La infraestructura productiva no publica microservicios de negocio directamente a Internet; el unico borde publico API es el BFF.

Componentes productivos:

- Red:
  - VPC dedicada por ambiente.
  - Subnets publicas para CloudFront/ALB/API edge cuando aplique.
  - Subnets privadas para ECS Fargate, Lambdas con VPC, RDS y RDS Proxy.
  - Security groups separados para BFF, microservicios, RDS, Lambdas y ALB interno.
  - Egress controlado mediante NAT Gateway o VPC endpoints para ECR, CloudWatch Logs, Secrets Manager, KMS, SQS y EventBridge segun costo/ambiente.
- Frontend:
  - SPA compilada como artefacto estatico.
  - S3 privado como origen.
  - CloudFront con Origin Access Control.
  - Headers de seguridad mediante Response Headers Policy o CloudFront Function.
  - WAF queda como hardening productivo recomendado antes de exposicion comercial.
- Entrada publica:
  - API Gateway HTTP API o ALB publico solo hacia `bff-service`.
  - Enrutamiento publico restringido a `/api/v1/**`.
  - TLS obligatorio con ACM y dominio administrado por Route 53 cuando exista dominio productivo.
- BFF:
  - `bff-service` en ECS Fargate.
  - Responsable de sesion publica, CSRF, resolucion de identidad, autorizacion de borde, normalizacion de errores, propagacion de `X-Correlation-Id`, `X-Company-Id`, `X-User-Id` e idempotencia.
  - No contiene reglas de negocio propias de billing, inventario, contabilidad, identidad, tenant, catalogos, terceros, auditoria o nomina.
- Microservicios privados:
  - `tenant-service`, `identity-service`, `catalog-service`, `thirdparty-service`, `inventory-service`, `billing-service`, `dian-provider-service`, `accounting-service`, `audit-service` y `payroll-service` en ECS Fargate privado.
  - Discovery interno por Cloud Map o ALB interno.
  - Logs por servicio en CloudWatch Logs.
  - Imagenes por servicio en ECR.
  - Secrets y variables sensibles inyectadas desde Secrets Manager/SSM, nunca quemadas en imagen.
- Persistencia:
  - RDS/Aurora PostgreSQL privado.
  - Cifrado en reposo con KMS.
  - Backups habilitados por ambiente.
  - RDS Proxy recomendado para proteger conexiones desde ECS/Lambda.
  - Separacion inicial por esquemas/bases de servicio; evolucion a instancias separadas si volumen, cumplimiento o autonomia operacional lo exige.
- Secretos:
  - Terraform crea KMS, policies, roles y secretos base de aplicacion sin valores reales versionados.
  - Los secretos por empresa se crean en runtime por servicios autorizados bajo prefijos deterministas.
  - PostgreSQL guarda solo referencias, alias, huellas, vencimientos, estado y metadata no sensible.
- Observabilidad:
  - CloudWatch Logs por contenedor y Lambda.
  - Metricas de ECS, ALB/API Gateway, RDS, Lambda, SQS y DLQ.
  - Alarmas recomendadas para errores 5xx, saturacion de CPU/memoria, conexiones RDS, cola/DLQ, expiracion de certificados DIAN y vencimiento de licencias.
- Estado Terraform:
  - Local/dev puede usar `init -backend=false`.
  - Produccion requiere backend remoto S3 con bloqueo DynamoDB antes de trabajo colaborativo o despliegue real.

### Alcance cerrado por TASK-143

TASK-143 define eventos productivos administrados en AWS. No se usara NATS, RabbitMQ ni broker self-hosted en produccion.

Patron aprobado:

1. El microservicio productor escribe su cambio de negocio y su evento en Outbox dentro de la misma transaccion local.
2. Un dispatcher del productor publica eventos `PENDING`/`FAILED` hacia EventBridge cuando `EVENTING_EVENTBRIDGE_ENABLED=true`.
3. EventBridge enruta por `source`, `detailType`, `companyId` y tipo de evento hacia colas SQS por capacidad.
4. Cada cola SQS tiene DLQ y redrive policy.
5. Cada Lambda consume desde SQS, registra Inbox/idempotencia antes de materializar efectos y usa partial batch response para reintentar solo mensajes fallidos.
6. Los consumidores no bloquean ni revierten la transaccion original del productor.
7. Los errores permanentes quedan en DLQ y se investigan por auditoria/operacion.

Rutas event-driven iniciales:

- `audit-events`: materializa eventos de auditoria en `audit-service`.
- `inventory-effects`: descuenta stock o registra efectos de inventario derivados de ventas confirmadas.
- `accounting-effects`: genera asientos contables derivados de ventas/compras/gastos/pagos confirmados.
- `provider-retries`: reintenta fallas tecnicas de conexion DIAN sin duplicar numeracion ni documentos.
- `reporting-projections`: materializa proyecciones consultables por empresa y periodo.

Eventos canonicos iniciales:

- `SaleConfirmed`
- `ElectronicDocumentValidated`
- `ElectronicDocumentRejected`
- `InventoryMovementRegistered`
- `PurchaseConfirmed`
- `AccountingEntryPosted`
- `AuditEventRequested`
- `ProviderSubmissionFailed`
- `LicenseUsageChanged`

Reglas no negociables:

- Todo evento debe incluir `eventId`, `eventType`, `eventVersion`, `occurredAt`, `producer`, `companyId`, `correlationId`, `idempotencyKey` y `payload`.
- `eventId + consumer` debe ser unico en Inbox.
- El payload no debe contener secretos, certificados, PIN, claves tecnicas, tokens, cookies ni passwords.
- Cada Lambda debe retornar fallos parciales de SQS cuando el runtime lo soporte.
- Cada consumidor debe ser idempotente por `eventId`, `companyId`, `consumer` e identificador de documento origen cuando aplique.
- La publicacion asincrona no reemplaza validaciones sincronicas criticas: RBAC, licencia, stock disponible y reglas fiscales siguen validandose antes de confirmar comandos.

### Evidencia Context7 TASK-142/TASK-143

- Library/tool: AWS (`/websites/aws_amazon`).
- Topic consulted: ECS Fargate, RDS PostgreSQL, Secrets Manager, CloudWatch Logs and private service networking.
- Relevant finding: AWS documenta servicios ECS Fargate con configuracion de red `awsvpc`; los servicios pueden vivir en subnets privadas y exponerse por load balancer/API de entrada, manteniendo RDS y dependencias internas privadas.
- Decision impact: La arquitectura publica solo CloudFront/API/BFF y mantiene microservicios/RDS en red privada.
- Library/tool: AWS (`/websites/aws_amazon`).
- Topic consulted: CloudFront security headers.
- Relevant finding: CloudFront Functions/Response Headers pueden agregar HSTS, CSP, `X-Content-Type-Options`, frame protection y `Referrer-Policy`.
- Decision impact: TASK-158 queda trazada a CloudFront/BFF para hardening HTTP productivo.
- Library/tool: AWS (`/websites/aws_amazon`).
- Topic consulted: SQS-triggered Lambda partial batch failures and DLQ.
- Relevant finding: Lambda con SQS puede devolver `batchItemFailures` para reintentar solo mensajes fallidos; SQS soporta redrive policy hacia DLQ.
- Decision impact: Las Lambdas de TASK-143 usan Inbox/idempotencia y partial batch response para no reprocesar lotes completos.

## TASK-153 a TASK-163 seguridad de autenticacion productiva

Terraform debe crear infraestructura base de autenticacion y seguridad, no objetos por cada empresa:

- Cognito User Pool/App Client con OAuth Authorization Code + PKCE.
- MFA obligatorio para ROOT/administradores o grupos equivalentes.
- KMS key y IAM policies para que el BFF cree/lea secretos por empresa bajo prefijo permitido.
- Headers de seguridad en CloudFront o BFF.
- Variables de entorno productivas para deshabilitar `POST /api/v1/auth/login` dummy.

La creacion de secretos por empresa ocurre en runtime cuando ROOT crea una empresa o configura DIAN. El backend autorizado crea nombres deterministas por `env/companyId/capability`, etiqueta los secretos y guarda referencias no sensibles en PostgreSQL.

- Terraform es la fuente de verdad para infraestructura cloud.
- La SPA se publica como artefacto estatico en S3 privado con distribucion CloudFront y OAC.
- API Gateway o ALB publico enruta unicamente hacia el BFF; los microservicios permanecen privados dentro de la VPC.
- BFF y microservicios de negocio se despliegan en ECS Fargate con logs en CloudWatch, secretos desde Secrets Manager y discovery interno.
- PostgreSQL productivo vive en RDS privado, preferiblemente con RDS Proxy para proteger conexiones de ECS/Lambda.
- Procesos event-driven transversales usan EventBridge/SQS + Lambda con DLQ, reintentos e idempotencia.
- No se incorpora NATS, RabbitMQ ni broker self-hosted para produccion.
- Las tareas locales Docker Compose siguen existiendo solo como entorno de desarrollo y E2E, no como arquitectura productiva.
