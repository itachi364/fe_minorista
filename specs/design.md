# Design: Backend Clean Architecture basado en microservicios

## Decision tecnica

Se usara Clean Architecture dentro de una estrategia basada en microservicios. Cada microservicio debe separar dominio, casos de uso, puertos, adaptadores y configuracion framework.

La unidad de despliegue aprobada es el microservicio por bounded context. No se creara un artefacto o contenedor por endpoint individual. Cada endpoint debe pertenecer al microservicio que representa su capacidad de negocio.

## Microservicios propuestos

- `identity-service`: usuarios, roles, autenticacion, autorizacion y auditoria base.
- `thirdparty-service`: clientes, proveedores, tipos de documento y validaciones de identificacion.
- `catalog-service`: paises, impuestos, metodos de pago, parametros fiscales y catalogos DIAN.
- `inventory-service`: productos, categorias, stock, kardex y movimientos.
- `billing-service`: factura electronica, POS electronico, notas, resoluciones, numeracion, CUFE/CUDE, estados fiscales.
- `dian-provider-service`: adaptador hacia proveedor tecnologico DIAN, firma delegada si aplica, envio, consulta, reintentos y normalizacion de respuestas.
- `accounting-service`: plan de cuentas, comprobantes, asientos, libro diario y libro mayor.
- `reporting-service`: consultas operativas, fiscales y contables.

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
- Orquesta inventario, contabilidad y proveedor tecnologico.
- Mantiene estados del ciclo fiscal.

Estado TASK-041:

- `billing-service` fisico queda creado para ventas POS.
- `POST /api/v1/issuers` y `GET /api/v1/issuers/current` gestionan el emisor fiscal activo por empresa.
- `POST /api/v1/numbering-resolutions` y `GET /api/v1/numbering-resolutions` gestionan resoluciones por empresa, tipo de documento, ambiente, rango y vigencia.
- `POST /api/v1/sales` calcula totales por linea y valida stock contra `inventory-service`.
- `POST /api/v1/sales/{saleId}/confirm` exige emisor activo, asigna numeracion desde resolucion vigente, genera documento electronico POS y envia la solicitud por HTTP a `dian-provider-service`.
- TASK-037 agrego efectos automaticos posteriores a validacion: `SALE_OUT` contra `inventory-service` y asiento `SALE_CONFIRMED` contra `accounting-service`.
- `electronic_document.inventory_applied_at` y `electronic_document.accounting_applied_at` registran aplicacion idempotente de efectos posteriores.

### Politica de calculo fiscal inicial

- El calculo se realiza por linea.
- La base gravable de cada linea corresponde a `cantidad * precio_unitario - descuento`.
- Los descuentos se aplican antes del impuesto.
- Todos los valores monetarios se redondean a 2 decimales con `HALF_UP`.
- El subtotal, impuestos, descuentos y total del documento corresponden a la suma de los valores ya calculados por linea.
- Los ajustes especificos que exija el anexo tecnico DIAN o un proveedor tecnologico seleccionado deberan documentarse antes de modificar esta politica.

### dian-provider-service

- Expone un contrato interno estable para emitir documentos.
- Encapsula detalles del proveedor tecnologico DIAN.
- Normaliza respuestas tecnicas.
- Maneja timeouts, reintentos, idempotencia y errores externos.

Estado TASK-036:

- `dian-provider-service` fisico queda creado con Clean Architecture y persistencia propia.
- Expone `POST /api/v1/provider/electronic-pos`, `POST /api/v1/provider/electronic-invoices` y `GET /api/v1/provider/submissions/{trackingId}`.
- En modo local solo soporta `DIAN_PROVIDER_MODE=mock`; cualquier modo distinto falla de forma explicita.
- Persiste los envios mock en `dian_provider.provider_submission` sin credenciales ni secretos reales.
- `billing-service` consume el mock por HTTP mediante `DIAN_PROVIDER_SERVICE_URL`.

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
- La integracion automatica desde `inventory-service` y `accounting-service` se hara por lotes posteriores mediante REST sincrono o eventos aprobados.

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
9. `billing-service` asigna prefijo/consecutivo desde resolucion vigente, emite el documento electronico y lo envia a `dian-provider-service`.
10. `dian-provider-service` responde mediante mock local deterministico.
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
6. `dian-provider-service` envia al proveedor tecnologico DIAN.
7. `billing-service` registra estado y artefactos: CUFE, QR, XML, PDF o representacion grafica.
8. `inventory-service` descuenta stock cuando el documento alcance el estado aprobado.
9. `accounting-service` registra asiento contable.

## Flujo de POS electronico

1. Punto de venta solicita emision POS.
2. `billing-service` valida caja, resolucion POS, productos y adquirente si aplica.
3. `billing-service` calcula totales, impuestos y CUDE usando prefijo/consecutivo autorizado.
4. `billing-service` envia el documento equivalente electronico al proveedor tecnologico.
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
- `NUMBER_ASSIGNED` -> `SENT_TO_PROVIDER`: documento enviado al proveedor tecnologico.
- `SENT_TO_PROVIDER` -> `VALIDATED`: proveedor acepta o valida el documento; se registran CUFE/CUDE, QR, XML y representacion grafica cuando existan.
- `SENT_TO_PROVIDER` -> `REJECTED`: proveedor rechaza el documento; se conservan codigo y mensaje seguro de rechazo.
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
- La generacion final de CUDE debe ajustarse al Anexo Tecnico de Documento Equivalente Electronico vigente y a la respuesta del proveedor tecnologico cuando se implemente el adaptador real.
- El documento POS nace en `NUMBER_ASSIGNED`; el envio al proveedor y la validacion pasan por las tareas de proveedor y trazabilidad.

### Politica de prueba end-to-end local de facturacion

- Para habilitar pruebas locales completas, el backend debe exponer endpoints REST y persistencia PostgreSQL para configurar emisor, configurar resoluciones, emitir POS electronico, enviar el documento a un proveedor DIAN mock y consultar el resultado.
- El proveedor DIAN mock debe ser deterministico, no debe hacer llamadas externas y no debe requerir credenciales reales.
- En modo local, el mock puede devolver respuestas simuladas `ACCEPTED` o `REJECTED` usando parametros de request o configuracion local segura.
- El modo local se configura con `DIAN_PROVIDER_MODE=mock`. En esta version no existe adaptador real; cualquier valor distinto de `mock` debe fallar explicitamente para evitar una falsa integracion productiva.
- El resultado simulado se configura con `DIAN_MOCK_DEFAULT_STATUS`, usando `ACCEPTED` por defecto y permitiendo `REJECTED` o `FAILED` para pruebas negativas.
- Los errores simulados pueden configurarse con `DIAN_MOCK_ERROR_CODE` y `DIAN_MOCK_ERROR_MESSAGE`; cuando no se definan, el mock debe usar mensajes seguros predeterminados sin secretos.
- Una respuesta `ACCEPTED` del mock debe registrar tracking ID, CUFE/CUDE simulado, QR simulado y artefactos dummy para permitir validar el flujo de persistencia y consulta.
- Esta politica no sustituye la integracion real con proveedor tecnologico DIAN ni valida cumplimiento tecnico final del anexo DIAN; solo habilita pruebas funcionales internas hasta seleccionar proveedor y certificados.
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
- El `reporting-service` fisico queda diferido hasta la implementacion de Outbox/Inbox, eventos AWS y proyecciones de lectura consolidadas.

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
- TASK-053 incluye `POST /api/v1/accounting-setup/basic` para crear una plantilla minima editable por empresa con cuentas `1105`, `1110`, `1305`, `1435`, `2205`, `2408`, `4135` y `5135`, y reglas base para venta, compra, gasto y pago de cuenta por pagar.
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

## Modelo de datos faltante

- `issuer_profile`
- `tax_responsibility`
- `numbering_resolution`
- `number_sequence`
- `electronic_document`
- `electronic_document_line`
- `electronic_document_tax`
- `electronic_document_artifact`
- `provider_submission`
- `provider_response`
- `credit_note`
- `debit_note`
- `pos_adjustment_note`
- `inventory_movement`
- `stock_balance`
- `account`
- `accounting_entry`
- `accounting_entry_line`
- `audit_event`

## Contratos externos

El proveedor tecnologico DIAN debe integrarse mediante puerto de salida:

```java
interface ElectronicDocumentProviderPort {
    ProviderSubmissionResult submitInvoice(ProviderInvoiceRequest request);
    ProviderSubmissionResult submitPosDocument(ProviderPosRequest request);
    ProviderSubmissionResult submitCreditNote(ProviderCreditNoteRequest request);
    ProviderStatusResult queryStatus(String providerTrackingId);
}
```

La implementacion concreta dependera del proveedor seleccionado.

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
- Los errores del proveedor tecnologico DIAN deben mapearse a `EXTERNAL_PROVIDER_ERROR` cuando existan endpoints o adaptadores HTTP que los expongan.

## Observabilidad

- Logs estructurados.
- Correlation ID por request.
- Las peticiones HTTP usan `X-Correlation-Id` cuando llega en la solicitud o generan un UUID cuando falta; el identificador se expone en la respuesta, se guarda como atributo de request y se registra en MDC con la llave `correlationId`.
- Los logs tecnicos de inicio y fin de request deben emitirse como mensajes estructurados con `event`, `correlationId`, `method`, `path`, `status` y `durationMs`, sin registrar cuerpos, credenciales ni cabeceras sensibles.
- Metricas para emisiones, rechazos, reintentos, latencia del proveedor y errores.
- Auditoria fiscal separada de logs tecnicos.

## Estrategia de pruebas

- Unit tests de dominio y casos de uso.
- Tests de adaptadores con mocks del proveedor tecnologico.
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
- El `bff-service` vive en ECS Fargate y agrega respuestas, normaliza errores, propaga `Authorization`, `X-Company-Id`, `X-Correlation-Id` e `Idempotency-Key`, y protege al frontend de contratos internos inestables.

### Computo backend

- Los microservicios Spring Boot de larga vida se despliegan en ECS Fargate: `tenant-service`, `identity-service`, `catalog-service`, `thirdparty-service`, `inventory-service`, `billing-service`, `dian-provider-service`, `accounting-service`, `audit-service` y `reporting-service` cuando se materialice.
- Los procesos event-driven cortos se implementan como Lambdas: auditoria asincrona, efectos de inventario/contabilidad derivados de documentos, proyecciones de reportes, reintentos de estado del proveedor, notificaciones y tareas programadas de licencias.
- La base de datos productiva objetivo sera RDS/Aurora PostgreSQL, separando datos por servicio mediante base o esquema segun la fase.
- Los secretos, certificados y credenciales se resuelven desde Secrets Manager o Parameter Store en runtime.

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

`provider-submission-retry-lambda` consume la cola `provider-retries` y procesa `ProviderSubmissionFailed`/`ProviderSubmissionPending`. El consumidor carga el documento y snapshot de venta desde `billing`, ignora documentos ya `VALIDATED` o `REJECTED`, reenvia al `dian-provider-service` con la misma clave de idempotencia y actualiza `billing.electronic_document`. Si el proveedor acepta, publica `SaleConfirmed` y `ElectronicDocumentValidated` en `billing.outbox_event` para que inventario, contabilidad y reportes avancen por el canal asincrono. Si el proveedor sigue fallando, reporta el `messageId` en `SQSBatchResponse` para reintento y DLQ; si rechaza, marca `REJECTED` sin retry automatico.

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

La SPA deja de capturar manualmente `Authorization` y `X-Company-Id`. El flujo de sesion aprobado es:

1. El usuario ingresa email y password.
2. La SPA llama `POST /api/v1/auth/login` por medio del BFF.
3. La SPA guarda en memoria `tokenType`, `accessToken`, `userId`, `email`, `fullName` y `expiresAt`.
4. La SPA llama `GET /api/v1/me/companies` con `Authorization: Bearer <accessToken>`.
5. La SPA selecciona una empresa activa del usuario; si hay varias, permite cambiarla desde el header operativo.
6. La SPA llama `GET /api/v1/companies/{companyId}/license/validation?action=CREATE_TRANSACTION` para conocer si la empresa puede operar transacciones.
7. Los comandos de negocio posteriores envian `Authorization`, `X-Company-Id`, `X-Correlation-Id` e `Idempotency-Key` desde el estado de sesion.

Los formularios operativos son React controlled inputs. Cada campo que antes existia en el JSON editable queda representado por un input, select, checkbox o linea editable. El payload JSON se construye al enviar el formulario; la UI puede mostrar la respuesta del backend, pero el usuario no edita JSON crudo.

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
## TASK-067 rediseï¿½o visual profesional

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

La sesion autenticada se persiste en `sessionStorage` para tolerar recarga de pagina antes del timeout de inactividad. El snapshot contiene datos de sesion, empresa activa, accesos, licencia y empresas disponibles para `ROOT`. La sesion se restaura solo si la ultima actividad registrada ocurrio hace menos de 5 minutos. La actividad se reinicia con eventos del usuario (`click`, `keydown`, `mousemove`, `scroll`, `touchstart`). Al superar 5 minutos sin actividad se limpia la sesion y se muestra login con modal informativo.

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
- La UI de configuracion debe consumir estos catalogos desde `catalog-service` via BFF. Mientras se implementan endpoints, los catalogos estaticos del frontend quedan como fallback transitorio.
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
