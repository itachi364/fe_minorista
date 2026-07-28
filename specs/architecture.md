# Architecture

## Estilo seleccionado

Clean Architecture basada en microservicios.

## Principios

- Dominio independiente de Spring, JPA, HTTP y proveedor tecnologico.
- Casos de uso como centro de la aplicacion.
- Puertos de entrada para comandos y consultas.
- Puertos de salida para persistencia, proveedor tecnologico, mensajeria y otros servicios.
- Adaptadores intercambiables.
- Contratos API versionados.
- Secretos fuera del repositorio.

## Bounded contexts

- Identidad y seguridad.
- Terceros.
- Catalogos fiscales.
- Inventario.
- Compras, gastos y cuentas por pagar, inicialmente dentro de inventario/contabilidad hasta justificar un bounded context independiente.
- Facturacion electronica y POS.
- Integracion proveedor tecnologico DIAN.
- Contabilidad.
- Reportes.
- Licenciamiento por empresa.

## Comunicacion

Fase inicial:

- REST sincrono entre servicios.
- Idempotencia en operaciones fiscales.
- Correlation ID propagado.

Fase posterior:

- Outbox/Inbox en productores y consumidores, con EventBridge/SQS + Lambdas para produccion AWS despues de cerrar la logica backend core, migrar legacy pendiente y aprobar depuracion.
- Eventos para `SaleConfirmed`, `ElectronicDocumentValidated`, `InventoryMovementRegistered`, `AccountingEntryPosted` y `AuditEventRequested`.
- Consumidores idempotentes por empresa, evento y documento origen.

## Persistencia

- Base de datos por microservicio cuando se extraigan fisicamente.
- En fase transitoria, esquemas separados o modulos separados dentro del backend actual.
- Migraciones versionadas obligatorias.

## Despliegue sugerido

### Local development

- Docker Compose con `postgres` y un contenedor por microservicio activo.
- PostgreSQL por esquema/base en fase local.
- Sin dependencias de arranque entre microservicios, salvo base de datos.

### AWS production target

- Frontend SPA en Amazon S3 privado servido por CloudFront.
- Entrada publica por API Gateway hacia un BFF.
- `bff-service` en ECS Fargate como fachada publica del frontend.
- `billing-service`, `inventory-service`, `accounting-service`, `dian-provider-service`, `thirdparty-service`, `catalog-service`, `identity-service`, `tenant-service`, `audit-service` y `reporting-service` en ECS Fargate.
- Procesos event-driven transversales en Lambda disparados por EventBridge/SQS.
- Persistencia en RDS/Aurora PostgreSQL por base o esquema de servicio, segun fase de madurez.
- Secretos y certificados en AWS Secrets Manager o Parameter Store, nunca en imagenes ni repositorio.

## Decision de extraccion fisica

La migracion fisica a microservicios se realizara por bounded context, no por endpoint individual. Cada microservicio tendra:

- Artefacto Maven independiente.
- Aplicacion Spring Boot independiente.
- Dockerfile propio.
- Puerto HTTP propio.
- Healthcheck propio.
- Variables de entorno propias.
- Migraciones de base de datos propias.
- Pruebas unitarias, de controlador y de persistencia propias.

Los endpoints de cada microservicio viviran dentro del artefacto del bounded context correspondiente. Por ejemplo, `POST /api/v1/products` y `GET /api/v1/products/{id}/availability` pertenecen a `inventory-service`, mientras `POST /api/v1/electronic-pos` pertenece a `billing-service`.

## Estrategia de despliegue local multi-contenedor

La primera version fisica usara Docker Compose:

- Un contenedor por microservicio.
- Un contenedor PostgreSQL local.
- Bases de datos o esquemas separados por servicio.
- Red interna Docker para comunicacion entre servicios.
- Puertos publicados solo para servicios que deban probarse desde el host.

La estrategia inicial de comunicacion sera REST sincrona con `X-Correlation-Id`, `X-Company-Id` e `Idempotency-Key` en comandos criticos. Los eventos definidos en `specs/api-contract.md` se mantendran como contrato conceptual y podran implementarse despues mediante broker.

## Orden recomendado de extraccion fisica

1. `tenant-service`, porque crea la frontera real de empresa.
2. `catalog-service` y `thirdparty-service`, porque reducen dependencias legacy y alimentan ventas/compras.
3. `inventory-service`, porque debe controlar stock, costos y kardex. Implementado en TASK-034.
4. `dian-provider-service`, porque aisla el mock y prepara el adaptador real.
5. `accounting-service`, porque ya tiene dominio avanzado y puede exponerse como servicio independiente.
6. `billing-service`, porque orquesta venta, documento fiscal, proveedor, inventario y contabilidad.
7. `audit-service`, para consultas y consolidacion de auditoria fiscal/tecnica.
8. `identity-service` y licenciamiento, antes de escenarios reales multiempresa.
9. Reportes minimos sobre datos limpios del modelo nuevo.

## Regla para depuracion legacy

Ningun paquete legacy ni tabla legacy debe eliminarse por intuicion. La depuracion requiere:

- Mapa de reemplazo legacy -> bounded context.
- Evidencia de que el endpoint o caso de uso equivalente existe en el microservicio nuevo.
- Pruebas automatizadas o checklist end-to-end que cubra el reemplazo.
- Verificacion de que no existan referencias de compilacion ni de runtime.
- Migracion o respaldo de datos cuando aplique.

## Regla de orden para backend core, depuracion y eventos cloud

El orden aprobado para las proximas fases es:

1. Completar logica de negocio backend: clientes/adquirentes fiscales, proveedores, NIT con digito de verificacion automatico, bienes, servicios, insumos, movimientos manuales, compras, gastos, cuentas por pagar, reportes, usuarios/roles y licencias.
2. Migrar el legacy pendiente al modelo Clean Architecture y microservicios existentes, manteniendo compatibilidad hasta aprobar ruptura.
3. Eliminar codigo, endpoints y tablas legacy solo despues de matriz de reemplazo, E2E aprobado y verificacion de referencias.
4. Definir la arquitectura cloud AWS objetivo: Frontend CloudFront/S3, API Gateway/BFF, microservicios ECS Fargate, eventos EventBridge/SQS y Lambdas.
5. Implementar Outbox/Inbox y consumidores Lambda para desacoplar efectos posteriores, auditoria, reportes y reintentos.

La infraestructura event-driven no debe introducirse en runtime antes de que el flujo de negocio funcione completamente por API, persistencia PostgreSQL y pruebas locales desde cero. No se usaran brokers self-hosted; el destino productivo aprobado es AWS administrado con EventBridge/SQS + Lambda.


## Clasificacion AWS de workloads

### ECS Fargate

Usar ECS Fargate para servicios HTTP de larga vida que deben mantener healthcheck, escalamiento por servicio, configuracion externa y despliegue independiente:

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
- `reporting-service` cuando se materialice como servicio fisico

### Lambda

Usar Lambda para procesos cortos, idempotentes y disparados por eventos:

- `audit-event-writer`
- `inventory-sale-effect`
- `accounting-sale-effect`
- `accounting-purchase-effect`
- `reporting-projection-updater`
- `provider-submission-status-retry`
- `license-expiration-check`
- `notification-dispatcher`

Cada Lambda debe consumir eventos con `eventId`, `companyId`, `correlationId`, `source`, `type`, `payloadVersion` e `idempotencyKey`, y registrar Inbox/estado de procesamiento cuando escriba datos propios.

## Recomendacion de migracion incremental

1. Corregir configuracion sensible.
2. Introducir paquetes Clean Architecture dentro del proyecto actual.
3. Implementar `billing` como modulo nuevo sin romper CRUD existente.
4. Refactorizar modulos CRUD existentes de forma incremental hacia la misma estructura usada por `billing`.
5. Usar `Categoria` como piloto de refactor por ser un modulo pequeno y de bajo riesgo.
6. Separar `dian-provider` como servicio o modulo independiente.
7. Extraer inventario y contabilidad cuando contratos esten estables.
8. Mantener pruebas de contrato durante la extraccion.
9. Ejecutar flujo end-to-end desde cero antes de eliminar cualquier elemento legacy.

## Refactorizacion de modulos existentes

Los paquetes legacy `controller`, `service`, `repository`, `models`, `DTO` y `mappers` se migraran gradualmente hacia bounded contexts alineados con Clean Architecture.

Orden recomendado:

1. `catalog`: `Categoria`, `Producto`, `Impuesto`, `Pais`, `MetodoPago`, `TipoDocumento`, `TipoGasto`.
2. `thirdparty`: `Cliente`, `Proveedor`.
3. `inventory`: stock, compras, productos inventariables y movimientos.
4. `billing`: facturas, POS, numeracion, proveedor DIAN y documentos electronicos.
5. `accounting`: PUC, asientos, libro diario y libro mayor.
6. `audit`: auditoria y registro de accesos.

Reglas de migracion:

- Mantener compatibilidad de endpoints existentes salvo aprobacion explicita.
- Migrar primero comportamiento a casos de uso y puertos.
- Mantener adaptadores JPA como detalle de infraestructura.
- Agregar pruebas antes o durante cada refactor para fijar comportamiento.
- No mezclar refactor arquitectonico con nuevas reglas de negocio.

## Riesgos arquitectonicos

- Microservicios prematuros pueden aumentar complejidad operacional.
- La integracion con proveedor tecnologico depende de contrato comercial y documentacion especifica.
- La normatividad cambia y requiere mantenimiento continuo.
- El modelo contable debe ser validado por contador.
