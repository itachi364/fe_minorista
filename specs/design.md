# Design: Backend Clean Architecture basado en microservicios

## Decision tecnica

Se usara Clean Architecture dentro de una estrategia basada en microservicios. Cada microservicio debe separar dominio, casos de uso, puertos, adaptadores y configuracion framework.

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

### inventory-service

- Administra productos y stock.
- Registra movimientos inmutables.
- Expone disponibilidad de productos.

### accounting-service

- Genera asientos desde eventos fiscales.
- Valida partida doble.
- Expone libro diario y mayor.

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
3. `billing-service` calcula totales, impuestos y CUDE.
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
