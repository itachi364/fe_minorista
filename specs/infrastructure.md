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
- `auth`: Amazon Cognito User Pool, App Client OAuth code flow + PKCE, dominio Hosted UI administrado, grupos base y MFA software token habilitado.

Modulo objetivo pendiente:

- `auth`: modulo Terraform Cognito base implementado; queda pendiente endurecer custom domain, politicas granulares de MFA por grupo/accion y puente definitivo Cognito -> identidad/permisos internos.

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
- `reporting-service`

Los servicios quedan con `desired_count = 0` hasta que existan imagenes publicadas en ECR, Dockerfiles productivos y pipeline de despliegue.

Estado aclarado: `reporting-service` existe como microservicio fisico HTTP para catalogo/opciones/query de reportes avanzados. Los endpoints operativos en servicios duenos se conservan como fuentes canonicas y las proyecciones event-driven viven en `reporting-projection-lambda`.

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
- Endurecer modulo Terraform `auth` con custom domain ACM/Route 53, politicas granulares de MFA por grupo/accion y revocacion de tokens.
- Agregar CloudFront Function/Response Headers Policy para security headers.
- El store productivo inicial de sesiones BFF usa PostgreSQL en schema `bff`, payload cifrado AES-GCM e identificadores opacos hasheados. DynamoDB TTL o ElastiCache cifrado quedan como evolucion si la escala operativa lo exige.
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

Docker Compose agrega `bff-service` en el puerto `BFF_SERVICE_PORT` y `frontend` en `FRONTEND_PORT`. `bff-service` no declara `depends_on` hacia microservicios de negocio; sus dependencias REST son de runtime. El servicio `frontend` usa Node 22 para alinear Vite y el scanner de Sonar sin advertencias de motor, con proxy Vite hacia `bff-service` dentro de la red Compose.

La arquitectura productiva se mantiene alineada con el target AWS: SPA estatica en S3/CloudFront, API Gateway hacia BFF en ECS Fargate y microservicios internos privados.

## TASK-064 infraestructura de conexiones PostgreSQL

TASK-064 introduce configuracion explicita de Hikari por microservicio para evitar saturacion de conexiones en local y preparar el salto a RDS/RDS Proxy. Esta decision afecta Compose, variables de entorno y propiedades `spring.datasource.hikari.*`.

Reglas vigentes:

- Cada microservicio declara pool maximo, minimo idle, timeout, idle timeout y max lifetime por variables propias y fallback global.
- En local se mantiene `DB_POOL_MAX_SIZE=3` como base conservadora.
- En AWS, ECS y Lambdas deben pasar por RDS Proxy o equivalente administrado antes de escalar concurrencia.
- Los errores de conexion se diagnostican por servicio; un microservicio no debe depender del arranque de otro microservicio.

## TASK-065 a TASK-077 infraestructura frontend/BFF/RBAC local

Estas tareas completan el entorno operativo local sobre BFF y SPA, sin cambiar todavia el target AWS.

Impacto de infraestructura:

- `frontend` corre como contenedor Node/Vite solo para desarrollo local y E2E; produccion compila artefacto estatico para S3/CloudFront.
- La SPA no consume microservicios internos directamente; todas las llamadas publicas pasan por `bff-service`.
- `bff-service` propaga `X-Company-Id`, `X-Correlation-Id`, `X-User-Id` e `Idempotency-Key` hacia servicios internos.
- El login local/transitorio usa `identity-service`; la autenticacion productiva con Cognito queda en TASK-164 a TASK-174.
- ROOT local se usa solo para pruebas y administracion inicial; no requiere licencia empresarial.
- RBAC y permisos efectivos se validan en backend/BFF, no solo en la SPA.
- La modularizacion frontend no introduce catalogos de negocio locales ni secretos en el bundle.

## TASK-078 a TASK-089 infraestructura de catalogos y parametrizacion operativa

Estas tareas mueven datos regulatorios/operativos a base de datos y reducen dependencias estaticas del frontend.

Impacto de infraestructura:

- `catalog-service` gobierna catalogos DB-only, DIVIPOLA, departamentos, municipios, responsabilidades fiscales, regimenes, metodos de pago, billeteras y tipos de documento DIAN.
- `inventory-service` gobierna productos, impuestos configurados por producto, codigo de barras y disponibilidad.
- `billing-service` resuelve consumidor final desde configuracion persistida, no desde constantes frontend.
- La UI obtiene catalogos por BFF; si el backend no responde, la accion se bloquea en vez de inventar opciones locales.
- La pistola de codigo de barras USB HID se trata como entrada de teclado del navegador; no requiere driver ni contenedor adicional.

## TASK-090 a TASK-093 infraestructura de auditoria y logs

Estas tareas consolidan auditoria visible en UI y eliminan paneles tecnicos de respuesta/error.

Impacto de infraestructura:

- `audit-service` es el punto de consulta de eventos de auditoria.
- Las acciones mutables deben producir auditoria sin secretos ni payload sensible.
- La SPA consume logs por BFF y muestra por defecto eventos del dia.
- Los errores visibles muestran correlation ID cuando existe, pero no exponen stack trace ni respuesta cruda.
- Los eventos asincronos de auditoria productiva se enrutan por EventBridge/SQS/Lambda segun TASK-143.

## TASK-094 a TASK-112 catalogos DB-only, contabilidad v2 y nomina

Estas tareas agregan nuevos dominios y datos operativos que deben considerarse en infraestructura local y cloud.

Impacto de infraestructura:

- `payroll-service` se agrega como microservicio fisico local y artefacto ECS objetivo.
- Nomina electronica mock vive como capacidad opcional por empresa; no obliga a activar nomina electronica globalmente.
- Contabilidad v2 usa `accounting-service`; reportes avanzados usan `reporting-service` ECS independiente.
- Los reportes operativos se exponen desde servicios duenos y se agregan por BFF cuando aplica.
- Los catalogos operativos no deben vivir en `initialState` ni recursos frontend productivos.
- Los nuevos servicios mantienen esquemas propios y migraciones Flyway por bounded context.

## TASK-113 a TASK-128 licencias, servicios con insumos y administracion empresarial

Estas tareas endurecen operacion multiempresa, cuotas y administracion desde ROOT/OWNER.

Impacto de infraestructura:

- `tenant-service` gobierna licencias, vigencia, modulos habilitados y cuotas comerciales.
- `identity-service` gobierna usuarios, roles, permisos, membresias y asignaciones por empresa.
- `billing-service` valida limite mensual de documentos antes de emitir documentos fiscales.
- `identity-service` valida limite de usuarios activos por licencia antes de crear/activar usuarios empresariales.
- El consumo asistido de insumos se mantiene en `inventory-service`; los efectos posteriores pueden publicarse por Outbox.
- El BFF debe resolver nombre de empresa para usuarios empresariales y no exponer UUID como dato principal.

## TASK-129 a TASK-141 productizacion operativa antes de AWS final

Estas tareas cierran el flujo operativo antes de profundizar infraestructura productiva.

Impacto de infraestructura:

- El E2E desde cero debe validar empresa, licencia, OWNER, catalogos, tercero, producto, stock, venta POS, conector DIAN mock, inventario, asiento contable, auditoria y reportes.
- Compras, gastos, pagos, servicios con insumos, reportes y reglas contables generan datos canonicos en servicios duenos.
- `bff-service` debe tener pruebas de contrato contra rutas criticas de microservicios.
- El aislamiento multiempresa se valida por `company_id` en APIs, reportes y acciones mutables.
- Las proyecciones event-driven deben poder reconstruirse desde tablas canonicas.
- Las tablas administrativas de usuarios/roles son UX/frontend; no agregan recursos cloud nuevos.

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
- Decision impact: TASK-169 queda trazada a CloudFront/BFF para hardening HTTP productivo.
- Library/tool: AWS (`/websites/aws_amazon`).
- Topic consulted: SQS-triggered Lambda partial batch failures and DLQ.
- Relevant finding: Lambda con SQS puede devolver `batchItemFailures` para reintentar solo mensajes fallidos; SQS soporta redrive policy hacia DLQ.
- Decision impact: Las Lambdas de TASK-143 usan Inbox/idempotencia y partial batch response para no reprocesar lotes completos.

## TASK-144 cierre UX administrativo

TASK-144 no agrega recursos cloud ni contenedores nuevos. Su impacto de infraestructura se limita a mantener la SPA/BFF como borde operativo unico para administracion de usuarios y roles.

Reglas:

- Las tablas profesionales de roles/usuarios son presentacion frontend.
- La autorizacion real permanece en BFF/identity-service.
- No se agregan endpoints directos desde la SPA hacia microservicios internos.

## TASK-145 a TASK-163 DIAN real parametrizable por empresa

Estas tareas evolucionan `dian-provider-service` como conector DIAN parametrizable por empresa, manteniendo el modo mock para local/E2E y cerrando dentro de la misma fase el diseno e implementacion objetivo del envio real certificado.

Impacto de infraestructura:

- Secrets Manager almacena certificados, PIN, claves tecnicas y credenciales por empresa como secretos separados bajo prefijo controlado.
- KMS cifra secretos por ambiente; IAM limita lectura/escritura por servicio y prefijo.
- Terraform crea KMS, policies, roles y estructura base, pero no crea secretos por empresa de forma estatica.
- La creacion/rotacion de secretos por empresa ocurre en runtime desde servicios autorizados, con auditoria y sin exponer valores.
- `dian-provider-service` puede operar en `MOCK` y en pipeline real configurable por empresa sin compartir certificado global.
- Las pruebas de conexion DIAN real deben ejecutarse sin registrar certificados, PIN, tokens ni payload sensible.
- En produccion, la salida hacia DIAN debe controlarse por subnets privadas con NAT Gateway o VPC endpoints/egress aprobado segun el destino tecnico disponible.

Estado:

- `dian-provider-service` existe como microservicio fisico, conector mock y pipeline real configurable `stub/http`.
- La persistencia objetivo de configuracion DIAN por empresa esta documentada en `database-design.md` y `data-dictionary.md`.
- La configuracion DIAN por empresa, la compuerta tecnica y el cierre funcional TASK-153 a TASK-163 quedan cubiertos por Fase 20.
- La certificacion productiva final depende de certificados/credenciales reales por empresa, URLs oficiales, fixtures DIAN y validacion de habilitacion.

Estado 2026-08-24:
- `dian-provider-service` incluye tabla `dian_provider.dian_company_configuration` para metadata no sensible por empresa.
- Los secretos se modelan como referencias bajo `/facturaelectronica/{env}/companies/{companyId}/...`.
- `infra/aws/modules/secrets` crea KMS dedicado para Secrets Manager.
- `infra/aws/modules/ecs` permite a runtime crear/actualizar secretos bajo el patron empresarial autorizado.
- Pendiente productivo: adaptador AWS SDK real para Secrets Manager, almacenamiento S3/KMS de artefactos, firma XMLDSig/XAdES certificada y E2E de habilitacion DIAN con credenciales reales empresariales.

Requerimientos cloud para cierre real:

- Secrets Manager/KMS por empresa para certificado, PIN, clave tecnica y credenciales DIAN.
- S3 privado/KMS para artefactos fiscales reales: XML firmado, ZIP/AttachedDocument, QR, representacion grafica y respuesta DIAN.
- IAM minimo para que solo `dian-provider-service` lea secretos DIAN y solo servicios autorizados consulten artefactos por BFF.
- Egress controlado desde subnets privadas hacia endpoints DIAN de habilitacion/produccion.
- CloudWatch Logs con sanitizacion obligatoria; no registrar XML completo con datos sensibles, certificados, PIN, claves ni respuestas crudas.
- SQS/EventBridge para reintentos de proveedor cuando el fallo sea temporal, preservando idempotencia por documento.

## TASK-164 a TASK-174 seguridad de autenticacion productiva

Estado 2026-08-24:
- `bff-service` soporta `AUTH_MODE=local|cognito`, valida cierre seguro en produccion y emite headers de seguridad.
- `GET /api/v1/auth/session`, `GET /api/v1/auth/login-url` y `POST /api/v1/auth/logout` preparan la transicion a cookie BFF.
- `GET /api/v1/auth/login-url` genera `state`, `nonce`, `code_verifier`, `code_challenge` PKCE S256 y cookie transitoria `NF_OAUTH_ATTEMPT` `HttpOnly` de 5 minutos.
- `infra/aws/modules/auth` crea Cognito User Pool, App Client sin secreto, Hosted UI domain, grupos `ROOT`, `COMPANY_ADMIN`, `SELLER`, `ACCOUNTANT` y MFA software token opcional.
- `infra/aws/envs/dev` cablea Cognito hacia `bff-service` con `COGNITO_BASE_URL`, `COGNITO_CLIENT_ID`, `COGNITO_REDIRECT_URI` y `COGNITO_LOGOUT_URI`.
- La SPA envia cookies `same-origin`, propaga `X-CSRF-Token` si existe `NF_CSRF` y deshabilita sourcemaps en build productivo.
- Completado: almacenamiento cifrado persistente/distribuido de sesiones BFF sobre PostgreSQL para ECS multi tarea, con fallback memoria explicito.
- Pendiente: persistencia fuerte de `cognitoSubject`/provisionamiento productivo, auditoria dedicada de eventos OAuth callback/CSRF/MFA y enforcement granular MFA por grupos/acciones criticas.

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

<!-- BEGIN SDD TASK INFRASTRUCTURE TRACEABILITY -->
## Trazabilidad individual de infraestructura por task

Esta seccion documenta de forma uniforme el impacto de infraestructura de cada task. Cuando una task no cambia infraestructura, queda expresado explicitamente para evitar huecos de trazabilidad.

### TASK-001 - Preparar Docker y variables de entorno para secretos
- Estado: Completada.
- Fase: Fase 0: Seguridad y base SDD.
- Impacto de infraestructura: Afecta entorno local Docker/Compose, variables de entorno y gestion de secretos dummy.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-002 - Crear estructura SDD local
- Estado: Completada.
- Fase: Fase 0: Seguridad y base SDD.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-003 - Disenar modelo de base de datos multiempresa
- Estado: Completada.
- Fase: Fase 1: Arquitectura y plataforma.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-004 - Definir contratos entre microservicios
- Estado: Completada.
- Fase: Fase 1: Arquitectura y plataforma.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-005 - Actualizar Spring Boot a version soportada/LTS
- Estado: Completada.
- Fase: Fase 1: Arquitectura y plataforma.
- Impacto de infraestructura: Afecta empaquetado Maven, artefactos independientes y construccion/despliegue por servicio.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-006 - Crear estructura Clean Architecture para `billing-service`
- Estado: Completada.
- Fase: Fase 1: Arquitectura y plataforma.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-007 - Crear migraciones versionadas de base de datos
- Estado: Completada.
- Fase: Fase 1: Arquitectura y plataforma.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-008 - Implementar configuracion de emisor y resoluciones
- Estado: Completada.
- Fase: Fase 2: Facturacion electronica y conector DIAN.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-009 - Implementar calculo de factura electronica
- Estado: Completada.
- Fase: Fase 2: Facturacion electronica y conector DIAN.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-010 - Implementar puerto y adaptador de conexion DIAN
- Estado: Completada.
- Fase: Fase 2: Facturacion electronica y conector DIAN.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-011 - Implementar estados y trazabilidad de documentos electronicos
- Estado: Completada.
- Fase: Fase 2: Facturacion electronica y conector DIAN.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-012 - Implementar notas credito y debito
- Estado: Completada.
- Fase: Fase 2: Facturacion electronica y conector DIAN.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-013 - Implementar emision de documento equivalente electronico POS
- Estado: Completada.
- Fase: Fase 3: POS electronico.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-014 - Refactorizar modulos CRUD existentes hacia Clean Architecture
- Estado: Completada.
- Fase: Fase 4: Refactorizacion arquitectonica legacy.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-015 - Implementar nota de ajuste POS
- Estado: Completada.
- Fase: Fase 5: POS electronico - ajustes.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-016 - Implementar movimientos de inventario
- Estado: Completada.
- Fase: Fase 6: Inventario.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-017 - Implementar validacion de disponibilidad
- Estado: Completada.
- Fase: Fase 6: Inventario.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-018 - Implementar plan de cuentas basico
- Estado: Completada.
- Fase: Fase 7: Contabilidad base.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-019 - Completar refactor de modulos legacy restantes hacia Clean Architecture
- Estado: Completada.
- Fase: Fase 8: Refactorizacion legacy restante.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-020 - Migrar persistencia y contratos legacy a Clean Architecture completa
- Estado: Completada.
- Fase: Fase 8: Refactorizacion legacy restante.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-021 - Eliminar codigo muerto legacy despues de migracion Clean Architecture completa
- Estado: Completada.
- Fase: Fase 8: Refactorizacion legacy restante.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-022 - Implementar asientos contables automaticos
- Estado: Completada.
- Fase: Fase 9: Contabilidad.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-023 - Implementar libro diario y libro mayor
- Estado: Completada.
- Fase: Fase 9: Contabilidad.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-024 - Estandarizar errores API
- Estado: Completada.
- Fase: Fase 10: Calidad, auditoria y observabilidad.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-025 - Implementar auditoria fiscal
- Estado: Completada.
- Fase: Fase 10: Calidad, auditoria y observabilidad.
- Impacto de infraestructura: Afecta contratos de eventos, Outbox/Inbox, auditoria y procesamiento asincrono.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-026 - Implementar correlation ID y logs estructurados
- Estado: Completada.
- Fase: Fase 10: Calidad, auditoria y observabilidad.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-027 - Implementar persistencia JPA y endpoints REST para billing/POS
- Estado: Completada.
- Fase: Fase 11: Pruebas end-to-end locales con Docker.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-028 - Implementar conector DIAN mock configurable
- Estado: Completada.
- Fase: Fase 11: Pruebas end-to-end locales con Docker.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-029 - Implementar persistencia JPA y endpoints REST para accounting
- Estado: Completada.
- Fase: Fase 11: Pruebas end-to-end locales con Docker.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-030 - Crear seed local y guia de pruebas Docker
- Estado: Completada.
- Fase: Fase 11: Pruebas end-to-end locales con Docker.
- Impacto de infraestructura: Afecta entorno local Docker/Compose, variables de entorno y gestion de secretos dummy.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-031 - Redisenar estructura Maven multi-modulo para microservicios fisicos
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Afecta empaquetado Maven, artefactos independientes y construccion/despliegue por servicio.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-032 - Implementar `tenant-service` para empresas multiempresa
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-033 - Migrar catalogos y terceros legacy a Clean Architecture y microservicios
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-034 - Implementar `inventory-service` completo con costos, compras, stock y kardex
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-035 - Implementar venta completa y emision electronica conectada al flujo
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-036 - Separar `dian-provider-service` con mock configurable
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Afecta empaquetado Maven, artefactos independientes y construccion/despliegue por servicio.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-037 - Conectar facturacion validada con inventario y contabilidad
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-038 - Implementar prueba end-to-end Docker desde cero
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Afecta entorno local Docker/Compose, variables de entorno y gestion de secretos dummy.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-039 - Identificar codigo y tablas legacy no usadas
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-040 - Eliminar codigo muerto y tablas legacy reemplazadas
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-041 - Migrar emisor, resoluciones y numeracion fiscal a billing-service
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-042 - Migrar audit-service como microservicio fisico
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-043 - Conectar billing-service como productor de auditoria fiscal
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Afecta contratos de eventos, Outbox/Inbox, auditoria y procesamiento asincrono.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-044 - Desacoplar dependencias de arranque entre microservicios
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-045 - Definir estrategia de mensajeria asincrona cloud
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Impacto de infraestructura: Afecta contratos de eventos, Outbox/Inbox, auditoria y procesamiento asincrono.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-046 - Cerrar diseno backend core pendiente antes de depuracion legacy
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-047 - Implementar terceros fiscales con DV NIT automatico
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-048 - Implementar bienes, servicios, insumos y referencias operativas
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-049 - Ajustar ventas y documentos para bienes y servicios
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-050 - Implementar movimientos manuales de insumos
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-051 - Implementar compras, gastos y cuentas por pagar
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-052 - Completar documentos fiscales y consultas fiscales
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-053 - Completar contabilidad parametrizable PUC
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-054 - Implementar reportes minimos operativos, fiscales y contables
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-055 - Implementar cuentas por cobrar y recaudos
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-056 - Implementar usuarios, roles, permisos y auditoria de acceso
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Afecta contratos de eventos, Outbox/Inbox, auditoria y procesamiento asincrono.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-057 - Implementar licenciamiento por empresa
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-058 - Migrar legacy pendiente al modelo Clean Architecture completo
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-059 - Depurar y eliminar codigo muerto, endpoints y tablas legacy
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-060 - Definir arquitectura cloud AWS, BFF y clasificacion ECS Fargate/Lambda
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-061 - Implementar IaC AWS inicial para frontend, BFF, ECS Fargate, RDS y servicios base
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-062 - Implementar event-driven AWS con Outbox/Inbox, EventBridge/SQS y Lambdas
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Impacto de infraestructura: Afecta infraestructura productiva AWS objetivo y debe reflejarse en Terraform/IAM/red/seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-063 - Disenar e implementar frontend SPA y BFF inicial
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-064 - Optimizar gestion de conexiones PostgreSQL por microservicio
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-065 - Mejorar frontend con login, empresa activa y formularios controlados
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-066 - Restringir UI operativa por sesion y licencia activa
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-067 - RediseÃ¯Â¿Â½ar experiencia visual profesional de toda la aplicacion
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-068 - Disenar RBAC modular con ROOT global y roles por empresa
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-069 - Implementar RBAC modular en identity-service
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-070 - Implementar UI de administracion de usuarios, roles y permisos
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-071 - Aplicar navegacion y acciones frontend basadas en permisos efectivos
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-072 - Implementar bootstrap ROOT minimo para pruebas locales
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-073 - Completar flujo ROOT operativo
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-074 - Corregir tipo de documento de empresa a codigo DIAN numerico
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-075 - Unificar tipos de documento de identificacion como codigo DIAN numerico
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-076 - Ajustar experiencia funcional colombiana y RBAC operativo
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-077 - Modularizar SPA frontend por Clean Code y SOLID
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-078 - Importar catalogo completo DIVIPOLA para municipios colombianos
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-079 - Parametrizar responsabilidades fiscales, regimenes y medios de pago
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-080 - Mejorar seleccion de responsabilidades fiscales
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-081 - Persistir sesion y cerrar por inactividad
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-082 - Pulir UX de login y venta POS
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-083 - Buscador de cliente en Venta POS
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-084 - NIT y digito de verificacion segun concepto DIAN
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-085 - Simplificar registro de clientes naturales
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-086 - Persistir y administrar catalogos oficiales y operativos
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-087 - Crear modulo administrativo de catalogos parametrizables
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-088 - Auditar y eliminar tablas legacy no usadas
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-089 - Configurar impuestos por producto, scanner POS y consumidor final parametrizable
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-090 - Definir politica transversal de auditoria para acciones mutables
- Estado: Completada.
- Fase: Fase 16: Auditoria, logs y acciones mutables.
- Impacto de infraestructura: Afecta contratos de eventos, Outbox/Inbox, auditoria y procesamiento asincrono.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-091 - Permitir administracion ROOT auditada de catalogos globales
- Estado: Completada.
- Fase: Fase 16: Auditoria, logs y acciones mutables.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-092 - Reemplazar paneles Respuesta/Error por modal de proceso
- Estado: Completada.
- Fase: Fase 16: Auditoria, logs y acciones mutables.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-093 - Modulo Logs/Auditoria para ROOT y administradores
- Estado: Completada.
- Fase: Fase 16: Auditoria, logs y acciones mutables.
- Impacto de infraestructura: Afecta contratos de eventos, Outbox/Inbox, auditoria y procesamiento asincrono.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-094 - Eliminar `initialState` demo y catalogos locales de negocio en frontend
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-095 - Completar catalogos DB-only para UI operativa
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-096 - Ajustar modales de proceso y mensajes contextuales
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-097 - Redisenar modulo Logs/Auditoria
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Afecta contratos de eventos, Outbox/Inbox, auditoria y procesamiento asincrono.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-098 - Mejorar UX de uso de items de inventario
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-099 - Disenar modulo contable funcional v2
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-100 - Implementar contabilidad operativa v2
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-101 - Disenar modulo de nomina y clasificacion laboral/contractual
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-102 - Crear `payroll-service`
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-103 - Implementar pagos diarios verbales/jornal
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-104 - Configurar nomina electronica opcional por empresa
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-105 - Implementar nomina electronica mock
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-106 - Integrar nomina con contabilidad
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-107 - Endurecer RBAC para catalogos, logs, contabilidad y nomina
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-108 - Mejorar frontend profesional y componentes reutilizables
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-109 - Prueba E2E desde cero sin datos demo frontend
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-110 - Revision normativa y catalogos de cumplimiento
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-111 - Preparacion cloud/productiva para nuevos modulos
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-112 - Commit y reporte de cierre de fase
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-113 - Implementar consumo asistido de insumos por servicios facturados
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-114 - Corregir error de login por licencia no configurada
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-115 - Extender licencias empresariales con modulos contratados
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-116 - Crear modulo ROOT para administrar licencias
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-117 - Aplicar licencia por modulo en menues y operaciones
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-118 - Auditar administracion de licencias
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-119 - E2E licencia parametrizable
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-120 - Aplicar cuotas comerciales de licencia
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-121 - Ajustar UX/RBAC de empresa y permisos
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-122 - Unificar permiso de Venta POS e internacionalizar permisos
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-123 - Redisenar navegacion principal con submenus
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-124 - Crear pantalla exclusiva de Roles
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-125 - Crear pantalla exclusiva de Usuarios
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-126 - Reducir autocierre de modales exitosos
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-127 - Endurecer contratos backend de roles
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-128 - Actualizar e inactivar usuarios empresariales
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-129 - Implementar E2E operativo desde cero para venta POS electronica
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-130 - Completar compras y entradas de inventario con contabilidad
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-131 - Completar servicios facturables con consumo manual de insumos
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-132 - Crear listados operativos profesionales
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-133 - Endurecer validacion backend de RBAC y licencias
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-134 - Tablero ROOT de uso de licencias
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Afecta identity/tenant/BFF y politicas de acceso; infraestructura nueva solo si requiere autenticacion productiva.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-135 - Auditoria transversal verificable
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Afecta contratos de eventos, Outbox/Inbox, auditoria y procesamiento asincrono.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-136 - Robustecer sesion, expiracion y restauracion
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-137 - Parametrizar reglas contables PUC por evento
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Afecta contratos de eventos, Outbox/Inbox, auditoria y procesamiento asincrono.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-138 - Generar comprobantes/asientos automaticos
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-139 - Implementar reportes minimos contables y operativos
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-140 - Pruebas de contrato BFF/microservicios
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-141 - E2E de aislamiento multiempresa
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-142 - Completar Terraform AWS productivo
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Afecta infraestructura productiva AWS objetivo y debe reflejarse en Terraform/IAM/red/seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-143 - Completar eventos productivos AWS
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Afecta infraestructura productiva AWS objetivo y debe reflejarse en Terraform/IAM/red/seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-144 - Mejorar tablas administrativas de usuarios y roles
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Impacto de infraestructura: Afecta PostgreSQL/Flyway y datos de referencia consumidos por servicios.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-145 - Replantear alcance DIAN como software parametrizable por empresa
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Impacto productivo pendiente: secretos por empresa, auth administrada, hardening BFF/SPA y auditoria de seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-146 - Disenar modulo de configuracion DIAN por empresa
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Impacto productivo pendiente: secretos por empresa, auth administrada, hardening BFF/SPA y auditoria de seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-147 - Persistencia segura de certificados y secretos DIAN por empresa
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Afecta entorno local Docker/Compose, variables de entorno y gestion de secretos dummy.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-148 - Ajustar contratos API para configuracion DIAN y prueba de conexion
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-149 - Actualizar infraestructura AWS para secretos DIAN por empresa
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Afecta entorno local Docker/Compose, variables de entorno y gestion de secretos dummy.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-150 - Renombrar lenguaje funcional de proveedor a conector DIAN
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Impacto productivo pendiente: secretos por empresa, auth administrada, hardening BFF/SPA y auditoria de seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-151 - Preparar flujo tecnico DIAN real segun caja de herramientas
- Estado: Completada como compuerta tecnica. El contenedor `dian-provider-service` recibe variables para ubicar artefactos DIAN locales/configurados; el envio real certificado sigue cerrado hasta implementar el adaptador productivo.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Impacto productivo pendiente: secretos por empresa, auth administrada, hardening BFF/SPA y auditoria de seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-152 - Implementar UI de Configuracion DIAN por empresa
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Impacto productivo pendiente: secretos por empresa, auth administrada, hardening BFF/SPA y auditoria de seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-153 - Actualizar base normativa DIAN vigente y matriz de versionado tecnico
- Estado: Completada. Base normativa consolidada y matriz versionada; debe revalidarse antes de habilitar produccion real.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: No crea recursos cloud; condiciona endpoints, artefactos tecnicos y versionado por ambiente.
- Control operativo: documentar fuentes oficiales, fecha de consulta y version/anexo antes de habilitar recursos productivos.

### TASK-154 - Disenar generacion XML UBL 2.1 para documentos fiscales
- Estado: Completada. XML UBL 2.1 de referencia implementado para factura, POS electronico y notas con payload sanitizado.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: No requiere recurso nuevo; puede requerir empaquetar artefactos tecnicos validados con el contenedor o cargarlos desde storage seguro.
- Control operativo: versionar artefactos tecnicos y fixtures sanitizados sin secretos.

### TASK-155 - Disenar calculo CUFE/CUDE y QR productivo
- Estado: Completada. Calculo deterministico CUFE/CUDE y QR de referencia implementado sin registrar claves tecnicas.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: No crea recursos cloud; depende de secretos empresariales ya administrados por KMS/Secrets Manager.
- Control operativo: evitar logs de claves tecnicas y validar reproducibilidad por ambiente.

### TASK-156 - Implementar firma XML con certificado empresarial
- Estado: Completada con adaptador de referencia. La firma XMLDSig/XAdES certificada queda condicionada a certificado real de empresa y validacion de habilitacion.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Usa Secrets Manager/KMS para certificado y PIN por empresa.
- Control operativo: IAM minimo por servicio/prefijo, rotacion auditable y respuesta sin valores secretos.

### TASK-157 - Implementar validacion XSD, Schematron y listas de codigos
- Estado: Completada con compuerta tecnica fail-closed cuando faltan artefactos XSD/Schematron/listas configuradas.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Requiere disponibilidad confiable de XSD/Schematron/listas en runtime.
- Control operativo: fallar cerrado si faltan artefactos o version esperada.

### TASK-158 - Implementar transporte real DIAN para habilitacion y produccion
- Estado: Completada con transporte configurable `stub/http` por empresa y separacion de modo mock/real.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Requiere egress controlado desde servicios privados hacia endpoints DIAN y configuracion de timeouts/reintentos.
- Control operativo: separar habilitacion/produccion y nunca hacer fallback a mock en modo real.

### TASK-159 - Persistir respuestas DIAN y ApplicationResponse
- Estado: Completada mediante migracion Flyway V003, trazas tecnicas, artefactos y resultados de validacion.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Agrega migraciones en `dian-provider-service` y almacenamiento de metadata tecnica.
- Control operativo: persistir solo mensajes sanitizados y referencias a artefactos privados.

### TASK-160 - Implementar reintentos DIAN e idempotencia de efectos posteriores
- Estado: Completada con idempotencia de envio y base de reintentos asincronos para fallas tecnicas.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Usa SQS/EventBridge/provider retry Lambda cuando el fallo sea temporal.
- Control operativo: DLQ, reintento con idempotencia y estados terminales claros.

### TASK-161 - Almacenar artefactos fiscales reales de forma segura
- Estado: Completada con puerto de almacenamiento y adaptador local privado; S3/KMS queda como adaptador productivo.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Usa S3 privado/KMS o storage cloud equivalente para artefactos fiscales.
- Control operativo: acceso por BFF/RBAC, hash, retencion y auditoria de descarga.

### TASK-162 - Ajustar contratos API y modelo de datos para DIAN real
- Estado: Completada. Contratos mantienen endpoints estables y agregan trazabilidad/artefactos neutrales.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: Puede requerir nuevas migraciones, variables de entorno y permisos IAM.
- Control operativo: contratos deben declarar headers, errores, idempotencia y storage seguro.

### TASK-163 - Definir suite obligatoria de pruebas DIAN antes de produccion
- Estado: Completada como suite base local; antes de produccion se requieren fixtures sanitizados y pruebas de habilitacion por empresa.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Impacto de infraestructura: No crea recursos permanentes; puede usar stubs locales y entornos de habilitacion DIAN.
- Control operativo: bloquear despliegue real si no existe evidencia de pruebas con fixtures sanitizados.

### TASK-164 - Disenar autenticacion productiva con Cognito Hosted UI y PKCE
- Estado: Completada. Modulo Cognito Terraform, variables BFF, PKCE S256, callback/token exchange, puente Cognito -> identidad interna por `sub` persistente y sesion cifrada server-side implementados.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Impacto de infraestructura: Afecta infraestructura productiva AWS objetivo y debe reflejarse en Terraform/IAM/red/seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-165 - Reemplazar tokens en SPA por sesion BFF con cookie segura
- Estado: Completada. Cookie/CSRF base, endpoints de sesion/logout, hidratacion SPA por cookie, proxy con autorizacion interna server-side y sanitizacion de storage para sesiones Cognito/cookie implementados. El bearer queda limitado a modo local/E2E.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-166 - Crear almacenamiento server-side de sesion cifrada
- Estado: Completada. `BFF_SESSION_STORE=jdbc` persiste sesiones cifradas en PostgreSQL; `memory` queda como fallback local/test explicito.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Impacto de infraestructura: Impacto productivo pendiente: secretos por empresa, auth administrada, hardening BFF/SPA y auditoria de seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-167 - Implementar logout seguro y revocacion
- Estado: Completada. Logout limpia cookies, invalida sesion server-side, revoca sesion interna, audita `LOGOUT` y revoca `refresh_token` Cognito best-effort.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Impacto de infraestructura: Impacto productivo pendiente: secretos por empresa, auth administrada, hardening BFF/SPA y auditoria de seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-168 - Hardening frontend contra exposicion de datos sensibles
- Estado: Completada. Sourcemaps productivos deshabilitados, fetch con cookies/CSRF, snapshots Cognito/cookie sin tokens e hidratacion productiva sin `Authorization` construido por la SPA. El bearer queda limitado a modo local/E2E.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-169 - Agregar security headers CloudFront/BFF
- Estado: Completada.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Impacto de infraestructura: Afecta borde BFF/SPA local; no expone microservicios internos directamente.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-170 - Implementar proteccion CSRF para sesiones por cookie
- Estado: Completada.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Impacto de infraestructura: Afecta infraestructura productiva AWS objetivo y debe reflejarse en Terraform/IAM/red/seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-171 - MFA obligatorio para ROOT, administradores y acciones criticas
- Estado: Completada. Cognito habilita MFA software token y el BFF bloquea mutaciones criticas sin evidencia MFA en la sesion.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Impacto de infraestructura: Afecta infraestructura productiva AWS objetivo y debe reflejarse en Terraform/IAM/red/seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-172 - Provisionamiento runtime de secretos AWS por empresa
- Estado: Completada.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Impacto de infraestructura: Afecta entorno local Docker/Compose, variables de entorno y gestion de secretos dummy.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-173 - Auditoria de seguridad transversal
- Estado: Completada. El BFF emite auditoria best-effort para mutaciones company-scoped, CSRF invalido y MFA faltante; identity audita login Cognito, vinculo de sujeto Cognito y logout.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Impacto de infraestructura: Afecta contratos de eventos, Outbox/Inbox, auditoria y procesamiento asincrono.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-174 - Modo transicion local y bloqueo productivo de auth dummy
- Estado: Completada. `AUTH_MODE=local` queda local/E2E y el BFF falla cerrado en produccion si no usa Cognito.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Impacto de infraestructura: Impacto productivo pendiente: secretos por empresa, auth administrada, hardening BFF/SPA y auditoria de seguridad.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-175 - Cerrar consistencia documental SDD antes de nueva implementacion
- Estado: Completada.
- Fase: Fase 22: Gobierno SDD, diagramas y limpieza final.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-176 - Actualizar diagramas Mermaid a la arquitectura y modelo vigentes
- Estado: Completada.
- Fase: Fase 22: Gobierno SDD, diagramas y limpieza final.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-177 - Cerrar brechas documentales de estado actual versus objetivo
- Estado: Completada.
- Fase: Fase 22: Gobierno SDD, diagramas y limpieza final.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

### TASK-178 - Ejecutar limpieza final legacy y artefactos huerfanos antes de nuevas mejoras
- Estado: Completada.
- Fase: Fase 22: Gobierno SDD, diagramas y limpieza final.
- Impacto de infraestructura: Sin cambio directo de infraestructura; se ejecuta en servicios existentes, base de datos ya definida o documentacion SDD.
- Control operativo: mantener trazabilidad en Docker/local, Terraform/AWS o documentacion SDD segun el alcance de la task.

## TASK-179 a TASK-189 infraestructura objetivo

### Branding y archivos empresariales

- Local: `tenant-service` puede exponer assets desde un volumen Docker controlado o almacenamiento local configurado por variable de entorno.
- AWS: logos, favicons, exportaciones y artefactos POS deben almacenarse en S3 privado con cifrado KMS, versionado opcional, lifecycle y prefijos por ambiente/empresa.
- Acceso de lectura: por BFF o CloudFront con politicas restrictivas; no se permiten buckets publicos de escritura.
- Uploads: limites de tamano y tipos MIME en BFF/tenant-service, mas configuracion multipart Spring Boot.
- Auditoria: toda carga, actualizacion, eliminacion, descarga o error debe generar evento sin contenido binario.

### Reporting-service y exportaciones

- `reporting-service` es ECS Fargate privado desde TASK-185 y orquesta consultas hacia servicios duenos.
- El BFF sera el unico borde publico para reportes.
- Exportaciones pequenas pueden generarse sincronicamente; exportaciones grandes deben poder pasar a flujo asincrono con EventBridge/SQS/Lambda o worker interno del servicio.
- Los archivos exportados se almacenan en S3 privado con expiracion y metadata de auditoria.
- Los reportes pueden usar proyecciones reconstruibles, pero los datos canonicos pertenecen a los servicios de negocio.

### Artefactos POS e impresion termica

- Fase 1: no requiere infraestructura cloud adicional; usa vista imprimible web con CSS para papel 58/80 mm y auditoria de solicitud.
- Fase 2: conectores ESC/POS, WebUSB, WebSerial o agente local requieren decision SDD adicional, validacion de impresoras reales, permisos del navegador/SO, seguridad del endpoint local y soporte operativo.
- Artefactos fiscales y comprobantes se almacenan en S3 privado en produccion y en storage local controlado en desarrollo.
- Reimpresiones generan registros de `print_job`, no nuevos documentos fiscales.

### Trazabilidad de infraestructura

- Requisitos: RF-131 a RF-145.
- Acceptance criteria: AC-193 a AC-208.
- Tareas: TASK-179 a TASK-189.
- Modulos/servicios: `frontend`, `ecs`, `secrets`, `messaging`, futuro almacenamiento S3 de artefactos, `tenant-service`, `billing-service`, `reporting-service`.

### TASK-179 - Adoptar marca NexoFiscal en frontend y documentacion visible
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Impacto de infraestructura: Sin cambio directo; afecta SPA y metadata publica.
- Control operativo: validar build frontend y cache de CloudFront cuando exista despliegue cloud.

### TASK-180 - Disenar branding empresarial parametrizable
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Impacto de infraestructura: Define storage local/S3 privado para assets empresariales.
- Control operativo: revisar IAM, KMS, prefijos por empresa y lifecycle.

### TASK-181 - Implementar backend de branding empresarial
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Impacto de infraestructura: Agrega limites multipart configurables y storage local parametrizable para desarrollo mediante `TENANT_BRANDING_STORAGE_PATH`.
- Control operativo: en AWS debe evolucionar a S3 privado/KMS por empresa usando el puerto de storage sin exponer buckets.

### TASK-182 - Implementar UI de branding y aplicacion dinamica
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Impacto de infraestructura: La SPA consume URLs de assets versionadas por hash para favicon/logo.
- Control operativo: CloudFront debe respetar cache por hash y servir fallback NexoFiscal cuando no exista branding.

### TASK-183 - Disenar artefactos fiscales, comprobantes POS e impresion termica
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Impacto de infraestructura: Define storage de artefactos POS y eventual canal de impresion.
- Control operativo: separar fase web print de conectores directos.

### TASK-184 - Disenar reporting-service y contratos de reportes avanzados
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Impacto de infraestructura: Define futuro servicio ECS privado y posibles colas/eventos para exportaciones.
- Control operativo: mantener BFF como unico borde publico.

### TASK-185 - Implementar reporting-service con reportes iniciales
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Impacto de infraestructura: Agrega `reporting-service` a Docker Compose y Terraform ECS dev con URL interna hacia BFF.
- Control operativo: las consultas iniciales orquestan fuentes canonicas; exportaciones y consultas pesadas deben evolucionar con paginacion/asincronia en tareas posteriores.

### TASK-186 - Implementar UI avanzada de reportes
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Impacto de infraestructura: Sin cambio directo; consume BFF.
- Control operativo: la UI usa catalogo backend, filtros dinamicos y visualizacion cliente inicial; consultas pesadas/exportaciones quedan para tareas posteriores.

### TASK-187 - Implementar exportacion de reportes
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Impacto de infraestructura: Sin storage adicional en fase inicial; CSV/XLS se generan sincronicamente desde `reporting-service`.
- Control operativo: BFF audita la descarga sincrona como `POST`; archivos pesados, expiracion, storage privado y procesamiento asincrono quedan para evolucion posterior.

### TASK-188 - Implementar comprobante POS imprimible e impresion web
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Impacto de infraestructura: Sin cambio cloud inicial; usa navegador e imprime desde cliente.
- Control operativo: `POST /sales/{saleId}/receipt` permite auditoria BFF; conectores directos quedan para fase posterior con hardware validado.

### TASK-189 - Implementar historico avanzado de ventas/documentos
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Impacto de infraestructura: Sin nuevo contenedor; consulta sobre `billing-service` con filtros multiempresa y reimpresion auditada por BFF.
- Control operativo: vigilar performance, agregar paginacion/indices dedicados si el volumen de ventas crece y mantener aislamiento por `company_id`.

## TASK-190 a TASK-196 infraestructura objetivo

Estado: implementacion local completada; destino AWS mantiene S3/KMS/SES/SQS como adaptadores productivos.

Componentes AWS objetivo:

- `reporting-service` en ECS Fargate privado para API de jobs y consultas de estado.
- `report-export-worker-lambda` para generar archivos pesados desde SQS/EventBridge.
- EventBridge/SQS con DLQ para solicitudes de exportacion asincrona.
- S3 privado con SSE-KMS para archivos exportados.
- SES para notificaciones de reporte listo.
- CloudWatch Logs/Metrics/Alarms para jobs, errores, expiraciones y volumen de descargas.
- Secrets Manager/SSM Parameter Store para parametros de ambiente no sensibles o sensibles segun corresponda.

Componentes locales/Docker implementados:

- `reporting-service` con PostgreSQL/Flyway para jobs.
- Worker programado interno controlado por `REPORT_EXPORT_WORKER_ENABLED`.
- Volumen Docker privado `report_exports_data` para archivos exportados.
- BFF como unica entrada publica para `POST/GET /api/v1/reports/export-jobs` y `/reportes/descarga/{token}`.

Variables objetivo:

```env
APP_PUBLIC_BASE_URL=http://localhost:5173
REPORT_DOWNLOAD_PRESIGNED_TTL_SECONDS=5
REPORT_LINK_TOKEN_TTL_HOURS=72
REPORT_EXPORT_RETENTION_DAYS=7
REPORT_EXPORT_BUCKET_NAME=
REPORT_EXPORT_KMS_KEY_ID=
REPORT_EXPORT_QUEUE_URL=
REPORT_EXPORT_DLQ_URL=
REPORT_NOTIFICATION_EMAIL_FROM=
```

Reglas:

- `APP_PUBLIC_BASE_URL` es obligatorio en ambientes desplegados y no se hardcodea en codigo.
- La URL S3 prefirmada se genera solo al momento del clic y usa `REPORT_DOWNLOAD_PRESIGNED_TTL_SECONDS`, inicialmente `5`.
- El token intermediado usa `REPORT_LINK_TOKEN_TTL_HOURS`; puede vivir mas que la URL S3 porque no entrega el archivo directamente.
- El bucket S3 no debe ser publico; todo acceso de usuario final pasa por BFF/link intermediado.
- Los objetos exportados deben tener retencion configurable y limpieza automatica.
- Las Lambdas deben ser idempotentes y usar DLQ/reintentos sin bloquear `reporting-service`.

### TASK-190 - Disenar reportes asincronos avanzados
- Estado: Completada.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Impacto de infraestructura: Define umbrales y modo de ejecucion sincrono/asincrono; localmente usa scheduler interno.
- Control operativo: separar reportes interactivos de trabajos batch.

### TASK-191 - Disenar contratos API para jobs de reportes
- Estado: Completada.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Impacto de infraestructura: BFF sigue siendo unica entrada publica; `reporting-service` queda privado.
- Control operativo: contratos deben soportar retries, idempotencia y errores funcionales.

### TASK-192 - Disenar persistencia de trabajos de reportes
- Estado: Completada.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Impacto de infraestructura: PostgreSQL requiere tablas e indices por empresa, usuario, estado y expiracion.
- Control operativo: no guardar secretos ni URLs S3 prefirmadas persistentes.

### TASK-193 - Disenar worker asincrono de exportacion
- Estado: Completada.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Impacto de infraestructura: Lambda/worker, cola, DLQ, permisos S3/KMS minimos en AWS; scheduler interno en local.
- Control operativo: idempotencia por job y reintentos seguros.

### TASK-194 - Disenar descarga segura desde S3 con enlace intermediado
- Estado: Completada.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Impacto de infraestructura: BFF enruta descarga intermediada; en AWS necesitara permiso limitado para presign de objetos autorizados.
- Control operativo: TTL S3 de 5 segundos desde el clic; token de correo con TTL independiente.

### TASK-195 - Disenar notificaciones por correo
- Estado: Completada.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Impacto de infraestructura: SES, dominio/correo verificado y auditoria de envio en AWS; localmente notificacion por log controlado.
- Control operativo: no incluir datos sensibles ni adjuntos pesados en correo.

### TASK-196 - Disenar UI de reportes avanzados asincronos
- Estado: Completada.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Impacto de infraestructura: Sin nuevo recurso cloud; consume BFF.
- Control operativo: mostrar estados claros y no forzar retorno al modulo para descargar desde correo.

## TASK-249 infraestructura documental

Estado: completada.

### TASK-249 - Normalizar documentacion SDD y README del repositorio
- Estado: Completada.
- Fase: Fase 33: Gobierno documental y README operativo.
- Impacto de infraestructura: Sin cambios de recursos, contenedores ni cloud.
- Control operativo: README debe describir ejecucion local con Docker Compose, ejecucion por servicio, pruebas, SonarQube, Swagger/OpenAPI, migraciones, seguridad y variables de configuracion sin incluir secretos ni planeacion funcional.
- Validacion documental: la trazabilidad funcional queda en `specs/`; el README queda como ayuda de repositorio para instalacion, operacion local y validacion tecnica.

<!-- END SDD TASK INFRASTRUCTURE TRACEABILITY -->
