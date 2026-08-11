# Requirements: Backend para facturacion electronica, POS, inventario y contabilidad

## Contexto

El proyecto actual es un backend Java/Spring Boot para un negocio pequeno, con modulos CRUD existentes para clientes, proveedores, productos, categorias, compras, gastos, impuestos, paises, metodos de pago, parametros y tipos de documento.

El objetivo de esta especificacion es definir las actividades faltantes para evolucionar el backend hacia una plataforma de facturacion electronica colombiana, inventario y contabilidad, usando Clean Architecture basada en microservicios y emision mediante proveedor tecnologico DIAN.

## Fuentes normativas de referencia

- DIAN - Sistema de Facturacion Electronica, normatividad: https://micrositios.dian.gov.co/sistema-de-facturacion-electronica/normatividad/
- Resolucion DIAN 000165 de 2023: https://normograma.dian.gov.co/dian/compilacion/docs/resolucion_dian_0165_2023.htm
- DIAN - Modificaciones publicadas a la Resolucion 000165, incluyendo Resoluciones 000189 de 2024 y 000202 de 2025, segun pagina oficial de normatividad DIAN.
- DIAN - Anexo Tecnico Factura Electronica de Venta v1.9.
- DIAN - Documento Equivalente Electronico y POS electronico: https://micrositios.dian.gov.co/sistema-de-facturacion-electronica/documento-equivalente-electronico/
- DIAN - Anexo Tecnico Documento Equivalente Electronico v1.0.
- Ley 1314 de 2009.
- Decreto 2420 de 2015.

Nota: esta especificacion tecnica no reemplaza validacion legal, tributaria o contable por contador publico, revisor fiscal o asesor tributario.

## Problema

El backend actual contiene entidades y operaciones administrativas basicas, pero no implementa todavia los componentes obligatorios para operar facturacion electronica y POS electronico en Colombia:

- Generacion de documentos electronicos conforme a anexos tecnicos DIAN.
- Integracion con proveedor tecnologico DIAN.
- Numeracion autorizada y resoluciones.
- CUFE/CUDE, QR, firma, XML UBL, ApplicationResponse y trazabilidad.
- Estados de documentos electronicos.
- Notas credito/debito y notas de ajuste de POS.
- Inventario transaccional asociado a ventas, compras y devoluciones.
- Registro contable y tributario basico.
- Seguridad, auditoria, observabilidad y pruebas suficientes.

## Objetivo

Definir e implementar progresivamente un backend basado en microservicios con Clean Architecture para:

- Emitir facturas electronicas de venta mediante proveedor tecnologico DIAN.
- Emitir documento equivalente electronico tipo tiquete POS.
- Gestionar inventario para un negocio pequeno.
- Registrar movimientos contables basicos.
- Mantener trazabilidad, pruebas automatizadas y cumplimiento normativo verificable.

## Alcance funcional

### Facturacion electronica

- Configurar emisor, responsabilidades fiscales, regimen, tributos y datos de contacto.
- Configurar resoluciones de numeracion, prefijos, rangos, vigencia y ambiente.
- Crear, calcular, validar y emitir facturas electronicas de venta.
- Generar y almacenar representacion estructurada del documento.
- Integrarse con proveedor tecnologico DIAN para emision, transmision, validacion y consulta de estado.
- Registrar CUFE, QR, XML, PDF/representacion grafica, estado DIAN, eventos y errores.
- Emitir notas credito y notas debito.
- Gestionar contingencia y reintentos cuando aplique.

### POS electronico

- Emitir documento equivalente electronico tipo tiquete de maquina registradora con sistema POS.
- Generar CUDE, QR y estructura tecnica requerida.
- Enviar documento POS al proveedor tecnologico.
- Registrar notas de ajuste para correccion o anulacion del POS electronico.
- Identificar adquirente cuando sea necesario para soportar impuestos descontables, costos o deducciones.

### Inventario

- Administrar productos, categorias, codigos de barras y stock.
- Registrar movimientos de inventario por compra, venta, anulacion, devolucion y ajuste manual.
- Prevenir ventas cuando no haya stock suficiente, salvo configuracion explicita.
- Mantener kardex y trazabilidad por producto.

### Contabilidad

- Configurar plan de cuentas basico.
- Registrar comprobantes contables derivados de ventas, compras, gastos, impuestos y ajustes.
- Mantener libro diario y libro mayor basicos.
- Asociar documentos tributarios con asientos contables.
- Soportar reportes minimos para revision contable del negocio.

### Seguridad y auditoria

- Autenticacion y autorizacion por roles.
- Auditoria de operaciones sensibles.
- No exponer secretos, certificados, llaves o credenciales.
- Trazabilidad por usuario, fecha, documento y transaccion.

## Fuera de alcance inicial

- Frontend movil. El frontend web SPA inicial queda incluido desde TASK-063 como capa operativa de prueba sobre BFF.
- Nomina electronica.
- RADIAN avanzado, salvo decision posterior.
- Integracion directa DIAN sin proveedor tecnologico.
- Multiempresa avanzado, salvo que se confirme.
- NIIF completo para empresas medianas/grandes.
- Conciliacion bancaria automatica.

## Stakeholders

- Propietario del negocio.
- Cajero o vendedor.
- Administrador del sistema.
- Contador.
- Proveedor tecnologico DIAN.
- DIAN.
- Cliente/adquirente.

## Requisitos funcionales

- RF-001: El sistema debe permitir configurar la informacion fiscal del emisor.
- RF-002: El sistema debe permitir administrar resoluciones de numeracion para factura electronica y POS electronico.
- RF-003: El sistema debe crear facturas electronicas a partir de cliente, productos, impuestos, medios de pago y totales.
- RF-004: El sistema debe emitir facturas mediante proveedor tecnologico DIAN.
- RF-005: El sistema debe registrar estados de emision y respuestas del proveedor tecnologico.
- RF-006: El sistema debe emitir POS electronico como documento equivalente electronico.
- RF-007: El sistema debe generar notas credito, notas debito y notas de ajuste POS.
- RF-008: El sistema debe actualizar inventario por ventas, compras, devoluciones y ajustes.
- RF-009: El sistema debe registrar movimientos contables asociados a documentos.
- RF-010: El sistema debe exponer APIs REST versionadas por microservicio.
- RF-011: El sistema debe registrar auditoria de cambios y operaciones fiscales.
- RF-012: El sistema debe permitir consultar documentos por numero, prefijo, cliente, estado, fecha y CUFE/CUDE.
- RF-013: El sistema debe permitir crear empresas/tenants reales y usar su identificador como frontera obligatoria de datos de negocio.
- RF-014: El sistema debe ejecutar el flujo completo de venta desde inventario hasta documento electronico, proveedor DIAN mock, descuento de stock y asiento contable automatico.
- RF-015: El sistema debe migrar los modulos legacy a bounded contexts con Clean Architecture antes de eliminar codigo o tablas antiguas.
- RF-016: El sistema debe administrar clientes/adquirentes con nombre completo o razon social, tipo de documento, numero de documento, digito de verificacion cuando aplique, tipo de persona, contacto y datos fiscales necesarios para facturacion.
- RF-017: El sistema debe calcular automaticamente el digito de verificacion para terceros y empresas con tipo de documento NIT, usando el algoritmo oficial DIAN documentado.
- RF-018: El sistema debe administrar proveedores como terceros que venden bienes, servicios publicos, servicios intangibles, insumos, gastos o activos al negocio.
- RF-019: El sistema debe administrar items vendibles diferenciando bienes fisicos, servicios/intangibles e insumos no vendibles cuando aplique.
- RF-020: El sistema debe permitir que un servicio o intangible se facture como item vendible sin descontar automaticamente insumos asociados.
- RF-021: El sistema debe permitir registrar referencias de insumos sugeridos para servicios, solo como informacion operativa, sin generar consumos automaticos.
- RF-022: El sistema debe permitir movimientos manuales de inventario para insumos por compra, consumo, desperdicio, ajuste de entrada y ajuste de salida.
- RF-056: El sistema debe permitir, despues de vender un servicio facturable, cargar los insumos asociados como sugerencia y confirmar manualmente las cantidades reales consumidas para descontarlas del inventario.
- RF-023: El sistema debe registrar compras, gastos, cuentas por pagar y pagos basicos asociados a proveedores, inventario y contabilidad.
- RF-024: El sistema debe generar factura electronica o POS electronico tanto para bienes fisicos como para servicios, conservando snapshot fiscal de lineas, impuestos y tercero adquirente.
- RF-025: El sistema debe exponer reportes operativos minimos de ventas, inventario/kardex, compras/gastos, documentos electronicos, cuentas por cobrar/pagar y libros contables.
- RF-026: El sistema debe implementar usuarios, roles y permisos por empresa antes de operar en escenarios reales multiempresa.
- RF-027: El sistema debe implementar licenciamiento por empresa para habilitar, suspender o limitar el uso de la plataforma segun condiciones comerciales.
- RF-028: El sistema debe administrar cuentas por cobrar por empresa, cliente, documento origen, vencimiento, saldo, pagos parciales/totales y estado de cartera.
- RF-029: El sistema debe exponer una SPA web inicial que consuma unicamente el BFF para probar el flujo operativo desde empresa hasta venta POS/factura mock y reportes.
- RF-030: El sistema debe simplificar el registro de clientes naturales para factura electronica, fijando automaticamente perfil fiscal no responsable/no aplica, bloqueando NIT/DV y evitando datos de persona juridica.
- RF-031: El sistema debe administrar catalogos oficiales y operativos como datos versionados/configurables, permitiendo inactivar o extender catalogos empresariales sin alterar codigos regulatorios oficiales.
- RF-038: El sistema debe exponer un modulo administrativo de catalogos que permita seleccionar un catalogo por nombre en espanol, listar sus registros, crear nuevos items permitidos, actualizar etiquetas/descripciones y activar o inactivar registros segun permisos.
- RF-039: El sistema debe retirar catalogos operativos/regulatorios hardcodeados del frontend; la SPA solo puede consumir catalogos desde base de datos mediante BFF y `catalog-service`.
- RF-040: El sistema debe auditar tablas, migraciones Flyway, entidades JPA, repositorios, endpoints y datos legacy para eliminar solamente lo que no participa en el flujo actual ni conserva datos utiles pendientes de migracion.
- RF-041: El sistema debe administrar impuestos de venta como catalogo fiscal versionado y configurable, con codigos tecnicos, etiqueta en espanol, tarifa y fuente normativa.
- RF-042: El sistema debe asociar a cada producto/servicio/insumo vendible el impuesto de venta aplicable desde inventario, para que el vendedor POS no capture impuesto ni tarifa al vender.
- RF-043: El sistema debe permitir escaneo de codigo de barras USB HID en inventario y venta POS usando campos dedicados; al escanear en venta debe buscar automaticamente el producto y agregar o incrementar la linea sin clic manual.
- RF-044: El sistema debe permitir que el comprador decida si desea identificarse para factura electronica; si no, la venta debe usar un perfil fiscal de consumidor final parametrizado en base de datos, sin crear tercero ni quemar datos en frontend.
- RF-045: El sistema debe eliminar datos de negocio precargados en frontend; la SPA no debe contener `initialState` con empresas, terceros, productos, resoluciones, ventas, catalogos regulatorios ni datos demo.
- RF-046: El unico seed funcional permitido para pruebas locales iniciales es el usuario global `ROOT`; cualquier otro dato de prueba debe crearse por API durante scripts E2E o migraciones controladas de catalogos.
- RF-047: El sistema debe cargar todos los catalogos operativos y regulatorios desde PostgreSQL mediante `catalog-service` y BFF; si no estan disponibles, la UI debe bloquear el formulario dependiente con error controlado.
- RF-048: El sistema debe implementar un modulo de nomina para registrar empleados, contratos, pagos periodicos, pagos diarios verbales, devengados, deducciones y costos por empleado.
- RF-049: El sistema debe permitir configurar por empresa si usa o no nomina electronica; la funcionalidad queda disponible, pero no se activa automaticamente para todas las empresas.
- RF-050: El sistema debe registrar pagos diarios verbales/jornales con fecha, actividad, horas o jornada, valor acordado, valor pagado, medio de pago, observaciones, evidencia opcional y clasificacion laboral/contractual.
- RF-051: El sistema debe clasificar pagos de personal como empleado formal, trabajador por dias, pago diario verbal, contratista independiente o pago operativo pendiente de clasificacion.
- RF-052: El sistema debe mostrar advertencias legales configurables cuando un pago diario verbal pueda implicar obligaciones laborales, seguridad social, riesgos laborales o revision contable.
- RF-053: El sistema debe implementar un modulo contable operativo para ingresos, egresos, costos de operacion, activos, cuentas por cobrar, cuentas por pagar y reportes basicos por empresa.
- RF-054: El sistema debe mejorar el modulo de logs para mostrar por defecto los eventos del dia actual, filtrar por fechas y permitir `resourceType` opcional desde lista desplegable cargada de backend.
- RF-055: El sistema debe personalizar los mensajes de modales segun contexto; login por credenciales invalidas debe mostrar mensaje especifico y errores 5xx deben mostrar fallo interno generico.
- RF-056: El permiso empresarial `SALES_CREATE` debe habilitar el flujo completo de venta POS: registrar venta, confirmar POS y emitir el documento electronico asociado hacia DIAN/proveedor mock. El permiso `FISCAL_DOCUMENTS_ISSUE` queda reservado para operaciones fiscales avanzadas como configuracion de emisor/resoluciones, notas, ajustes, gestion manual y reenvios.
- RF-057: Las traducciones visibles del frontend deben gestionarse mediante una libreria de internacionalizacion y recursos externos. Los codigos internos pueden permanecer en ingles, pero la UI debe renderizar textos en espanol sin diccionarios manuales dispersos en componentes.

## Requisitos no funcionales

- RNF-001: Java 17 y Spring Boot 3.x.
- RNF-002: Clean Architecture dentro de cada microservicio.
- RNF-003: Persistencia PostgreSQL.
- RNF-004: Pruebas unitarias obligatorias para todo codigo nuevo.
- RNF-005: Errores estructurados sin exponer informacion sensible.
- RNF-006: Secretos en variables de entorno o gestor de secretos.
- RNF-007: Logs estructurados con correlation ID.
- RNF-008: APIs documentadas con OpenAPI.
- RNF-009: Migraciones de base de datos versionadas.
- RNF-010: Integraciones externas desacopladas mediante puertos y adaptadores.
- RNF-011: Cada microservicio fisico debe tener artefacto de build independiente, contenedor propio, healthcheck y configuracion externa por variables de entorno.
- RNF-012: La comunicacion inicial entre microservicios sera REST sincrona con `X-Correlation-Id`, `X-Company-Id` e idempotencia en comandos criticos.
- RNF-013: La extraccion fisica debe mantener pruebas unitarias, pruebas de controlador, pruebas de persistencia y pruebas end-to-end locales en Docker Compose.
- RNF-014: La mensajeria asincrona objetivo en produccion AWS usara patron Outbox/Inbox con EventBridge/SQS y consumidores Lambda cuando el flujo funcional core este completo, probado y depurado.
- RNF-015: Los casos de uso de identificacion fiscal deben ser deterministas y probados con datos de NIT validos e invalidos.
- RNF-016: Los reportes iniciales deben ejecutarse por empresa, rango de fechas y filtros basicos sin exponer informacion de otros tenants.
- RNF-017: Las reglas de licenciamiento deben evaluarse antes de ejecutar comandos de negocio que creen documentos, usuarios o transacciones segun el plan contratado.
- RNF-018: La limpieza legacy debe ejecutarse solo despues de una matriz de reemplazo, prueba E2E aprobada y verificacion de referencias con compilacion completa.
- RNF-019: El despliegue cloud objetivo debe usar frontend estatico en Amazon S3 + CloudFront, entrada publica por API Gateway/BFF, microservicios Spring Boot en ECS Fargate, procesos event-driven en Lambda y persistencia en RDS/Aurora PostgreSQL.
- RNF-020: Ningun microservicio de negocio debe exponerse directamente al navegador; el frontend debe consumir el BFF/API Gateway y los servicios internos deben permanecer privados.
- RNF-021: La IaC productiva no debe incluir contenedores, artefactos o rutas legacy eliminadas; Docker Compose queda limitado a desarrollo local y pruebas.
- RNF-022: La SPA debe mantener estado inicial vacio o derivado de sesion/API; no debe importar datos de negocio desde archivos locales.
- RNF-023: La carga de catalogos en frontend debe usar estados asincronos explicitos, cleanup de fetches/temporizadores y bloqueo seguro de formularios cuando falten catalogos requeridos.
- RNF-024: La SPA debe centralizar textos traducibles con `react-i18next`/`i18next` o libreria equivalente, manteniendo recursos versionados por idioma y evitando constantes UI duplicadas.

## Reglas de negocio

- RN-001: Un documento fiscal no debe emitirse sin resolucion vigente y numeracion disponible.
- RN-002: Un numero autorizado no debe reutilizarse.
- RN-003: Una factura validada por DIAN no debe editarse; cualquier correccion debe realizarse mediante nota credito o debito segun aplique.
- RN-004: Un POS electronico corregido o anulado debe usar nota de ajuste.
- RN-005: Los totales del documento deben cuadrar con lineas, impuestos, descuentos y cargos.
- RN-006: La venta debe afectar inventario solo cuando el documento alcance el estado definido como contablemente efectivo.
- RN-007: Las credenciales y certificados no deben almacenarse en texto plano en el repositorio.
- RN-008: Todo evento fiscal debe conservar trazabilidad suficiente para auditoria.
- RN-009: Los totales fiscales se calcularan por linea.
- RN-010: Los descuentos se aplicaran antes del impuesto.
- RN-011: Los importes monetarios se redondearan a 2 decimales usando `HALF_UP`.
- RN-012: Los totales del documento se obtendran sumando los resultados redondeados de sus lineas.
- RN-013: La unidad minima de despliegue sera el microservicio por bounded context, no un contenedor por endpoint individual.
- RN-014: Un documento fiscal aceptado por el proveedor DIAN mock debe afectar inventario y contabilidad una sola vez por empresa, aunque el comando se reintente.
- RN-015: Las tablas y clases legacy solo podran eliminarse despues de demostrar que el flujo equivalente existe en Clean Architecture, que la prueba end-to-end pasa y que no quedan referencias de compilacion o runtime.
- RN-016: Para tipo de documento NIT, el digito de verificacion se calcula automaticamente a partir del numero base; no se debe capturar como valor libre salvo importacion historica controlada.
- RN-017: Para tipos de documento distintos a NIT, el digito de verificacion debe quedar nulo o vacio.
- RN-018: El tipo de persona debe ser `NATURAL` o `JURIDICA`; una persona juridica debe usar razon social y una persona natural debe permitir nombre completo.
- RN-019: Un tercero puede tener rol `CUSTOMER`, `SUPPLIER` o ambos dentro de una misma empresa sin duplicar la identificacion fiscal.
- RN-020: Un bien fisico con stock activo debe validar disponibilidad y descontar stock cuando la venta/documento sea efectivo segun politica aprobada.
- RN-021: Un servicio/intangible puede venderse y facturarse, pero no debe descontar insumos automaticamente por receta.
- RN-022: Los insumos asociados a servicios deben afectarse mediante movimientos manuales de inventario por compra, consumo, desperdicio o ajuste.
- RN-023: Un movimiento manual de consumo o desperdicio de insumo requiere motivo, producto/insumo, cantidad, usuario o proceso origen, fecha y empresa.
- RN-060: El consumo asistido de insumos por servicio debe requerir confirmacion explicita del usuario, motivo, `sourceDocumentId` de la venta o documento origen, `Idempotency-Key` y cantidades mayores a cero; no puede crear consumos duplicados para el mismo insumo en una misma confirmacion.
- RN-024: Las compras de productos o insumos incrementan inventario al confirmarse; los gastos sin inventario no deben crear stock.
- RN-025: Las compras, gastos y cuentas por pagar deben contabilizarse con reglas parametrizables por empresa y cuentas PUC aprobadas.
- RN-026: Ninguna venta, compra, gasto, movimiento o reporte puede operar sin `company_id`.
- RN-027: Una empresa con licencia suspendida no debe poder emitir documentos fiscales ni crear nuevas transacciones de negocio, salvo consultas y acciones administrativas permitidas.
- RN-028: Los reportes deben basarse en datos persistidos por los microservicios activos, no en tablas legacy que esten pendientes de eliminacion.
- RN-029: La depuracion de tablas legacy debe ocurrir despues de migrar clientes, proveedores, productos, servicios, compras, gastos, facturas, auditoria y datos contables necesarios al modelo Clean Architecture.
- RN-030: La integracion event-driven productiva con Outbox/Inbox, EventBridge/SQS y Lambdas se implementara despues de cerrar la logica backend core, migrar legacy pendiente y aprobar la depuracion.
- RN-031: Una cuenta por cobrar solo debe crearse desde una venta/documento fiscal valido para credito o por registro aprobado de cartera inicial; los pagos deben disminuir saldo sin permitir saldos negativos.
- RN-032: Los procesos event-driven transversales como auditoria, proyecciones de reportes, reintentos de proveedor, notificaciones y efectos posteriores no criticos deben implementarse como Lambdas idempotentes disparadas por eventos, sin convertirlos en dependencias de arranque de los microservicios.
- RN-033: La plataforma no usara brokers self-hosted en produccion; la mensajeria asincrona aprobada para cloud es EventBridge/SQS + Lambda.
- RN-034: Cuando un tercero sea solo cliente y persona natural, el sistema debe fijar `taxResponsibilities=["R-99-PN"]`, `taxRegime=NO_RESPONSABLE_IVA`, impedir `identificationTypeCode=31`, dejar `verificationDigit` nulo y no aceptar razon social ni nombre comercial.
- RN-035: Para un cliente natural simple sin direccion, el municipio debe derivarse de la empresa/emisor fiscal activo; solo al registrar direccion de residencia se permite seleccionar otro municipio.
- RN-036: Los catalogos regulatorios DIAN/DANE deben conservar codigo, etiqueta, fuente, version y vigencia; las empresas solo podran inactivar/activar opciones permitidas o crear opciones operativas si existe mapeo fiscal valido.
- RN-037: Los codigos tecnicos de catalogo se almacenan en ingles en base de datos, pero toda etiqueta visible de la SPA debe presentarse en espanol profesional.
- RN-038: Los catalogos regulatorios globales solo pueden ser creados, actualizados o inactivados por `ROOT`; los administradores empresariales solo pueden gestionar activacion/inactivacion empresarial o catalogos operativos permitidos dentro de su empresa.
- RN-039: La eliminacion de tablas o migraciones legacy requiere matriz de uso, verificacion de referencias, respaldo/migracion de datos cuando aplique, migracion Flyway nueva y validacion sobre base limpia y base local actual.
- RN-040: El impuesto de una linea POS se calcula desde el snapshot del producto en inventario; `billing-service` no debe confiar en `taxCode` o `taxRate` enviados por la SPA para ventas POS.
- RN-041: Los impuestos fiscales deben cargarse desde `catalog-service`/base de datos y conservar `source`, `sourceVersion`, vigencia y estado; no deben existir listas fiscales productivas hardcodeadas en frontend.
- RN-042: El canal de venta POS operativo es interno y siempre se procesa como `POS`/`ELECTRONIC_POS` en esta fase; la SPA no debe pedir al vendedor seleccionar canal.
- RN-043: El escaneo USB HID se interpreta como entrada de teclado solo en campos de codigo de barras; la SPA debe mantener foco controlado para inventario/POS y procesar el codigo por debounce automatico o terminador del scanner sin requerir clic.
- RN-044: Si el comprador no desea factura electronica nominada, `billing-service` debe resolver el consumidor final desde configuracion fiscal persistida y usarlo como snapshot del adquirente, sin persistirlo como tercero empresarial.
- RN-045: El perfil de consumidor final debe ser parametrizable por empresa o global, auditable y modificable por `ROOT` si DIAN/proveedor tecnologico cambia el contrato tecnico; la SPA solo envia la decision `buyerIdentificationMode`.
- RN-046: Toda accion mutable del backend (`POST`, `PUT`, `PATCH`, `DELETE`) debe registrar auditoria segura con empresa, usuario cuando exista, accion, recurso, resultado, detalle no sensible, fecha y correlacion.
- RN-047: La auditoria no debe registrar secretos, tokens, passwords, certificados, hashes de contrasena ni payloads completos con datos sensibles.
- RN-048: Un fallo temporal de auditoria no debe revertir una accion de negocio ya persistida; debe dejar trazabilidad tecnica y/o evento pendiente de reintento cuando exista infraestructura asincrona.
- RN-049: La SPA no debe mostrar paneles tecnicos permanentes de respuesta/error JSON en el flujo operativo; debe mostrar modal de proceso, exito o error generico y orientar a revisar Logs/Auditoria.
- RN-050: Los catalogos regulatorios, operativos, tipos de tercero, tipos de persona, tipos de item, metodos de pago, billeteras, impuestos, responsabilidades fiscales, regimenes y DIVIPOLA deben provenir de base de datos.
- RN-051: El frontend puede conservar constantes de presentacion como nombres de pasos o labels de navegacion, pero no opciones de negocio seleccionables ni datos demo.
- RN-052: Ningun formulario debe autocompletar datos demo de empresa, tercero, producto, resolucion, venta, usuario o catalogo; el usuario debe capturar datos o seleccionarlos desde catalogos persistidos.
- RN-053: Los scripts E2E deben crear los datos de prueba por API desde cero y no depender de `initialState` del frontend.
- RN-054: El flujo de nomina electronica se habilita solo si la configuracion empresarial `payrollElectronicEnabled=true`; si esta apagada, la empresa puede registrar nomina interna sin generar documento soporte electronico mock.
- RN-055: Un pago diario verbal no se debe tratar automaticamente como exento de obligaciones laborales; el sistema debe registrar clasificacion, advertencia y auditoria de la decision administrativa.
- RN-056: Si el pago corresponde a contratista independiente, el costo debe integrarse como egreso/proveedor o gasto operativo, no como empleado de nomina formal.
- RN-057: Todo registro de nomina, pago diario, liquidacion, activacion de nomina electronica y contabilizacion asociada debe generar auditoria segura.
- RN-058: La ausencia de licencia configurada debe responder con codigo funcional `LICENSE_NOT_CONFIGURED` o mensaje equivalente en UI; no debe mostrarse como error generico de operacion.
- RN-059: Una licencia parametrizable debe almacenar modulos habilitados; si no se especifican modulos, se interpreta como licencia sin acceso operativo salvo administracion ROOT.
- RN-061: Los modulos licenciables son capacidades comerciales de alto nivel y se almacenan en ingles en backend/base de datos, pero la UI debe presentarlos en espanol.
- RN-062: ROOT puede asignar o cambiar modulos licenciados para cualquier empresa; administradores empresariales solo pueden operar dentro de los modulos que ROOT habilito para su empresa.

## Supuestos

- Se usara proveedor tecnologico DIAN para emision y validacion de documentos.
- El negocio emitira POS electronico.
- El backend se evolucionara hacia microservicios, manteniendo compatibilidad temporal con el proyecto actual.
- La correccion de credenciales hardcodeadas queda incluida como tarea aprobada para fase de implementacion.
- Mientras no exista proveedor tecnologico, contrato tecnico, certificado y credenciales reales, la integracion DIAN se implementara con un adaptador dummy local sin llamadas externas.
- La migracion fisica a microservicios se hara por bounded context para mantener un balance entre independencia de despliegue y complejidad operacional.
- En local se usara Docker Compose con contenedores por microservicio; la separacion de bases de datos podra iniciar con esquemas o bases separadas en PostgreSQL y evolucionar a instancias independientes.
- Para produccion AWS se adopta EventBridge/SQS + Lambda como objetivo event-driven administrado. No se contempla despliegue on-premise ni broker self-hosted.

## Restricciones

- No implementar codigo sin criterios de aceptacion.
- No modificar comportamiento sin actualizar specs.
- No agregar dependencias sin aprobacion.
- No guardar secretos reales en archivos versionados.
- Validar cambios normativos antes de salir a produccion.
- No mezclar refactorizacion arquitectonica de modulos legacy con cambios funcionales no aprobados.
- Cada refactor debe preservar compatibilidad publica o documentar y aprobar cualquier ruptura antes de implementarla.
- No crear nanoservicios por endpoint; cada artefacto debe representar una capacidad de negocio cohesionada.
- No eliminar codigo, tablas ni migraciones legacy hasta completar la matriz de reemplazo y la prueba end-to-end aprobada.
- No introducir broker, login ni infraestructura adicional antes de completar y validar el flujo core de negocio por API, persistencia PostgreSQL y prueba end-to-end desde cero, salvo documentacion/planificacion SDD aprobada.
- No mantener catalogos regulatorios u operativos hardcodeados en frontend, ni fallback productivo de catalogos locales.
- No crear seeds de empresas, terceros, productos, ventas, resoluciones o usuarios empresariales para UI local; solo `ROOT` puede existir como seed inicial de pruebas.

## Fuentes normativas de referencia

- Facturacion electronica y documento equivalente electronico: documentacion tecnica y normatividad oficial DIAN, incluyendo anexos tecnicos vigentes, Resolucion 00165 de 2023 y modificaciones publicadas.
- Nomina electronica: documento soporte de pago de nomina electronica DIAN y Resolucion 000013 de 2021; se implementa como funcionalidad opcional por empresa, no como activacion obligatoria global.
- DIVIPOLA: codificacion oficial DANE; departamentos y municipios se modelan en tablas separadas y la UI muestra nombres, no codigos crudos.
- PUC colombiano: Decreto 2650 de 1993 y modificaciones publicadas en SUIN/Juriscol; la plantilla contable inicial es editable y debe evolucionar hacia carga parametrizable del PUC completo.
- Los catalogos regulatorios deben quedar en base de datos con fuente, version, vigencia y estado; el frontend no puede contener catalogos fiscales productivos hardcodeados.

## Criterios de aceptacion

Los criterios detallados se encuentran en `specs/acceptance-criteria.md`.

## Trazabilidad

Cada tarea de `specs/tasks.md` debe enlazar uno o mas requisitos funcionales, no funcionales y criterios de aceptacion.

## Requisitos RBAC modular aprobado

- RF-030: El sistema debe soportar un usuario `ROOT` global de plataforma que no pertenece a ninguna empresa y no depende de licencia empresarial para iniciar sesion.
- RF-031: El usuario `ROOT` debe poder crear empresas contratantes, configurar o activar licencias y crear/asignar el administrador inicial de cada empresa.
- RF-032: Todos los roles distintos de `ROOT` deben pertenecer a una empresa especifica y estar aislados por `company_id`.
- RF-033: Cada empresa debe poder crear roles personalizados con nombres propios y permisos modulares dentro de su alcance empresarial.
- RF-034: El sistema debe impedir que un actor cree, edite o asigne roles con permisos iguales, superiores o no poseidos por el actor.
- RF-035: Los permisos globales `GLOBAL_*` deben ser exclusivos de `ROOT` y no deben asignarse a roles empresariales.
- RF-036: El frontend debe mostrar panel global para `ROOT` y panel empresarial para usuarios de empresa segun permisos efectivos.
- RF-037: El backend debe validar permisos efectivos en cada accion protegida; el frontend no es fuente de seguridad.
- RF-038: ROOT debe poder administrar catalogos globales/regulatorios de la plataforma, incluyendo crear, actualizar, activar e inactivar registros, siempre dejando auditoria.
- RF-039: La UI debe ofrecer un modulo de Logs/Auditoria visible solo para ROOT, administradores de empresa o roles con permiso `AUDIT_VIEW`.
- RF-040: ROOT debe poder consultar auditoria de la empresa activa seleccionada y administradores de empresa solo auditoria de su empresa.
- RF-041: ROOT debe poder crear, actualizar, activar y suspender licencias empresariales desde la SPA sin usar llamadas manuales.
- RF-042: Una licencia empresarial debe definir vigencia, limites comerciales y modulos habilitados de forma parametrizable.
- RF-043: El sistema debe validar licencia por modulo/accion antes de habilitar menus y antes de ejecutar operaciones criticas.
- RF-044: Si una empresa no tiene licencia configurada, el login empresarial debe mostrar un mensaje claro de licencia no configurada, cerrar la sesion y no mostrar menus.
- RF-045: El licenciamiento comercial no reemplaza RBAC; un usuario solo puede acceder a funcionalidades permitidas por la licencia de la empresa y por sus permisos efectivos.
- RF-046: El sistema debe aplicar los limites comerciales de licencia: `maxUsers` limita la cantidad de usuarios activos con acceso a una empresa y `maxMonthlyDocuments` limita la cantidad de documentos fiscales emitidos por mes en `billing-service`.
- RF-047: Cuando una empresa alcance un limite de licencia, el backend debe bloquear la operacion con mensaje funcional claro y la UI debe mostrarlo como error de regla de negocio.
- RF-048: La administracion de empresa debe diferenciar alcance ROOT y alcance empresarial: ROOT puede crear, actualizar, activar e inactivar empresas; OWNER/ADMIN empresarial solo puede actualizar la empresa activa y no debe ver acciones de creacion de nuevas empresas.
- RF-049: La UI debe mostrar nombres de empresa y etiquetas de permisos/modulos en espanol, sin exponer UUID como dato principal al usuario final; los codigos internos pueden permanecer en ingles en API, backend y base de datos.
