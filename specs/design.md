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
- Para NIT, el digito de verificacion se calcula automaticamente desde el numero base mediante el algoritmo DIAN: pesos de derecha a izquierda `3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71`; se suma cada digito multiplicado por su peso; si el residuo modulo 11 es 0 el DV es 0, si es 1 el DV es 1, en otros casos el DV es `11 - residuo`.
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
- Los roles minimos sugeridos son `OWNER`, `ADMIN`, `CASHIER`, `ACCOUNTANT` y `AUDITOR`.
- La autorizacion debe evaluar empresa, rol y permiso antes de ejecutar comandos de negocio.
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
- Decision impact: TASK-059 lote 2 does not delete or modify applied thirdparty migration files and preserves `thirdparty.cliente`/`thirdparty.proveedor` tables for a later migration/respaldo plan.

## TASK-059 lote 2 legacy thirdparty cleanup decision

El lote 2 de TASK-059 retira el codigo runtime legacy de terceros (`/api/clientes`, `/api/proveedores`, modelo `Customer`/`Supplier`, adaptadores, repositorios, mappers y cliente REST a catalogo). El contrato canonico queda en `/api/v1/third-parties`, `/api/v1/customers` y `/api/v1/suppliers`. Las migraciones Flyway y tablas `thirdparty.cliente`/`thirdparty.proveedor` se conservan temporalmente para migracion o respaldo aprobado.

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
