# Architecture

## Estilo seleccionado

Clean Architecture basada en microservicios.

## Principios

- Dominio independiente de Spring, JPA, HTTP, cloud SDKs y conexion DIAN concreta.
- Casos de uso como centro de la aplicacion.
- Puertos de entrada para comandos y consultas.
- Puertos de salida para persistencia, conexion DIAN parametrizable, mensajeria y otros servicios.
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
- Configuracion/conector DIAN parametrizable por empresa.
- Contabilidad.
- Reportes.
- Licenciamiento por empresa.

## Comunicacion

Fase inicial:

- REST sincrono entre servicios.
- Idempotencia en operaciones fiscales.
- Correlation ID propagado.

Target productivo aprobado:

- Outbox/Inbox en productores y consumidores, con EventBridge/SQS + Lambdas para produccion AWS.
- Eventos para `SaleConfirmed`, `ElectronicDocumentValidated`, `InventoryMovementRegistered`, `AccountingEntryPosted` y `AuditEventRequested`.
- Consumidores idempotentes por empresa, evento y documento origen.

## Persistencia

- Base de datos/esquema por microservicio fisico.
- En local se permite PostgreSQL compartido con esquemas separados por servicio.
- En produccion se usa RDS/Aurora PostgreSQL privado; la separacion puede evolucionar de esquemas a bases/instancias por servicio si el volumen o cumplimiento lo exige.
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
- `billing-service`, `inventory-service`, `accounting-service`, `dian-provider-service`, `thirdparty-service`, `catalog-service`, `identity-service`, `tenant-service`, `audit-service`, `payroll-service` y BFF en ECS Fargate.
- Procesos event-driven transversales en Lambda disparados por EventBridge/SQS.
- Persistencia en RDS/Aurora PostgreSQL por base o esquema de servicio, segun fase de madurez.
- Secretos y certificados en AWS Secrets Manager o Parameter Store, nunca en imagenes ni repositorio.
- Assets empresariales, exportaciones de reportes y artefactos POS en S3 privado con KMS, metadata en PostgreSQL y acceso controlado por BFF/CloudFront.

Estado de materializacion: `reporting-service` existe como artefacto fisico desde TASK-185. Orquesta catalogo, opciones y consultas de reportes avanzados sobre servicios duenos de datos; las proyecciones asincronas reconstruibles siguen correspondiendo a `reporting-projection-lambda` cuando el flujo event-driven se materialice.

## Marca NexoFiscal, branding y documentos

- La marca publica objetivo de la aplicacion es `NexoFiscal`.
- `tenant-service` sera el owner de la configuracion de branding empresarial: nombre visual, logos, favicon y metadata de assets.
- El frontend debe aplicar branding por empresa activa y usar fallback `NexoFiscal` si no existe logo empresarial.
- `billing-service` sera el owner de artefactos POS/documentos fiscales: comprobante imprimible, XML/JSON tecnico cuando aplique, QR, hash y metadata de almacenamiento.
- Reimpresiones POS se tratan como eventos operativos auditables; no crean nuevos documentos fiscales.

## Reportes avanzados objetivo

- El modulo actual de reportes minimos se mantiene sobre servicios duenos de datos.
- `reporting-service` es el owner de reportes avanzados, catalogo de reportes, filtros dinamicos, ejecuciones, exportaciones y descargas.
- El BFF no ejecuta agregaciones pesadas; enruta, autoriza, normaliza errores y conserva borde publico.
- `reporting-projection-lambda` sigue siendo una proyeccion reconstruible, no la fuente canonica unica.

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

La comunicacion operacional usa REST sincrono para comandos/consultas inmediatas y eventos Outbox/Inbox para efectos posteriores, auditoria, proyecciones y reintentos. No se usaran brokers self-hosted; el destino productivo aprobado es AWS EventBridge/SQS + Lambda.

## Orden recomendado de extraccion fisica

1. `tenant-service`, porque crea la frontera real de empresa.
2. `catalog-service` y `thirdparty-service`, porque reducen dependencias legacy y alimentan ventas/compras.
3. `inventory-service`, porque debe controlar stock, costos y kardex. Implementado en TASK-034.
4. `dian-provider-service`, porque aisla el mock y prepara la conexion DIAN configurable por empresa.
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

El orden aprobado y documentado de la fase actual es:

1. Completar logica de negocio backend: clientes/adquirentes fiscales, proveedores, NIT con digito de verificacion automatico, bienes, servicios, insumos, movimientos manuales, compras, gastos, cuentas por pagar, reportes, usuarios/roles y licencias.
2. Migrar el legacy pendiente al modelo Clean Architecture y microservicios existentes, manteniendo compatibilidad hasta aprobar ruptura.
3. Eliminar codigo, endpoints y tablas legacy solo despues de matriz de reemplazo, E2E aprobado y verificacion de referencias.
4. Definir y validar la arquitectura cloud AWS objetivo: Frontend CloudFront/S3, API Gateway/BFF, microservicios ECS Fargate, eventos EventBridge/SQS y Lambdas. Cerrado por TASK-142.
5. Implementar Outbox/Inbox y consumidores Lambda para desacoplar efectos posteriores, auditoria, reportes y reintentos. Cerrado por TASK-143.

La infraestructura event-driven no reemplaza las validaciones sincronicas criticas. Ventas, stock, licencia, RBAC y reglas fiscales se validan antes de confirmar comandos; los eventos materializan efectos posteriores, reportes, auditoria y reintentos con idempotencia.


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
  - Mantiene el mock local y evoluciona como conector real DIAN parametrizable por empresa.
  - No se crea un microservicio nuevo para DIAN real; se agregan puertos/adaptadores internos para XML UBL, CUFE/CUDE, QR, firma, validacion tecnica, transporte, respuestas y artefactos.
  - Cada empresa usa su propia configuracion, certificado y secretos. La plataforma no opera como proveedor tecnologico DIAN.
- `accounting-service`
- `audit-service`
- `reporting-service` cuando se materialice como servicio fisico
- `payroll-service`

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

Cada Lambda debe consumir eventos con `eventId`, `companyId`, `correlationId`, `source`, `type`, `payloadVersion` e `idempotencyKey`, y registrar Inbox/estado de procesamiento cuando escriba datos propios. Los payloads no deben contener secretos, certificados, PIN, claves DIAN, passwords, cookies ni tokens.

## Seguridad productiva

- Amazon Cognito Hosted UI + Authorization Code Grant + PKCE es el target productivo de autenticacion.
- La SPA productiva no captura passwords ni almacena access/refresh/id tokens.
- El BFF intercambia codigo OAuth, crea sesion server-side cifrada y entrega cookie opaca `HttpOnly`, `Secure`, `SameSite`.
- Mutaciones autenticadas por cookie requieren CSRF.
- ROOT y administradores requieren MFA en produccion.
- CloudFront/BFF deben emitir HSTS, CSP, `X-Content-Type-Options`, proteccion anti-frame y `Referrer-Policy`.
- Secrets Manager/KMS administran secretos de aplicacion y secretos DIAN por empresa.
- Las empresas clientes son responsables de su habilitacion/certificacion DIAN y de configurar sus parametros; la plataforma no opera como proveedor tecnologico DIAN.

## Recomendacion de migracion incremental

1. Corregir configuracion sensible.
2. Introducir paquetes Clean Architecture dentro del proyecto actual.
3. Implementar `billing` como modulo nuevo sin romper CRUD existente.
4. Refactorizar modulos CRUD existentes de forma incremental hacia la misma estructura usada por `billing`.
5. Usar `Categoria` como piloto de refactor por ser un modulo pequeno y de bajo riesgo.
6. Separar `dian-provider` como servicio tecnico de conexion DIAN/mock configurable por empresa.
7. Extraer inventario y contabilidad cuando contratos esten estables.
8. Mantener pruebas de contrato durante la extraccion.
9. Ejecutar flujo end-to-end desde cero antes de eliminar cualquier elemento legacy.

## Refactorizacion de modulos existentes

Los paquetes legacy `controller`, `service`, `repository`, `models`, `DTO` y `mappers` se migraran gradualmente hacia bounded contexts alineados con Clean Architecture.

Orden recomendado:

1. `catalog`: `Categoria`, `Producto`, `Impuesto`, `Pais`, `MetodoPago`, `TipoDocumento`, `TipoGasto`.
2. `thirdparty`: `Cliente`, `Proveedor`.
3. `inventory`: stock, productos inventariables, movimientos fisicos y compras documentales sin aumento automatico de inventario.
4. `billing`: facturas, POS, numeracion, configuracion/conexion DIAN y documentos electronicos.
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
- La conexion DIAN real depende de configuracion, habilitacion/certificacion y certificados de cada empresa cliente.
- Fase 20 cierra el backlog DIAN real en `TASK-145` a `TASK-163`; reportes asincronos avanzados no se ejecutan antes de ese cierre.
- La normatividad cambia y requiere mantenimiento continuo.
- El modelo contable debe ser validado por contador.

## Decision TASK-250 a TASK-258

La fase 34 queda documentada como implementada para cerrar inconsistencias de UX y dominio antes de seguir ampliando funcionalidades.

- `inventory-service` mantiene propiedad sobre productos, stock y kardex; productos pueden actualizarse/inactivarse sin borrar historicos.
- `billing-service` debe tratar el QR como parte del artefacto fiscal/imprimible y renderizarlo graficamente desde contenido mock o DIAN real.
- `inventory-service` conserva compras documentales como control financiero de proveedor, sin aumentar stock.
- `accounting-service` conserva gastos y deudores; gastos se simplifican a total-only y deudores no deben producir `500` por reglas de negocio esperadas.
- `tenant-service` es el candidato natural para centralizar metadata de archivos empresariales porque ya administra empresa y branding; otros servicios guardan referencias privadas.
- El BFF sigue siendo el borde publico para autorizacion, auditoria, rutas de archivo y errores funcionales.
- El frontend debe mantener formularios controlados y estados claros: crear/actualizar producto, evidencia opcional, color picker y errores funcionales visibles.

## Decision DIAN SOAP WCF habilitacion

La conexion SOAP real DIAN se mantiene dentro de `dian-provider-service`; no se crea un microservicio nuevo. El servicio agrega un adaptador especializado de transporte SOAP WCF detras de `DianTransportPort`, separado de los modos `mock`, `stub` y `http` de referencia.

Decisiones:

- El endpoint de habilitacion objetivo es `https://vpfe-hab.dian.gov.co/WcfDianCustomerServices.svc`.
- El WSDL objetivo es `https://vpfe-hab.dian.gov.co/WcfDianCustomerServices.svc?wsdl` o `?singleWsdl`.
- Operaciones iniciales: `SendTestSetAsync`, `GetStatusZip`, `GetStatus`, `SendBillSync`, `SendBillAsync` y posteriormente `GetNumberingRange` si se aprueba sincronizar resoluciones.
- El certificado empresarial se configura como archivo `.p12` o `.pfx`, nunca como textarea ni valor persistido en DB.
- Apache CXF/WSS4J queda como candidato tecnico para cliente SOAP, JAX-WS y WS-Security X.509.
- La caja de herramientas DIAN local se usa como fuente de validacion tecnica y fixtures sanitizados; no debe convertirse en dependencia runtime con artefactos innecesarios.

## Decision TASK-261 a TASK-272

La fase 35 se documenta como preparacion priorizada para salida comercial. No cambia la arquitectura vigente hasta aprobacion de implementacion.

Responsabilidades objetivo:

- `bff-service`: compone readiness empresarial, aplica seguridad de borde, normaliza errores funcionales, evita exponer servicios internos y sirve como entrada publica para auditoria, reportes, descargas e impresion.
- `tenant-service`: mantiene empresa, licencia, branding, metadata de archivos, estado de storage y configuraciones empresariales transversales.
- `dian-provider-service`: conserva el flujo mock y real DIAN por empresa, con generacion/firma/transporte/consulta de documentos fiscales y sin convertir a NexoFiscal en proveedor tecnologico.
- `billing-service`: coordina cierre de venta, documento fiscal, comprobante imprimible, QR fiscal, historico de ventas y datos base para reportes.
- `accounting-service`: mantiene reglas, cuentas, asientos, gastos, compras financieras, deudores, cuentas por pagar/cobrar y readiness contable.
- `reporting-service`: produce datasets normalizados, exportaciones y jobs pesados sin devolver JSON tecnico a la UI.
- `audit-service`: persiste eventos auditables sanitizados y expone busquedas filtradas segun rol/empresa.
- SPA NexoFiscal: presenta flujos guiados, dashboards, formularios controlados, graficas y acciones claras; no decide seguridad ni autorizacion final.

Prioridad arquitectonica:

1. P0: readiness empresarial, hardening de seguridad y DIAN real.
2. P1: onboarding contable, reportes gerenciales, auditoria visible y CI/CD.
3. P2: impresion termica con hardware real, gestion financiera diaria, storage robusto y observabilidad avanzada.

Riesgos:

- La impresion termica depende de navegador, drivers o agente local y debe validarse con hardware real.
- DIAN real depende de certificados y proceso de habilitacion por empresa.
- El readiness no debe duplicar reglas de negocio; debe consultar capacidades de dominio o endpoints internos estables.
- Las metricas y logs deben evitar datos sensibles, especialmente certificados, tokens, PIN y payloads fiscales completos.
