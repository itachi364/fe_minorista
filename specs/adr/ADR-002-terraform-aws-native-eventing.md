# ADR-002: Terraform y mensajeria AWS administrada

## Estado

Aceptada.

## Contexto

La plataforma se disenara para operar 100% en cloud AWS. No se contempla despliegue on-premise ni administracion de brokers self-hosted como parte del producto objetivo.

El backend esta migrando a microservicios Spring Boot por bounded context, con Clean Architecture, contenedores independientes y procesos event-driven para auditoria, efectos posteriores, reportes y reintentos.

## Decision

- Terraform sera la herramienta oficial de infraestructura como codigo.
- La mensajeria productiva usara servicios administrados AWS: EventBridge/SQS + Lambda.
- Los microservicios HTTP de larga vida se desplegaran en ECS Fargate.
- El frontend se desplegara como SPA en S3 privado servido por CloudFront.
- La entrada publica usara API Gateway hacia un BFF en ECS Fargate.
- La persistencia productiva usara RDS/Aurora PostgreSQL.
- No se usara broker self-hosted como destino productivo ni como requisito de desarrollo.
- Los productores usaran Outbox y los consumidores usaran Inbox/idempotencia para evitar perdida o duplicacion de eventos.

## Consecuencias

### Positivas

- Menor carga operativa frente a operar un cluster de broker propio.
- Integracion natural con Lambda, reintentos, DLQ, IAM, CloudWatch y Terraform AWS Provider.
- Mejor alineacion con una plataforma SaaS cloud-first vendible mediante licencias.
- Reduccion de superficie de mantenimiento para despliegues multiempresa.

### Trade-offs

- Mayor acoplamiento operativo a AWS.
- Los costos y limites de EventBridge/SQS/Lambda deben monitorearse por ambiente y volumen.
- Los contratos de eventos deben disenar idempotencia, versionado y DLQ desde el inicio.

## Context7 evidence

- Library/tool: Terraform AWS Provider (`/hashicorp/terraform-provider-aws`).
- Topic consulted: ECS Fargate, Lambda, API Gateway, SQS/EventBridge y recursos administrados en Terraform.
- Relevant finding: El provider soporta recursos para ECS Fargate, API Gateway, Lambda, SQS event source mappings y recursos administrados AWS necesarios para el target.
- Decision impact: Terraform puede modelar la infraestructura productiva sin scripts imperativos.

- Library/tool: Self-hosted broker documentation via Context7.
- Topic consulted: self-hosted broker production deployment, clustering, resources and operations.
- Relevant finding: A self-hosted persistent broker requires dedicated resources, HA/clustering, and network/storage operational planning as critical infrastructure.
- Decision impact: Para una plataforma 100% AWS cloud, se evita operar broker self-hosted y se seleccionan EventBridge/SQS + Lambda.

## Validacion esperada

- `terraform fmt -check`.
- `terraform validate`.
- Plan revisado sin recursos legacy.
- Pruebas unitarias de Outbox/Inbox e idempotencia.
- Prueba E2E desde cero validando venta, documento fiscal mock, inventario, contabilidad, auditoria y proyecciones.