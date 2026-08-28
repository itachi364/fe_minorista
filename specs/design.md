# Design: Backend Clean Architecture basado en microservicios

## Decision tecnica

Se usara Clean Architecture dentro de una estrategia basada en microservicios. Cada microservicio debe separar dominio, casos de uso, puertos, adaptadores y configuracion framework.

La unidad de despliegue aprobada es el microservicio por bounded context. No se creara un artefacto o contenedor por endpoint individual. Cada endpoint debe pertenecer al microservicio que representa su capacidad de negocio.

## Estado actual versus objetivo

Estado implementado y desplegable localmente:

- Microservicios Spring Boot: `bff-service`, `tenant-service`, `identity-service`, `catalog-service`, `thirdparty-service`, `inventory-service`, `billing-service`, `dian-provider-service`, `accounting-service`, `audit-service`, `payroll-service` y `reporting-service`.
- Lambdas Java implementadas como artefactos Maven: `audit-event-writer-lambda`, `inventory-sale-effect-lambda`, `accounting-sale-entry-lambda`, `provider-submission-retry-lambda` y `reporting-projection-lambda`.
- Autenticacion local/transitoria: `POST /api/v1/auth/login` con token opaco Bearer, limitada a desarrollo/E2E.
- DIAN local/transitorio: `dian-provider-service` conserva modo mock para E2E local y agrega pipeline real configurable por empresa en modo `stub/http`.
- Reportes actuales: `reporting-service` orquesta catalogo/opciones/query de reportes avanzados y consume endpoints de servicios duenos; las proyecciones asincronas siguen en `reporting-projection-lambda`.

Objetivo pendiente:

- Cognito Hosted UI + PKCE, sesiones BFF server-side, cookies `HttpOnly`, CSRF, MFA y bloqueo productivo del login dummy: TASK-164 a TASK-174.
- Produccion DIAN certificada por empresa con certificado real, URLs oficiales y fixtures de habilitacion aprobados; la base funcional configurable de Fase 20 `TASK-145` a `TASK-163` ya esta implementada.
- OpenAPI versionado por servicio/BFF como artefacto controlado; Springdoc solo habilita documentacion runtime.

## Microservicios implementados y objetivo

- `bff-service`: frontera publica de la SPA, autorizacion de borde, normalizacion de errores y ruteo.
- `tenant-service`: empresas, licencias y estado del tenant.
- `identity-service`: usuarios, roles, autenticacion, autorizacion y auditoria base.
- `thirdparty-service`: clientes, proveedores, tipos de documento y validaciones de identificacion.
- `catalog-service`: paises, impuestos, metodos de pago, parametros fiscales y catalogos DIAN.
- `inventory-service`: productos, categorias, stock, kardex y movimientos.
- `billing-service`: factura electronica, POS electronico, notas, resoluciones, numeracion, CUFE/CUDE, estados fiscales.
- `dian-provider-service`: conector DIAN interno, mock local y conexion real configurable por empresa; no representa una oferta de proveedor tecnologico DIAN de la plataforma.
- `accounting-service`: plan de cuentas, comprobantes, asientos, libro diario y libro mayor.
- `audit-service`: auditoria fiscal, tecnica y consultas de logs.
- `payroll-service`: trabajadores, pagos diarios verbales y nomina electronica mock opcional.
- `reporting-projection-lambda`: proyecciones reconstruibles desde eventos canonicos.

## Estructura Clean Architecture por microservicio

```text
<service>
  src/main/java/.../<service>
    domain/
      model/
      valueobject/
      rule/
      event/
    application/
      usecase/
      port/in/
      port/out/
      dto/
    infrastructure/
      persistence/
      provider/
      messaging/
      config/
    interfaces/
      rest/
      mapper/
      error/
```

## Componentes principales

### billing-service

- Gestiona resoluciones de numeracion.
- Crea documentos fiscales.
- Calcula totales e impuestos.
- Orquesta inventario, contabilidad y conexion DIAN.
- Mantiene estados del ciclo fiscal.

Estado TASK-041:

- `billing-service` fisico queda creado para ventas POS.
- `POST /api/v1/issuers` y `GET /api/v1/issuers/current` gestionan el emisor fiscal activo por empresa.
- `POST /api/v1/numbering-resolutions` y `GET /api/v1/numbering-resolutions` gestionan resoluciones por empresa, tipo de documento, ambiente, rango y vigencia.
- `POST /api/v1/sales` calcula totales por linea y valida stock contra `inventory-service`.
- `POST /api/v1/sales/{saleId}/confirm` exige emisor activo, configuracion DIAN empresarial activa, asigna numeracion desde resolucion vigente, genera documento electronico POS y envia la solicitud por HTTP a `dian-provider-service`.
- TASK-037 agrego efectos automaticos posteriores a validacion: `SALE_OUT` contra `inventory-service` y asiento `SALE_CONFIRMED` contra `accounting-service`.
- `electronic_document.inventory_applied_at` y `electronic_document.accounting_applied_at` registran aplicacion idempotente de efectos posteriores.

### Politica de calculo fiscal inicial

- El calculo se realiza por linea.
- La base gravable de cada linea corresponde a `cantidad * precio_unitario - descuento`.
- Los descuentos se aplican antes del impuesto.
- Todos los valores monetarios se redondean a 2 decimales con `HALF_UP`.
- El subtotal, impuestos, descuentos y total del documento corresponden a la suma de los valores ya calculados por linea.
- Los ajustes especificos que exija el anexo tecnico DIAN o el modo de operacion configurado por la empresa deberan documentarse antes de modificar esta politica.

### dian-provider-service

- Expone un contrato interno estable para emitir documentos.
- Encapsula detalles tecnicos de la conexion DIAN por empresa.
- Normaliza respuestas tecnicas.
- Maneja timeouts, reintentos, idempotencia y errores externos.
- No presta ni comercializa servicio de proveedor tecnologico DIAN; es un componente tecnico del software parametrizable.
- En modo real debe resolver configuracion por `companyId`, validar que la empresa haya configurado certificado/credenciales/resoluciones requeridas y evitar cualquier certificado global compartido.

Estado TASK-036:

- `dian-provider-service` fisico queda creado con Clean Architecture y persistencia propia.
- Expone `POST /api/v1/provider/electronic-pos`, `POST /api/v1/provider/electronic-invoices` y `GET /api/v1/provider/submissions/{trackingId}`.
- En modo local solo soporta `DIAN_PROVIDER_MODE=mock`; cualquier modo distinto falla de forma explicita.
- Persiste los envios mock en `dian_provider.provider_submission` sin credenciales ni secretos reales.
- `billing-service` consume el mock por HTTP mediante `DIAN_PROVIDER_SERVICE_URL`.

### Politica de configuracion DIAN por empresa

- Cada empresa facturadora es responsable de registrarse, habilitarse y certificarse ante DIAN segun el modo de operacion que declare.
- La plataforma se presenta como software parametrizable para conexion DIAN por empresa, no como proveedor tecnologico DIAN.
- La configuracion DIAN pertenece a una sola empresa y queda aislada por `company_id`.
- La configuracion debe soportar al menos `MOCK` para desarrollo/E2E y `SOFTWARE_PROPIO_CLIENTE` para el modo objetivo donde la empresa opera su propio software parametrizado.
- La UI debe mostrar una declaracion clara antes de activar modo real: la empresa es responsable de su proceso DIAN, certificado, software ID/PIN, resoluciones, rangos y cumplimiento normativo.
- Certificados, PIN tecnico, claves, tokens y credenciales se almacenan fuera de base de datos como secretos cifrados o referencias seguras. La DB solo guarda alias, huella, vencimiento, estado, referencias y metadata no sensible.
- Toda carga, actualizacion, prueba, activacion, inactivacion y uso de configuracion DIAN registra auditoria segura sin exponer secretos ni payloads completos.
- El modo real debe bloquear emision cuando la configuracion este incompleta, vencida, inactiva, no probada o no habilitada.
- La integracion productiva final debe validar XML UBL, firma, CUFE/CUDE, QR, AttachedDocument, ApplicationResponse y reglas XSD/Schematron vigentes antes de salir a produccion.

### Diseno de cierre DIAN real

El `dian-provider-service` evoluciona como el mismo microservicio tecnico, no como un servicio nuevo. Para mantener Clean Architecture y SOLID, el caso de uso de envio real depende de puertos pequenos y reemplazables:

- `FiscalDocumentXmlBuilderPort`: construye XML UBL 2.1 desde snapshots fiscales canonicos.
- `DianIdentifierCalculationPort`: calcula CUFE/CUDE y contenido QR.
- `DianSignaturePort`: firma XML con certificado empresarial resuelto desde secretos.
- `DianTechnicalValidationPort`: valida XSD, Schematron y listas de codigos.
- `DianTransportPort`: transmite a DIAN en habilitacion o produccion segun configuracion empresarial.
- `DianResponseMapperPort`: normaliza `ApplicationResponse`, rechazos, tracking y errores.
- `FiscalArtifactStoragePort`: almacena XML firmado, ZIP/AttachedDocument, QR, representacion grafica y respuesta DIAN.

Flujo objetivo:

1. `billing-service` confirma venta/documento y envia un snapshot fiscal canonico con `companyId`, `documentId`, tipo documental e `Idempotency-Key`.
2. `dian-provider-service` resuelve configuracion activa de la empresa y valida ambiente, certificado, secretos, resolucion y estado de pruebas.
3. Se construye XML UBL 2.1 para factura electronica, documento equivalente electronico POS o notas fiscales.
4. Se calcula CUFE/CUDE y QR segun el anexo vigente y la configuracion tecnica de la empresa.
5. Se firma el XML con certificado empresarial sin exponer PIN, certificado ni claves.
6. Se ejecuta validacion tecnica local XSD/Schematron/listas de codigos; si falla, no hay transporte.
7. Se transmite a DIAN usando el endpoint de habilitacion o produccion de la empresa.
8. Se normaliza y persiste respuesta DIAN, `ApplicationResponse`, tracking y estado final/reintentable.
9. Se almacenan artefactos fiscales privados con hash y metadata.
10. `billing-service` actualiza el estado del documento sin duplicar efectos de inventario ni contabilidad.

Reglas de seguridad:

- El modo real nunca hace fallback a mock.
- Los payloads completos DIAN no se imprimen en logs ni auditoria.
- Los artefactos fiscales se consultan por BFF/RBAC; el navegador no recibe rutas internas de storage.
- La vigencia normativa debe verificarse nuevamente antes de activar produccion real para una empresa.

Fuentes oficiales consultadas el 2026-08-24:

- DIAN Documentacion Tecnica: anexos tecnicos, OASIS UBL 2.1 y caja de herramientas.
- DIAN Resolucion 000165 de 2023: sistema de facturacion, anexo tecnico de factura electronica de venta y documento equivalente electronico.
- DIAN Resolucion 000202 de 2025: modificaciones sobre factura electronica/documento equivalente, transmision/validacion y consumidor final.

### inventory-service

- Administra productos y stock.
- Registra movimientos inmutables.
- Expone disponibilidad de productos.

### accounting-service

- Genera asientos desde eventos fiscales.
- Valida partida doble.
- Expone libro diario y mayor.
- TASK-037 extrae el codigo contable Clean Architecture desde el monolito legacy a `services/accounting-service` como artefacto Spring Boot independiente.
- Expone `POST /api/v1/accounts`, `POST /api/v1/accounting-rules`, `POST /api/v1/accounting-entries`, `GET /api/v1/reports/journal` y `GET /api/v1/reports/ledger`.
- La generacion de asientos es idempotente por `companyId`, `sourceType` y `sourceId`.

### audit-service

- TASK-042 extrae auditoria fiscal/tecnica a `services/audit-service` como microservicio fisico con Clean Architecture.
- Expone `POST /api/v1/audit-events` y `GET /api/v1/audit-events`.
- Persiste eventos en `audit.audit_event` aislados por `company_id`.
- Registra `event_type`, `resource_type`, `resource_id`, `action`, `result`, `user_id`, `detail` seguro y `occurred_at`.
- TASK-043 conecta `billing-service` como primer productor automatico mediante REST sincrono best-effort para `ELECTRONIC_DOCUMENT`/`SALE`/`CONFIRM_SALE`.
- La integracion automatica con inventario, contabilidad, auditoria, reintentos y reportes ya tiene contratos event-driven con Outbox/Inbox y destino productivo EventBridge/SQS + Lambda; en local puede operar de forma sincronica/idempotente o con dispatcher deshabilitado segun configuracion.

## Comunicacion inicial entre microservicios

- La primera version fisica usara REST sincrono entre servicios.
- `X-Correlation-Id` debe propagarse entre servicios.
- `X-Company-Id` debe propagarse en toda operacion de negocio.
- `Idempotency-Key` sera obligatorio en comandos fiscales, inventario y contabilizacion automatica.
- Los errores entre servicios deben usar el contrato estandar definido en `specs/api-contract.md`.
- Los eventos `SaleConfirmed`, `ElectronicDocumentValidated`, `InventoryMovementRegistered`, `AccountingEntryPosted` y `AuditEventRequested` ya tienen contrato canonico, registro durable en Outbox local y dispatcher condicional hacia EventBridge. `AuditEventRequested` ya tiene consumidor Lambda inicial con Inbox real; los demas consumidores se implementan por lotes dentro de TASK-062.

## Politica de arranque local de microservicios

- En Docker Compose ningun microservicio debe depender del arranque o healthcheck de otro microservicio.
- La unica dependencia de arranque permitida para los servicios de aplicacion es `postgres`, porque los servicios requieren base de datos para migraciones, persistencia y lectura de datos.
- Las relaciones REST entre microservicios son dependencias de runtime de casos de uso especificos, no dependencias de arranque del contenedor.
- Si un microservicio par no esta disponible, el servicio llamador debe mantenerse iniciado y responder con error controlado cuando se invoque el caso de uso que requiere esa integracion.
- La prueba E2E y los scripts operativos son responsables de esperar la salud de cada servicio requerido antes de ejecutar el flujo completo.

## Autenticacion y sesion productiva

La arquitectura productiva aprobada elimina contrasenas y tokens reutilizables del navegador. La SPA no debe manejar directamente passwords, `accessToken`, `refreshToken`, `idToken` ni bearer tokens internos.

Flujo objetivo:

1. La SPA solicita al BFF una URL de login: `GET /api/v1/auth/login-url`.
2. El BFF genera `state`, `nonce` y PKCE `code_verifier`/`code_challenge`, conserva el verificador server-side temporalmente y devuelve/redirige a Cognito Hosted UI.
3. El usuario ingresa credenciales y MFA en Cognito Hosted UI o proveedor de identidad aprobado.
4. Cognito redirige al BFF en `/api/v1/auth/callback?code=&state=`.
5. El BFF valida `state`, intercambia el codigo por tokens y obtiene claims minimos de Cognito.
6. El BFF solicita a `identity-service` una sesion interna para el usuario local activo asociado al `sub` Cognito persistente. Si el `sub` aun no esta vinculado, `identity-service` solo puede enlazarlo una vez contra un usuario activo previamente provisionado por correo; no se autocrean usuarios durante login.
7. El BFF cifra tokens server-side, crea una sesion opaca y responde con cookie `HttpOnly`, `Secure`, `SameSite=Lax` o `Strict`, expiracion corta y token CSRF no sensible cuando aplique.
8. La SPA consulta `GET /api/v1/auth/session` para conocer usuario, empresas, permisos resumidos, licencia y expiracion funcional sin recibir tokens.
9. En cada request, el BFF resuelve la cookie opaca contra su sesion server-side, valida CSRF para mutaciones y propaga `Authorization` interno y `X-User-Id` hacia microservicios sin exponer tokens al navegador.
10. Logout ejecuta `POST /api/v1/auth/logout`, invalida sesion server-side, limpia cookie y revoca tokens Cognito cuando aplique.

Reglas:

- En produccion, el login propio `POST /api/v1/auth/login` queda deshabilitado o no expuesto publicamente; solo puede mantenerse para desarrollo local controlado.
- El almacenamiento server-side de sesion puede implementarse inicialmente en PostgreSQL bajo un schema de BFF o en un store administrado equivalente. Los tokens deben cifrarse con KMS o envelope encryption antes de persistir.
- El navegador solo conserva estado no sensible de UI; no se guardan tokens ni passwords en `sessionStorage`, `localStorage`, IndexedDB o variables globales.
- La cookie de sesion no es legible por JavaScript por `HttpOnly`.
- Los endpoints mutables con cookie requieren proteccion CSRF.
- ROOT y administradores empresariales requieren MFA en produccion.
- El BFF debe registrar auditoria segura de login, callback, logout, refresh, acceso denegado, CSRF invalido y cambios de sesion sin registrar tokens ni cookies.
- CloudFront/BFF deben aplicar HSTS, CSP, `X-Content-Type-Options`, proteccion anti-frame y `Referrer-Policy`.
- Las builds productivas no deben publicar sourcemaps sin control ni contener logs de depuracion sensibles.

## Mensajeria asincrona objetivo

- La opcion productiva objetivo para eventos asincronos en AWS sera Outbox/Inbox con publicacion hacia EventBridge/SQS y consumidores Lambda idempotentes.
- La infraestructura event-driven se implementa por fases: contratos y Outbox local; publicadores EventBridge/SQS; consumidores Lambda con Inbox/idempotencia. Los primeros consumidores implementados son `audit-event-writer-lambda` para `AuditEventRequested`, `inventory-sale-effect-lambda` para efectos de inventario desde `SaleConfirmed` y `accounting-sale-entry-lambda` para asientos contables desde `SaleConfirmed` y `provider-submission-retry-lambda` para reintentos tecnicos de proveedor desde `ProviderSubmissionFailed`.
- La migracion asincrona debe usar patron Outbox/Inbox para publicar y consumir eventos sin perder operaciones cuando un servicio, cola, bus o consumidor no este disponible temporalmente.
- Los consumidores deben ser idempotentes por `companyId`, tipo de evento, recurso origen e identificador de evento.
- Los eventos objetivo iniciales son `SaleConfirmed`, `ElectronicDocumentValidated`, `InventoryMovementRegistered`, `AccountingEntryPosted`, `AuditEventRequested` y `ProviderSubmissionFailed`.
- Los flujos HTTP sincronicos que permanezcan despues de introducir eventos deberan evaluarse con timeouts, reintentos controlados y circuit breaker cuando apliquen.
- La programacion reactiva no es prioridad inicial; solo se evaluara si aparece una necesidad concreta de concurrencia o streaming que no pueda resolverse con el modelo actual.
- No se usara broker self-hosted; la mensajeria productiva se implementara con servicios administrados AWS.

## Flujo end-to-end objetivo antes de depuracion legacy

1. `tenant-service` crea la empresa y devuelve `companyId`.
2. `catalog-service` expone catalogos oficiales y configuraciones fiscales base.
3. `thirdparty-service` crea cliente y proveedor aislados por empresa.
4. `billing-service` configura emisor y resolucion fiscal para la empresa.
5. `accounting-service` crea cuentas PUC y reglas contables parametrizadas por empresa.
6. `inventory-service` crea productos con costo y registra compra o ajuste inicial de stock.
7. `billing-service` crea venta POS/factura para productos existentes.
8. `billing-service` consulta disponibilidad en `inventory-service`.
9. `billing-service` asigna prefijo/consecutivo desde resolucion vigente, valida la configuracion DIAN empresarial, emite el documento electronico y lo envia a `dian-provider-service`.
10. `dian-provider-service` responde mediante mock local deterministico en desarrollo o mediante la conexion DIAN configurada por empresa cuando el modo real este aprobado.
11. Si el documento queda aceptado, `billing-service` solicita a `inventory-service` registrar `SALE_OUT`.
12. `billing-service` solicita a `accounting-service` generar asiento contable desde la regla aprobada.
13. `billing-service` registra en `audit-service` la trazabilidad fiscal/tecnica de la confirmacion de venta y documento electronico.
14. La prueba E2E verifica por API y PostgreSQL empresa, configuraciones, inventario, documento, envio mock, asiento y auditoria central.

## Politica de consistencia inicial entre servicios

- El flujo local usara orquestacion sincrona desde `billing-service`.
- La afectacion de inventario y contabilidad debe ser idempotente por `company_id`, `source_type` y `source_id`.
- Si una llamada posterior a la validacion fiscal falla, el sistema debe registrar el error y permitir reintento seguro sin duplicar numeracion, movimientos ni asientos.
- La implementacion asincrona queda diferida hasta cerrar el flujo core y aprobar la tarea de Outbox/Inbox con EventBridge/SQS, consumidores Lambda, contratos de eventos, reintentos y DLQ.

## Flujo de factura electronica

1. Cliente solicita crear factura.
2. `billing-service` valida cliente, productos, resolucion y reglas fiscales.
3. `billing-service` calcula totales e impuestos.
4. `billing-service` reserva o asigna numeracion.
5. `billing-service` solicita emision a `dian-provider-service`.
6. `dian-provider-service` envia a la conexion DIAN configurada para la empresa facturadora.
7. `billing-service` registra estado y artefactos: CUFE, QR, XML, PDF o representacion grafica.
8. `inventory-service` descuenta stock cuando el documento alcance el estado aprobado.
9. `accounting-service` registra asiento contable.

## Flujo de POS electronico

1. Punto de venta solicita emision POS.
2. `billing-service` valida caja, resolucion POS, productos y adquirente si aplica.
3. `billing-service` calcula totales, impuestos y CUDE usando prefijo/consecutivo autorizado.
4. `billing-service` envia el documento equivalente electronico a la conexion DIAN configurada para la empresa.
5. Se registran estado, CUDE, QR, XML y representacion.
6. Inventario y contabilidad se actualizan segun politica transaccional aprobada.

## Estados sugeridos

- `DRAFT`
- `CALCULATED`
- `NUMBER_ASSIGNED`
- `SENT_TO_PROVIDER`
- `VALIDATED`
- `REJECTED`
- `FAILED`
- `CONTINGENCY`
- `CANCELLED_BY_NOTE`
- `ADJUSTED`

### Transiciones iniciales de estado

- `DRAFT` -> `CALCULATED`: documento calculado.
- `CALCULATED` -> `NUMBER_ASSIGNED`: numeracion fiscal asignada.
- `NUMBER_ASSIGNED` -> `SENT_TO_PROVIDER`: documento enviado al conector DIAN configurado.
- `SENT_TO_PROVIDER` -> `VALIDATED`: DIAN o el modo configurado acepta o valida el documento; se registran CUFE/CUDE, QR, XML y representacion grafica cuando existan.
- `SENT_TO_PROVIDER` -> `REJECTED`: DIAN o el modo configurado rechaza el documento; se conservan codigo y mensaje seguro de rechazo.
- `SENT_TO_PROVIDER` -> `FAILED`: fallo tecnico de envio o respuesta no procesable.
- `FAILED` -> `CONTINGENCY`: la operacion entra en manejo de contingencia aprobado.
- `VALIDATED` -> `CANCELLED_BY_NOTE`: anulacion mediante nota permitida.
- `VALIDATED` -> `ADJUSTED`: ajuste mediante nota permitida.

Toda transicion fiscal debe registrar evento de trazabilidad con estado anterior, estado nuevo, usuario, fecha, accion, resultado y detalle seguro. Las transiciones no listadas se consideran invalidas hasta que una especificacion posterior las apruebe.

### Politica inicial de notas credito/debito

- Una nota credito o debito solo puede crearse referenciando una factura electronica validada.
- Una nota POS se manejara aparte como nota de ajuste POS.
- La nota debe incluir motivo obligatorio y valores monetarios positivos.
- En esta fase inicial, `total = subtotal + tax_total`.
- La nota nace en estado `DRAFT`; numeracion, envio al proveedor, inventario y contabilidad se ejecutaran en tareas posteriores.
- Una factura validada no debe modificarse directamente; cualquier correccion fiscal debe modelarse mediante nota credito o debito.

### Politica inicial de POS electronico

- El documento POS electronico se modela como `ELECTRONIC_POS`.
- Debe tener numeracion fiscal asignada mediante resolucion vigente.
- Debe calcular totales con la politica fiscal definida para documentos electronicos.
- Debe permitir datos de adquirente cuando el comprador requiera soporte fiscal.
- El CUDE inicial se genera como hash deterministico de los datos fiscales principales del documento para pruebas internas.
- La generacion final de CUDE debe ajustarse al Anexo Tecnico de Documento Equivalente Electronico vigente y a la respuesta DIAN/modo configurado cuando se implemente el adaptador real.
- El documento POS nace en `NUMBER_ASSIGNED`; el envio al conector DIAN y la validacion pasan por las tareas de conexion DIAN y trazabilidad.

### Politica de prueba end-to-end local de facturacion

- Para habilitar pruebas locales completas, el backend debe exponer endpoints REST y persistencia PostgreSQL para configurar emisor, configurar resoluciones, emitir POS electronico, enviar el documento a un conector DIAN mock y consultar el resultado.
- El conector DIAN mock debe ser deterministico, no debe hacer llamadas externas y no debe requerir credenciales reales.
- En modo local, el mock puede devolver respuestas simuladas `ACCEPTED` o `REJECTED` usando parametros de request o configuracion local segura.
- El modo local se configura con `DIAN_PROVIDER_MODE=mock`. La configuracion real puede validar la presencia de artefactos tecnicos DIAN, pero el envio certificado real sigue fallando cerrado hasta implementar generacion XML, firma, validacion completa y transporte DIAN para evitar una falsa integracion productiva.
- El resultado simulado se configura con `DIAN_MOCK_DEFAULT_STATUS`, usando `ACCEPTED` por defecto y permitiendo `REJECTED` o `FAILED` para pruebas negativas.
- Los errores simulados pueden configurarse con `DIAN_MOCK_ERROR_CODE` y `DIAN_MOCK_ERROR_MESSAGE`; cuando no se definan, el mock debe usar mensajes seguros predeterminados sin secretos.
- Una respuesta `ACCEPTED` del mock debe registrar tracking ID, CUFE/CUDE simulado, QR simulado y artefactos dummy para permitir validar el flujo de persistencia y consulta.
- Esta politica no sustituye la integracion real con DIAN ni valida cumplimiento tecnico final del anexo DIAN; solo habilita pruebas funcionales internas hasta configurar certificado, software ID/PIN, credenciales, resoluciones y proceso de habilitacion por empresa.
- Los endpoints locales deben requerir `X-Company-Id` para mantener aislamiento multiempresa desde la primera prueba.

### Politica inicial de nota de ajuste POS

- Una nota de ajuste POS solo puede crearse referenciando un POS electronico emitido por la misma empresa.
- La nota de ajuste POS se modela como `POS_ADJUSTMENT_NOTE`.
- La nota debe indicar si corresponde a anulacion (`CANCELLATION`) o correccion (`CORRECTION`).
- La nota debe recibir numeracion fiscal propia mediante una resolucion vigente para `POS_ADJUSTMENT_NOTE`.
- La nota no puede reutilizar el mismo prefijo y numero fiscal del POS original.
- La nota nace en estado `NUMBER_ASSIGNED`; el envio al proveedor y la validacion pasan por las tareas de proveedor y trazabilidad.

### Politica inicial de movimientos de inventario

- El stock se modela por empresa y producto mediante `StockBalance`.
- Los movimientos `PURCHASE_IN`, `RETURN_IN` y `ADJUSTMENT_IN` incrementan el stock actual.
- Los movimientos `SALE_OUT` y `ADJUSTMENT_OUT` disminuyen el stock actual.
- Todo movimiento debe registrar empresa, producto, cantidad, stock anterior, stock resultante, documento origen, usuario y fecha.
- El dominio no permite que un movimiento deje el stock resultante en valor negativo.
- La disponibilidad para venta se calcula como `currentStock - reservedStock`.
- Una solicitud de venta debe rechazarse cuando la cantidad solicitada supera la disponibilidad calculada, salvo configuracion futura explicitamente aprobada.

### Politica objetivo de terceros fiscales

- El modelo objetivo consolida clientes y proveedores como terceros fiscales por empresa.
- Un tercero puede cumplir rol de cliente, proveedor o ambos sin duplicar su identificacion.
- Los datos minimos son tipo de persona, tipo de documento, numero de documento, digito de verificacion cuando aplique, nombre completo o razon social, contacto, direccion y estado.
- El tipo de persona sera `NATURAL` o `JURIDICA`.
- Para `identificationTypeCode=31` (NIT), el digito de verificacion se calcula automaticamente desde el numero base mediante el algoritmo DIAN: pesos de derecha a izquierda `3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71`; se suma cada digito multiplicado por su peso; si el residuo modulo 11 es 0 el DV es 0, si es 1 el DV es 1, en otros casos el DV es `11 - residuo`.
- Para tipos de documento distintos a NIT, el digito de verificacion debe quedar nulo o vacio.
- El servicio de terceros debe exponer el DV calculado en la respuesta y rechazar inconsistencias cuando una importacion o actualizacion incluya un DV que no corresponde.

### Politica objetivo de bienes, servicios e insumos

- El inventario debe manejar un catalogo de items operativos por empresa con tipo `PHYSICAL_GOOD`, `SERVICE` o `SUPPLY`.
- `PHYSICAL_GOOD` representa bienes tangibles vendidos por el negocio y puede tener control de stock.
- `SERVICE` representa servicios o intangibles vendibles, como corte de pelo, manicura o consultoria.
- `SUPPLY` representa insumos comprados o consumidos por la operacion, vendibles solo si la empresa lo configura explicitamente.
- Un item vendible puede facturarse en POS electronico o factura electronica.
- Un bien fisico con `stockTracked=true` valida disponibilidad y descuenta stock al confirmarse la venta.
- Un servicio no descuenta automaticamente insumos, aunque tenga referencias de insumos sugeridos.
- Los insumos usados por servicios se afectan manualmente con movimientos `CONSUMPTION_OUT`, `WASTE_OUT`, `ADJUSTMENT_IN` o `ADJUSTMENT_OUT`, porque la cantidad real usada depende de la operacion y no existe algoritmo aprobado.
- La relacion servicio-insumo sera una referencia operativa opcional para ayudar al usuario a recordar insumos comunes; no genera movimientos automaticos ni reservas.

### Politica objetivo de compras, gastos y cuentas por pagar

- Una compra con lineas de inventario incrementa stock solo cuando se confirma.
- Una compra confirmada intenta generar contabilizacion y cuenta por pagar mediante `accounting-service` cuando la URL esta configurada; la llamada es best-effort para no convertir la contabilidad en dependencia de disponibilidad de `inventory-service`.
- Un gasto sin inventario registra proveedor, concepto, subtotal, impuestos, total, evidencia opcional y estado, pero no genera movimientos de stock.
- Una compra o gasto puede crear cuenta por pagar cuando no se paga de contado.
- Las cuentas por pagar deben asociarse a proveedor, documento origen, fecha de vencimiento, saldo y estado.
- Los pagos parciales o totales disminuyen el saldo de la cuenta por pagar y generan trazabilidad contable.
- La contabilizacion de compras, gastos, IVA descontable, proveedores, caja y bancos debe seguir reglas parametrizables por empresa basadas en PUC.

### Politica objetivo de reportes minimos

- Los reportes iniciales deben filtrar siempre por empresa.
- Reportes operativos minimos: ventas por periodo, documentos electronicos por estado, inventario disponible, kardex por item, compras/gastos por periodo y cuentas por pagar.
- El reporte de cuentas por cobrar queda implementado en TASK-055 sobre un agregado transaccional de cartera por cliente; no se infiere solo desde la cuenta PUC `1305`.
- Reportes contables minimos: libro diario, libro mayor, balance de comprobacion simple y saldos por cuenta.
- Los reportes deben consultar tablas del modelo Clean Architecture activo; no deben depender de tablas legacy pendientes de depuracion.
- La implementacion inicial expone endpoints de lectura en `billing-service`, `inventory-service` y `accounting-service`, porque esos servicios son duenos del dato.
- El `reporting-service` fisico queda diferido hasta que exista una necesidad aprobada de consultas consolidadas separadas. La proyeccion asincrona inicial ya vive en `reporting-projection-lambda`.

### Politica objetivo de identidad, permisos y licenciamiento

- `identity-service` administra usuarios, roles, permisos y membresias por empresa desde TASK-056 como microservicio fisico Clean Architecture con schema `identity`.
- Modelo objetivo aprobado en TASK-068: `ROOT` es un usuario global de plataforma, no pertenece a ninguna empresa, no depende de licencia empresarial y puede registrar empresas contratantes y entregar el administrador inicial.
- Todos los roles distintos de `ROOT` son roles por empresa, configurables y aislados por `company_id`.
- Cada empresa puede crear sus propios roles empresariales y asignar permisos modulares segun su operacion.
- La autorizacion debe evaluar empresa, permisos efectivos y alcance global/empresarial antes de ejecutar comandos de negocio.
- Un actor solo puede crear, editar o asignar roles con permisos estrictamente menores que sus permisos efectivos; no puede delegar permisos iguales, superiores ni permisos que no posee.
- Los permisos `GLOBAL_*` son exclusivos de `ROOT` y estan prohibidos en roles empresariales.
- `tenant-service` administra desde TASK-057 el estado de licencia de cada empresa: activa, suspendida, vencida o cancelada.
- La licencia se persiste en `tenant.company_license` con plan, vigencia, limites de usuarios/documentos y auditoria basica.
- Los servicios de negocio consultan `GET /api/v1/companies/{companyId}/license/validation?action=...` antes de comandos que creen usuarios, transacciones o documentos fiscales.
- Una licencia suspendida o vencida devuelve `allowed=false` con `reasonCode` estructurado y debe bloquear nuevas transacciones de negocio y emision fiscal, pero permite consultas, exportaciones y administracion necesaria segun politica aprobada.
- La auditoria debe registrar accesos, cambios de configuracion, emision fiscal, movimientos de inventario, compras, gastos, pagos y cambios de licencia.

### Orden objetivo antes de limpieza y eventos cloud

1. Definir y cerrar la logica backend faltante de terceros, items vendibles, servicios, insumos, compras, gastos, reportes, permisos y licencias.
2. Implementar y probar esos casos de uso por API y PostgreSQL en microservicios Clean Architecture.
3. Migrar el legacy pendiente al modelo nuevo y demostrar equivalencia funcional.
4. Ejecutar depuracion y eliminacion de codigo/tablas legacy solo con matriz de reemplazo aprobada.
5. Implementar Outbox/Inbox con EventBridge/SQS y Lambdas cuando el flujo core ya este estable.

### Politica inicial de plan de cuentas PUC

- El plan de cuentas por empresa usa codigos PUC numericos.
- La clasificacion inicial se deriva del primer digito del codigo: 1 activo, 2 pasivo, 3 patrimonio, 4 ingresos, 5 gastos, 6 costos de ventas, 7 costos de produccion u operacion, 8 cuentas de orden deudoras y 9 cuentas de orden acreedoras.
- Los niveles se calculan por longitud: 1 digito clase, 2 grupo, 4 cuenta, 6 subcuenta y mas de 6 auxiliar.
- La naturaleza inicial se deriva de la clase: debito para activo, gastos, costos y cuentas de orden deudoras; credito para pasivo, patrimonio, ingresos y cuentas de orden acreedoras.
- Esta tarea no carga el catalogo PUC completo como datos semilla; esa carga queda pendiente para una migracion o tarea de parametrizacion aprobada.

### Politica inicial de asientos contables automaticos

- Los asientos se generan mediante reglas contables configurables por empresa, no mediante cuentas hardcodeadas en el codigo.
- Cada regla contable se asocia a un evento contable, un tipo de documento origen y una lista de lineas parametrizadas por cuenta PUC, lado debito/credito y tipo de valor: subtotal, impuesto o total.
- La empresa puede configurar cuentas distintas para caja, bancos, cartera, ingresos, IVA generado, IVA descontable, inventario, proveedores, gastos y cuentas por pagar, segun su operacion y criterio contable.
- Las plantillas base sugeridas para pruebas son:
  - Venta POS/factura: debito a caja/cartera por total, credito a ingresos por subtotal y credito a IVA generado por impuesto.
  - Compra: debito a inventario o gasto por subtotal, debito a IVA descontable por impuesto y credito a proveedor/caja/banco por total.
  - Gasto: debito a cuenta de gasto por subtotal, debito a IVA descontable por impuesto y credito a caja/banco/cuenta por pagar por total.
- Un asiento contable solo puede quedar en estado `POSTED` si la suma de debitos es igual a la suma de creditos.
- No se permite contabilizar dos veces el mismo documento origen para la misma empresa usando la misma combinacion `company_id`, `source_type` y `source_id`.
- Una linea contable no puede tener debito y credito al mismo tiempo, ni quedar sin valor.
- Las lineas con valor cero derivadas de una regla se omiten para soportar operaciones sin impuesto sin romper el balance.
- La persistencia local usa tablas prefijadas `accounting_*` para evitar colisiones entre bounded contexts.
- La administracion inicial de reglas contables se expone por REST y valida que las cuentas PUC existan antes de activar la regla.
- TASK-053 permite listar cuentas PUC por empresa, listar reglas contables por empresa/evento/estado, reemplazar la regla activa de un evento y desactivar reglas activas sin eliminar historial.
- TASK-053/TASK-100 incluye `POST /api/v1/accounting-setup/basic` para crear una plantilla minima editable por empresa con cuentas `1105`, `1110`, `1305`, `1435`, `2205`, `2408`, `4135`, `5105` y `5135`, y reglas base para venta, compra, gasto, pago de cuenta por pagar, recaudo de cuenta por cobrar y pago diario de nomina.
- La plantilla base es una ayuda operativa local; no carga el PUC oficial completo y debe poder ser reemplazada por parametrizacion de cada empresa.
- El reemplazo de reglas conserva el historial dejando la regla anterior `active=false`; la generacion de asientos siempre usa la regla activa vigente al momento del comando.
- En la implementacion local actual, los asientos se crean directamente en estado `POSTED`; el flujo de borradores contables queda pendiente hasta que sea aprobado.

### Politica inicial de libro diario y libro mayor

- El libro diario consulta asientos `POSTED` por empresa y rango de fechas.
- El libro diario expone fecha contable, descripcion, tipo e identificador de documento origen, lineas, cuentas, tercero, debitos y creditos.
- El libro diario se ordena por fecha, descripcion e identificador del asiento.
- El libro mayor consulta el mismo periodo y agrupa movimientos por cuenta contable.
- El libro mayor suma debitos y creditos por cuenta y calcula saldo segun la naturaleza PUC de la cuenta:
  - Naturaleza debito: `saldo = debitos - creditos`.
  - Naturaleza credito: `saldo = creditos - debitos`.
- Las consultas de libros deben aislar siempre por `company_id`.
- La persistencia JPA y los endpoints REST de libro diario/mayor estan implementados para pruebas locales; exportaciones, saldos iniciales y periodos cerrados quedan para tareas posteriores.

## Modelo de datos vigente

El modelo de datos vigente se documenta de forma detallada en `specs/database-design.md` y `specs/data-dictionary.md`. Esta seccion resume los agregados por bounded context y reemplaza la lista historica de tablas faltantes.

- `tenant-service`:
  - `tenant.company`: empresa contratante, identificacion DIAN numerica, estado y datos administrativos.
  - `tenant.company_license`: licencia parametrizable por vigencia, modulos, limites de usuarios y documentos.
- `identity-service`:
  - `identity.user_account`: usuario autenticable local/transitorio y puente hacia Cognito productivo mediante `cognito_subject`.
  - `identity.company_role`, `identity.role_permission`, `identity.user_company`, `identity.user_company_role`: RBAC empresarial modular.
  - `identity.global_user_role`: rol global `ROOT`.
- `catalog-service`:
  - `catalog.catalog_definition`, `catalog.catalog_item`, `catalog.company_catalog_item_setting`: catalogos regulatorios/operativos DB-only.
  - `catalog.department`, `catalog.municipality`: DIVIPOLA relacional por departamento/municipio.
- `thirdparty-service`:
  - `thirdparty.third_party`: clientes/proveedores fiscales por empresa, tipo de persona, tipo de documento DIAN, DV calculado, responsabilidades y regimen.
- `inventory-service`:
  - `inventory.product`: bienes fisicos, servicios/intangibles e insumos, con impuesto de venta configurado desde catalogo.
  - `inventory.stock_balance`, `inventory.inventory_movement`: stock simple, kardex y movimientos idempotentes.
  - `inventory.purchase`, `inventory.purchase_line`: compras/entradas con proveedor, costo, medio de pago y contabilidad.
  - `inventory.service_supply_reference`: relacion sugerida servicio-insumo sin descuento automatico.
- `billing-service`:
  - `billing.issuer_profile`, `billing.numbering_resolution`, `billing.number_sequence`: emisor fiscal, resoluciones y numeracion.
  - `billing.sale`, `billing.sale_line`: venta POS con snapshot de producto, precio, impuesto y adquirente.
  - `billing.electronic_document`, `billing.electronic_document_line`, `billing.electronic_document_tax`, `billing.electronic_document_artifact`, `billing.electronic_document_trace_event`: documento electronico/POS, totales, impuestos, artefactos y trazabilidad.
  - `billing.final_consumer_profile`: consumidor final parametrizado, no quemado en frontend ni creado como tercero.
  - `billing.fiscal_adjustment_note`, `billing.pos_adjustment_note`: notas fiscales y ajustes POS.
- `dian-provider-service`:
  - `dian_provider.provider_configuration`: configuracion DIAN aislada por empresa con referencias seguras a secretos.
  - `dian_provider.provider_submission`, `dian_provider.provider_response`: envios/respuestas normalizadas sin secretos.
- `accounting-service`:
  - `accounting.accounting_account`, `accounting.accounting_rule`, `accounting.accounting_rule_line`: PUC y reglas por empresa/evento.
  - `accounting.accounting_entry`, `accounting.accounting_entry_line`: asientos balanceados y libros.
  - `accounting.accounts_receivable`, `accounting.accounts_payable`, pagos y saldos derivados: cartera/cuentas por pagar operativas.
- `audit-service`:
  - `audit.audit_event`: auditoria fiscal, tecnica y de seguridad sin secretos ni payload sensible.
- `payroll-service`:
  - `payroll.payroll_settings`, `payroll.worker`, `payroll.contract`, `payroll.daily_labor_payment`, `payroll.electronic_payroll_document`: nomina opcional, pagos diarios verbales y documento soporte mock.
- `bff-service`:
  - `bff.oauth_login_attempt`, `bff.web_session`: estado OAuth/PKCE y sesiones productivas server-side cifradas.
- Eventing/reporting:
  - `*.outbox_event`, `*.inbox_event`: publicacion/consumo idempotente.
  - `reporting.reporting_event_projection`: proyecciones reconstruibles desde eventos canonicos.

Regla SDD: si una tabla aparece en codigo o Flyway y no esta descrita en `database-design.md`/`data-dictionary.md`, debe documentarse antes de evolucionar el flujo. Si una tabla se conserva solo por historia o migracion, debe quedar clasificada en `specs/legacy-cleanup-audit.md` o en la matriz de reemplazo vigente.

## Contratos externos

La conexion DIAN debe integrarse mediante puerto de salida:

```java
interface ElectronicDocumentProviderPort {
    ProviderSubmissionResult submitInvoice(ProviderInvoiceRequest request);
    ProviderSubmissionResult submitPosDocument(ProviderPosRequest request);
    ProviderSubmissionResult submitCreditNote(ProviderCreditNoteRequest request);
    ProviderStatusResult queryStatus(String providerTrackingId);
}
```

La implementacion concreta dependera del modo DIAN configurado por cada empresa y de los contratos tecnicos vigentes de DIAN. La plataforma no debe asumir ni comunicar que actua como proveedor tecnologico DIAN.

## Seguridad

- Autenticacion y autorizacion por roles.
- Secretos por variables de entorno o gestor de secretos.
- Certificados y tokens nunca versionados.
- Auditoria de operaciones fiscales.
- Validacion de entrada en DTOs.
- Errores publicos sin stack trace ni secretos.

### Politica inicial de auditoria fiscal

- Las operaciones fiscales sensibles deben registrar evento de auditoria con empresa, recurso, accion, resultado, fecha, usuario cuando este disponible y detalle seguro.
- Mientras no exista autenticacion/autorizacion implementada, `user_id` puede ser nulo; cuando una capa de seguridad provea usuario, debe propagarse al evento.
- La auditoria fiscal se mantiene separada de logs tecnicos y trazabilidad interna.
- Los detalles de auditoria no deben incluir secretos, certificados, tokens, credenciales ni payloads sensibles.

### Politica inicial de errores API

- Todas las excepciones expuestas por controladores REST deben responder con el error estandar definido en `specs/api-contract.md`.
- La respuesta publica debe incluir `timestamp`, `status`, `code`, `message`, `correlationId` y `details`.
- `X-Correlation-Id` debe propagarse al cuerpo de error cuando llegue en la peticion; si no llega, el backend genera un identificador seguro para la respuesta.
- Los errores de validacion de DTOs deben mapearse a `VALIDATION_ERROR` con detalle por campo.
- Recursos inexistentes deben mapearse a `RESOURCE_NOT_FOUND`.
- Duplicados deben mapearse a `DUPLICATE_RESOURCE`.
- Reglas de negocio deben mapearse a `BUSINESS_RULE_VIOLATION`.
- Errores no controlados deben mapearse a `INTERNAL_ERROR` con mensaje seguro sin stack trace, secretos ni detalles internos.
- Los errores de la conexion DIAN deben mapearse a `EXTERNAL_PROVIDER_ERROR` cuando existan endpoints o adaptadores HTTP que los expongan.

## Observabilidad

- Logs estructurados.
- Correlation ID por request.
- Las peticiones HTTP usan `X-Correlation-Id` cuando llega en la solicitud o generan un UUID cuando falta; el identificador se expone en la respuesta, se guarda como atributo de request y se registra en MDC con la llave `correlationId`.
- Los logs tecnicos de inicio y fin de request deben emitirse como mensajes estructurados con `event`, `correlationId`, `method`, `path`, `status` y `durationMs`, sin registrar cuerpos, credenciales ni cabeceras sensibles.
- Metricas para emisiones, rechazos, reintentos, latencia del proveedor y errores.
- Auditoria fiscal separada de logs tecnicos.

## Estrategia de pruebas

- Unit tests de dominio y casos de uso.
- Tests de adaptadores con mocks de la conexion DIAN.
- Tests de controladores REST.
- Tests de persistencia para repositorios criticos.
- Tests de integracion para flujos factura/POS/inventario/contabilidad.

## Migracion desde proyecto actual

1. Documentar specs.
2. Externalizar credenciales.
3. Introducir estructura Clean Architecture en el monolito actual como fase intermedia.
4. Extraer microservicios por bounded context cuando los contratos esten estables.
5. Mantener compatibilidad de endpoints existentes durante la migracion, si el usuario lo confirma.

## Licenciamiento como politica transversal

TASK-058 conecta el licenciamiento por empresa como politica de aplicacion en los servicios consumidores iniciales:

- `billing-service` consulta `tenant-service` mediante el puerto `LicenseValidationPort` antes de crear ventas nuevas y antes de emitir/confirmar documentos fiscales.
- `identity-service` consulta `tenant-service` mediante el puerto `LicenseValidationPort` antes de crear membresias o asignar roles dentro de una empresa.
- Los adaptadores HTTP fallan cerrado con error de negocio cuando la licencia no puede validarse, evitando crear transacciones sin licencia activa.
- Docker Compose solo conserva `postgres` como dependencia de arranque; las URLs entre microservicios se configuran por variables y no con `depends_on` entre servicios de negocio.

## Context7 evidence

- Library/tool: Amazon Cognito (`/websites/aws_amazon_cognito`).
- Topic consulted: Hosted UI Authorization Code Grant with PKCE, callback state and token endpoint.
- Relevant finding: Cognito Hosted UI uses `/oauth2/authorize` with `code_challenge` and exchanges `code` with `code_verifier` at `/oauth2/token`; callback returns `code` and `state`.
- Decision impact: `bff-service` has `AUTH_MODE=cognito`, `/api/v1/auth/login-url`, `/api/v1/auth/callback`, PKCE S256, encrypted server-side session storage and production fail-closed guard; `identity-service` links Cognito `sub` persistently after prior user provisioning.

- Library/tool: Spring Security reference 6.5 (`/websites/spring_io_spring-security_reference_6_5`).
- Topic consulted: CSRF for SPA and security headers.
- Relevant finding: SPA requests should carry CSRF in a header and servers should emit browser hardening headers such as HSTS, frame options and content type options.
- Decision impact: BFF implements security headers and conditional CSRF validation for cookie sessions without breaking local bearer-token E2E mode.

- Library/tool: AWS SDK for Java v2 (`/aws/aws-sdk-java-v2`).
- Topic consulted: Secrets Manager runtime handling and exception discipline.
- Relevant finding: runtime clients must handle AWS failures explicitly and avoid exposing secret values.
- Decision impact: `dian-provider-service` now uses `SecretVaultPort` and metadata-only persistence; Terraform grants KMS/Secrets Manager permissions for company-scoped runtime secret paths.

- Library/tool: Testing Library (`/testing-library/testing-library-docs`).
- Topic consulted: pruebas asincronas de UI con `waitFor`, `findBy` y aserciones orientadas al usuario.
- Relevant finding: las pruebas deben esperar cambios visibles del DOM en vez de acoplarse a detalles internos de render o tiempos.
- Decision impact: la suite React valida busqueda de clientes, metodos de pago y reportes con eventos de usuario y esperas asincronas estables.

- Library/tool: Context7 MCP.
- Topic consulted: TASK-059 legacy cleanup.
- Relevant finding: Context7 tools were not available in this Codex session after tool discovery; no framework, library, API or runtime behavior decision was introduced in this cleanup batch.
- Decision impact: The implementation is limited to removing `services/legacy-monolith`, updating Maven/docs/specs, and preserving PostgreSQL legacy tables until a data migration/backup plan is approved.

## TASK-059 legacy cleanup decision

El lote 1 de TASK-059 elimina el codigo del monolito legacy porque no participa en el reactor Maven activo, Docker Compose ni el flujo E2E aprobado. Las tablas `public.*` legacy no se eliminan: la auditoria de datos muestra filas historicas en catalogo, terceros, billing y contabilidad que requieren migracion o respaldo aprobado antes de cualquier operacion destructiva.
## TASK-059 lote 2 Context7 evidence

- Library/tool: Spring Boot (`/spring-projects/spring-boot`).
- Topic consulted: component scanning, `@SpringBootApplication`, `@EntityScan` and repository scanning during controller/config cleanup.
- Relevant finding: `scanBasePackages` affects component scanning but does not replace JPA entity or repository scanning, which are configured separately through `@EntityScan` and repository annotations.
- Decision impact: Removing thirdparty legacy controllers also requires removing legacy use-case beans, JPA entities and repositories from the active thirdparty scan path.

- Library/tool: Flyway (`/flyway/flyway`).
- Topic consulted: validation behavior when applied migration files are deleted or changed.
- Relevant finding: Flyway validation reports applied migrations that are no longer resolved locally and checksum mismatches for changed migrations; repair is required only when such changes are intentional.
- Decision impact: TASK-059 lote 2 did not delete or modify applied thirdparty migration files. TASK-088 later removes `thirdparty.cliente`/`thirdparty.proveedor` with a Flyway safeguard that aborts if company-scoped legacy data exists.

## TASK-059 lote 2 legacy thirdparty cleanup decision

El lote 2 de TASK-059 retira el codigo runtime legacy de terceros (`/api/clientes`, `/api/proveedores`, modelo `Customer`/`Supplier`, adaptadores, repositorios, mappers y cliente REST a catalogo). El contrato canonico queda en `/api/v1/third-parties`, `/api/v1/customers` y `/api/v1/suppliers`. En TASK-088 las tablas `thirdparty.cliente`/`thirdparty.proveedor` se eliminan con salvaguarda Flyway para no borrar datos reales de empresa.

## Arquitectura cloud AWS objetivo

### Frontend y entrada publica

- El frontend objetivo sera una SPA servida desde Amazon S3 privado mediante CloudFront.
- El navegador no consumira microservicios internos directamente.
- API Gateway expone la entrada publica hacia un `bff-service`.
- El `bff-service` vive en ECS Fargate y agrega respuestas, normaliza errores, administra sesion segura, propaga identidad interna, `X-Company-Id`, `X-User-Id`, `X-Correlation-Id` e `Idempotency-Key`, y protege al frontend de contratos internos inestables.

### Computo backend

- Los microservicios Spring Boot de larga vida se despliegan en ECS Fargate: `bff-service`, `tenant-service`, `identity-service`, `catalog-service`, `thirdparty-service`, `inventory-service`, `billing-service`, `dian-provider-service`, `accounting-service`, `audit-service`, `payroll-service` y `reporting-service` solo si se materializa en una tarea futura.
- Los procesos event-driven cortos se implementan como Lambdas: auditoria asincrona, efectos de inventario/contabilidad derivados de documentos, proyecciones de reportes, reintentos de estado de conexion DIAN, notificaciones y tareas programadas de licencias.
- La base de datos productiva objetivo sera RDS/Aurora PostgreSQL, separando datos por servicio mediante base o esquema segun la fase.
- Los secretos, certificados y credenciales se resuelven desde Secrets Manager o Parameter Store en runtime.
- Amazon Cognito User Pool/App Client es el proveedor de identidad productivo para login, MFA, revocacion y politicas de autenticacion. El BFF intercambia codigos OAuth y conserva tokens cifrados server-side.

### Event-driven target

- Los productores escriben primero en su transaccion local y registran Outbox.
- TASK-062 lote 1 registra Outbox en `billing-service`, `inventory-service` y `accounting-service`.
- TASK-062 lote 2 agrega dispatcher Outbox condicional por servicio: lee eventos `PENDING`/`FAILED`, publica a EventBridge con `source=producer` y `detailType=eventType`, y marca `PUBLISHED` o `FAILED` sin tumbar el microservicio.
- TASK-062 lote 3 agrega consumidores reales iniciales: `audit-event-writer-lambda` valida Inbox en `audit_inbox_event` y materializa `AuditEventRequested` en `audit_event`; `inventory-sale-effect-lambda` valida Inbox en `inventory.inbox_event`, descuenta stock por lineas `stockTracked=true` y registra movimientos `SALE_OUT` idempotentes desde `SaleConfirmed`; `accounting-sale-entry-lambda` valida Inbox en `accounting_inbox_event`, aplica la regla contable activa `SALE_CONFIRMED`/`SALE` y crea asientos idempotentes; `provider-submission-retry-lambda` reintenta documentos con proveedor en `FAILED`, actualiza el documento y republica eventos de validacion si la DIAN mock acepta; `reporting-projection-lambda` consume eventos de ventas, documentos electronicos, inventario y contabilidad para mantener proyecciones consultables por empresa y periodo.
- La caida de una Lambda o cola no debe impedir que los servicios HTTP permanezcan arriba ni debe revertir transacciones ya confirmadas localmente.


## Consumidor Lambda de auditoria

`audit-event-writer-lambda` consume `AuditEventRequested` desde la cola `audit-events`. El handler parsea el envelope entregado por EventBridge/SQS, normaliza el payload de auditoria, inserta primero en `audit_inbox_event` y despues en `audit_event`. Los duplicados no fallan el lote; los errores de parseo o persistencia devuelven el `messageId` en `SQSBatchResponse` para reintento selectivo y posterior DLQ de SQS.

## Consumidor Lambda de inventario

`inventory-sale-effect-lambda` consume la cola `inventory-effects` y procesa eventos `SaleConfirmed`. El payload del evento contiene el snapshot de lineas de venta, incluyendo `lineId`, producto, tipo de item, `stockTracked`, cantidad y costo unitario. El consumidor ignora eventos no soportados, inserta primero en `inventory.inbox_event`, bloquea el saldo de stock con transaccion JDBC, aplica `SALE_OUT` solo a lineas con inventario y reutiliza la misma clave de idempotencia del flujo sincronico para evitar descuentos duplicados durante la transicion.

## Consumidor Lambda contable

`accounting-sale-entry-lambda` consume la cola `accounting-effects` y procesa eventos `SaleConfirmed`. El payload incluye `saleId`, `customerId` opcional, `subtotal`, `taxTotal`, `total` e `issuedAt`. El consumidor registra Inbox en `accounting_inbox_event`, evita duplicados por `SALE`/`saleId`, consulta la regla activa `SALE_CONFIRMED`, valida cuentas PUC activas, crea el asiento balanceado y publica `AccountingEntryPosted` en `accounting_outbox_event` cuando el asiento es nuevo.

## Consumidor Lambda de reintento proveedor

`provider-submission-retry-lambda` consume la cola `provider-retries` y procesa `ProviderSubmissionFailed`/`ProviderSubmissionPending`. El consumidor carga el documento y snapshot de venta desde `billing`, ignora documentos ya `VALIDATED` o `REJECTED`, reenvia al `dian-provider-service` con la misma clave de idempotencia y actualiza `billing.electronic_document`. Si la conexion DIAN acepta, publica `SaleConfirmed` y `ElectronicDocumentValidated` en `billing.outbox_event` para que inventario, contabilidad y reportes avancen por el canal asincrono. Si la conexion sigue fallando, reporta el `messageId` en `SQSBatchResponse` para reintento y DLQ; si rechaza, marca `REJECTED` sin retry automatico.

## Context7/Web evidence - decision DIAN por empresa

- Library/tool: DIAN official website.
- Topic consulted: opciones para facturar electronicamente, proveedor tecnologico, desarrollo propio/software propio y certificado digital.
- Relevant finding: DIAN publica alternativas de facturacion: servicio gratuito, desarrollo/software propio y proveedor tecnologico autorizado. Para software propio o proveedor tecnologico se requiere certificado digital y proceso de habilitacion del facturador.
- Decision impact: El producto se documenta como software parametrizable por empresa; cada empresa configura y asume su proceso DIAN. La plataforma no se presenta como proveedor tecnologico DIAN ni usa certificado global compartido.

## Context7 evidence - autenticacion productiva y proteccion navegador

- Library/tool: AWS Documentation via Context7 (`/websites/aws_amazon`).
- Topic consulted: Cognito Authorization Code Grant + PKCE.
- Relevant finding: Cognito Hosted UI soporta Authorization Code Grant y PKCE; el code flow con PKCE evita exponer tokens directamente en el navegador cuando el intercambio lo realiza el backend/BFF.
- Decision impact: La autenticacion productiva se movera a Cognito Hosted UI + PKCE con callback en BFF.

- Library/tool: AWS Documentation via Context7 (`/websites/aws_amazon`).
- Topic consulted: Cognito App Client, token revocation and prevent user existence errors.
- Relevant finding: Cognito App Clients pueden configurarse con OAuth code flow, revocacion de tokens, expiraciones y prevencion de enumeracion de usuarios.
- Decision impact: Terraform debe crear App Client productivo con code flow, revocacion y politicas de seguridad; ROOT/admin requieren MFA.

- Library/tool: AWS Documentation via Context7 (`/websites/aws_amazon`).
- Topic consulted: CloudFront security headers and TLS.
- Relevant finding: CloudFront puede agregar HSTS, CSP, `X-Content-Type-Options`, `X-Frame-Options` y `Referrer-Policy`; AWS documenta TLS/HTTPS para cifrado en transito.
- Decision impact: La proteccion no se basa en cifrado JavaScript casero, sino en TLS, Hosted UI, cookies HttpOnly, CSP, CSRF y no exposicion de tokens al navegador.

## Context7 evidence - OpenAPI

- Library/tool: Springdoc OpenAPI (`/springdoc/springdoc-openapi`).
- Topic consulted: exposicion de OpenAPI JSON/YAML y Swagger UI en Spring Boot WebMVC.
- Relevant finding: Springdoc expone documentacion runtime en `/v3/api-docs`, `/v3/api-docs.yaml` y Swagger UI cuando el servicio esta levantado.
- Decision impact: La dependencia Springdoc habilita exploracion runtime, pero la plataforma aun debe generar y versionar artefactos OpenAPI por servicio/BFF para cumplir contratos formales.

## Consumidor Lambda de reportes

`reporting-projection-lambda` consume la cola `reporting-projections` y procesa `SaleConfirmed`, `ElectronicDocumentValidated`, `InventoryMovementRegistered` y `AccountingEntryPosted`. El consumidor registra Inbox idempotente en `reporting.reporting_inbox_event` y materializa un resumen normalizado por `companyId`, periodo, agregado, estado, monto y payload JSON en `reporting.reporting_event_projection`. Los eventos no soportados se descartan como exitosos; errores de parseo o persistencia se reportan con `SQSBatchResponse` para reintento selectivo.
## Context7 evidence - AWS cloud target

- Library/tool: AWS Documentation via Context7 (`/websites/aws_amazon`).
- Topic consulted: ECS Fargate versus Lambda for long-running microservices and event-driven workloads.
- Relevant finding: AWS describes Lambda as event-driven compute with native triggers, while Fargate is better aligned with services/containers that run continuously and need service-level control.
- Decision impact: Spring Boot microservices and the BFF will target ECS Fargate; short, idempotent, event-triggered processes will target Lambda.
- Library/tool: Terraform AWS Provider (`/hashicorp/terraform-provider-aws`).
- Topic consulted: EventBridge, SQS, DLQ, Lambda event source mappings and Lambda permissions.
- Relevant finding: The provider supports managed EventBridge rules/targets, SQS queues/DLQs, Lambda event source mappings and permissions required for AWS-native event-driven delivery.
- Decision impact: TASK-062 uses local transactional Outbox/Inbox tables as the service boundary and keeps the production delivery target on EventBridge/SQS + Lambda, without self-hosted brokers.
- Library/tool: AWS SDK for Java 2.x (`/websites/aws_amazon_sdk-for-java_developer-guide`).
- Topic consulted: AWS SDK for Java 2.x EventBridge PutEvents.
- Relevant finding: `PutEvents` publishes custom events with `source`, `detailType`, JSON `detail` and optional `eventBusName`; responses must inspect `failedEntryCount` and per-entry errors.
- Decision impact: The Outbox dispatcher publishes each canonical event to EventBridge with `source=producer`, `detailType=eventType`, `detail` as the common event envelope and marks failures for retry.

- Library/tool: AWS SDK for Java 2.x (`/websites/aws_amazon_sdk-for-java_developer-guide`).
- Topic consulted: AWS SDK for Java 2.x SQS send message.
- Relevant finding: SQS delivery uses `queueUrl` and `messageBody`; FIFO queues can add group/deduplication fields when needed.
- Decision impact: Direct SQS publishing is not used in producers; EventBridge routes events to SQS queues managed by Terraform, preserving producer decoupling.

- Library/tool: AWS Documentation via Context7 (`/websites/aws_amazon`).
- Topic consulted: SPA/static website hosting with S3 and CloudFront.
- Relevant finding: AWS documentation recommends CloudFront in front of S3 for HTTPS and secure private-origin delivery.
- Decision impact: Frontend target is S3 + CloudFront; browser traffic enters backend through API Gateway/BFF, not directly to microservices.

- Library/tool: AWS Lambda Java Support Libraries (`/aws/aws-lambda-java-libs`).
- Topic consulted: Java SQS handlers and Lambda event objects.
- Relevant finding: `aws-lambda-java-events` 3.16.0 provides `SQSEvent` for SQS-triggered Java Lambdas.
- Decision impact: `audit-event-writer-lambda`, `inventory-sale-effect-lambda`, `accounting-sale-entry-lambda`, `provider-submission-retry-lambda` and `reporting-projection-lambda` use `SQSEvent` plus `SQSBatchResponse` for partial item failures.

## TASK-063 frontend y BFF inicial

El frontend inicial se implementa como una SPA React/Vite en `apps/facturaelectronica-web` y consume exclusivamente el `bff-service` por `/api/v1`. El navegador no llama microservicios internos directamente.

`bff-service` es un microservicio Spring Boot sin persistencia propia en este lote. Su responsabilidad es exponer la frontera publica de API, enrutar contratos aprobados hacia servicios internos, propagar `Authorization`, `X-Company-Id`, `X-Correlation-Id` e `Idempotency-Key`, conservar respuestas JSON y devolver errores seguros cuando la ruta no esta expuesta o el servicio interno no responde.

Rutas publicas iniciales del BFF:

- `companies` -> `tenant-service`.
- `auth`, `users`, `me` -> `identity-service`.
- `catalogs`, `company-catalogs` -> `catalog-service`.
- `third-parties`, `customers`, `suppliers` -> `thirdparty-service`.
- `products`, `purchases`, `inventory-movements`, `service-supply-references` -> `inventory-service`.
- `issuers`, `numbering-resolutions`, `sales`, `electronic-pos`, `electronic-invoices`, `credit-notes`, `debit-notes` -> `billing-service`.
- `accounts`, `accounting-rules`, `accounting-setup`, `accounting-entries`, `accounts-payable`, `accounts-receivable`, `expenses` -> `accounting-service`.
- `reports/sales`, `reports/electronic-documents` -> `billing-service`.
- `reports/inventory-stock`, `reports/kardex`, `reports/purchases` -> `inventory-service`.
- `reports/expenses`, `reports/journal`, `reports/ledger`, `reports/trial-balance`, `reports/accounts-receivable` -> `accounting-service`.
- `audit-events` -> `audit-service`.

El primer UI permite operar el flujo funcional por pasos: empresa, terceros, inventario, configuracion fiscal, venta POS/factura mock y reportes. Los formularios usan JSON editable para acelerar pruebas mientras se estabilizan formularios definitivos y autenticacion real.

## TASK-063 Context7 evidence

- Library/tool: React (`/reactjs/react.dev`).
- Topic consulted: component purity and synchronization with external systems.
- Relevant finding: React recommends keeping rendering pure and using Effects only to synchronize with external systems, with cleanup for async work.
- Decision impact: La SPA mantiene el fetch encapsulado en `api/client.js`; los componentes gestionan estado de UI y no contienen reglas fiscales.

- Library/tool: Vite (`/vitejs/vite`).
- Topic consulted: env variables, dev server proxy and static production build.
- Relevant finding: Vite uses `VITE_*` env variables for client code, supports `server.proxy`, and emits static assets through `vite build`.
- Decision impact: `apps/facturaelectronica-web` usa `VITE_BFF_BASE_URL`, proxy local `/api` y build estatico apto para S3/CloudFront.

- Library/tool: Spring Boot 3.5 (`/websites/spring_io_spring-boot_3_5`).
- Topic consulted: Actuator health endpoints for production-ready services.
- Relevant finding: Spring Boot Actuator exposes health endpoints for service monitoring.
- Decision impact: `bff-service` incluye Actuator `health,info` como los demas microservicios.
## TASK-065 login y formularios frontend

La SPA deja de capturar manualmente `Authorization` y `X-Company-Id`. El flujo de sesion inicial local aprobado fue:

1. El usuario ingresa email y password.
2. La SPA llama `POST /api/v1/auth/login` por medio del BFF.
3. La SPA guarda en memoria `tokenType`, `accessToken`, `userId`, `email`, `fullName` y `expiresAt`.
4. La SPA llama `GET /api/v1/me/companies` con `Authorization: Bearer <accessToken>`.
5. La SPA selecciona una empresa activa del usuario; si hay varias, permite cambiarla desde el header operativo.
6. La SPA llama `GET /api/v1/companies/{companyId}/license/validation?action=CREATE_TRANSACTION` para conocer si la empresa puede operar transacciones.
7. Los comandos de negocio posteriores envian `Authorization`, `X-Company-Id`, `X-Correlation-Id` e `Idempotency-Key` desde el estado de sesion.

Los formularios operativos son React controlled inputs. Cada campo que antes existia en el JSON editable queda representado por un input, select, checkbox o linea editable. El payload JSON se construye al enviar el formulario; la UI puede mostrar la respuesta del backend, pero el usuario no edita JSON crudo.

Estado objetivo productivo: TASK-164 a TASK-171 reemplazan este flujo local por Cognito Hosted UI + PKCE y sesion server-side en BFF con cookie segura. El contrato local con bearer token queda limitado a desarrollo/E2E y no debe exponerse al publico.

Context7 evidence:

- Library/tool: React (`/reactjs/react.dev`).
- Topic consulted: controlled form inputs, object state updates and form submission.
- Relevant finding: React documents controlled inputs with state, immutable object updates using spread syntax and `onSubmit` with `preventDefault` for form submission.

### TASK-079 - Catalogos fiscales y medios de pago

- Library/tool: React oficial (`/reactjs/react.dev`).
- Topic consulted: controlled select inputs and conditional form fields.
- Relevant finding: los componentes de formulario (`input`, `select`, `textarea`) deben controlarse con `value`/`onChange`, y React recomienda renderizado condicional para campos dependientes de estado.
- Decision impact: responsabilidades fiscales, regimen tributario, metodo de pago y billetera virtual se implementan como selects controlados; el selector de billetera solo se muestra cuando el metodo es `VIRTUAL_WALLET`.
- Library/tool: Spring Boot oficial (`/spring-projects/spring-boot`).
- Topic consulted: DTO validation with `@Valid @RequestBody`.
- Relevant finding: Spring Boot integra Jakarta Bean Validation para validar cuerpos REST anotados.
- Decision impact: los DTOs REST mantienen `@Valid` y el dominio valida catalogos fiscales/pagos antes de persistir.
- Decision impact: La SPA usa formularios controlados y construye payloads en submit, sin mutar estado ni depender de textarea JSON.
## TASK-066 control de sesion y licencia en frontend

La SPA usa un flujo de acceso cerrado: cuando no existe `session`, React renderiza solamente la pantalla de login. El shell operativo, menu lateral, formularios, paneles de respuesta y selector de empresa quedan fuera del arbol renderizado hasta que el login sea exitoso y exista una licencia activa.

Flujo aprobado:

1. `POST /api/v1/auth/login` autentica el usuario.
2. `GET /api/v1/me/companies` obtiene las empresas autorizadas para el usuario autenticado.
3. `GET /api/v1/companies/{companyId}/license/validation?action=CREATE_TRANSACTION` valida internamente la licencia de la primera empresa seleccionada.
4. Si la licencia permite operar, se crea la sesion de UI y se habilitan menus y formularios.
5. Si la licencia no permite operar o no hay empresa asociada, la sesion se limpia, se muestra un modal informativo y se conserva solo la pantalla de login.
6. El cambio de empresa dispara validacion de licencia automaticamente; no existe boton manual de validacion.
7. El boton superior `Cerrar sesion` limpia token, empresa, licencia, respuesta y errores.

### Context7 evidence

- Library/tool: React (`/reactjs/react.dev`).
- Topic consulted: conditional rendering, state-driven UI and interaction side effects.
- Relevant finding: React recomienda renderizado condicional basado en estado y ubicar efectos causados por interacciones en event handlers; el render debe permanecer puro.
- Decision impact: `App.jsx` retorna una pantalla de login aislada cuando `session` es nula y ejecuta login/licencia/logout desde handlers controlados.

## TASK-068 RBAC modular con ROOT global

El modelo de autorizacion objetivo deja de depender de roles fijos hardcodeados y pasa a RBAC configurable con permisos persistidos.

### Principios

- `ROOT` es global de plataforma: administra clientes del software, empresas, licencias y administradores iniciales.
- `ROOT` no tiene `company_id`, no consume licencia empresarial y no opera ventas, inventario ni contabilidad de una empresa salvo que se cree una membresia empresarial separada aprobada en el futuro.
- Todo rol distinto de `ROOT` pertenece a una empresa especifica y se aisla por `company_id`.
- El administrador inicial de empresa es creado o asignado por `ROOT` al activar la empresa contratante.
- El administrador empresarial puede crear usuarios y roles dentro de su empresa, siempre que los permisos delegados sean estrictamente menores que sus permisos efectivos.
- Las empresas son libres de crear nombres de roles propios: `Vendedor`, `Contador`, `Supervisor POS`, `Auxiliar Inventario`, etc.
- El backend es la fuente de verdad de autorizacion; el frontend solo refleja permisos para mejorar experiencia.

### Catalogo inicial de permisos

Permisos globales exclusivos de `ROOT`:

- `GLOBAL_COMPANIES_MANAGE`
- `GLOBAL_LICENSES_MANAGE`
- `GLOBAL_USERS_MANAGE`
- `GLOBAL_ROLES_MANAGE`
- `GLOBAL_AUDIT_VIEW`

Permisos empresariales iniciales:

- `COMPANY_USERS_MANAGE`
- `COMPANY_ROLES_MANAGE`
- `COMPANY_SETTINGS_MANAGE`
- `SALES_CREATE`
- `SALES_CANCEL`
- `FISCAL_DOCUMENTS_ISSUE`
- `INVENTORY_VIEW`
- `INVENTORY_MANAGE`
- `PURCHASES_MANAGE`
- `ACCOUNTING_VIEW`
- `ACCOUNTING_MANAGE`
- `REPORTS_VIEW`
- `AUDIT_VIEW`

### Context7 evidence

- Library/tool: Spring Security 6.5 (`/websites/spring_io_spring-security_reference_6_5`).
- Topic consulted: authorities, permissions, role hierarchy and custom authorization managers.
- Relevant finding: Spring Security soporta autorizacion por authorities/permisos, jerarquias de roles y gestores de autorizacion personalizados para reglas dinamicas.
- Decision impact: `identity-service` debe calcular permisos efectivos desde BD y exponerlos al BFF/frontend; los microservicios deben validar permisos por accion mediante contratos internos o middleware aprobado, sin depender solo de nombres de roles fijos.
## TASK-067 rediseÃ¯Â¿Â½o visual profesional

La SPA adopta una presentacion de herramienta SaaS operativa: login centrado con panel de marca, shell autenticado con sidebar fijo en escritorio, area de trabajo clara, panel superior compacto, formularios densos y controles consistentes.

Decisiones visuales:

- Login aislado del shell operativo mientras no exista sesion valida.
- Layout autenticado con sidebar oscuro, workspace claro y paneles blancos con bordes y sombra sutil.
- Formularios con grid responsive y controles de altura estable para reducir saltos visuales.
- Estados y badges con colores funcionales: activo/ok, advertencia y error.
- Paneles de respuesta conservan estilo tecnico, pero mejor integrados al layout.
- Responsive para escritorio, tablet y movil sin solapamientos.

## TASK-072 bootstrap ROOT minimo

`identity-service` incorpora una primera base funcional para pruebas del usuario global `ROOT` antes del RBAC modular completo. La tabla `identity.global_user_role` asocia usuarios a roles globales; inicialmente solo acepta `ROOT`. El login retorna `globalRoles` para que el BFF y la SPA distingan alcance global de alcance empresarial.

En Docker local, el seed se controla con `IDENTITY_ROOT_USER_SEED_ENABLED`, `IDENTITY_ROOT_USER_EMAIL`, `IDENTITY_ROOT_USER_FULL_NAME` e `IDENTITY_ROOT_USER_PASSWORD`. Los valores por defecto son dummy y solo sirven para desarrollo. En produccion deben deshabilitarse o reemplazarse por secretos administrados.

La SPA detecta `globalRoles: ["ROOT"]`, omite `GET /api/v1/me/companies` y validacion de licencia empresarial, y muestra un panel global inicial. La administracion completa de empresas, administradores, roles configurables y permisos queda en TASK-069/TASK-070/TASK-071.
## TASK-073 flujo ROOT operativo

El panel `ROOT` deja de ser una vista limitada de creacion de empresa y pasa a ser el shell global de plataforma. `ROOT` ve todos los modulos, pero las acciones empresariales siguen requiriendo un `companyId` activo. Cuando `ROOT` crea una empresa contratante, la SPA toma el `id` retornado por `tenant-service` y lo usa como empresa activa para configurar terceros, inventario, fiscal, ventas y reportes.

Para entregar el administrador inicial, la SPA ejecuta dos comandos trazables: crea el usuario en `identity-service` y luego asigna `OWNER` en la empresa creada. En el modelo actual `OWNER` representa el administrador empresarial con todos los permisos de empresa; cuando se complete RBAC modular, este flujo migrara a roles configurables.

En backend, `identity-service` reconoce el alcance global `ROOT` desde `identity.global_user_role` y permite que asigne roles empresariales sin exigir membresia previa ni licencia empresarial. Los usuarios no ROOT conservan las validaciones de licencia y permiso `ROLES_MANAGE`.
## TASK-069 RBAC modular implementado

`identity-service` implementa el primer corte funcional de RBAC modular con permisos persistidos, roles empresariales por `company_id` y asignaciones por usuario. Los permisos efectivos se calculan combinando roles modulares y roles legacy durante la transicion, sin permitir permisos `GLOBAL_*` en roles empresariales.

La regla de delegacion queda en backend: un actor empresarial solo puede crear, editar o asignar roles con permisos estrictamente menores a sus permisos efectivos. `ROOT` conserva excepcion global para administrar la plataforma y entregar administradores iniciales.

El BFF enruta los nuevos contratos `/platform/*`, `/companies/{companyId}/roles`, `/role-assignments` y `/effective-permissions` hacia `identity-service`.
## TASK-074 identificacion DIAN numerica para empresas

- Fuente normativa consultada: DIAN documentacion tecnica y tabla parametrica de tipos de documento de identificacion.
- Codigos soportados inicialmente para `tenant.company.identification_type_code`: `11`, `12`, `13`, `21`, `22`, `31`, `41`, `42`, `43`, `47`, `48`.
- Decision: el contrato de empresa usa `identificationTypeCode` entero; no se acepta UUID ni texto libre para el tipo de documento.
- Impacto: la SPA muestra nombres legibles en lista desplegable, pero envia y persiste el codigo numerico DIAN.

### Context7 evidence

- Library/tool: Spring Boot 3.5 (`/websites/spring_io_spring-boot_3_5`).
- Topic consulted: validacion de request REST con Jakarta Bean Validation.
- Relevant finding: Spring Boot integra Bean Validation para validar DTOs de entrada antes de ejecutar el controlador.
- Decision impact: `CompanyRequest` valida `identificationTypeCode` con `@NotNull`, `@Min(1)` y `@Max(99)`, y el dominio valida pertenencia a codigos DIAN soportados.

- Library/tool: React (`/reactjs/react.dev`).
- Topic consulted: select controlado con estado.
- Relevant finding: un `<select>` controlado debe recibir `value` y actualizar estado en `onChange`; cada `<option>` puede tener valor distinto al texto mostrado.
- Decision impact: la SPA muestra descripcion DIAN y conserva/envia el codigo numerico.

## TASK-075 identificacion DIAN numerica transversal

- Fuente normativa: tabla DIAN de tipos de documento de identificacion ya registrada en TASK-074.
- Decision: todo campo `identificationTypeCode` o `identification_type_code` representa un codigo DIAN numerico entero de maximo dos digitos; no se aceptan aliases como `NIT`, `CC` o `CE` en contratos nuevos.
- Alcance aplicado: `tenant-service`, `thirdparty-service`, `catalog-service`, SPA y specs usan codigo numerico. Los tipos de documento fiscal electronico (`ELECTRONIC_POS`, `CREDIT_NOTE`, etc.) siguen siendo otro concepto y no se migran.
- Impacto: `identificationTypeCode=31` es el unico caso que dispara calculo automatico de digito de verificacion NIT.

## TASK-080 a TASK-082 experiencia fiscal y sesion frontend

La SPA usa una doble lista para responsabilidades fiscales en terceros y emisor fiscal. La lista izquierda muestra responsabilidades disponibles con codigo y significado; la lista derecha contiene las seleccionadas. Los botones `Agregar` y `Quitar` evitan escritura manual. La responsabilidad `R-99-PN` conserva la regla excluyente: si se agrega, reemplaza cualquier otra responsabilidad, y si se agrega una responsabilidad ordinaria mientras `R-99-PN` esta seleccionada, esta se remueve.

La sesion autenticada local se persiste en `sessionStorage` para tolerar recarga de pagina antes del timeout de inactividad. El snapshot contiene datos de sesion, empresa activa, accesos, licencia y empresas disponibles para `ROOT`. La sesion se restaura solo si la ultima actividad registrada ocurrio hace menos de 5 minutos. La actividad se reinicia con eventos del usuario (`click`, `keydown`, `mousemove`, `scroll`, `touchstart`). Al superar 5 minutos sin actividad se limpia la sesion y se muestra login con modal informativo.

Esta politica de `sessionStorage` queda marcada como modo local/transitorio. En produccion no se permite guardar tokens ni bearer tokens en storage del navegador; la restauracion de sesion se resuelve con cookie `HttpOnly` y `GET /api/v1/auth/session`.

El login no contiene credenciales dummy precargadas; usa placeholders y campos controlados. En venta POS, el identificador retornado al crear venta se muestra como estado no editable para habilitar la confirmacion; la fecha de venta se mantiene como responsabilidad del backend mediante `createdAt` y `confirmedAt`.

### Context7 evidence

- Library/tool: React oficial (`/reactjs/react.dev`).
- Topic consulted: controlled inputs/selects and focus events.
- Relevant finding: React documenta formularios controlados usando `value` y `onChange`, select controlado y eventos como `onFocus`/`onBlur` cuando se necesita reaccionar al foco.
- Decision impact: Login, doble lista fiscal, regimenes y medios de pago se mantienen como componentes controlados sin JSON editable.
- Library/tool: React oficial (`/reactjs/react.dev`).
- Topic consulted: `useEffect` with browser event listeners and cleanup.
- Relevant finding: React recomienda registrar listeners globales e intervalos dentro de `useEffect` y devolver una funcion de limpieza para remover listeners o cancelar intervalos.
- Decision impact: El cierre por inactividad usa listeners de actividad y un intervalo con cleanup, evitando sesiones perdidas por recarga y evitando listeners huerfanos.

## TASK-083 a TASK-084 busqueda de clientes y NIT/DV

La venta POS no captura `customerId` como texto libre. La SPA permite escribir el numero de documento del cliente y consulta `GET /api/v1/customers?active=true&identificationNumberPrefix=<texto>` por medio del BFF. El resultado se limita por `X-Company-Id` y rol `CUSTOMER`; al seleccionar una coincidencia, el estado de venta conserva el `customerId` tecnico para crear la venta.

Para NIT se aplica el concepto DIAN: el numero base se captura sin digito de verificacion, el DV se calcula y se presenta separado. No se aplica regla especial por prefijo `900` porque no queda soportada como concepto DIAN en las fuentes revisadas. El backend sigue siendo la fuente de verdad: calcula DV para `identificationTypeCode=31`, rechaza DV manual incorrecto y no acepta DV para documentos distintos a NIT. El frontend solo ayuda al usuario limpiando caracteres no numericos cuando el tipo es NIT y mostrando el DV como campo informativo de solo lectura.

### Context7 evidence

- Library/tool: React oficial (`/reactjs/react.dev`).
- Topic consulted: controlled input search and fetch cleanup.
- Relevant finding: React recomienda inputs controlados con `value`/`onChange` y `useEffect` con cleanup para ignorar respuestas obsoletas de fetch.
- Decision impact: El buscador de cliente POS usa input controlado, debounce y cleanup para evitar aplicar resultados viejos.
- Library/tool: Spring Boot 3.5 (`/websites/spring_io_spring-boot_3_5`).
- Topic consulted: REST controllers with request parameters and validation.
- Relevant finding: Spring Boot soporta controladores REST con `@GetMapping` y `@RequestParam`, manteniendo la logica de negocio fuera del controlador.
- Decision impact: `thirdparty-service` extiende `/customers` con filtro opcional sin cambiar el contrato base.

### Fuentes DIAN

- Resolucion 4 de 2019 DIAN: NIT diligenciado sin DV y DV separado.
- Concepto DIAN 13904 de 1988: el DV no se considera ultimo digito del NIT.
- Decreto 678 de 2022: NIT asignado por DIAN y adicionado con DV.

## TASK-085 clientes naturales simplificados

El registro de clientes naturales simples se optimiza para facturacion electronica de consumidor final o persona natural comun. La regla aplica solo cuando el tercero tiene `roles=["CUSTOMER"]` y `personType=NATURAL`.

Politica:

- La SPA no permite seleccionar NIT para cliente natural simple.
- El digito de verificacion no se captura ni se envia para cliente natural simple.
- `taxResponsibilities` queda fijo en `["R-99-PN"]`.
- `taxRegime` queda fijo en `NO_RESPONSABLE_IVA`.
- `businessName` y `tradeName` quedan vacios/no editables porque corresponden a persona juridica o establecimiento comercial.
- La direccion es opcional. Si esta vacia, `municipalityCode` se deriva del municipio activo de empresa/emisor fiscal; si se diligencia direccion, se habilita selector de departamento y municipio.
- `thirdparty-service` rechaza requests directos que intenten modificar cualquiera de los valores automaticos.

### Context7 evidence

- Library/tool: React oficial (`/reactjs/react.dev`).
- Topic consulted: controlled inputs, derived values and redundant state.
- Relevant finding: React recomienda mantener los campos controlados y derivar valores calculables desde el estado base en lugar de duplicarlos como estado editable.
- Decision impact: La SPA calcula el modo de cliente natural simple desde `thirdPartyType` y `personType`, y renderiza campos automaticos como read-only o no editables.

## TASK-086 catalogos oficiales y operativos administrables

El catalogo local estatico es suficiente para pruebas, pero el modelo SaaS requiere persistencia y administracion controlada de catalogos.

Politica propuesta:

- Catalogos regulatorios oficiales: tipos de documento DIAN, responsabilidades fiscales, regimenes, tipos de documento fiscal y codigos tributarios. Deben persistirse como catalogos globales versionados, con `code`, `label`, `source`, `version`, `valid_from`, `valid_to`, `active` y auditoria. No se editan codigos oficiales desde empresas.
- DIVIPOLA/DANE no se modela como catalogo generico; usa tablas relacionales `catalog.department` y `catalog.municipality`, con FK por `department_code`, porque el flujo de UI selecciona departamento y luego municipios asociados.
- Catalogos operativos por empresa: metodos de pago habilitados, billeteras virtuales habilitadas, categorias internas, centros de costo y etiquetas operativas. Pueden activarse/inactivarse por empresa y extenderse si mantienen un mapeo fiscal valido.
- La UI de configuracion debe consumir estos catalogos desde `catalog-service` via BFF. No se permite fallback productivo con catalogos estaticos del frontend; si el backend no entrega catalogos, el formulario dependiente debe bloquear la accion con error controlado.
- La depuracion futura no debe eliminar catalogos legacy hasta que existan seeds/migraciones y endpoints equivalentes en `catalog-service`.

Endpoints iniciales:

- `GET /api/v1/catalogs/{catalogCode}/items`
- `GET /api/v1/company-catalogs/{catalogCode}/items`
- `PUT /api/v1/company-catalogs/{catalogCode}/items/{itemCode}/activation`
- `GET /api/v1/catalogs/departments`
- `GET /api/v1/catalogs/departments/{departmentCode}/municipalities`
- `GET /api/v1/catalogs/municipalities/{municipalityCode}`

## TASK-087 modulo administrativo de catalogos

El modulo `Catalogos` sera una pantalla administrativa protegida por RBAC. El usuario selecciona primero el catalogo por nombre visible en espanol; la SPA envia el `catalogCode` tecnico en ingles y consulta los items desde BFF/catalog-service. No se conservaran catalogos regulatorios u operativos hardcodeados en el frontend como fallback; si el backend de catalogos no responde, la pantalla debe mostrar error controlado y no inventar opciones.

Politica:

- `catalog.catalog_definition` define catalogos administrables, etiqueta visible en espanol, descripcion, si es regulatorio, si es configurable por empresa y si ROOT puede editar items globales.
- `catalog.catalog_item` conserva `catalog_code` e `item_code` en ingles/codigo tecnico, con `label` y `description` en espanol para UI.
- ROOT puede crear, actualizar e inactivar items globales permitidos.
- Administradores empresariales solo pueden administrar `company-catalogs` para su empresa, o crear extensiones operativas si el catalogo lo permite.
- Los catalogos regulatorios DIAN/DANE no se editan libremente por empresa; solo se actualizan mediante migraciones/versiones controladas o accion ROOT aprobada.
- La SPA debe agregar el paso `Catalogos` al menu solo para ROOT, `COMPANY_CATALOGS_MANAGE` o permisos superiores aprobados.
- Los labels visibles de permisos y catalogos deben estar en espanol; codigos tecnicos en ingles quedan documentados en specs y contratos.

Context7 evidence:

- Library/tool: React oficial (`/reactjs/react.dev`).
- Topic consulted: controlled forms, conditional rendering and fetch cleanup.
- Relevant finding: React recomienda formularios controlados con `value`/`onChange`, renderizado condicional por estado y `useEffect` con cleanup para ignorar respuestas obsoletas de fetch.
- Decision impact: El modulo de catalogos usara selects/formularios controlados, renderizado por permisos efectivos y carga de datos desde BFF sin fallback local.

## TASK-088 auditoria y limpieza de tablas legacy

La limpieza de tablas y migraciones legacy se ejecutara como tarea separada y en dos fases obligatorias.

Fase A - Auditoria:

- Construir matriz por tabla con: esquema, tabla, microservicio dueno, entidad JPA/repositorio, migracion Flyway origen, endpoints/casos de uso que la usan, conteo de filas, decision y evidencia.
- Clasificar cada tabla como `EN_USO`, `LEGACY_CON_DATOS`, `LEGACY_SIN_USO`, `PENDIENTE_MIGRACION` o `CANDIDATA_A_ELIMINAR`.
- Validar referencias con busqueda de codigo, entidades JPA, SQL nativo, pruebas, migraciones y scripts Docker/IaC.
- Ejecutar o documentar prueba E2E del flujo actual para demostrar que las tablas candidatas no participan.

Fase B - Eliminacion aprobada y aplicada:

- Crear migracion Flyway nueva para eliminar solo tablas aprobadas.
- No editar migraciones ya aplicadas salvo que se decida reconstruir base local desde cero y se documente el procedimiento.
- Ejecutar la migracion contra la base local actual y validar que una base limpia se crea correctamente desde cero.
- Actualizar `data-model.md`, `data-dictionary.md`, `api-contract.md`, Docker/IaC y README para retirar referencias a tablas eliminadas.
- `catalog.tipodocumento` se migra primero hacia `catalog.catalog_item` como `DIAN_DOCUMENT_TYPE` y luego se elimina junto a las tablas legacy vacias de catalogo.
- `thirdparty.cliente` y `thirdparty.proveedor` se eliminan solo si no contienen filas con `company_id` no nulo; si hay datos de empresa real, Flyway aborta para exigir migracion/respaldo previo.
- El runtime Java legacy de catalogo se retira para no mantener entidades, repositorios, endpoints ni pruebas sin uso.

## TASK-089 POS con impuestos por producto, scanner y consumidor final parametrizable

La venta POS se mueve a un flujo operativo de caja: el vendedor identifica cliente cuando aplica, escanea productos, selecciona medio de pago y confirma. La clasificacion tributaria de cada linea no se captura en caja; viene configurada desde inventario y queda congelada como snapshot fiscal en billing.

### Decisiones

- `catalog-service` agrega el catalogo `SALES_TAX` con impuestos de venta vigentes/configurables, etiqueta en espanol, categoria tributaria, tarifa, fuente, version y vigencia.
- `inventory-service` persiste por producto `taxCategoryCode`, `taxCode`, `taxLabel` y `taxRate`. La respuesta de producto entrega esos campos a billing y a la SPA.
- `billing-service` calcula las lineas con `salePrice`, `taxCode` y `taxRate` obtenidos del snapshot de inventario; los campos de impuesto enviados desde frontend quedan fuera del contrato POS estable.
- La SPA de inventario muestra selector de impuesto desde catalogo y campo dedicado de codigo de barras.
- La SPA de venta POS no muestra canal, impuesto ni tasa editables. El canal interno queda `POS` y el documento fiscal asociado `ELECTRONIC_POS`.
- El scanner USB HID se trata como entrada de teclado en campos dedicados. En venta, el campo de scanner ejecuta busqueda automatica por debounce y tambien tolera terminador `Enter` del lector.
- Si el mismo codigo se escanea varias veces, se incrementa la cantidad de la linea existente.
- El comprador puede elegir si desea factura electronica nominada. Si no desea, la SPA envia `buyerIdentificationMode=FINAL_CONSUMER`.
- `billing-service` resuelve el consumidor final desde configuracion persistida, no desde frontend ni como tercero guardado. El valor normativo inicial se documenta como perfil fiscal parametrizable y se ajusta por proveedor/DIAN sin despliegue de frontend.

### Configuracion de consumidor final

El perfil fiscal de consumidor final vive en base de datos de `billing-service`, con posibilidad de override por empresa. Campos iniciales:

- `company_id`: nulo para perfil global o UUID para override empresarial.
- `profile_code`: `FINAL_CONSUMER`.
- `identification_type_code`: codigo DIAN parametrizable.
- `identification_number`: numero parametrizable; seed local inicial `222222222222`.
- `display_name`: etiqueta visible, por defecto `Consumidor final`.
- `active`, `source`, `source_version`, `updated_at`.

La SPA solo decide entre `IDENTIFIED_CUSTOMER` y `FINAL_CONSUMER`; no conoce ni envia numero, tipo de documento ni razon social del consumidor final.

### Context7 evidence

- Library/tool: React oficial (`/reactjs/react.dev`).
- Topic consulted: controlled inputs, keyboard event listeners, refs and focus management.
- Relevant finding: React recomienda inputs controlados con `value`/`onChange`, listeners con cleanup en `useEffect` y foco programatico mediante refs.
- Decision impact: El scanner HID se implementa con input controlado dedicado, debounce/terminador, cleanup y foco posterior al escaneo para aceptar el siguiente producto.
- Library/tool: Spring Boot 3.5 (`/websites/spring_io_spring-boot_3_5`).
- Topic consulted: REST controllers, request validation and Spring Data repositories.
- Relevant finding: Spring Boot soporta controladores REST con anotaciones MVC, `@Valid @RequestBody` y repositorios Spring Data JPA detras de adaptadores.
- Decision impact: Los cambios de contrato se exponen en controladores delgados, DTOs validados y persistencia detras de puertos de Clean Architecture.

### Fuentes DIAN consultadas

- DIAN documentacion tecnica del sistema de facturacion electronica: los anexos tecnicos publicados por DIAN son la fuente de reglas de validacion vigentes.
- Resolucion DIAN 165 de 2023: adopta el Anexo Tecnico de Factura Electronica de Venta 1.9 y el Anexo Tecnico de Documento Equivalente Electronico 1.0.
- DIAN prensa/guia de factura electronica: si el comprador no entrega datos debe aparecer la frase `Consumidor final`.

## TASK-090 a TASK-093 auditoria global, catalogos y UX operativa

### Decisiones

- Toda accion mutable debe generar auditoria de negocio o seguridad. La cobertura se implementa por bounded context; esta iteracion conecta catalogos globales ROOT con `audit-service` y formaliza la politica transversal para los demas servicios.
- Los catalogos regulatorios son protegidos para empresas, pero editables por ROOT porque ROOT administra la plataforma completa. Cada cambio debe registrar accion, recurso, resultado, usuario actor si llega por header y detalle seguro.
- El modulo Logs/Auditoria consume `audit-service`. ROOT y administradores lo ven desde UI; ROOT opera sobre la empresa activa seleccionada en esta iteracion, evitando consultas globales sin controles backend adicionales.
- La UI elimina paneles `Respuesta`/`Error`; las respuestas tecnicas quedan para logs, tests o consola de desarrollo, no para el flujo operativo.

### Context7 evidence

- Library/tool: React oficial (`/reactjs/react.dev`).
- Topic consulted: async UI state, conditional rendering, loading/error/success states and effect cleanup for timers.
- Relevant finding: React recomienda modelar estados visuales explicitos como estados de envio, exito y error, renderizar condicionalmente y limpiar temporizadores/efectos.
- Decision impact: La SPA reemplaza paneles tecnicos persistentes por un modal de proceso controlado por estado y conserva `correlationId` solo como contexto tecnico.

## TASK-094 a TASK-112 catalogos DB-only, contabilidad y nomina

### Decisiones de frontend y catalogos

- La SPA no debe contener `initialState` con datos demo de negocio ni catalogos regulatorios/operativos. Los formularios deben iniciar vacios, con valores derivados de sesion/API o con selecciones explicitas del usuario.
- Los archivos locales de datos solo pueden conservar constantes de presentacion no sensibles, por ejemplo nombres de pasos o etiquetas de navegacion. Las opciones seleccionables de negocio deben venir de PostgreSQL por `catalog-service` y BFF.
- El unico seed funcional permitido para pruebas locales iniciales es el usuario global `ROOT` en `identity-service`. Empresas, administradores, terceros, productos, resoluciones y ventas deben crearse por API o por scripts E2E.
- El modulo de catalogos queda como fuente operativa para tipos de tercero, tipos de persona, tipos de item, impuestos de venta, medios de pago, billeteras, responsabilidades fiscales, regimenes, tipos de documento fiscal y entornos fiscales.
- DIVIPOLA mantiene tablas especializadas `catalog.department` y `catalog.municipality`; la UI selecciona departamento por nombre y luego municipio por nombre.

### Decisiones de UX asincrona

- El modal de proceso usa estados explicitos `idle`, `loading`, `success` y `error`.
- El estado `success` debe cerrarse automaticamente mediante temporizador con cleanup.
- El estado `error` debe permanecer visible hasta cierre manual.
- Errores 401/403 del login se traducen como credenciales incorrectas.
- Errores 5xx, timeout o red se traducen como fallo interno generico y referencia a Logs/Auditoria.
- Validaciones 400 deben mostrar mensaje funcional de validacion sin stack trace ni payload tecnico.

### Decisiones de inventario

- Los checks tecnicos `saleEnabled`, `purchaseEnabled` y `stockTracked` se conservan en contrato/backend, pero la UI debe presentarlos como configuracion guiada de uso del item:
  - Producto vendible con inventario.
  - Servicio/intangible vendible.
  - Insumo comprado y controlado.
  - Gasto/servicio de proveedor no inventariable.
- La UI debe explicar el impacto operacional de cada opcion en espanol y evitar que el usuario final tenga que entender flags tecnicos.
- Los servicios facturables pueden tener insumos asociados como sugerencias operativas. Despues de crear una venta con lineas `SERVICE`, la SPA permite cargar esas sugerencias, editar cantidades reales y confirmar consumo.
- La confirmacion de consumo asistido no crea receta automatica ni descuenta insumos sin accion del usuario; genera movimientos `CONSUMPTION_OUT` idempotentes con origen `MANUAL_SUPPLY_CONSUMPTION` y `sourceDocumentId` de la venta/documento origen.

### Decisiones contables

- `accounting-service` sera el dueno de ingresos, egresos, costos de operacion, activos, cuentas por cobrar, cuentas por pagar, reglas PUC y reportes contables.
- Ventas, compras, gastos y nomina publican o solicitan contabilizacion usando contratos idempotentes.
- Si falta una regla contable PUC, el comando debe fallar de forma explicita y auditable; no se deben generar asientos incompletos.
- El pago diario verbal se contabiliza con evento `PAYROLL_DAILY_PAYMENT_REGISTERED`, origen `PAYROLL_DAILY_PAYMENT`, debito a `5105` y credito a `1105` en la plantilla basica editable.
- `payroll-service` invoca `accounting-service` mediante puerto de salida/adaptador HTTP best-effort configurable por `ACCOUNTING_SERVICE_URL`; la falla de contabilidad no revierte el pago diario persistido ni crea dependencia de arranque entre contenedores.

### Decisiones de nomina

- Se agrega `payroll-service` como bounded context independiente para empleados, contratos, periodos, liquidaciones, pagos diarios verbales y configuracion de nomina electronica.
- La nomina electronica queda como funcionalidad configurable por empresa. Si `electronicPayrollEnabled=false`, la empresa puede registrar nomina interna sin generar documento soporte electronico mock.
- Los pagos diarios verbales se modelan como registro operacional auditado, no como exencion automatica de obligaciones laborales.
- El sistema debe clasificar cada pago de personal como empleado formal, trabajador por dias, pago diario verbal, contratista independiente o pago pendiente de clasificacion.
- Un contratista independiente se integra contablemente como egreso/proveedor o gasto operativo, no como empleado formal.
- El flujo inicial genera documento soporte mock por pago diario verbal mediante `POST /api/v1/payroll/electronic-documents` solo cuando la empresa tenga la funcionalidad activada.

### Logs/Auditoria

- El modulo Logs/Auditoria muestra por defecto eventos del dia actual.
- Los filtros UI son rango de fechas y `resourceType` opcional cargado desde backend.
- Se elimina filtro manual por `resourceId` de la UI operativa.
- La tabla debe mostrar fecha, usuario, accion, tipo de recurso, resultado, detalle seguro y correlacion cuando exista.

### RBAC en BFF

- El frontend oculta modulos segun permisos efectivos, pero la autorizacion real para catÃ¡logos administrables, contabilidad, nomina y logs se valida en `bff-service` contra `identity-service`.
- `ROOT` se valida con `/api/v1/platform/permissions`, que debe ser root-only. Si identidad devuelve 403/401/error, el BFF no debe asumir alcance global.
- En el modo local actual, los usuarios empresariales envian `Authorization`, `X-Company-Id` y `X-User-Id`; el BFF confirma que `X-User-Id` coincide con `/api/v1/me` antes de evaluar permisos efectivos de empresa.
- En el modo productivo TASK-164/TASK-174, la SPA no envia `Authorization`; el BFF resuelve la cookie segura, deriva `X-User-Id` y propaga identidad interna.
- Las mutaciones de plataforma en `tenant-service` quedan reservadas para `ROOT`.

### Cloud productivo

- Terraform incluye `payroll-service` como servicio ECS/Fargate privado, con `PAYROLL_DB_PASSWORD` inyectado desde Secrets Manager/RDS y `ACCOUNTING_SERVICE_URL` por Cloud Map.
- El BFF en ECS recibe URLs internas Cloud Map hacia todos los microservicios para evitar defaults `localhost` en despliegue productivo.

### Evidencia normativa TASK-110

- DIAN facturacion electronica: la documentacion tecnica vigente publicada en julio de 2026 lista anexos tecnicos de factura electronica, incluyendo version 1.9, y la normatividad vigente referencia Resolucion 00165 de 2023 y modificaciones posteriores. Impacto: los catalogos fiscales deben seguir siendo parametrizables desde base de datos y no quedar quemados en frontend.
  - Fuente: https://www.dian.gov.co/impuestos/factura-electronica/documentacion/Paginas/documentacion-tecnica.aspx
  - Fuente: https://micrositios.dian.gov.co/sistema-de-facturacion-electronica/normatividad/
- DIAN datos de factura: la identificacion de factura electronica exige datos de emisor/receptor, productos, medios de pago, impuestos, QR y CUFE; consumidor final aplica cuando el comprador no entrega datos. Impacto: POS mantiene consumidor final parametrizado y evita exigir registro de tercero para ese caso.
  - Fuente: https://www.dian.gov.co/Prensa/Paginas/NG-Como-identificar-una-factura-electronica.aspx
- DIAN nomina electronica: el documento soporte de pago de nomina electronica soporta costos/deducciones asociados a pagos derivados de relacion laboral/legal y se genera por beneficiario cuando el sujeto obligado lo requiere como soporte fiscal. Impacto: nomina electronica queda opcional por empresa y bloqueada cuando `electronicPayrollEnabled=false`.
  - Fuente: https://www.dian.gov.co/impuestos/Paginas/Sistema-de-Factura-Electronica/Documento-Soporte-de-Pago-de-Nomina-Electronica.aspx
  - Fuente: https://normograma.dian.gov.co/dian/compilacion/docs/resolucion_dian_0013_2021.htm
- DANE DIVIPOLA: DIVIPOLA es nomenclatura estandarizada para identificar departamentos, municipios, distritos, areas no municipalizadas y centros poblados mediante codigos. Impacto: se mantienen tablas relacionales `catalog.department` y `catalog.municipality`, con UI por nombre y persistencia por codigo DANE.
  - Fuente: https://www.dane.gov.co/index.php/sistema-estadistico-nacional-sen/normas-y-estandares/nomenclaturas-y-clasificaciones/nomenclaturas/codificacion-de-la-division-politica-administrativa-de-colombia-divipola
  - Fuente: https://geoportal.dane.gov.co/servicios/descarga-y-metadatos/descarga-divipola/
- PUC Colombia: el Decreto 2650 de 1993 define el Plan Unico de Cuentas para comerciantes obligados a llevar contabilidad. Impacto: la plantilla basica usa cuentas PUC editables y la carga completa del PUC queda como parametrizacion/carga regulatoria futura.
  - Fuente: https://suin-juriscol.gov.co/viewDocument.asp?id=1772403

### Context7 evidence

- Library/tool: React oficial (`/reactjs/react.dev`).
- Topic consulted: controlled forms, async UI state, fetch cleanup and timer cleanup.
- Relevant finding: React recomienda formularios controlados, estados visuales explicitos para envio/exito/error y `useEffect` con cleanup para evitar respuestas obsoletas o temporizadores huerfanos.
- Decision impact: Los formularios no tendran datos demo locales; la carga de catalogos sera asincrona desde BFF, el modal de exito cerrara con temporizador limpio y los errores quedaran visibles hasta cierre manual.
- Library/tool: Terraform AWS Provider (`/hashicorp/terraform-provider-aws`).
- Topic consulted: ECS Fargate task definitions, environment variables, secrets and logging.
- Relevant finding: Las definiciones de contenedor ECS/Fargate declaran `environment`, `secrets`, puertos y `awslogs`; Fargate opera con `awsvpc` y servicios privados pueden resolverse internamente.
- Decision impact: `infra/aws` inyecta URLs internas Cloud Map al BFF, secretos RDS a servicios y registra `payroll-service` como artefacto ECS/Fargate privado.

## TASK-114 a TASK-119 licenciamiento parametrizable

### Decisiones

- `tenant-service` sigue siendo el dueno de empresas y licencias.
- La licencia empresarial es una capa comercial: define vigencia, estado, limites y modulos contratados.
- RBAC sigue siendo una capa de seguridad operativa: define que usuario puede ejecutar acciones dentro de los modulos que la licencia habilita.
- ROOT administra licencias desde un modulo de plataforma y no depende de licencia empresarial.
- Los modulos licenciables se guardan como codigos tecnicos en ingles (`COMPANY`, `THIRDPARTY`, `INVENTORY`, `BILLING`, `ACCOUNTING`, `PAYROLL`, `REPORTS`, `CATALOGS`, `AUDIT`, `USERS`) y se muestran en espanol en la SPA.
- El login empresarial valida licencia automaticamente; si no existe licencia configurada, muestra mensaje especifico de licencia pendiente y cierra la sesion.
- Una licencia sin modulos habilitados no permite operacion empresarial, aunque este activa y vigente.
- `maxUsers` y `maxMonthlyDocuments` no son solo metadatos comerciales: se aplican como cuotas operativas.
- `tenant-service` devuelve los limites vigentes en la respuesta de validacion de licencia para evitar llamadas adicionales.
- `identity-service` aplica `maxUsers` antes de crear un nuevo acceso empresarial por membresia legacy o asignacion RBAC modular.
- `billing-service` aplica `maxMonthlyDocuments` antes de emitir un documento fiscal nuevo. El conteo mensual inicial incluye documentos generados desde ventas y notas fiscales del bounded context `billing`.
- Si ROOT necesita ampliar cupos, debe actualizar la licencia; los servicios de negocio no deben ignorar cuotas configuradas.

### Context7 evidence

- Library/tool: Spring Boot 3.5 (`/spring-projects/spring-boot`).
- Topic consulted: REST controllers with request body validation and JSON error handling.
- Relevant finding: Spring Boot soporta `@Valid @RequestBody` para validar payloads JSON de endpoints administrativos y `@ControllerAdvice`/error JSON para respuestas controladas.
- Decision impact: El modulo de licencias se expone con controladores REST delgados, DTOs validados y errores funcionales especificos para licencia no configurada o modulo no contratado.
- Library/tool: React oficial (`/reactjs/react.dev`).
- Topic consulted: controlled forms, conditional rendering and async state.
- Relevant finding: React recomienda manejar formularios como inputs controlados, estados explicitos de envio/error y render condicional por estado.
- Decision impact: La SPA tendra formulario ROOT de licencias con checkboxes controlados por modulo y mensajes especificos de licencia en login.
- Library/tool: Spring Boot 3.5 (`/spring-projects/spring-boot`).
- Topic consulted: request body validation and JSON business error handling.
- Relevant finding: `@Valid @RequestBody` activa validacion Jakarta en controladores REST y los errores pueden estandarizarse con handlers JSON.
- Decision impact: Los limites se exponen y consumen mediante contratos REST tipados; las violaciones de cuota se responden como regla de negocio, sin stack trace ni payload tecnico.

## TASK-121 UX/RBAC de empresa y permisos

### Decisiones

- `CompanySessionPanel` renderiza dos experiencias:
  - ROOT: selector de empresa, alcance plataforma y accion global.
  - Usuario empresarial: empresa activa informativa con nombre visible; no hay selector ni UUID como etiqueta principal.
- Al iniciar sesion empresarial, la SPA valida licencia, carga licencia y consulta `GET /api/v1/companies/{companyId}` para hidratar nombre/identificacion de la empresa activa.
- `CompanyForm` usa accion contextual:
  - ROOT crea empresas con `POST /api/v1/companies`.
  - OWNER/ADMIN empresarial actualiza su empresa con `PUT /api/v1/companies/{activeCompanyId}`.
- El boton `Crear empresa` no se renderiza para usuarios empresariales; para ellos se muestra `Actualizar empresa`.
- Los permisos RBAC mantienen codigos internos en ingles, pero se traducen en UI mediante un helper de etiquetas. El payload de creacion de roles conserva `permissionCodes` originales.
- La autorizacion real debe permanecer en backend/BFF; el frontend solo mejora experiencia y evita acciones incorrectas evidentes.

### Context7 evidence

- Library/tool: React oficial (`/reactjs/react.dev`).
- Topic consulted: conditional rendering and controlled form inputs.
- Relevant finding: React recomienda render condicional con estado y formularios controlados con `value`/`onChange`; el estado debe dirigir que componente se muestra.
- Decision impact: La cabecera de empresa cambia entre selector ROOT y campo informativo empresarial sin duplicar estado; los formularios siguen controlados y las etiquetas visibles se derivan antes del render.

## TASK-122 POS obligatorio e i18n de permisos

### Decisiones

- El permiso `SALES_CREATE` representa el flujo completo de caja: crear venta POS, confirmar POS y emitir automaticamente el documento electronico asociado. El vendedor no necesita `FISCAL_DOCUMENTS_ISSUE` para completar una venta normal.
- El permiso `FISCAL_DOCUMENTS_ISSUE` queda reservado para configuracion fiscal, resoluciones, notas, ajustes, reenvios o gestion manual de documentos.
- El BFF aplica autorizacion real por ruta de `billing-service`:
  - `/sales/**`, `/electronic-pos` y confirmaciones POS aceptan `SALES_CREATE` para mutaciones.
  - `/issuers/**`, `/numbering-resolutions/**`, `/credit-notes/**`, `/debit-notes/**` y ajustes POS exigen permiso fiscal avanzado.
- La SPA oculta/muestra `Venta POS` solo con `SALES_CREATE` y conserva `Fiscal` para permisos fiscales o configuracion empresarial.
- Las etiquetas visibles de modulos/permisos se centralizan en `react-i18next`/`i18next` con recursos `es`, evitando diccionarios manuales en componentes/utilidades.

### Context7 evidence

- Library/tool: React i18next (`/i18next/react-i18next`).
- Topic consulted: setup with `useTranslation` hook and external translation resources in React.
- Relevant finding: La libreria documenta integracion con i18next, recursos externos y uso de `t()`/hooks para traducir componentes React.
- Decision impact: La SPA inicializa i18n al arrancar y resuelve textos de permisos/modulos desde recursos versionados en `src/i18n/locales/es/translation.json`.

## TASK-123 a TASK-128 navegacion, roles, usuarios y modales

### Decisiones

- La SPA deja de usar una lista plana de modulos. `Ventas` queda como pantalla inicial y los modulos administrativos se agrupan en `Configuracion` y `Contabilidad`.
- `Roles` y `Usuarios` se separan en pantallas propias para evitar el acoplamiento entre cargar permisos, crear roles y asignar usuarios.
- La pantalla `Roles` carga permisos y roles al entrar, permite crear/actualizar roles con permisos disponibles y activar/inactivar roles desde una tabla.
- La pantalla `Usuarios` carga roles y usuarios al entrar, crea usuarios asignando un rol obligatorio en el mismo flujo y permite actualizar/activar/inactivar usuarios de la empresa.
- Los usuarios inactivos no deben operar: `identity-service` bloquea login de usuario inactivo y los listados/acciones empresariales exponen su estado.
- Los modales de exito se cierran automaticamente con un timeout maximo de 1 segundo; los errores quedan esperando accion del usuario.

### Context7 evidence

- Library/tool: React (`/reactjs/react.dev`).
- Topic consulted: controlled forms, list rendering, conditional UI and timer cleanup.
- Relevant finding: React recomienda formularios controlados, listas con `map()` y `key`, UI por estado y efectos/temporizadores con cleanup.
- Decision impact: Las pantallas `Roles` y `Usuarios` usan estado controlado, render de tablas derivado de arrays y `ActionStatusModal` conserva cleanup del temporizador.

## TASK-129 a TASK-143 productizacion operativa

### Decisiones

- La siguiente fase se ejecuta desde el flujo de negocio completo y no desde infraestructura adicional: primero se prueba crear datos reales por API, vender, facturar con conector DIAN mock, afectar inventario, contabilizar y auditar.
- El E2E no depende de datos demo del frontend ni de seeds empresariales. Solo se permite el usuario `ROOT` local para iniciar el flujo de pruebas.
- Las compras y entradas de inventario se modelan como flujo operativo independiente de ventas, con proveedor, costo, stock, medio de pago y regla contable.
- Los servicios facturables pueden sugerir insumos, pero el descuento de insumos queda como accion confirmada por usuario. Esto respeta negocios pequenos donde el consumo real es variable.
- Las pantallas operativas deben evolucionar de formularios sueltos a modulos con listado, busqueda, paginacion, estado, acciones y formularios de creacion/edicion.
- BFF y microservicios seguiran validando permisos y licencia. La visibilidad de menus en SPA solo mejora experiencia, no reemplaza autorizacion.
- El tablero ROOT de licencias calcula uso comercial con datos persistidos: usuarios activos por empresa y documentos fiscales emitidos del mes.
- Las reglas contables PUC se parametrizan por empresa y evento para no quemar cuentas ni asumir una unica operacion contable para todos los negocios.
- La infraestructura productiva queda orientada 100% AWS cloud: SPA en S3/CloudFront, BFF y microservicios en ECS Fargate, PostgreSQL en RDS, secretos en Secrets Manager y eventos en EventBridge/SQS + Lambda.
- `bff-service` agrega `GET /api/v1/platform/licenses/usage` para ROOT y consulta `tenant-service`, `identity-service` y `billing-service` sin acoplar esos microservicios entre si.
- `accounting-service` expone reportes financieros minimos desde el libro mayor: estado de resultados y balance general basico por prefijos PUC.

### Flujo E2E objetivo desde cero

1. ROOT inicia sesion.
2. ROOT crea empresa contratante.
3. ROOT crea licencia activa con modulos y limites.
4. ROOT crea administrador inicial OWNER.
5. El administrador inicia sesion y opera dentro de su empresa.
6. Se crean parametros minimos: catalogos requeridos, emisor/resolucion fiscal mock y reglas contables PUC.
7. Se crea cliente/proveedor y producto vendible con impuesto desde catalogo.
8. Se registra entrada/stock inicial o compra.
9. Se registra venta POS; si no hay comprador nominativo se usa consumidor final parametrizado.
10. `billing-service` confirma POS, emite documento electronico mock y publica o registra efectos.
11. Inventario descuenta stock, contabilidad genera asiento balanceado y auditoria registra trazabilidad.
12. Reportes/listados muestran los datos creados y no exponen informacion de otra empresa.

### Context7 evidence

- Library/tool: React oficial (`/reactjs/react.dev`).
- Topic consulted: controlled forms, list rendering, conditional rendering and component state for admin dashboards.
- Relevant finding: React recomienda dividir interfaces en jerarquias de componentes, mantener estado minimo, usar formularios controlados, renderizar listas con `map()`/`key` y modelar estados asincronos de envio/exito/error.
- Decision impact: Las nuevas pantallas operativas se construiran como features separadas con componentes de formulario, tabla/listado, filtros y modales, evitando volver a concentrar flujo en un unico archivo.

## TASK-144 tablas administrativas de usuarios y roles

### Decisiones

- Las tablas de `Usuarios` y `Roles` deben usar el mismo lenguaje visual de los listados operativos: `DataTable`, busqueda local, encabezados consistentes, estados vacios, badges y acciones contextuales.
- Los datos principales se renderizan con jerarquia visual: nombre destacado, detalle secundario y conteos o codigos como metadatos, sin exponer estructuras JSON ni UUID como informacion principal.
- Las acciones de actualizar, activar e inactivar se mantienen en la fila para preservar el flujo actual, pero se presentan como botones compactos y alineados.
- Esta tarea no cambia contratos REST, RBAC, licencias ni persistencia; solo mejora presentacion y mantenibilidad del frontend.

### Context7 evidence

- Library/tool: React oficial (`/reactjs/react.dev`).
- Topic consulted: rendering dynamic lists with stable keys and extracting reusable components.
- Relevant finding: React recomienda renderizar colecciones con `map()`, usar `key` estable derivado de datos y extraer componentes reutilizables para estructuras repetidas.
- Decision impact: Las filas de usuarios y roles se renderizan desde arreglos de datos con claves por `id`, reutilizando `DataTable` y componentes visuales pequenos para nombre, estado y acciones.

### Validaciones agregadas

- `services/accounting-service` cubre calculo de estado de resultados y balance basico desde cuentas PUC.
- `services/bff-service` cubre agregacion ROOT de uso de licencias y ruteo de nuevos reportes contables.
- La SPA muestra uso comercial en `Licencias` y consume los reportes financieros desde backend.

## TASK-178 limpieza final legacy y artefactos huerfanos

### Decisiones

- El codigo runtime activo se considera migrado cuando no existen imports desde microservicios hacia paquetes legacy `DTO`, `mapper`, `models`, `repository`, `service` o `validator`, ni endpoints legacy expuestos por controladores.
- Las guias historicas que describen el monolito o pruebas transitorias no deben seguir enlazadas desde README como flujo vigente.
- Los artefactos generados o de IDE ignorados (`target`, `dist`, `.idea`, `.settings`, `.github/java-upgrade`, `.github/modernize`) se eliminan localmente si no estan rastreados por Git.
- La depuracion de tablas `public.*` se hace con script operativo seguro porque ningun microservicio activo gobierna Flyway sobre `public`.
- El script de DB solo elimina tablas vacias; cualquier tabla con filas queda preservada hasta migracion, respaldo o descarte aprobado.
- No se reescriben migraciones Flyway ya aplicadas. Una eventual compactacion/baseline de migraciones queda para una decision separada antes de produccion.
- El lenguaje visible cambia de "proveedor DIAN/proveedor tecnologico" a "conector DIAN" cuando se refiere al componente tecnico del software.

### Context7 evidence

- Library/tool: Flyway (`/flyway/flyway`).
- Topic consulted: versioned SQL migrations, validation and schema history.
- Relevant finding: Flyway valida migraciones aplicadas contra las migraciones resueltas localmente, incluyendo checksum, tipo, descripcion y migraciones aplicadas no resueltas.
- Decision impact: La limpieza no borra ni modifica migraciones historicas; usa script nuevo idempotente para tablas vacias y preserva datos con filas.

<!-- BEGIN SDD TASK DESIGN TRACEABILITY -->
## Trazabilidad individual de diseno por task

Esta seccion normaliza la documentacion SDD para que cada task tenga una decision de diseno verificable, incluso cuando el detalle ampliado exista en secciones historicas anteriores.

### TASK-001 - Preparar Docker y variables de entorno para secretos
- Estado: Completada.
- Fase: Fase 0: Seguridad y base SDD.
- Decision de diseno: Define base de ejecucion SDD, configuracion inicial y controles minimos para avanzar con cambios trazables.
- Componentes/capas: infra/aws, docker-compose.

### TASK-002 - Crear estructura SDD local
- Estado: Completada.
- Fase: Fase 0: Seguridad y base SDD.
- Decision de diseno: Define base de ejecucion SDD, configuracion inicial y controles minimos para avanzar con cambios trazables.
- Componentes/capas: backend modular.

### TASK-003 - Disenar modelo de base de datos multiempresa
- Estado: Completada.
- Fase: Fase 1: Arquitectura y plataforma.
- Decision de diseno: Define decisiones base de arquitectura, contratos, version de plataforma y modelo multiempresa.
- Componentes/capas: tenant-service.

### TASK-004 - Definir contratos entre microservicios
- Estado: Completada.
- Fase: Fase 1: Arquitectura y plataforma.
- Decision de diseno: Define decisiones base de arquitectura, contratos, version de plataforma y modelo multiempresa.
- Componentes/capas: inventory-service.

### TASK-005 - Actualizar Spring Boot a version soportada/LTS
- Estado: Completada.
- Fase: Fase 1: Arquitectura y plataforma.
- Decision de diseno: Define decisiones base de arquitectura, contratos, version de plataforma y modelo multiempresa.
- Componentes/capas: backend modular.

### TASK-006 - Crear estructura Clean Architecture para `billing-service`
- Estado: Completada.
- Fase: Fase 1: Arquitectura y plataforma.
- Decision de diseno: Modela el nucleo de facturacion electronica/POS con dominio, casos de uso, persistencia y conector DIAN mock.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-007 - Crear migraciones versionadas de base de datos
- Estado: Completada.
- Fase: Fase 1: Arquitectura y plataforma.
- Decision de diseno: Modela el nucleo de facturacion electronica/POS con dominio, casos de uso, persistencia y conector DIAN mock.
- Componentes/capas: backend modular.

### TASK-008 - Implementar configuracion de emisor y resoluciones
- Estado: Completada.
- Fase: Fase 2: Facturacion electronica y conector DIAN.
- Decision de diseno: Modela el nucleo de facturacion electronica/POS con dominio, casos de uso, persistencia y conector DIAN mock.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-009 - Implementar calculo de factura electronica
- Estado: Completada.
- Fase: Fase 2: Facturacion electronica y conector DIAN.
- Decision de diseno: Modela el nucleo de facturacion electronica/POS con dominio, casos de uso, persistencia y conector DIAN mock.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-010 - Implementar puerto y adaptador de conexion DIAN
- Estado: Completada.
- Fase: Fase 2: Facturacion electronica y conector DIAN.
- Decision de diseno: Modela el nucleo de facturacion electronica/POS con dominio, casos de uso, persistencia y conector DIAN mock.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-011 - Implementar estados y trazabilidad de documentos electronicos
- Estado: Completada.
- Fase: Fase 2: Facturacion electronica y conector DIAN.
- Decision de diseno: Modela el nucleo de facturacion electronica/POS con dominio, casos de uso, persistencia y conector DIAN mock.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-012 - Implementar notas credito y debito
- Estado: Completada.
- Fase: Fase 2: Facturacion electronica y conector DIAN.
- Decision de diseno: Modela el nucleo de facturacion electronica/POS con dominio, casos de uso, persistencia y conector DIAN mock.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-013 - Implementar emision de documento equivalente electronico POS
- Estado: Completada.
- Fase: Fase 3: POS electronico.
- Decision de diseno: Modela el nucleo de facturacion electronica/POS con dominio, casos de uso, persistencia y conector DIAN mock.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-014 - Refactorizar modulos CRUD existentes hacia Clean Architecture
- Estado: Completada.
- Fase: Fase 4: Refactorizacion arquitectonica legacy.
- Decision de diseno: Migra funcionalidad legacy hacia Clean Architecture y retira codigo reemplazado bajo validacion.
- Componentes/capas: backend modular.

### TASK-015 - Implementar nota de ajuste POS
- Estado: Completada.
- Fase: Fase 5: POS electronico - ajustes.
- Decision de diseno: Migra funcionalidad legacy hacia Clean Architecture y retira codigo reemplazado bajo validacion.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-016 - Implementar movimientos de inventario
- Estado: Completada.
- Fase: Fase 6: Inventario.
- Decision de diseno: Migra funcionalidad legacy hacia Clean Architecture y retira codigo reemplazado bajo validacion.
- Componentes/capas: inventory-service.

### TASK-017 - Implementar validacion de disponibilidad
- Estado: Completada.
- Fase: Fase 6: Inventario.
- Decision de diseno: Migra funcionalidad legacy hacia Clean Architecture y retira codigo reemplazado bajo validacion.
- Componentes/capas: backend modular.

### TASK-018 - Implementar plan de cuentas basico
- Estado: Completada.
- Fase: Fase 7: Contabilidad base.
- Decision de diseno: Migra funcionalidad legacy hacia Clean Architecture y retira codigo reemplazado bajo validacion.
- Componentes/capas: accounting-service.

### TASK-019 - Completar refactor de modulos legacy restantes hacia Clean Architecture
- Estado: Completada.
- Fase: Fase 8: Refactorizacion legacy restante.
- Decision de diseno: Migra funcionalidad legacy hacia Clean Architecture y retira codigo reemplazado bajo validacion.
- Componentes/capas: backend modular.

### TASK-020 - Migrar persistencia y contratos legacy a Clean Architecture completa
- Estado: Completada.
- Fase: Fase 8: Refactorizacion legacy restante.
- Decision de diseno: Migra funcionalidad legacy hacia Clean Architecture y retira codigo reemplazado bajo validacion.
- Componentes/capas: backend modular.

### TASK-021 - Eliminar codigo muerto legacy despues de migracion Clean Architecture completa
- Estado: Completada.
- Fase: Fase 8: Refactorizacion legacy restante.
- Decision de diseno: Migra funcionalidad legacy hacia Clean Architecture y retira codigo reemplazado bajo validacion.
- Componentes/capas: backend modular.

### TASK-022 - Implementar asientos contables automaticos
- Estado: Completada.
- Fase: Fase 9: Contabilidad.
- Decision de diseno: Completa persistencia, API REST, contabilidad, auditoria, errores y pruebas locales para el backend inicial.
- Componentes/capas: accounting-service.

### TASK-023 - Implementar libro diario y libro mayor
- Estado: Completada.
- Fase: Fase 9: Contabilidad.
- Decision de diseno: Completa persistencia, API REST, contabilidad, auditoria, errores y pruebas locales para el backend inicial.
- Componentes/capas: accounting-service.

### TASK-024 - Estandarizar errores API
- Estado: Completada.
- Fase: Fase 10: Calidad, auditoria y observabilidad.
- Decision de diseno: Completa persistencia, API REST, contabilidad, auditoria, errores y pruebas locales para el backend inicial.
- Componentes/capas: backend modular.

### TASK-025 - Implementar auditoria fiscal
- Estado: Completada.
- Fase: Fase 10: Calidad, auditoria y observabilidad.
- Decision de diseno: Completa persistencia, API REST, contabilidad, auditoria, errores y pruebas locales para el backend inicial.
- Componentes/capas: billing-service, dian-provider-service, audit-service.

### TASK-026 - Implementar correlation ID y logs estructurados
- Estado: Completada.
- Fase: Fase 10: Calidad, auditoria y observabilidad.
- Decision de diseno: Completa persistencia, API REST, contabilidad, auditoria, errores y pruebas locales para el backend inicial.
- Componentes/capas: audit-service.

### TASK-027 - Implementar persistencia JPA y endpoints REST para billing/POS
- Estado: Completada.
- Fase: Fase 11: Pruebas end-to-end locales con Docker.
- Decision de diseno: Completa persistencia, API REST, contabilidad, auditoria, errores y pruebas locales para el backend inicial.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-028 - Implementar conector DIAN mock configurable
- Estado: Completada.
- Fase: Fase 11: Pruebas end-to-end locales con Docker.
- Decision de diseno: Completa persistencia, API REST, contabilidad, auditoria, errores y pruebas locales para el backend inicial.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-029 - Implementar persistencia JPA y endpoints REST para accounting
- Estado: Completada.
- Fase: Fase 11: Pruebas end-to-end locales con Docker.
- Decision de diseno: Completa persistencia, API REST, contabilidad, auditoria, errores y pruebas locales para el backend inicial.
- Componentes/capas: backend modular.

### TASK-030 - Crear seed local y guia de pruebas Docker
- Estado: Completada.
- Fase: Fase 11: Pruebas end-to-end locales con Docker.
- Decision de diseno: Completa persistencia, API REST, contabilidad, auditoria, errores y pruebas locales para el backend inicial.
- Componentes/capas: infra/aws, docker-compose.

### TASK-031 - Redisenar estructura Maven multi-modulo para microservicios fisicos
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: inventory-service.

### TASK-032 - Implementar `tenant-service` para empresas multiempresa
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: tenant-service.

### TASK-033 - Migrar catalogos y terceros legacy a Clean Architecture y microservicios
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: inventory-service, catalog-service, thirdparty-service.

### TASK-034 - Implementar `inventory-service` completo con costos, compras, stock y kardex
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: inventory-service.

### TASK-035 - Implementar venta completa y emision electronica conectada al flujo
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-036 - Separar `dian-provider-service` con mock configurable
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-037 - Conectar facturacion validada con inventario y contabilidad
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: billing-service, dian-provider-service, inventory-service, accounting-service.

### TASK-038 - Implementar prueba end-to-end Docker desde cero
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: infra/aws, docker-compose.

### TASK-039 - Identificar codigo y tablas legacy no usadas
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: facturaelectronica-web, bff-service.

### TASK-040 - Eliminar codigo muerto y tablas legacy reemplazadas
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: facturaelectronica-web, bff-service.

### TASK-041 - Migrar emisor, resoluciones y numeracion fiscal a billing-service
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-042 - Migrar audit-service como microservicio fisico
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: inventory-service.

### TASK-043 - Conectar billing-service como productor de auditoria fiscal
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: billing-service, dian-provider-service, inventory-service, audit-service.

### TASK-044 - Desacoplar dependencias de arranque entre microservicios
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: inventory-service.

### TASK-045 - Definir estrategia de mensajeria asincrona cloud
- Estado: Completada.
- Fase: Fase 12: Microservicios fisicos, desacoplamiento e infraestructura base.
- Decision de diseno: Separa el monolito en microservicios fisicos, conecta efectos entre dominios y define desacoplamiento/eventos cloud.
- Componentes/capas: infra/aws, docker-compose.

### TASK-046 - Cerrar diseno backend core pendiente antes de depuracion legacy
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Cierra reglas core de negocio: terceros, inventario, compras, ventas, contabilidad PUC, reportes, usuarios y licencias.
- Componentes/capas: microservicios.

### TASK-047 - Implementar terceros fiscales con DV NIT automatico
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Cierra reglas core de negocio: terceros, inventario, compras, ventas, contabilidad PUC, reportes, usuarios y licencias.
- Componentes/capas: billing-service, dian-provider-service, thirdparty-service.

### TASK-048 - Implementar bienes, servicios, insumos y referencias operativas
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Cierra reglas core de negocio: terceros, inventario, compras, ventas, contabilidad PUC, reportes, usuarios y licencias.
- Componentes/capas: inventory-service.

### TASK-049 - Ajustar ventas y documentos para bienes y servicios
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Cierra reglas core de negocio: terceros, inventario, compras, ventas, contabilidad PUC, reportes, usuarios y licencias.
- Componentes/capas: billing-service, dian-provider-service, inventory-service.

### TASK-050 - Implementar movimientos manuales de insumos
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Cierra reglas core de negocio: terceros, inventario, compras, ventas, contabilidad PUC, reportes, usuarios y licencias.
- Componentes/capas: inventory-service.

### TASK-051 - Implementar compras, gastos y cuentas por pagar
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Cierra reglas core de negocio: terceros, inventario, compras, ventas, contabilidad PUC, reportes, usuarios y licencias.
- Componentes/capas: inventory-service, accounting-service.

### TASK-052 - Completar documentos fiscales y consultas fiscales
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Cierra reglas core de negocio: terceros, inventario, compras, ventas, contabilidad PUC, reportes, usuarios y licencias.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-053 - Completar contabilidad parametrizable PUC
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Cierra reglas core de negocio: terceros, inventario, compras, ventas, contabilidad PUC, reportes, usuarios y licencias.
- Componentes/capas: accounting-service.

### TASK-054 - Implementar reportes minimos operativos, fiscales y contables
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Cierra reglas core de negocio: terceros, inventario, compras, ventas, contabilidad PUC, reportes, usuarios y licencias.
- Componentes/capas: billing-service, dian-provider-service, accounting-service.

### TASK-055 - Implementar cuentas por cobrar y recaudos
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Cierra reglas core de negocio: terceros, inventario, compras, ventas, contabilidad PUC, reportes, usuarios y licencias.
- Componentes/capas: accounting-service.

### TASK-056 - Implementar usuarios, roles, permisos y auditoria de acceso
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Cierra reglas core de negocio: terceros, inventario, compras, ventas, contabilidad PUC, reportes, usuarios y licencias.
- Componentes/capas: identity-service, bff-service, audit-service.

### TASK-057 - Implementar licenciamiento por empresa
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Cierra reglas core de negocio: terceros, inventario, compras, ventas, contabilidad PUC, reportes, usuarios y licencias.
- Componentes/capas: tenant-service.

### TASK-058 - Migrar legacy pendiente al modelo Clean Architecture completo
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Cierra reglas core de negocio: terceros, inventario, compras, ventas, contabilidad PUC, reportes, usuarios y licencias.
- Componentes/capas: microservicios.

### TASK-059 - Depurar y eliminar codigo muerto, endpoints y tablas legacy
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Alinea la arquitectura objetivo AWS, BFF, ECS Fargate, Lambda y patron Outbox/Inbox para eventos.
- Componentes/capas: facturaelectronica-web, bff-service.

### TASK-060 - Definir arquitectura cloud AWS, BFF y clasificacion ECS Fargate/Lambda
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Alinea la arquitectura objetivo AWS, BFF, ECS Fargate, Lambda y patron Outbox/Inbox para eventos.
- Componentes/capas: infra/aws, docker-compose.

### TASK-061 - Implementar IaC AWS inicial para frontend, BFF, ECS Fargate, RDS y servicios base
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Alinea la arquitectura objetivo AWS, BFF, ECS Fargate, Lambda y patron Outbox/Inbox para eventos.
- Componentes/capas: inventory-service, facturaelectronica-web, bff-service, infra/aws, docker-compose.

### TASK-062 - Implementar event-driven AWS con Outbox/Inbox, EventBridge/SQS y Lambdas
- Estado: Completada.
- Fase: Fase 13: Backend core fiscal, terceros, inventario, contabilidad y reportes.
- Decision de diseno: Alinea la arquitectura objetivo AWS, BFF, ECS Fargate, Lambda y patron Outbox/Inbox para eventos.
- Componentes/capas: infra/aws, docker-compose.

### TASK-063 - Disenar e implementar frontend SPA y BFF inicial
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: facturaelectronica-web, bff-service.

### TASK-064 - Optimizar gestion de conexiones PostgreSQL por microservicio
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: billing-service, dian-provider-service, inventory-service.

### TASK-065 - Mejorar frontend con login, empresa activa y formularios controlados
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: identity-service, bff-service, tenant-service, facturaelectronica-web.

### TASK-066 - Restringir UI operativa por sesion y licencia activa
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: identity-service, bff-service, tenant-service.

### TASK-067 - RediseÃ¯Â¿Â½ar experiencia visual profesional de toda la aplicacion
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: BFF/SPA y servicios core.

### TASK-068 - Disenar RBAC modular con ROOT global y roles por empresa
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: identity-service, bff-service, tenant-service.

### TASK-069 - Implementar RBAC modular en identity-service
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: identity-service, bff-service.

### TASK-070 - Implementar UI de administracion de usuarios, roles y permisos
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: identity-service, bff-service.

### TASK-071 - Aplicar navegacion y acciones frontend basadas en permisos efectivos
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: identity-service, bff-service, facturaelectronica-web.

### TASK-072 - Implementar bootstrap ROOT minimo para pruebas locales
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: BFF/SPA y servicios core.

### TASK-073 - Completar flujo ROOT operativo
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: BFF/SPA y servicios core.

### TASK-074 - Corregir tipo de documento de empresa a codigo DIAN numerico
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: billing-service, dian-provider-service, tenant-service.

### TASK-075 - Unificar tipos de documento de identificacion como codigo DIAN numerico
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: billing-service, dian-provider-service, catalog-service.

### TASK-076 - Ajustar experiencia funcional colombiana y RBAC operativo
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: identity-service, bff-service.

### TASK-077 - Modularizar SPA frontend por Clean Code y SOLID
- Estado: Completada.
- Fase: Fase 14: BFF, frontend, RBAC y UX operativa.
- Decision de diseno: Construye la capa frontend/BFF inicial, sesion local, RBAC, permisos, formularios y UX administrativa.
- Componentes/capas: facturaelectronica-web, bff-service.

### TASK-078 - Importar catalogo completo DIVIPOLA para municipios colombianos
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Decision de diseno: Parametriza catalogos colombianos, DIVIPOLA, reglas fiscales, consumidores finales, scanner POS e impuestos por producto.
- Componentes/capas: catalog-service.

### TASK-079 - Parametrizar responsabilidades fiscales, regimenes y medios de pago
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Decision de diseno: Parametriza catalogos colombianos, DIVIPOLA, reglas fiscales, consumidores finales, scanner POS e impuestos por producto.
- Componentes/capas: billing-service, dian-provider-service, catalog-service.

### TASK-080 - Mejorar seleccion de responsabilidades fiscales
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Decision de diseno: Parametriza catalogos colombianos, DIVIPOLA, reglas fiscales, consumidores finales, scanner POS e impuestos por producto.
- Componentes/capas: billing-service, dian-provider-service, catalog-service.

### TASK-081 - Persistir sesion y cerrar por inactividad
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Decision de diseno: Parametriza catalogos colombianos, DIVIPOLA, reglas fiscales, consumidores finales, scanner POS e impuestos por producto.
- Componentes/capas: identity-service, bff-service.

### TASK-082 - Pulir UX de login y venta POS
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Decision de diseno: Parametriza catalogos colombianos, DIVIPOLA, reglas fiscales, consumidores finales, scanner POS e impuestos por producto.
- Componentes/capas: billing-service, dian-provider-service, identity-service, bff-service, facturaelectronica-web.

### TASK-083 - Buscador de cliente en Venta POS
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Decision de diseno: Parametriza catalogos colombianos, DIVIPOLA, reglas fiscales, consumidores finales, scanner POS e impuestos por producto.
- Componentes/capas: billing-service, dian-provider-service, thirdparty-service.

### TASK-084 - NIT y digito de verificacion segun concepto DIAN
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Decision de diseno: Parametriza catalogos colombianos, DIVIPOLA, reglas fiscales, consumidores finales, scanner POS e impuestos por producto.
- Componentes/capas: billing-service, dian-provider-service, thirdparty-service.

### TASK-085 - Simplificar registro de clientes naturales
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Decision de diseno: Parametriza catalogos colombianos, DIVIPOLA, reglas fiscales, consumidores finales, scanner POS e impuestos por producto.
- Componentes/capas: thirdparty-service.

### TASK-086 - Persistir y administrar catalogos oficiales y operativos
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Decision de diseno: Parametriza catalogos colombianos, DIVIPOLA, reglas fiscales, consumidores finales, scanner POS e impuestos por producto.
- Componentes/capas: catalog-service.

### TASK-087 - Crear modulo administrativo de catalogos parametrizables
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Decision de diseno: Parametriza catalogos colombianos, DIVIPOLA, reglas fiscales, consumidores finales, scanner POS e impuestos por producto.
- Componentes/capas: catalog-service.

### TASK-088 - Auditar y eliminar tablas legacy no usadas
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Decision de diseno: Parametriza catalogos colombianos, DIVIPOLA, reglas fiscales, consumidores finales, scanner POS e impuestos por producto.
- Componentes/capas: facturaelectronica-web, bff-service.

### TASK-089 - Configurar impuestos por producto, scanner POS y consumidor final parametrizable
- Estado: Completada.
- Fase: Fase 15: Catalogos colombianos y reglas fiscales operativas.
- Decision de diseno: Parametriza catalogos colombianos, DIVIPOLA, reglas fiscales, consumidores finales, scanner POS e impuestos por producto.
- Componentes/capas: billing-service, dian-provider-service, inventory-service.

### TASK-090 - Definir politica transversal de auditoria para acciones mutables
- Estado: Completada.
- Fase: Fase 16: Auditoria, logs y acciones mutables.
- Decision de diseno: Centraliza auditoria, logs y experiencia de acciones mutables mediante modales y consulta operativa.
- Componentes/capas: audit-service.

### TASK-091 - Permitir administracion ROOT auditada de catalogos globales
- Estado: Completada.
- Fase: Fase 16: Auditoria, logs y acciones mutables.
- Decision de diseno: Centraliza auditoria, logs y experiencia de acciones mutables mediante modales y consulta operativa.
- Componentes/capas: catalog-service.

### TASK-092 - Reemplazar paneles Respuesta/Error por modal de proceso
- Estado: Completada.
- Fase: Fase 16: Auditoria, logs y acciones mutables.
- Decision de diseno: Centraliza auditoria, logs y experiencia de acciones mutables mediante modales y consulta operativa.
- Componentes/capas: facturaelectronica-web, bff-service.

### TASK-093 - Modulo Logs/Auditoria para ROOT y administradores
- Estado: Completada.
- Fase: Fase 16: Auditoria, logs y acciones mutables.
- Decision de diseno: Centraliza auditoria, logs y experiencia de acciones mutables mediante modales y consulta operativa.
- Componentes/capas: audit-service.

### TASK-094 - Eliminar `initialState` demo y catalogos locales de negocio en frontend
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: catalog-service, thirdparty-service, facturaelectronica-web, bff-service.

### TASK-095 - Completar catalogos DB-only para UI operativa
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: catalog-service.

### TASK-096 - Ajustar modales de proceso y mensajes contextuales
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: facturaelectronica-web, bff-service.

### TASK-097 - Redisenar modulo Logs/Auditoria
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: audit-service.

### TASK-098 - Mejorar UX de uso de items de inventario
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: inventory-service, facturaelectronica-web, bff-service.

### TASK-099 - Disenar modulo contable funcional v2
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: accounting-service.

### TASK-100 - Implementar contabilidad operativa v2
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: accounting-service.

### TASK-101 - Disenar modulo de nomina y clasificacion laboral/contractual
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: accounting-service.

### TASK-102 - Crear `payroll-service`
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: identity-service, bff-service.

### TASK-103 - Implementar pagos diarios verbales/jornal
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: BFF/SPA y servicios core.

### TASK-104 - Configurar nomina electronica opcional por empresa
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: accounting-service, tenant-service.

### TASK-105 - Implementar nomina electronica mock
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: accounting-service.

### TASK-106 - Integrar nomina con contabilidad
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: accounting-service.

### TASK-107 - Endurecer RBAC para catalogos, logs, contabilidad y nomina
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: accounting-service, identity-service, bff-service, catalog-service, audit-service.

### TASK-108 - Mejorar frontend profesional y componentes reutilizables
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: facturaelectronica-web, bff-service.

### TASK-109 - Prueba E2E desde cero sin datos demo frontend
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: facturaelectronica-web, bff-service.

### TASK-110 - Revision normativa y catalogos de cumplimiento
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: catalog-service.

### TASK-111 - Preparacion cloud/productiva para nuevos modulos
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: infra/aws, docker-compose.

### TASK-112 - Commit y reporte de cierre de fase
- Estado: Completada.
- Fase: Fase 17: Catalogos DB-only, contabilidad v2 y nomina.
- Decision de diseno: Elimina datos demo de UI, usa catalogos DB-only y expande contabilidad/nomina con integraciones auditables.
- Componentes/capas: accounting-service.

### TASK-113 - Implementar consumo asistido de insumos por servicios facturados
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: billing-service, dian-provider-service, inventory-service.

### TASK-114 - Corregir error de login por licencia no configurada
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: identity-service, bff-service, tenant-service.

### TASK-115 - Extender licencias empresariales con modulos contratados
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: tenant-service.

### TASK-116 - Crear modulo ROOT para administrar licencias
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: tenant-service.

### TASK-117 - Aplicar licencia por modulo en menues y operaciones
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: tenant-service.

### TASK-118 - Auditar administracion de licencias
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: tenant-service.

### TASK-119 - E2E licencia parametrizable
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: tenant-service.

### TASK-120 - Aplicar cuotas comerciales de licencia
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: tenant-service.

### TASK-121 - Ajustar UX/RBAC de empresa y permisos
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: identity-service, bff-service, tenant-service, facturaelectronica-web.

### TASK-122 - Unificar permiso de Venta POS e internacionalizar permisos
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: billing-service, dian-provider-service, identity-service, bff-service.

### TASK-123 - Redisenar navegacion principal con submenus
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: facturaelectronica-web, bff-service.

### TASK-124 - Crear pantalla exclusiva de Roles
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: identity-service, bff-service.

### TASK-125 - Crear pantalla exclusiva de Usuarios
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: identity-service, bff-service.

### TASK-126 - Reducir autocierre de modales exitosos
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: facturaelectronica-web, bff-service.

### TASK-127 - Endurecer contratos backend de roles
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: identity-service, bff-service.

### TASK-128 - Actualizar e inactivar usuarios empresariales
- Estado: Completada.
- Fase: Fase 18: Licencias, servicios con insumos y UX/RBAC empresarial.
- Decision de diseno: Extiende licenciamiento, cuotas, servicios con insumos, navegacion, roles, usuarios y modales operativos.
- Componentes/capas: identity-service, bff-service, tenant-service.

### TASK-129 - Implementar E2E operativo desde cero para venta POS electronica
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-130 - Completar compras y entradas de inventario con contabilidad
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: inventory-service, accounting-service.

### TASK-131 - Completar servicios facturables con consumo manual de insumos
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: billing-service, dian-provider-service, inventory-service.

### TASK-132 - Crear listados operativos profesionales
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: BFF/SPA y servicios core.

### TASK-133 - Endurecer validacion backend de RBAC y licencias
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: identity-service, bff-service, tenant-service.

### TASK-134 - Tablero ROOT de uso de licencias
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: tenant-service.

### TASK-135 - Auditoria transversal verificable
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: audit-service.

### TASK-136 - Robustecer sesion, expiracion y restauracion
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: identity-service, bff-service.

### TASK-137 - Parametrizar reglas contables PUC por evento
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: accounting-service, infra/aws, docker-compose.

### TASK-138 - Generar comprobantes/asientos automaticos
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: accounting-service.

### TASK-139 - Implementar reportes minimos contables y operativos
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: accounting-service.

### TASK-140 - Pruebas de contrato BFF/microservicios
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: inventory-service.

### TASK-141 - E2E de aislamiento multiempresa
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: tenant-service.

### TASK-142 - Completar Terraform AWS productivo
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: infra/aws, docker-compose.

### TASK-143 - Completar eventos productivos AWS
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: infra/aws, docker-compose.

### TASK-144 - Mejorar tablas administrativas de usuarios y roles
- Estado: Completada.
- Fase: Fase 19: Productizacion operativa, E2E, reportes e infraestructura AWS.
- Decision de diseno: Cierra productizacion operativa con E2E, listados, RBAC backend, reportes, Terraform AWS y eventos productivos.
- Componentes/capas: identity-service, bff-service, facturaelectronica-web.

### TASK-145 - Replantear alcance DIAN como software parametrizable por empresa
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: Define evolucion de dian-provider-service como conector DIAN parametrizable por empresa, sin rol de proveedor tecnologico.
- Componentes/capas: billing-service, dian-provider-service, tenant-service.

### TASK-146 - Disenar modulo de configuracion DIAN por empresa
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: Define evolucion de dian-provider-service como conector DIAN parametrizable por empresa, sin rol de proveedor tecnologico.
- Componentes/capas: billing-service, dian-provider-service, tenant-service.

### TASK-147 - Persistencia segura de certificados y secretos DIAN por empresa
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: Define evolucion de dian-provider-service como conector DIAN parametrizable por empresa, sin rol de proveedor tecnologico.
- Componentes/capas: billing-service, dian-provider-service, tenant-service, infra/aws, docker-compose.

### TASK-148 - Ajustar contratos API para configuracion DIAN y prueba de conexion
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: Define evolucion de dian-provider-service como conector DIAN parametrizable por empresa, sin rol de proveedor tecnologico.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-149 - Actualizar infraestructura AWS para secretos DIAN por empresa
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: Define evolucion de dian-provider-service como conector DIAN parametrizable por empresa, sin rol de proveedor tecnologico.
- Componentes/capas: billing-service, dian-provider-service, tenant-service, infra/aws, docker-compose.

### TASK-150 - Renombrar lenguaje funcional de proveedor a conector DIAN
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: Define evolucion de dian-provider-service como conector DIAN parametrizable por empresa, sin rol de proveedor tecnologico.
- Componentes/capas: billing-service, dian-provider-service, thirdparty-service.

### TASK-151 - Preparar flujo tecnico DIAN real segun caja de herramientas
- Estado: Completada como preparacion tecnica. `dian-provider-service` incorpora un puerto de artefactos tecnicos DIAN y un adaptador filesystem que valida XSD UBL 2.1, Schematron DIAN, XSL compilado y lista de codigos configurables antes de aprobar pruebas en modo real. El cierre funcional de envio DIAN real configurable queda cubierto en Fase 20 TASK-153 a TASK-163.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: Define evolucion de dian-provider-service como conector DIAN parametrizable por empresa, sin rol de proveedor tecnologico.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-152 - Implementar UI de Configuracion DIAN por empresa
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: Define evolucion de dian-provider-service como conector DIAN parametrizable por empresa, sin rol de proveedor tecnologico.
- Componentes/capas: billing-service, dian-provider-service, tenant-service.

### TASK-153 - Actualizar base normativa DIAN vigente y matriz de versionado tecnico
- Estado: Completada.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: El envio real DIAN se implementa solo contra fuentes oficiales verificadas y versionadas por fecha/anexo; toda diferencia normativa queda documentada antes de codificar.
- Componentes/capas: SDD/documentacion, billing-service, dian-provider-service.

### TASK-154 - Disenar generacion XML UBL 2.1 para documentos fiscales
- Estado: Completada. Implementado `FiscalDocumentXmlBuilderPort` y `DefaultFiscalDocumentXmlBuilderAdapter` con XML UBL 2.1 base por tipo documental.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: La generacion XML se encapsula en builders por tipo documental y version tecnica, alimentados por snapshots fiscales canonicos.
- Componentes/capas: billing-service, dian-provider-service.

### TASK-155 - Disenar calculo CUFE/CUDE y QR productivo
- Estado: Completada. Implementado `DianIdentifierCalculationPort` y `Sha256DianIdentifierCalculationAdapter` con identificador/QR determinista sin exponer claves.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: CUFE/CUDE/QR se calculan en puertos deterministas con fixtures sanitizados y sin exponer claves tecnicas.
- Componentes/capas: dian-provider-service.

### TASK-156 - Implementar firma XML con certificado empresarial
- Estado: Completada como adaptador de referencia. `ReferenceDianSignatureAdapter` usa referencias seguras y digest trazable; XMLDSig/XAdES certificado se conecta reemplazando el adaptador cuando existan certificado y fixtures oficiales.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: La firma usa certificados propios de cada empresa desde gestor de secretos y falla cerrado ante secreto ausente, vencido o no autorizado.
- Componentes/capas: dian-provider-service, infra/aws.

### TASK-157 - Implementar validacion XSD, Schematron y listas de codigos
- Estado: Completada como validacion tecnica base. `BasicDianTechnicalValidationAdapter` valida estructura, UBL, identificadores y firma de referencia; la compuerta de artefactos falla cerrado si faltan XSD/Schematron/XSL/listas configuradas.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: La validacion tecnica local bloquea el transporte DIAN real cuando XML, reglas o catalogos no cumplen el anexo vigente.
- Componentes/capas: dian-provider-service.

### TASK-158 - Implementar transporte real DIAN para habilitacion y produccion
- Estado: Completada como transporte configurable. `ConfigurableDianTransportAdapter` soporta modo `stub` para pruebas controladas y modo `http` por URL/configuracion empresarial; el modo real no degrada a mock.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: El transporte real se resuelve por configuracion empresarial, separando habilitacion/produccion, timeouts, correlacion e idempotencia.
- Componentes/capas: dian-provider-service, infra/aws.

### TASK-159 - Persistir respuestas DIAN y ApplicationResponse
- Estado: Completada. Flyway V003 crea trazas, artefactos y resultados de validacion DIAN; JPA persiste eventos, ApplicationResponse y metadata segura.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: Las respuestas DIAN se guardan como eventos/estados tecnicos asociados al documento fiscal, con mensajes sanitizados y trazabilidad de tracking.
- Componentes/capas: dian-provider-service, billing-service.

### TASK-160 - Implementar reintentos DIAN e idempotencia de efectos posteriores
- Estado: Completada. `DianProviderSubmissionService` aplica idempotencia por empresa/documento/tipo/idempotency key y retorna el envio existente sin duplicar persistencia.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: Los reintentos usan claves idempotentes y estados terminales para no duplicar documentos, inventario ni contabilidad.
- Componentes/capas: billing-service, dian-provider-service, provider-submission-retry-lambda.

### TASK-161 - Almacenar artefactos fiscales reales de forma segura
- Estado: Completada para almacenamiento local seguro de desarrollo. `LocalFiscalArtifactStorageAdapter` guarda referencias privadas, hash y metadata; S3/KMS queda como destino productivo equivalente.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: Los artefactos fiscales reales se almacenan en storage privado con hash, metadata y acceso por BFF/RBAC.
- Componentes/capas: dian-provider-service, billing-service, bff-service, infra/aws.

### TASK-162 - Ajustar contratos API y modelo de datos para DIAN real
- Estado: Completada. Contratos REST usan artefactos neutrales `provider://submissions/{trackingId}/artifacts`; modelo `dian_provider` incorpora trazas tecnicas.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: Los contratos y tablas objetivo consolidan headers, errores funcionales, respuesta DIAN, artefactos e idempotencia.
- Componentes/capas: SDD/documentacion, billing-service, dian-provider-service.

### TASK-163 - Definir suite obligatoria de pruebas DIAN antes de produccion
- Estado: Completada. Suite base de `dian-provider-service` valida controlador, mock, flujo real stub/http, idempotencia, configuracion incompleta y compuerta tecnica; produccion requiere E2E de habilitacion con credenciales reales de empresa.
- Fase: Fase 20: Backlog DIAN real parametrizable por empresa.
- Decision de diseno: La produccion real DIAN queda bloqueada hasta contar con pruebas unitarias, integracion, contrato y E2E con fixtures sanitizados.
- Componentes/capas: dian-provider-service, billing-service, scripts/E2E.

### TASK-164 - Disenar autenticacion productiva con Cognito Hosted UI y PKCE
- Estado: Completada. Implementado modulo Cognito Terraform, modo `AUTH_MODE=cognito`, fail-closed productivo, PKCE S256, callback/token exchange, puente Cognito -> identidad interna por `sub` persistente con alta previa por email y sesion cifrada local.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Decision de diseno: Define autenticacion productiva con Cognito, sesion BFF segura, MFA, CSRF, headers y manejo de secretos runtime.
- Componentes/capas: identity-service, bff-service, thirdparty-service.

### TASK-165 - Reemplazar tokens en SPA por sesion BFF con cookie segura
- Estado: Completada. Implementados endpoint de sesion, cookies base, hidratacion SPA por cookie, proxy con autorizacion interna server-side, logout y sanitizacion de `sessionStorage` para sesiones Cognito/cookie. El bearer queda limitado a modo local/E2E.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Decision de diseno: Define autenticacion productiva con Cognito, sesion BFF segura, MFA, CSRF, headers y manejo de secretos runtime.
- Componentes/capas: identity-service, bff-service, facturaelectronica-web.

### TASK-166 - Crear almacenamiento server-side de sesion cifrada
- Estado: Completada. `bff-service` usa `BffSessionStore` como puerto interno, `JdbcBffSessionStore` por defecto con PostgreSQL/Flyway en schema `bff` para ECS multi tarea y `BffEncryptedSessionStore` en memoria solo como fallback explicito (`BFF_SESSION_STORE=memory`). La cookie mantiene un identificador opaco, la base persiste su hash SHA-256 y los payloads de intentos OAuth/sesiones se cifran con AES-GCM antes de guardar.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Decision de diseno: Define autenticacion productiva con Cognito, sesion BFF segura, MFA, CSRF, headers y manejo de secretos runtime.
- Componentes/capas: identity-service, bff-service.

### TASK-167 - Implementar logout seguro y revocacion
- Estado: Completada. Logout limpia cookies BFF, CSRF y OAuth attempt, invalida sesion BFF, invoca `identity-service` para revocar el token interno y registrar auditoria `LOGOUT`, y revoca `refresh_token` Cognito de forma best-effort cuando existe.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Decision de diseno: Define autenticacion productiva con Cognito, sesion BFF segura, MFA, CSRF, headers y manejo de secretos runtime.
- Componentes/capas: identity-service, bff-service.

### TASK-168 - Hardening frontend contra exposicion de datos sensibles
- Estado: Completada. Build productivo sin sourcemaps, fetch con cookies/CSRF, snapshots Cognito/cookie sin tokens e hidratacion productiva sin `Authorization` construido en JavaScript. El token local queda acotado a desarrollo/E2E.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Decision de diseno: Define autenticacion productiva con Cognito, sesion BFF segura, MFA, CSRF, headers y manejo de secretos runtime.
- Componentes/capas: billing-service, dian-provider-service, facturaelectronica-web, bff-service.

### TASK-169 - Agregar security headers CloudFront/BFF
- Estado: Completada.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Decision de diseno: Define autenticacion productiva con Cognito, sesion BFF segura, MFA, CSRF, headers y manejo de secretos runtime.
- Componentes/capas: infra/aws, docker-compose.

### TASK-170 - Implementar proteccion CSRF para sesiones por cookie
- Estado: Completada.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Decision de diseno: Define autenticacion productiva con Cognito, sesion BFF segura, MFA, CSRF, headers y manejo de secretos runtime.
- Componentes/capas: identity-service, bff-service.

### TASK-171 - MFA obligatorio para ROOT, administradores y acciones criticas
- Estado: Completada. Cognito User Pool habilita software token MFA y grupos base; BFF deriva `mfaAuthenticated` desde el `id_token` Cognito y bloquea mutaciones criticas de plataforma/empresa cuando falta MFA, manteniendo ventas POS sin MFA adicional.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Decision de diseno: Define autenticacion productiva con Cognito, sesion BFF segura, MFA, CSRF, headers y manejo de secretos runtime.
- Componentes/capas: identity-service, bff-service.

### TASK-172 - Provisionamiento runtime de secretos AWS por empresa
- Estado: Completada.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Decision de diseno: Define autenticacion productiva con Cognito, sesion BFF segura, MFA, CSRF, headers y manejo de secretos runtime.
- Componentes/capas: tenant-service, infra/aws, docker-compose.

### TASK-173 - Auditoria de seguridad transversal
- Estado: Completada. Mutaciones BFF cuentan con auditoria best-effort; CSRF y MFA generan eventos `BFF_SECURITY` company-scoped cuando existe `X-Company-Id`; identity audita `COGNITO_LOGIN`, `LINK_COGNITO_SUBJECT` y `LOGOUT`.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Decision de diseno: Define autenticacion productiva con Cognito, sesion BFF segura, MFA, CSRF, headers y manejo de secretos runtime.
- Componentes/capas: audit-service.

### TASK-174 - Modo transicion local y bloqueo productivo de auth dummy
- Estado: Completada.
- Fase: Fase 21: Backlog autenticacion productiva y hardening.
- Decision de diseno: Define autenticacion productiva con Cognito, sesion BFF segura, MFA, CSRF, headers y manejo de secretos runtime.
- Componentes/capas: identity-service, bff-service.

### TASK-175 - Cerrar consistencia documental SDD antes de nueva implementacion
- Estado: Completada.
- Fase: Fase 22: Gobierno SDD, diagramas y limpieza final.
- Decision de diseno: Gobierna consistencia SDD, diagramas, brechas documentales y limpieza final antes de nuevas mejoras.
- Componentes/capas: SDD/documentacion.

### TASK-176 - Actualizar diagramas Mermaid a la arquitectura y modelo vigentes
- Estado: Completada.
- Fase: Fase 22: Gobierno SDD, diagramas y limpieza final.
- Decision de diseno: Gobierna consistencia SDD, diagramas, brechas documentales y limpieza final antes de nuevas mejoras.
- Componentes/capas: SDD/documentacion.

### TASK-177 - Cerrar brechas documentales de estado actual versus objetivo
- Estado: Completada.
- Fase: Fase 22: Gobierno SDD, diagramas y limpieza final.
- Decision de diseno: Gobierna consistencia SDD, diagramas, brechas documentales y limpieza final antes de nuevas mejoras.
- Componentes/capas: SDD/documentacion.

### TASK-178 - Ejecutar limpieza final legacy y artefactos huerfanos antes de nuevas mejoras
- Estado: Completada.
- Fase: Fase 22: Gobierno SDD, diagramas y limpieza final.
- Decision de diseno: Gobierna consistencia SDD, diagramas, brechas documentales y limpieza final antes de nuevas mejoras.
- Componentes/capas: SDD/documentacion.

## TASK-179 a TASK-189: marca, branding, reportes avanzados e impresion POS

### Decisiones de diseno

- La marca publica de la aplicacion sera `NexoFiscal`; los nombres historicos del frontend deben migrarse sin cambiar nombres tecnicos de paquetes o esquemas si no aportan valor funcional inmediato.
- El branding empresarial pertenece al bounded context de `tenant-service`, porque describe a la empresa contratante y su experiencia visual.
- Los archivos de logo/favicon no deben almacenarse como binarios pesados en PostgreSQL; la base de datos guarda metadata, hash y referencia de almacenamiento. En local se puede usar volumen controlado; en AWS el objetivo es S3 privado con KMS y acceso por BFF/CloudFront controlado.
- La SPA aplica branding como sincronizacion con sistemas externos del navegador: titulo, favicon y logo visible se actualizan al cambiar empresa activa o sesion.
- Los formatos iniciales permitidos para branding deben ser PNG, JPEG, WebP e ICO. SVG solo debe evaluarse en una tarea posterior si existe sanitizacion estricta, porque puede introducir riesgo XSS.
- El modulo de reportes debe ser guiado por catalogo de reportes: tipo de reporte, filtros dinamicos, opciones de datos, graficos permitidos y formatos de exportacion.
- `reporting-service` existe como microservicio fisico desde TASK-185 para reportes avanzados; las exportaciones y proyecciones asincronas siguen evolucionando en tareas posteriores. El BFF no debe convertirse en motor de reportes.
- Los reportes deben usar datos canonicos de servicios duenos o proyecciones reconstruibles desde eventos. Ninguna proyeccion puede ser la unica fuente de verdad.
- El reporte de ventas por vendedor debe resolver vendedores desde `identity-service` por rol/permiso efectivo de ventas y cruzar con ventas confirmadas de `billing-service`.
- La exportacion debe iniciar con CSV/Excel para reportes tabulares e historicos. PDF gerencial queda como salida adicional parametrizable cuando existan plantillas aprobadas.
- Los artefactos POS deben conservar representacion imprimible y metadata fiscal para consulta, descarga y reimpresion posterior.
- La impresion POS se implementa por fases: primero impresion web con CSS para 58/80 mm, luego conector directo ESC/POS/WebUSB/WebSerial/agente local solo despues de validar impresoras reales y riesgos de seguridad del entorno local.

### Context7 evidence

- Library/tool: React (`/reactjs/react.dev`).
- Topic consulted: controlled forms and effects for browser synchronization.
- Relevant finding: React recommends controlled state for forms and `useEffect` with cleanup when synchronizing components with external browser systems.
- Decision impact: La SPA aplicara branding, favicon y estado de filtros de reportes desde estado controlado y efectos acotados.

- Library/tool: Spring Boot (`/spring-projects/spring-boot`).
- Topic consulted: type-safe external configuration properties for custom application settings.
- Relevant finding: Spring Boot recomienda `@ConfigurationProperties` para agrupar configuracion externa tipada en lugar de dispersar `@Value`.
- Decision impact: `bff.auth.session-store` queda como configuracion externa controlada (`jdbc` por defecto, `memory` fallback) y el BFF conserva propiedades tipadas para sesion/Cognito.

- Library/tool: Spring Boot (`/spring-projects/spring-boot`).
- Topic consulted: multipart file upload.
- Relevant finding: Spring Boot uses the standard Jakarta Servlet multipart support and exposes multipart limits through `spring.servlet.multipart.max-file-size` and `spring.servlet.multipart.max-request-size`.
- Decision impact: El backend de branding usara multipart nativo con limites explicitos por configuracion, sin dependencia adicional hasta que exista una razon aprobada.

- Library/tool: Spring Boot (`/spring-projects/spring-boot/v3.5.9`).
- Topic consulted: Spring MVC controllers and `ResponseEntity` for file downloads.
- Relevant finding: Spring MVC controllers can return `ResponseEntity` to customize status, headers and body; resources support HTTP range behavior when returned through MVC.
- Decision impact: TASK-187 implementa exportacion CSV/XLS sin dependencia adicional, usando `ResponseEntity<byte[]>`, `Content-Disposition` y tipos de contenido explicitos.

- Library/tool: AWS SDK for Java v2 (`/aws/aws-sdk-java-v2`).
- Topic consulted: S3 client and default credential resolution.
- Relevant finding: El SDK soporta clientes S3 con `DefaultCredentialsProvider`, evitando credenciales estaticas en codigo.
- Decision impact: La implementacion local usa filesystem parametrizable y deja preparado el puerto `BrandingAssetStoragePort` para adaptar S3 privado/KMS en AWS sin tocar dominio.

### Trazabilidad de diseno

- Requisitos: RF-131 a RF-145.
- Acceptance criteria: AC-193 a AC-208.
- Tareas: TASK-179 a TASK-189.
- Componentes/capas: `facturaelectronica-web`, `bff-service`, `tenant-service`, `billing-service`, `identity-service`, `reporting-service` objetivo, `audit-service`, `infra/aws`.

### TASK-179 - Adoptar marca NexoFiscal en frontend y documentacion visible
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Decision de diseno: Migrar textos publicos a NexoFiscal conservando compatibilidad tecnica donde el renombrado no sea necesario.
- Componentes/capas: `facturaelectronica-web`, README y SDD.

### TASK-180 - Disenar branding empresarial parametrizable
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Decision de diseno: Branding como configuracion del tenant con metadata en DB y archivos en almacenamiento seguro.
- Componentes/capas: `tenant-service`, `bff-service`, `facturaelectronica-web`, `infra/aws`.

### TASK-181 - Implementar backend de branding empresarial
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Decision de diseno: Endpoints multipart con validacion estricta, RBAC en BFF y auditoria de mutaciones por canal comun BFF.
- Componentes/capas: `tenant-service`, `bff-service`, `audit-service`.

### TASK-182 - Implementar UI de branding y aplicacion dinamica
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Decision de diseno: La SPA aplica logo/favicon/titulo por empresa activa con estado controlado y fallback NexoFiscal.
- Componentes/capas: `facturaelectronica-web`.

### TASK-183 - Disenar artefactos fiscales, comprobantes POS e impresion termica
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Decision de diseno: Artefactos persistidos y estrategia de impresion por fases.
- Componentes/capas: `billing-service`, `bff-service`, `facturaelectronica-web`.

### TASK-184 - Disenar reporting-service y contratos de reportes avanzados
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Decision de diseno: Reportes avanzados como microservicio cuando haya agregaciones, historicos y exportaciones.
- Componentes/capas: `reporting-service` objetivo, `bff-service`, servicios duenos de datos.

### TASK-185 - Implementar reporting-service con reportes iniciales
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Decision de diseno: `reporting-service` usa Clean Architecture y orquesta fuentes canonicas por REST interno, sin duplicar datos de negocio.
- Componentes/capas: `reporting-service`, `billing-service`, `inventory-service`, `accounting-service`, `payroll-service`, `tenant-service`, `identity-service`.

### TASK-186 - Implementar UI avanzada de reportes
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Decision de diseno: La SPA consume catalogo, opciones y query desde `reporting-service` via BFF; renderiza filtros genericos, tabla obligatoria y visualizacion inicial segun `chartTypes` permitidos.
- Componentes/capas: `facturaelectronica-web`, `bff-service`.

### TASK-187 - Implementar exportacion de reportes
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Decision de diseno: Exportaciones CSV y XLS compatible con Excel como descarga sincrona inicial, reutilizando el query validado del reporte; exportaciones pesadas quedan para procesamiento asincrono posterior.
- Componentes/capas: `reporting-service`, `bff-service`, `infra/aws`.

### TASK-188 - Implementar comprobante POS imprimible e impresion web
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Decision de diseno: Comprobante POS HTML reproducible desde la venta confirmada, servido por `POST` auditable y abierto por la SPA para `window.print()` en 58/80 mm.
- Componentes/capas: `billing-service`, `facturaelectronica-web`, `bff-service`.

### TASK-189 - Implementar historico avanzado de ventas/documentos
- Estado: Completada.
- Fase: Fase 23: Marca NexoFiscal, branding, reportes avanzados e impresion POS.
- Decision de diseno: Consulta operacional de ventas y documentos desde `billing-service`, con filtros por estado, fecha, vendedor, cliente, metodo de pago y estado fiscal. La SPA permite reimprimir comprobantes desde el listado sin reemitir documentos fiscales.
- Componentes/capas: `billing-service`, `bff-service`, `facturaelectronica-web`.

## TASK-190 a TASK-196: reportes asincronos avanzados con S3 y notificacion

Estado: implementacion inicial completada en local/Docker. Produccion AWS conserva el mismo diseno con S3 privado, KMS y SES como adaptadores productivos.

Decisiones:

- Los reportes pequenos siguen usando consulta/exportacion sincrona desde `reporting-service`.
- Los reportes pesados se modelan como jobs asincronos para evitar timeouts HTTP y permitir experiencia de usuario no bloqueante.
- El usuario podra solicitar el reporte desde la SPA y recibir correo cuando este listo.
- El correo contendra un enlace intermediado por NexoFiscal construido con `APP_PUBLIC_BASE_URL`, por ejemplo `{APP_PUBLIC_BASE_URL}/reportes/descarga/{token}`.
- El enlace de correo no sera una URL directa de S3.
- Al hacer clic, el BFF valida token, usuario/alcance, empresa, estado del job, licencia y RBAC; luego genera una URL prefirmada de S3 con TTL `REPORT_DOWNLOAD_PRESIGNED_TTL_SECONDS`, inicialmente `5`.
- `REPORT_LINK_TOKEN_TTL_HOURS` gobierna la vida del token enviado al correo; es independiente del TTL de S3.
- En local/Docker los archivos quedan en un volumen privado de `reporting-service`; en produccion quedan en S3 privado con KMS y retencion configurable.
- `reporting-projection-lambda` mantiene proyecciones reconstruibles; un `report-export-worker-lambda` o worker equivalente generara archivos pesados bajo demanda. La implementacion local usa un worker programado interno para pruebas end-to-end.
- SES queda como canal objetivo de notificacion por correo en AWS. La implementacion local registra la notificacion sin exponer tokens ni enlaces en logs.
- Toda solicitud, procesamiento, error, expiracion, revocacion, envio y descarga queda auditada sin exponer filtros sensibles completos, URLs S3, bucket/key publica ni secretos.

Flujo objetivo:

1. La SPA solicita `POST /api/v1/reports/export-jobs`.
2. El BFF valida sesion, RBAC, licencia y empresa activa.
3. `reporting-service` crea `report_export_job` en `PENDING`.
4. El worker toma el job pendiente, marca `PROCESSING`, genera el archivo y lo guarda en storage privado.
5. Si termina bien, marca `READY`, crea token de descarga y solicita notificacion por correo.
6. El usuario abre el enlace `{APP_PUBLIC_BASE_URL}/reportes/descarga/{token}`.
7. El BFF enruta la descarga al `reporting-service`, que valida el token hasheado, estado y expiracion.
8. La respuesta entrega la descarga de forma controlada; en produccion puede resolverse con URL S3 prefirmada de 5 segundos.

Context7 evidence:

- Library/tool: AWS SDK for Java v2.
- Topic consulted: S3 presigned GetObject URL.
- Relevant finding: AWS SDK Java v2 permite crear `S3Presigner` y generar `PresignedGetObjectRequest` con `signatureDuration`.
- Decision impact: La descarga pesada se resuelve al momento del clic con URL prefirmada de vida muy corta, no al momento de enviar el correo.

### TASK-190 - Disenar reportes asincronos avanzados
- Estado: Completada.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Decision de diseno: Clasificar reportes sincronos vs pesados, estados de job, permisos, licencia y reglas de expiracion.
- Componentes/capas: `reporting-service`, `bff-service`, `facturaelectronica-web`.

### TASK-191 - Disenar contratos API para jobs de reportes
- Estado: Completada.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Decision de diseno: Exponer contratos para crear jobs, consultar estado/listado y resolver enlaces intermediados de descarga.
- Componentes/capas: `reporting-service`, `bff-service`.

### TASK-192 - Disenar persistencia de trabajos de reportes
- Estado: Completada.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Decision de diseno: Persistir jobs, tokens hasheados, intentos de descarga y notificaciones sin guardar URLs S3 directas ni secretos.
- Componentes/capas: `reporting-service`, PostgreSQL.

### TASK-193 - Disenar worker asincrono de exportacion
- Estado: Completada.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Decision de diseno: Procesar jobs por SQS/EventBridge + Lambda/worker idempotente y guardar archivos en S3 privado.
- Componentes/capas: `report-export-worker-lambda`, `reporting-service`, S3.

### TASK-194 - Disenar descarga segura desde S3 con enlace intermediado
- Estado: Completada.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Decision de diseno: Link publico parametrizable con `APP_PUBLIC_BASE_URL`, token intermediado con TTL propio y URL S3 prefirmada al clic por 5 segundos.
- Componentes/capas: `bff-service`, `reporting-service`, S3.

### TASK-195 - Disenar notificaciones por correo
- Estado: Completada.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Decision de diseno: Enviar correo por SES con enlace intermediado y auditoria de envio, fallo y rebote tecnico cuando aplique.
- Componentes/capas: SES, `reporting-service`, `audit-service`.

### TASK-196 - Disenar UI de reportes avanzados asincronos
- Estado: Pendiente.
- Fase: Fase 24: Reportes asincronos avanzados con S3 y notificacion.
- Decision de diseno: Agregar modo "generar en segundo plano", listado de jobs, estados, descarga disponible y mensajes claros.

## TASK-197 a TASK-199: ajustes QA RBAC, POS e i18n

### Decisiones de diseno

- `ROOT` conserva el rol global maximo de plataforma y no pertenece a una empresa.
- `OWNER` se mantiene como rol interno maximo de empresa, pero al crear el administrador inicial tambien se materializa como `company_role` visible y asignable dentro de la empresa.
- El rol empresarial `OWNER` debe ser idempotente por empresa: si ya existe, se reutiliza; si falta, se crea activo, `systemSeed=true`, con permisos `COMPANY` y sin permisos `GLOBAL_*`.
- En UI, `OWNER` debe mostrarse como `Administrador propietario` para evitar confundirlo con `ROOT`.
- Confirmar POS requiere permisos de venta y configuracion fiscal valida. Un administrador con todos los permisos no debe poder saltarse emisor fiscal activo ni resolucion activa.
- La falta de emisor fiscal o numeracion debe responder como regla de negocio en espanol y la SPA debe mostrar una guia hacia `Fiscal`.
- Los codigos tecnicos de permisos y modulos pueden seguir en ingles en backend/API/base de datos, pero toda presentacion debe usar `react-i18next`.

### Context7 evidence

- Library/tool: react-i18next (`/i18next/react-i18next`).
- Topic consulted: `useTranslation` and JSON resource keys.
- Relevant finding: `useTranslation()` returns `t`, which resolves keys from configured resources and can use a default value only as fallback.
- Decision impact: La SPA debe centralizar etiquetas/descripciones en `translation.json`; los componentes no deben depender de descripciones inglesas del backend para permisos vigentes.

### TASK-197 - Materializar rol OWNER empresarial al crear administrador inicial
- Estado: Completada.
- Fase: Fase 25: Ajustes QA RBAC, POS e i18n.
- Decision de diseno: Crear/reutilizar rol empresarial `OWNER` visible, con permisos company-scoped, y asignarlo al administrador inicial junto con la membresia legacy necesaria para compatibilidad.
- Componentes/capas: `identity-service`, `facturaelectronica-web`, BFF.

### TASK-198 - Mejorar confirmacion POS ante configuracion fiscal faltante
- Estado: Completada.
- Fase: Fase 25: Ajustes QA RBAC, POS e i18n.
- Decision de diseno: Diferenciar error funcional de configuracion fiscal de errores de permisos y traducir el mensaje operativo.
- Componentes/capas: `billing-service`, `facturaelectronica-web`, BFF.

### TASK-199 - Completar i18n de permisos y modulos RBAC
- Estado: Completada.
- Fase: Fase 25: Ajustes QA RBAC, POS e i18n.
- Decision de diseno: Completar recursos `i18next` para todos los permisos actuales y mostrar nombres tecnicos de rol con etiqueta profesional en espanol cuando aplique.
- Componentes/capas: `facturaelectronica-web`.

### TASK-200 - Corregir activacion fiscal, precarga de empresa y errores de login
- Estado: Completada.
- Fase: Fase 25: Ajustes QA RBAC, POS e i18n.
- Decision de diseno: Mantener historial fiscal mediante registros activos/inactivos, exponer activacion explicita por API y reflejarlo en la SPA con tablas operativas. La empresa seleccionada por ROOT o la empresa del usuario se usa para hidratar formularios, y los errores de login diferencian credenciales invalidas de BFF/autenticacion no disponible.
- Componentes/capas: `billing-service`, `bff-service`, `facturaelectronica-web`.
- Context7 evidence:
  - Library/tool: Spring Boot.
  - Topic consulted: REST controllers, mappings and validation.
  - Relevant finding: Los controladores pueden exponer operaciones especializadas con `@GetMapping`, `@PostMapping`, `@PutMapping` y validacion de request body para separar comandos de consulta.
  - Decision impact: Se agregan endpoints de activacion/inactivacion fiscal sin sobrecargar la creacion ni eliminar registros historicos.
  - Library/tool: React.
  - Topic consulted: Controlled form state.
  - Relevant finding: Los formularios deben sincronizar estado controlado desde props/estado de aplicacion cuando cambia la entidad activa.
  - Decision impact: La SPA hidrata formularios de empresa/fiscal al cambiar empresa activa y evita que ROOT actualice datos equivocados.

### TASK-201 - Separar creacion, edicion y acciones por empresa para ROOT
- Estado: Completada.
- Fase: Fase 25: Ajustes QA RBAC, POS e i18n.
- Decision de diseno: Para `ROOT`, la empresa activa es contexto operativo y no debe hidratar automaticamente el formulario de creacion. El listado de empresas es la fuente de seleccion; `Actualizar`, `Crear administrador` y `Crear marca empresarial` se ejecutan desde acciones por fila con `company_id` fijado por la empresa seleccionada. Los modales muestran la empresa como campo bloqueado, pero mantienen editables los campos de administrador o branding.
- Componentes/capas: `facturaelectronica-web`, BFF existente, `tenant-service`/`identity-service` existentes.
- Context7 evidence:
  - Library/tool: React.
  - Topic consulted: Controlled form state for editable drafts selected from a table.
  - Relevant finding: React recomienda separar datos fuente de estado borrador editable y resetear/hidratar el formulario solo cuando cambia explicitamente la entidad seleccionada para edicion.
  - Decision impact: La SPA mantiene `companyForm` vacio para crear, usa un identificador de empresa en edicion para actualizar y usa identificadores de empresa objetivo separados para modales.

### TASK-202 - Separar Registro de Ventas de la pantalla POS
- Estado: Completada.
- Fase: Fase 26: QA visual y flujo operativo POS.
- Decision de diseno: `Ventas` queda como vista transaccional para registrar y confirmar POS; `Registro de Ventas` queda como vista de consulta historica inmutable con filtros y detalle fiscal/documental.
- Componentes/capas: `facturaelectronica-web`, BFF existente, `billing-service` existente.
- Criterios de diseno:
  - No mezclar formulario POS con historico operativo.
  - Usar `GET /api/v1/sales/history` para listado y `GET /api/v1/sales/{saleId}` para detalle.
  - No exponer acciones mutables sobre ventas registradas desde el historico.
  - Mostrar CUFE/CUDE, tracking y estado fiscal dentro del detalle cuando existan.
- Context7 evidence:
  - Library/tool: React.
  - Topic consulted: Conditional rendering and lists with keys.
  - Relevant finding: React recomienda renderizar listas desde arreglos usando identificadores estables del backend como `key` y separar vistas por condicion cuando representan estados/pantallas distintas.
  - Decision impact: El historico se implementa como componente y pantalla propios, con `sale.id` como llave estable.

### TASK-203 - Redisenar modales empresariales
- Estado: Completada.
- Fase: Fase 26: QA visual y flujo operativo POS.
- Decision de diseno: Los modales de administrador inicial y marca empresarial deben usar un contenedor modal responsivo, con grid propio y sin anidar paneles de pagina dentro de modales.
- Componentes/capas: `facturaelectronica-web`.
- Criterios de diseno:
  - El modal administra ancho maximo, alto maximo y scroll interno.
  - El campo empresa permanece bloqueado y los campos de datos se mantienen editables.
  - Branding usa formulario especifico para modal, no un `tool-panel` de pantalla completa incrustado.
  - Los botones de accion quedan alineados y visibles en escritorio y movil.
- Context7 evidence:
  - Library/tool: React.
  - Topic consulted: Controlled forms and local draft state.
  - Relevant finding: React recomienda inputs controlados por estado para formularios y estados visuales claros durante submit/error.
  - Decision impact: Los modales mantienen formularios controlados y separan composicion modal de paneles de pagina.

## TASK-204 a TASK-216: politica fiscal configurable, PIN operacional y documentos fiscales

### Decisiones de diseno

- `SaleChannel.POS` representa canal operativo de caja, scanner, pago y tirilla; no representa por si solo el tipo fiscal DIAN.
- La politica empresarial define `defaultPosDocumentType`. El valor recomendado y default del producto es `ELECTRONIC_INVOICE`.
- `ELECTRONIC_POS` queda disponible como documento equivalente electronico POS para empresas que lo configuren o para ventas excepcionales autorizadas.
- Las resoluciones se mantienen por tipo documental: una activa por `company_id`, `document_type` y `environment`.
- El override de tipo documental es una autorizacion operacional puntual, no una modificacion de politica global.
- El override se valida en backend con permiso `SALES_DOCUMENT_TYPE_OVERRIDE`, PIN operacional, motivo obligatorio, licencia, empresa y resolucion activa compatible.
- El PIN operacional tiene exactamente 6 digitos, se almacena como hash fuerte, se bloquea con 3 fallos y despues del desbloqueo queda `CHANGE_REQUIRED`.
- Nota credito, nota debito y nota de ajuste POS viven en modulos fiscales independientes, con permisos y resoluciones propias.
- La tirilla de una venta POS que emite factura electronica debe presentarse como representacion grafica de factura electronica de venta.

### Web/DIAN evidence

- Library/tool: DIAN official website.
- Topic consulted: Factura electronica de venta y documento equivalente electronico POS.
- Relevant finding: DIAN documenta que quienes opten por documentos equivalentes electronicos pueden soportar operaciones con documento equivalente electronico correspondiente o con factura electronica de venta; el documento equivalente electronico POS tiene requisitos propios y denominacion especifica.
- Decision impact: NexoFiscal separa canal POS de tipo fiscal, usa factura electronica como default comercial y conserva POS electronico como opcion parametrizable/auditada.

### Context7 evidence

- Library/tool: Spring Boot.
- Topic consulted: REST validation and exception handling.
- Relevant finding: Spring Boot integra Bean Validation con `@Valid @RequestBody` y permite mapear excepciones de dominio/reglas de negocio a respuestas REST claras.
- Decision impact: PIN, override y politica fiscal deben validar DTOs en interfaces y reglas de negocio en aplicacion/dominio, con errores funcionales auditables.

### TASK-204 - Documentar politica fiscal por empresa
- Estado: Implementado.
- Fase: Fase 27: Politica fiscal configurable, PIN operacional y documentos fiscales.
- Decision de diseno: Introducir `company_fiscal_policy` con `defaultPosDocumentType`, override permitido y PIN requerido.
- Componentes/capas: `billing-service`, `facturaelectronica-web`, BFF.

### TASK-205 - Configurar documento fiscal por defecto para venta POS
- Estado: Implementado.
- Fase: Fase 27.
- Decision de diseno: Default recomendado `ELECTRONIC_INVOICE`; `ELECTRONIC_POS` es opcion avanzada por empresa.

### TASK-206 - Mantener resolucion activa por tipo documental y ambiente
- Estado: Implementado.
- Fase: Fase 27.
- Decision de diseno: Confirmar y reforzar la regla existente de `company_id + document_type + environment`, evitando cualquier resolucion global.

### TASK-207 - Disenar PIN operacional de 6 digitos
- Estado: Implementado.
- Fase: Fase 27.
- Decision de diseno: PIN numerico exacto de 6 digitos, hash fuerte, no reutilizable como password de login.

### TASK-208 - Bloquear PIN tras 3 intentos fallidos
- Estado: Implementado.
- Fase: Fase 27.
- Decision de diseno: Contador consecutivo, estado `LOCKED` y auditoria de intentos.

### TASK-209 - Desbloqueo administrativo con cambio obligatorio
- Estado: Implementado.
- Fase: Fase 27.
- Decision de diseno: Desbloqueo deja el PIN en `CHANGE_REQUIRED`; el titular debe cambiarlo antes de autorizar.

### TASK-210 - Override de tipo documental por venta con PIN
- Estado: Implementado.
- Fase: Fase 27.
- Decision de diseno: Autorizacion puntual con vendedor, autorizador, PIN, motivo, tipo anterior y tipo nuevo.

### TASK-211 - Ajustar confirmacion POS para factura electronica por defecto
- Estado: Implementado.
- Fase: Fase 27.
- Decision de diseno: `SaleChannel.POS` confirma por defecto `ELECTRONIC_INVOICE` y llama al flujo de factura electronica.

### TASK-212 - Ajustar UI Fiscal/Ventas/Facturacion
- Estado: Implementado.
- Fase: Fase 27.
- Decision de diseno: Mostrar politica fiscal, resoluciones por tipo, override con modal PIN y textos en espanol.

### TASK-213 - Crear modulo independiente de Nota credito
- Estado: Implementado.
- Fase: Fase 27.
- Decision de diseno: Pantalla/contrato/permiso/resolucion `CREDIT_NOTE`.

### TASK-214 - Crear modulo independiente de Nota debito
- Estado: Implementado.
- Fase: Fase 27.
- Decision de diseno: Pantalla/contrato/permiso/resolucion `DEBIT_NOTE`.

### TASK-215 - Crear modulo independiente de Nota de ajuste POS
- Estado: Implementado.
- Fase: Fase 27.
- Decision de diseno: Pantalla/contrato/permiso/resolucion `POS_ADJUSTMENT_NOTE`, solo sobre documento equivalente POS.

### TASK-216 - Auditoria completa de PIN, override y notas fiscales
- Estado: Implementado inicial.
- Fase: Fase 27.
- Decision de diseno: Auditar intentos, bloqueos, desbloqueos, cambios y emisiones sin secretos ni payloads completos.

### TASK-217 - Cierre de venta en un solo paso
- Estado: Implementado.
- Fase: Fase 27.
- Decision de diseno: Exponer `POST /api/v1/sales/close` para que el backend cree y confirme la venta con una sola idempotency key. La SPA usa el label `Cerrar venta` como accion principal y deja de exigir que el vendedor cree una venta antes de emitir el documento fiscal.
- Componentes/capas: `billing-service` agrega caso de uso `close`, `bff-service` reutiliza la regla de acceso de ventas, `facturaelectronica-web` ejecuta el cierre y abre la tirilla/comprobante desde la venta confirmada.
- Errores: Faltantes de emisor/resolucion siguen como `BUSINESS_RULE_VIOLATION` y redirigen a Fiscal; fallos del conector DIAN se mapean a `EXTERNAL_PROVIDER_ERROR` con mensaje funcional.

### TASK-218 - Corregir reconocimiento ROOT y catalogo de permisos
- Estado: Implementado.
- Fase: Fase transversal: Bugs y estabilizacion operativa.
- Decision de diseno: `identity-service` separa el catalogo global de permisos root-only del catalogo empresarial. El permiso `OPERATIONAL_PIN_MANAGE` se incorpora al enum `PermissionCode` para alinear Flyway, JPA y dominio. El BFF conserva `ROOT` solo cuando `/api/v1/platform/permissions` responde correctamente para un actor root real.
- Componentes/capas: `identity-service` ajusta dominio, caso de uso y controlador; `bff-service` agrega prueba de lectura de catalogos globales sin empresa activa para ROOT.
- Riesgo mitigado: Un permiso en base de datos que no exista en el enum Java rompe Hibernate al mapear `@Enumerated(EnumType.STRING)` y deja a ROOT sin acceso global.

### Context7 evidence

- Library/tool: React.
- Topic consulted: Form submit events, state updates and conditional rendering.
- Relevant finding: React recomienda manejar formularios con una accion de submit, prevenir el refresh y actualizar estado/renderizado segun el resultado.
- Decision impact: `SaleForm` usa una accion principal `Cerrar venta` en vez de dos botones operativos para crear y emitir.
- Library/tool: Spring Framework.
- Topic consulted: `@ControllerAdvice` and `@ExceptionHandler`.
- Relevant finding: Spring MVC permite centralizar mapeo de excepciones a `ResponseEntity` con status y body.
- Decision impact: El conector DIAN se mapea como proveedor externo y no como error interno generico cuando falla la comunicacion.

### TASK-219 - Correccion de uso de licencia y seleccion ROOT explicita
- Estado: Implementado.
- Fase: Fase transversal: Bugs y estabilizacion operativa.
- Decision de diseno: `billing-service` reemplaza el JPQL de documentos electronicos con filtros opcionales nulos por una consulta Criteria dinamica que agrega predicados solo cuando hay valor real.
- Decision de UX: `ROOT` inicia sesion sin empresa activa, aun si habia una empresa persistida en `sessionStorage`; la seleccion de empresa es explicita antes de hidratar formularios derivados.
- Decision de licencias: El boton `Cargar licencia` se deshabilita cuando la licencia de la empresa seleccionada ya esta cargada o guardada.
- Riesgo mitigado: Evita 500 en el refresco de uso de licencias y reduce modificaciones accidentales sobre empresas precargadas.

#### Context7 evidence
- Library/tool: Spring Data JPA.
- Topic consulted: Consultas dinamicas con filtros opcionales.
- Relevant finding: Spring Data JPA documenta Specifications/Criteria como forma de construir predicados programaticamente y componer filtros en tiempo de ejecucion.
- Decision impact: Se usa Criteria API para evitar ambiguedad de tipos en PostgreSQL con parametros nulos.
- Library/tool: Hibernate ORM.
- Topic consulted: `@Enumerated(EnumType.STRING)` enum mapping.
- Relevant finding: Hibernate mapea enums `STRING` como el nombre del enum en columna `VARCHAR`; los valores de base de datos deben corresponder a constantes Java validas.
- Decision impact: Todo permiso insertado en `identity.permission_catalog` debe existir en `PermissionCode` y estar cubierto por pruebas.

### TASK-220 - Bloquear cierre de venta sin configuracion contable activa
- Estado: Implementado.
- Fase: Fase transversal: Bugs y estabilizacion operativa.
- Decision de diseno: `billing-service` ejecuta una prevalidacion contra `accounting-service` para confirmar que existe una regla contable activa `SALE_CONFIRMED` antes de asignar numeracion fiscal, invocar DIAN/mock o aplicar inventario.
- Componentes/capas: `AccountingEntryPort` agrega `ensureSalePostingConfigured(companyId)`, `AccountingEntryHttpAdapter` consulta `GET /api/v1/accounting-rules?eventType=SALE_CONFIRMED&active=true` y `SaleManagementService.confirm` invoca esta validacion antes de `AssignFiscalNumberUseCase`.
- Errores: Si falta configuracion contable se retorna `BUSINESS_RULE_VIOLATION` con mensaje funcional. `accounting-service` centraliza `IllegalStateException` en `400` para evitar `500` genericos por regla faltante.
- Riesgo mitigado: Evita ventas parcialmente confirmadas con inventario descontado y contabilidad fallida por empresas nuevas sin plantilla PUC inicializada.

### TASK-221 - Validaciones frontend de resolucion fiscal antes de POST
- Estado: Implementado.
- Fase: Fase transversal: Bugs y estabilizacion operativa.
- Decision de diseno: `ResolutionForm` valida localmente consecutivos positivos, orden del rango y vigencia antes de llamar al backend.
- UX: Los campos invalidos usan estado visual rojo y mensaje bajo el campo; el boton se deshabilita mientras exista error local.

### TASK-222 - Crear modulo visible de configuracion contable
- Estado: Implementado.
- Fase: Fase 28: Configuracion contable empresarial.
- Decision de diseno: La configuracion contable deja de vivir como accion secundaria en Reportes y pasa a un modulo propio bajo Contabilidad.
- Componentes/capas: La SPA agrega `AccountingConfigurationPanel`, navega con `Configuracion contable`, consulta `GET /api/v1/accounts`, `GET /api/v1/accounting-rules` e inicializa con `POST /api/v1/accounting-setup/basic`.
- Seguridad/licencia: El acceso se controla por licencia `ACCOUNTING` y permisos `ACCOUNTING_VIEW`/`ACCOUNTING_MANAGE`, manteniendo backend como fuente real de autorizacion.
- Relacion con ventas: El cierre de venta depende de que la empresa tenga regla contable `SALE_CONFIRMED` activa; el usuario puede resolverlo desde este modulo.

### TASK-223 - Asistente editable de plan de cuentas y reglas contables por empresa
- Estado: Implementado.
- Fase: Fase 28: Configuracion contable empresarial.
- Decision de diseno: La inicializacion automatica pasa a ser un asistente guiado y editable. El sistema puede sugerir una plantilla, pero siempre debe mostrar una vista previa antes de crear cuentas o reglas.
- UX: El formulario permite agregar multiples filas de cuentas PUC y multiples reglas contables. Dentro de cada regla, las filas se muestran como `movimientos contables`, no como `lineas`, para que el usuario entienda que cada fila afecta una cuenta.
- Contratos: `accounting-service` expone `POST /api/v1/accounts/batch`, `POST /api/v1/accounting-rules/batch` y `POST /api/v1/accounting-configuration/batch` para crear varias cuentas/reglas en una sola accion. El BFF enruta `accounting-configuration` hacia `accounting-service` y conserva autorizacion, correlation ID y auditoria transversal.
- Consistencia: `AccountingConfigurationService` valida todo el lote antes de persistir y ejecuta el guardado con transaccion declarativa. Si falla cualquier cuenta, regla o movimiento contable, se revierte todo el lote y se devuelve un error funcional.
- Regla historica: Cuentas o reglas usadas por asientos no se eliminan fisicamente; se inactivan o se versionan para mantener trazabilidad contable.
- Seguridad/licencia: Requiere licencia `ACCOUNTING` y permiso `ACCOUNTING_MANAGE`; `ACCOUNTING_VIEW` solo permite consulta.
- Validacion implementada: Vitest cubre el preview y submit batch del asistente. Maven cubre use cases, controller REST y route resolver BFF.

#### Context7 evidence
- Library/tool: React.
- Topic consulted: Controlled form inputs and client-side validation before submit.
- Relevant finding: React recomienda controlar valores de formulario con estado y renderizar mensajes/estado derivado antes de ejecutar acciones de submit.
- Decision impact: `ResolutionForm` calcula errores desde estado y bloquea el POST cuando el rango fiscal no cumple reglas locales.
- Library/tool: Spring Framework.
- Topic consulted: RestClient error handling and exception mapping with controller advice.
- Relevant finding: `RestClient` propaga errores HTTP como excepciones y Spring MVC permite mapear excepciones de dominio con `@ControllerAdvice`.
- Decision impact: El adaptador contable convierte fallos de disponibilidad/configuracion en reglas de negocio y `accounting-service` devuelve 400 en vez de 500 para regla faltante.
- Library/tool: React.
- Topic consulted: Dynamic controlled form inputs and submit handling.
- Relevant finding: React documenta formularios controlados mediante estado, handlers compartidos por `name` y manejo de submit con `preventDefault`, lo que permite validar y renderizar listas dinamicas antes de enviar.
- Decision impact: TASK-223 modela el asistente contable como estado controlado con filas dinamicas de cuentas, reglas y movimientos contables antes de construir el request batch.
- Library/tool: Spring Framework 6.2.
- Topic consulted: Request body validation, exception handling and transaction rollback.
- Relevant finding: Spring MVC soporta `@Valid @RequestBody` para validar payloads REST y Spring Transaction Management revierte transacciones declarativas ante excepciones runtime por defecto.
- Decision impact: TASK-223 exige validacion de payload batch, errores funcionales por fila y guardado transaccional para impedir configuraciones contables parciales.

### TASK-224 - Trazabilidad de uso y mantenimiento seguro de cuentas/reglas contables
- Estado: Implementado.
- Fase: Fase 28: Configuracion contable empresarial.
- Decision de diseno: El sistema debe distinguir configuraciones contables usadas y no usadas para permitir mantenimiento sin romper trazabilidad historica.
- Modelo: `accounting_entry` agrega `accounting_rule_id` nullable. Los asientos nuevos guardan la regla exacta usada; asientos historicos previos quedan sin regla trazada y no se reconstruyen por inferencia.
- Cuentas: El uso se calcula desde `accounting_entry_line.account_id`; una cuenta usada no se actualiza estructuralmente ni se inactiva.
- Reglas: El uso se calcula desde `accounting_entry.accounting_rule_id`; una regla usada no se actualiza estructuralmente ni se inactiva.
- UX: La SPA muestra columna `Uso` y botones `Actualizar`/`Inactivar` solo para recursos `Sin uso`.
- Consistencia: Actualizaciones e inactivaciones se validan en backend; el frontend solo mejora experiencia, no es la fuente de seguridad.
- Auditoria: Las mutaciones pasan por BFF y quedan cubiertas por auditoria transversal de acciones mutables.

### TASK-225 - Precio final con IVA incluido en inventario y resumen fiscal de venta
- Estado: Implementado.
- Fase: Fase 29: UX fiscal de inventario y ventas.
- Decision de diseno: La captura operativa de inventario usa `precio final` porque es el valor que conoce el negocio. La SPA calcula `precio sin IVA` y `valor IVA` desde la tarifa del impuesto seleccionado usando `base = total / (1 + tarifa / 100)` e `iva = total - base`.
- Contrato: `inventory-service` conserva `salePrice` como precio unitario sin IVA/base gravable. `finalSalePrice` no se envia al backend en esta iteracion porque es un valor derivado de UI.
- Ventas: La SPA no envia `unitPrice`, `taxCode` ni `taxRate`; `billing-service` sigue calculando lineas desde el snapshot de inventario. Para experiencia previa al cierre, la SPA muestra subtotal, IVA y total estimados con los datos de los productos escaneados.
- Codigo de barras: El campo `Codigo de barras` de inventario queda como input dedicado con `autoComplete=off`, aceptando escritura manual o lectores USB HID que se comportan como teclado.
- Impresion: La representacion grafica/tirilla debe mantener subtotal, IVA y total de `SaleResponse`, que provienen del backend confirmado.

#### Context7 evidence
- Library/tool: React.
- Topic consulted: Controlled inputs and avoiding redundant state for calculated values.
- Relevant finding: React recomienda derivar valores calculables desde estado base en lugar de duplicarlos como estado editable.
- Decision impact: `Precio sin IVA` y `Valor IVA` se calculan desde `Precio final` + `taxRate`; solo el precio final es editable.

<!-- END SDD TASK DESIGN TRACEABILITY -->
