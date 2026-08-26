# Requirements: Backend para facturacion electronica, POS, inventario y contabilidad

## Contexto

El proyecto actual es un backend Java/Spring Boot para un negocio pequeno, con modulos CRUD existentes para clientes, proveedores, productos, categorias, compras, gastos, impuestos, paises, metodos de pago, parametros y tipos de documento.

El objetivo de esta especificacion es definir las actividades faltantes para evolucionar el backend hacia una plataforma de facturacion electronica colombiana, inventario y contabilidad, usando Clean Architecture basada en microservicios y emision mediante configuracion DIAN parametrizable por empresa.

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
- Configuracion DIAN parametrizable por empresa, sin ofrecer el software como proveedor tecnologico DIAN.
- Numeracion autorizada y resoluciones.
- CUFE/CUDE, QR, firma, XML UBL, ApplicationResponse y trazabilidad.
- Estados de documentos electronicos.
- Notas credito/debito y notas de ajuste de POS.
- Inventario transaccional asociado a ventas, compras y devoluciones.
- Registro contable y tributario basico.
- Seguridad, auditoria, observabilidad y pruebas suficientes.

## Objetivo

Definir e implementar progresivamente un backend basado en microservicios con Clean Architecture para:

- Emitir facturas electronicas de venta mediante conexion DIAN configurada por cada empresa facturadora.
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
- Integrarse con la DIAN o con el modo de operacion que cada empresa configure bajo su responsabilidad como facturador electronico.
- Permitir que cada empresa configure certificado digital, software ID/PIN, ambiente, resoluciones, URLs y credenciales/referencias seguras requeridas por su proceso de habilitacion.
- Informar explicitamente que la plataforma no presta servicio de proveedor tecnologico DIAN; provee un modulo configurable para que cada empresa opere su propia conexion.
- Registrar CUFE, QR, XML, PDF/representacion grafica, estado DIAN, eventos y errores.
- Emitir notas credito y notas debito.
- Gestionar contingencia y reintentos cuando aplique.

### POS electronico

- Emitir documento equivalente electronico tipo tiquete de maquina registradora con sistema POS.
- Generar CUDE, QR y estructura tecnica requerida.
- Enviar documento POS a la conexion DIAN configurada para la empresa.
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
- Prestacion de servicios como proveedor tecnologico DIAN autorizado.
- Multiempresa avanzado, salvo que se confirme.
- NIIF completo para empresas medianas/grandes.
- Conciliacion bancaria automatica.

## Stakeholders

- Propietario del negocio.
- Cajero o vendedor.
- Administrador del sistema.
- Contador.
- DIAN.
- Empresa facturadora responsable de su habilitacion/certificacion.
- Asesor tributario/contador de la empresa.
- DIAN.
- Cliente/adquirente.

## Requisitos funcionales

- RF-001: El sistema debe permitir configurar la informacion fiscal del emisor.
- RF-002: El sistema debe permitir administrar resoluciones de numeracion para factura electronica y POS electronico.
- RF-003: El sistema debe crear facturas electronicas a partir de cliente, productos, impuestos, medios de pago y totales.
- RF-004: El sistema debe emitir facturas mediante la configuracion DIAN activa de la empresa.
- RF-005: El sistema debe registrar estados de emision y respuestas de la conexion DIAN configurada.
- RF-006: El sistema debe emitir POS electronico como documento equivalente electronico.
- RF-007: El sistema debe generar notas credito, notas debito y notas de ajuste POS.
- RF-008: El sistema debe actualizar inventario por ventas, compras, devoluciones y ajustes.
- RF-009: El sistema debe registrar movimientos contables asociados a documentos.
- RF-010: El sistema debe exponer APIs REST versionadas por microservicio.
- RF-011: El sistema debe registrar auditoria de cambios y operaciones fiscales.
- RF-012: El sistema debe permitir consultar documentos por numero, prefijo, cliente, estado, fecha y CUFE/CUDE.
- RF-013: El sistema debe permitir crear empresas/tenants reales y usar su identificador como frontera obligatoria de datos de negocio.
- RF-014: El sistema debe ejecutar el flujo completo de venta desde inventario hasta documento electronico, conector DIAN mock, descuento de stock y asiento contable automatico.
- RF-015: El sistema debe migrar los modulos legacy a bounded contexts con Clean Architecture antes de eliminar codigo o tablas antiguas.
- RF-016: El sistema debe administrar clientes/adquirentes con nombre completo o razon social, tipo de documento, numero de documento, digito de verificacion cuando aplique, tipo de persona, contacto y datos fiscales necesarios para facturacion.
- RF-017: El sistema debe calcular automaticamente el digito de verificacion para terceros y empresas con tipo de documento NIT, usando el algoritmo oficial DIAN documentado.
- RF-018: El sistema debe administrar proveedores como terceros que venden bienes, servicios publicos, servicios intangibles, insumos, gastos o activos al negocio.
- RF-019: El sistema debe administrar items vendibles diferenciando bienes fisicos, servicios/intangibles e insumos no vendibles cuando aplique.
- RF-020: El sistema debe permitir que un servicio o intangible se facture como item vendible sin descontar automaticamente insumos asociados.
- RF-021: El sistema debe permitir registrar referencias de insumos sugeridos para servicios, solo como informacion operativa, sin generar consumos automaticos.
- RF-022: El sistema debe permitir movimientos manuales de inventario para insumos por compra, consumo, desperdicio, ajuste de entrada y ajuste de salida.
- RF-032: El sistema debe permitir, despues de vender un servicio facturable, cargar los insumos asociados como sugerencia y confirmar manualmente las cantidades reales consumidas para descontarlas del inventario.
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
- RF-125: El permiso empresarial `SALES_CREATE` debe habilitar el flujo completo de venta POS: registrar venta, confirmar POS y emitir el documento electronico asociado hacia el conector DIAN mock o configurado. El permiso `FISCAL_DOCUMENTS_ISSUE` queda reservado para operaciones fiscales avanzadas como configuracion de emisor/resoluciones, notas, ajustes, gestion manual y reenvios.
- RF-126: Las traducciones visibles del frontend deben gestionarse mediante una libreria de internacionalizacion y recursos externos. Los codigos internos pueden permanecer en ingles, pero la UI debe renderizar textos en espanol sin diccionarios manuales dispersos en componentes.
- RF-127: La navegacion principal debe priorizar `Ventas` como modulo inicial y agrupar configuracion y procesos administrativos en submenus: `Configuracion` y `Contabilidad`.
- RF-128: La administracion de roles debe tener una pantalla exclusiva con formulario de permisos, tabla de roles por empresa, actualizacion y activacion/inactivacion.
- RF-129: La administracion de usuarios debe tener una pantalla exclusiva con creacion de usuario y asignacion obligatoria de rol en el mismo flujo, listado por empresa, actualizacion y activacion/inactivacion.
- RF-130: Las acciones exitosas deben cerrar el modal de progreso/exito en maximo 1 segundo; los errores deben permanecer visibles hasta cierre manual.
- RF-062: El sistema debe administrar una configuracion DIAN por empresa, aislada por `company_id`, con modo `MOCK`, `SOFTWARE_PROPIO_CLIENTE` o modo equivalente aprobado.
- RF-063: El sistema debe permitir capturar y actualizar referencias seguras a certificado digital, software ID, PIN tecnico, clave tecnica, ambiente, URLs de habilitacion/produccion y datos requeridos por DIAN sin exponer secretos.
- RF-064: El sistema debe permitir probar la configuracion DIAN de una empresa antes de habilitar emision real, registrando resultado y auditoria segura.
- RF-065: El sistema debe mostrar en la UI una declaracion operacional clara: el producto es software parametrizable por empresa y no presta servicio de proveedor tecnologico DIAN.
- RF-066: El sistema debe conservar modo `MOCK` para desarrollo/E2E y bloquear modo real si la configuracion DIAN de la empresa esta incompleta, vencida o no habilitada.
- RF-078: El sistema debe usar autenticacion productiva con Amazon Cognito Hosted UI y OAuth 2.0 Authorization Code Grant con PKCE.
- RF-079: El frontend productivo no debe capturar ni manejar directamente passwords, access tokens, refresh tokens ni bearer tokens reutilizables.
- RF-080: El BFF debe intercambiar el codigo OAuth por tokens, crear una sesion server-side y entregar al navegador solo una cookie opaca `HttpOnly`, `Secure` y `SameSite`.
- RF-081: El BFF debe validar la sesion en cada request, resolver identidad/empresa/permisos y propagar solo headers internos necesarios hacia microservicios.
- RF-082: El sistema debe permitir logout seguro con invalidacion de cookie, revocacion de sesion server-side y revocacion de tokens Cognito cuando aplique.
- RF-083: ROOT, administradores empresariales y acciones criticas deben requerir MFA en produccion.
- RF-084: El frontend debe eliminar logs, storage y paneles que expongan credenciales, tokens, headers sensibles, passwords o payloads completos.
- RF-085: Los endpoints mutables protegidos por cookie deben implementar proteccion CSRF.
- RF-086: La infraestructura productiva debe aplicar headers de seguridad: HSTS, CSP, `X-Content-Type-Options`, `X-Frame-Options`/`frame-ancestors` y `Referrer-Policy`.
- RF-087: Al crear una empresa, el backend autorizado debe poder crear secretos AWS por empresa de forma programatica, usando IAM minimo, KMS y auditoria.
- RF-088: El modo de autenticacion dummy/opaco actual queda permitido solo para desarrollo local y pruebas controladas; debe estar bloqueado en produccion.

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
- RNF-025: Los secretos DIAN por empresa deben almacenarse como referencias a un gestor de secretos; la base de datos solo puede guardar metadata, huellas, fechas, alias, estado y referencias no sensibles.
- RNF-026: La arquitectura debe separar la capacidad tecnica de conexion DIAN del rol regulatorio de proveedor tecnologico; la documentacion, UI y contratos no deben presentar el producto como proveedor tecnologico DIAN.
- RNF-027: Todo trafico productivo del navegador debe usar HTTPS/TLS; no se aceptan endpoints HTTP publicos para autenticacion o APIs.
- RNF-028: Los tokens Cognito almacenados server-side deben cifrarse en reposo con KMS o mecanismo equivalente; nunca deben guardarse ni mostrarse en claro.
- RNF-029: Las cookies de sesion productivas deben ser `HttpOnly`, `Secure`, `SameSite=Lax` o `Strict`, con expiracion corta y rotacion/renovacion controlada.
- RNF-030: La SPA productiva no debe generar sourcemaps publicos con codigo sensible ni exponer mensajes tecnicos detallados en consola.
- RNF-031: La auditoria de seguridad debe registrar login, logout, callback OAuth, refresh, fallos de autenticacion, acceso denegado, cambios de MFA, creacion de secretos y cambios de rol/licencia sin registrar secretos.
- RNF-032: Los reportes pesados deben ejecutarse de forma asincrona para evitar timeouts HTTP, usando colas administradas, workers idempotentes, almacenamiento privado y notificacion al usuario cuando el archivo este disponible.
- RNF-033: Las descargas de reportes pesados deben usar enlace intermediado por la aplicacion y URL prefirmada generada al momento del clic, con TTL parametrizable y auditoria de cada intento.

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
- RN-014: Un documento fiscal aceptado por el conector DIAN mock o configurado debe afectar inventario y contabilidad una sola vez por empresa, aunque el comando se reintente.
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
- RN-045: El perfil de consumidor final debe ser parametrizable por empresa o global, auditable y modificable por `ROOT` si DIAN o el modo de operacion configurado cambia el contrato tecnico; la SPA solo envia la decision `buyerIdentificationMode`.
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
- RN-063: Cada empresa es responsable de registrarse, habilitarse, certificar su modo de operacion, obtener y custodiar su certificado digital y mantener vigentes sus resoluciones ante DIAN.
- RN-064: El software no debe usar un certificado global del proveedor de la aplicacion para emitir documentos de empresas clientes.
- RN-065: La configuracion DIAN debe pertenecer a una sola empresa y no puede compartirse implicitamente entre tenants.
- RN-066: El certificado digital, PIN tecnico, claves, tokens y credenciales DIAN nunca deben aparecer en logs, auditoria, respuestas API ni payloads guardados.
- RN-067: Una empresa no puede emitir documentos en modo real si su configuracion DIAN activa no esta completa, no esta habilitada, esta vencida o no tiene resolucion vigente compatible.
- RN-068: El modo `MOCK` solo sirve para desarrollo, pruebas internas y E2E; no prueba cumplimiento tecnico final con anexos DIAN ni habilita operacion productiva.
- RN-069: La contrasena del usuario nunca debe llegar a la SPA productiva de la aplicacion; el ingreso de credenciales ocurre en Cognito Hosted UI o proveedor de identidad aprobado.
- RN-070: El navegador no debe recibir `accessToken`, `refreshToken`, `idToken` ni bearer token interno en respuestas API productivas.
- RN-071: `sessionStorage` y `localStorage` no deben guardar credenciales, tokens, passwords, headers de autorizacion ni secretos. Solo pueden guardar preferencias no sensibles de UI.
- RN-072: El BFF debe tratar la cookie de sesion como identificador opaco y resolver datos sensibles desde almacenamiento server-side cifrado.
- RN-073: La proteccion CSRF debe validarse antes de mutaciones `POST`, `PUT`, `PATCH` y `DELETE` cuando la autenticacion use cookies.
- RN-074: ROOT y administradores no pueden operar sin MFA activo en produccion.
- RN-075: La creacion programatica de secretos AWS por empresa debe ser idempotente, auditable y restringida por prefijo de ruta del ambiente/empresa.
- RN-076: La aplicacion no debe registrar en `console.log`, logs tecnicos, auditoria ni errores publicos valores de passwords, tokens, cookies, certificados, PIN, claves tecnicas o payloads completos con datos sensibles.
- RN-077: Los reportes pequenos pueden generarse sincronicamente desde `reporting-service`; los reportes pesados deben registrarse como jobs asincronos y no bloquear la experiencia del usuario.
- RN-078: El enlace enviado por correo para descargar un reporte pesado debe apuntar a la aplicacion usando `APP_PUBLIC_BASE_URL`; no debe exponer directamente URL de S3, bucket, key interna ni credenciales.
- RN-079: La URL prefirmada real de S3 debe generarse solo cuando el usuario hace clic en el enlace intermediado y debe expirar inicialmente a los 5 segundos mediante `REPORT_DOWNLOAD_PRESIGNED_TTL_SECONDS`.
- RN-080: El token del enlace intermediado debe tener TTL independiente, configurable mediante `REPORT_LINK_TOKEN_TTL_HOURS`, y debe estar asociado a empresa, usuario, job de reporte, estado y auditoria.
- RN-081: Si el token esta vencido, fue revocado, el reporte expiro o el job no esta listo, la aplicacion debe mostrar una pantalla clara sin filtrar informacion interna.
- RN-082: El rol empresarial `OWNER` materializado por empresa solo puede contener permisos de alcance `COMPANY`; no puede incluir permisos `GLOBAL_*` ni elevarse al alcance `ROOT`.
- RN-083: Las validaciones fiscales obligatorias para emitir POS/factura no se omiten por rol administrativo; el administrador puede configurar lo faltante, pero la emision debe fallar si falta emisor o numeracion activa.

## Supuestos

- Se usara un modulo de configuracion DIAN por empresa; cada empresa facturadora decide y configura su modo de operacion ante DIAN bajo su responsabilidad.
- El negocio emitira POS electronico.
- El backend se evolucionara hacia microservicios, manteniendo compatibilidad temporal con el proyecto actual.
- La correccion de credenciales hardcodeadas queda incluida como tarea aprobada para fase de implementacion.
- Mientras no existan configuraciones DIAN reales por empresa, certificado y credenciales reales, la integracion DIAN se implementara con un adaptador dummy local sin llamadas externas.
- La plataforma no se comercializara como proveedor tecnologico DIAN; se comercializara como software configurable para conexion DIAN por empresa. Esta decision debe validarse legal/tributariamente antes de operar comercialmente a escala.
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
- No almacenar certificados digitales reales, PIN, claves tecnicas ni credenciales DIAN en archivos versionados o columnas de texto plano.
- No presentar en UI, README, contratos comerciales ni documentacion tecnica que el producto presta servicios de proveedor tecnologico DIAN.
- No exponer en produccion la autenticacion dummy basada en `POST /api/v1/auth/login` con password manejado por la SPA.
- No guardar tokens productivos en `sessionStorage`, `localStorage`, IndexedDB ni variables globales del navegador.
- No publicar sourcemaps productivos sin control de acceso.

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

- RF-105: El sistema debe soportar un usuario `ROOT` global de plataforma que no pertenece a ninguna empresa y no depende de licencia empresarial para iniciar sesion.
- RF-106: El usuario `ROOT` debe poder crear empresas contratantes, configurar o activar licencias y crear/asignar el administrador inicial de cada empresa.
- RF-107: Todos los roles distintos de `ROOT` deben pertenecer a una empresa especifica y estar aislados por `company_id`.
- RF-108: Cada empresa debe poder crear roles personalizados con nombres propios y permisos modulares dentro de su alcance empresarial.
- RF-109: El sistema debe impedir que un actor cree, edite o asigne roles con permisos iguales, superiores o no poseidos por el actor.
- RF-110: Los permisos globales `GLOBAL_*` deben ser exclusivos de `ROOT` y no deben asignarse a roles empresariales.
- RF-111: El frontend debe mostrar panel global para `ROOT` y panel empresarial para usuarios de empresa segun permisos efectivos.
- RF-112: El backend debe validar permisos efectivos en cada accion protegida; el frontend no es fuente de seguridad.
- RF-113: ROOT debe poder administrar catalogos globales/regulatorios de la plataforma, incluyendo crear, actualizar, activar e inactivar registros, siempre dejando auditoria.
- RF-114: La UI debe ofrecer un modulo de Logs/Auditoria visible solo para ROOT, administradores de empresa o roles con permiso `AUDIT_VIEW`.
- RF-115: ROOT debe poder consultar auditoria de la empresa activa seleccionada y administradores de empresa solo auditoria de su empresa.
- RF-116: ROOT debe poder crear, actualizar, activar y suspender licencias empresariales desde la SPA sin usar llamadas manuales.
- RF-117: Una licencia empresarial debe definir vigencia, limites comerciales y modulos habilitados de forma parametrizable.
- RF-118: El sistema debe validar licencia por modulo/accion antes de habilitar menus y antes de ejecutar operaciones criticas.
- RF-119: Si una empresa no tiene licencia configurada, el login empresarial debe mostrar un mensaje claro de licencia no configurada, cerrar la sesion y no mostrar menus.
- RF-120: El licenciamiento comercial no reemplaza RBAC; un usuario solo puede acceder a funcionalidades permitidas por la licencia de la empresa y por sus permisos efectivos.
- RF-121: El sistema debe aplicar los limites comerciales de licencia: `maxUsers` limita la cantidad de usuarios activos con acceso a una empresa y `maxMonthlyDocuments` limita la cantidad de documentos fiscales emitidos por mes en `billing-service`.
- RF-122: Cuando una empresa alcance un limite de licencia, el backend debe bloquear la operacion con mensaje funcional claro y la UI debe mostrarlo como error de regla de negocio.
- RF-123: La administracion de empresa debe diferenciar alcance ROOT y alcance empresarial: ROOT puede crear, actualizar, activar e inactivar empresas; OWNER/ADMIN empresarial solo puede actualizar la empresa activa y no debe ver acciones de creacion de nuevas empresas.
- RF-124: La UI debe mostrar nombres de empresa y etiquetas de permisos/modulos en espanol, sin exponer UUID como dato principal al usuario final; los codigos internos pueden permanecer en ingles en API, backend y base de datos.
- RF-125: Antes de implementar nuevas mejoras, el proyecto debe quedar limpio de artefactos legacy/huerfanos: codigo runtime legacy sin uso, documentacion historica obsoleta, artefactos generados/IDE ignorados y tablas `public.*` vacias deben retirarse o quedar documentados con decision explicita.
- RF-165: Cuando `ROOT` cree el administrador inicial de una empresa, el sistema debe crear de forma idempotente un rol empresarial `OWNER`, asignarlo al administrador y mostrarlo en su panel como `Administrador propietario`.
- RF-166: La confirmacion POS debe distinguir falta de permisos de falta de configuracion fiscal. Si no existe emisor fiscal activo o resolucion activa, el backend debe retornar error funcional claro y la SPA debe guiar al usuario hacia configuracion fiscal.
- RF-167: Todos los permisos y modulos RBAC visibles en frontend deben traducirse mediante recursos `i18next`; ningun permiso vigente debe mostrarse con fallback en ingles como `Sales Cancel`.
- RF-168: El modulo Fiscal debe permitir registrar varios emisores fiscales por empresa, listar su estado y activar/inactivar emisores, garantizando que solo exista un emisor activo por empresa.
- RF-169: El modulo Fiscal debe permitir registrar varias resoluciones de numeracion por empresa, listar su estado y activar/inactivar resoluciones, garantizando una sola resolucion activa por empresa, tipo de documento fiscal y ambiente.
- RF-170: La SPA debe explicar funcionalmente que una resolucion de numeracion DIAN autoriza tipo documental, prefijo, rango, ambiente y vigencia, para evitar errores al confirmar POS.
- RF-171: La configuracion de empresa del usuario empresarial debe hidratarse con los datos de su empresa activa. Para `ROOT`, el formulario de empresa debe permanecer vacio para creacion y solo debe hidratarse cuando se elija explicitamente `Actualizar` desde la tabla de empresas.
- RF-172: El login local debe diferenciar credenciales invalidas de indisponibilidad del BFF/servicio de autenticacion, mostrando un mensaje funcional claro sin exponer detalles tecnicos.
- RF-173: ROOT debe administrar empresas desde una tabla con acciones por fila para actualizar, activar/inactivar, crear administrador inicial y configurar marca empresarial.
- RF-174: Los modales de administrador inicial y marca empresarial deben mostrar la empresa objetivo en un campo bloqueado, manteniendo editables los demas campos del formulario.
- RF-175: Al seleccionar actualizar empresa desde la tabla, el formulario debe llenarse con la empresa elegida y la accion principal debe cambiar a `Actualizar empresa`.
- RF-176: La SPA debe separar la pantalla operativa de venta POS de la consulta historica. `Ventas` debe enfocarse en registrar/confirmar ventas y `Registro de Ventas` debe mostrar ventas ya registradas como historico inmutable.
- RF-177: El historico `Registro de Ventas` solo debe permitir visualizar ventas y abrir detalle fiscal/documental, incluyendo CUFE/CUDE cuando exista. No debe permitir modificar, eliminar, inactivar, anular ni reemitir ventas.
- RF-178: Los modales de acciones empresariales deben tener una composicion visual profesional, sin desbordes, paneles anidados incoherentes ni campos cortados, conservando bloqueo solo del campo empresa objetivo cuando aplique.
- RF-179: El canal de venta POS no debe definir por si solo el tipo de documento fiscal. La empresa debe configurar una politica fiscal que indique el documento por defecto para ventas POS.
- RF-180: El documento fiscal por defecto recomendado para ventas POS debe ser `ELECTRONIC_INVOICE` (factura electronica de venta), permitiendo `ELECTRONIC_POS` como opcion avanzada parametrizable por empresa.
- RF-181: El sistema debe conservar una resolucion activa por empresa, tipo documental y ambiente, permitiendo que una misma empresa tenga resoluciones activas simultaneas para factura electronica, documento equivalente POS, nota credito, nota debito y nota de ajuste POS.
- RF-182: Un vendedor no debe poder cambiar el tipo documental de una venta sin autorizacion operacional de un usuario autorizado de la misma empresa.
- RF-183: El cambio excepcional del tipo documental de una venta debe aplicar solo a esa venta, sin cerrar la sesion del vendedor y sin modificar la politica fiscal por defecto de la empresa.
- RF-184: El sistema debe soportar un PIN operacional de autorizacion de exactamente 6 digitos numericos para administradores/supervisores autorizados.
- RF-185: El PIN operacional no debe almacenarse en texto plano, no reemplaza la contrasena de login y solo sirve para autorizar operaciones sensibles dentro de una sesion ya autenticada.
- RF-186: El PIN operacional debe bloquearse al tercer intento fallido consecutivo. Solo un administrador autorizado puede desbloquearlo y despues del desbloqueo el titular debe cambiarlo antes de volver a usarlo.
- RF-187: Toda creacion, cambio, fallo, bloqueo, desbloqueo y uso exitoso del PIN operacional debe quedar auditado con usuario vendedor, usuario autorizador, empresa, recurso, resultado y correlation ID, sin exponer el PIN.
- RF-188: El sistema debe implementar permisos separados para override de tipo documental, configuracion fiscal, notas credito, notas debito, notas de ajuste POS, reenvios y anulaciones/cancelaciones permitidas por norma.
- RF-189: Los modulos de Nota credito, Nota debito y Nota de ajuste POS deben ser independientes del modulo de Ventas y tener pantallas, contratos, permisos, resoluciones y auditoria propios.
- RF-190: Cuando una venta POS emita factura electronica de venta, la representacion imprimible debe identificarse como representacion grafica/tirilla de factura electronica de venta, no como documento equivalente POS.
- RF-191: Si una venta requiere documento equivalente electronico POS, el backend debe validar que exista resolucion `ELECTRONIC_POS` activa y que el cambio haya sido permitido por politica de empresa o por override autorizado.
- RF-193: El catalogo de permisos persistido y el enum backend `PermissionCode` deben permanecer sincronizados; cualquier permiso insertado por Flyway debe existir en codigo y tener prueba que impida romper `/api/v1/platform/permissions`.
- RF-194: `GET /api/v1/platform/permissions` debe ser un contrato exclusivo de `ROOT`; usuarios empresariales consultan permisos disponibles mediante `/api/v1/companies/{companyId}/permissions/catalog`.
- RF-195: El BFF debe reconocer `ROOT` solo mediante un contrato root-only de `identity-service`, nunca por un endpoint que tambien responda 200 a usuarios empresariales.
- RF-196: El tablero ROOT de uso de licencia debe calcular documentos mensuales sin fallar cuando la consulta de documentos electronicos no recibe filtros opcionales distintos a fecha/empresa.
- RF-197: Al iniciar sesion como `ROOT`, la SPA no debe precargar empresa activa ni formularios derivados; la seleccion de empresa debe ser explicita para evitar cambios accidentales.

## Requisitos fase productizacion operativa

- RF-089: El sistema debe contar con una prueba E2E desde cero que cree empresa, licencia, administrador, catalogos requeridos, tercero, inventario, venta POS, factura electronica mock, efecto de inventario, asiento contable y auditoria.
- RF-090: El sistema debe soportar compras/entradas de inventario con proveedor, costo, medio de pago, cuentas por pagar cuando aplique y asiento contable parametrizable.
- RF-091: El sistema debe soportar servicios facturables que consumen insumos controlados mediante confirmacion manual/asistida posterior o asociada a la venta, sin recetas automaticas obligatorias.
- RF-092: La SPA debe ofrecer listados profesionales con busqueda, paginacion, estado y accion contextual para ventas, documentos fiscales, terceros, productos, compras, servicios, movimientos, usuarios, roles, licencias y logs.
- RF-093: BFF y microservicios deben validar RBAC y licencia en endpoints criticos; el frontend nunca sera el control de seguridad principal.
- RF-094: ROOT debe ver un tablero de uso de licencia por empresa con usuarios activos, documentos emitidos en el mes, modulos habilitados, vigencia, estado y proximidad de vencimiento.
- RF-095: Toda accion mutable debe generar auditoria segura verificable, incluyendo intentos fallidos por permiso, licencia, regla de negocio o validacion.
- RF-096: La sesion debe soportar expiracion por inactividad, restauracion controlada y renovacion segura segun contrato backend; refrescar la pagina no debe cerrar sesion si sigue vigente.
- RF-097: Las reglas contables deben ser parametrizables por empresa y evento de negocio usando cuentas PUC validas.
- RF-098: Ventas, compras, gastos, pagos de nomina/pagos diarios y consumos relevantes deben generar comprobantes/asientos balanceados o fallar con error funcional si falta parametrizacion contable.
- RF-099: La plataforma debe exponer reportes minimos de estado de resultados, balance basico, libro diario, cuentas por cobrar, cuentas por pagar, inventario valorizado y uso de licencia.
- RF-100: El BFF debe tener pruebas de contrato contra los microservicios para rutas criticas de login, empresa, licencia, terceros, inventario, ventas, auditoria, usuarios y roles.
- RF-101: Debe existir prueba E2E de aislamiento multiempresa que demuestre que una empresa no ve ni modifica datos de otra.
- RF-102: La infraestructura AWS objetivo debe quedar definida con Terraform para SPA en S3/CloudFront, BFF/API en ECS Fargate, microservicios privados, RDS PostgreSQL, Secrets Manager, CloudWatch, SQS/EventBridge y Lambdas event-driven.
- RF-103: Los eventos asincronos productivos deben usar servicios administrados AWS, con Outbox/Inbox, idempotencia, reintentos y DLQ; no se usaran brokers self-hosted en produccion.
- RF-104: La documentacion SDD debe mantenerse consistente y trazable; cada decision vigente debe estar reflejada en requisitos, diseno, arquitectura, infraestructura, contratos API, modelo/diccionario de datos, criterios de aceptacion, tareas y README cuando aplique.

## Requisitos fase marca, branding, reportes avanzados e impresion POS

- RF-131: La aplicacion debe presentarse visualmente como `NexoFiscal` en el frontend, login, titulo del navegador, sidebar y textos publicos de producto.
- RF-132: Cada empresa debe poder configurar su marca visual dentro de su alcance: logo principal, favicon empresarial y logo de encabezado/login, con permisos de administrador empresarial o ROOT.
- RF-133: El sistema debe validar y almacenar logos empresariales de forma segura, limitando tamano, tipo MIME, extension, dimensiones y contenido permitido.
- RF-134: La SPA debe aplicar dinamicamente el branding de la empresa activa: favicon, logo superior derecho, logo de login y fallback visual de NexoFiscal cuando no exista logo empresarial.
- RF-135: El sistema debe conservar auditoria de carga, actualizacion, eliminacion o activacion de logos/branding empresarial.
- RF-136: El modulo de reportes debe evolucionar a un modulo avanzado con selector de reporte, filtros dinamicos segun reporte, rango de fechas obligatorio u opcional segun caso, tipo de grafico y exportacion.
- RF-137: El sistema debe evaluar e implementar `reporting-service` como microservicio fisico cuando los reportes requieran agregacion transversal, historicos, exportaciones o proyecciones que no deban vivir en BFF ni en servicios transaccionales.
- RF-138: Los reportes de ventas deben permitir agrupaciones utiles para toma de decisiones: por vendedor, producto/servicio, cliente, metodo de pago, estado fiscal, periodo y empresa.
- RF-139: El reporte de ventas por vendedor debe filtrar usuarios con rol/permiso de venta, no cualquier usuario empresarial.
- RF-140: El modulo de reportes debe incluir reportes de compras realizadas, inventario/kardex, rentabilidad basica, cuentas por cobrar, cuentas por pagar, reportes contables, nomina/pagos diarios y uso de licencia.
- RF-141: El usuario debe poder seleccionar el tipo de visualizacion del reporte: tabla, barras, lineas historicas, torta/donut o tarjetas KPI cuando aplique.
- RF-142: Los reportes historicos y tabulares deben poder exportarse al menos a CSV/Excel; PDF gerencial queda como salida adicional parametrizable.
- RF-143: El sistema debe generar y conservar artefactos de comprobantes/documentos POS: representacion imprimible, XML/JSON tecnico cuando aplique, QR y metadata de hash/almacenamiento.
- RF-144: El sistema debe permitir imprimir o reimprimir comprobantes POS en impresoras termicas mediante una estrategia gradual: primero impresion web 58/80 mm y luego conector ESC/POS/WebUSB/WebSerial/agente local si se aprueba por hardware real.
- RF-145: El historico de ventas y documentos debe permitir consultar ventas emitidas, detalle, vendedor, cliente/consumidor final, items, totales, estado DIAN/mock, artefactos, descargas y reimpresiones, siempre aislado por empresa y permisos.

## Requisitos fase reportes asincronos avanzados

- RF-146: El sistema debe permitir solicitar reportes pesados en segundo plano desde el modulo de reportes sin bloquear el request HTTP.
- RF-147: El sistema debe mantener un historico de jobs de reportes por empresa, usuario solicitante, reporte, filtros, formato, estado, fechas y error sanitizado cuando aplique.
- RF-148: El sistema debe soportar estados de job `PENDING`, `PROCESSING`, `READY`, `FAILED`, `EXPIRED` y `REVOKED`.
- RF-149: El sistema debe generar archivos de reportes pesados en almacenamiento privado S3/KMS o equivalente cloud, sin exponer bucket/key interna al navegador ni al correo.
- RF-150: El sistema debe enviar una notificacion por correo cuando el reporte pesado este listo, usando un link intermediado por la aplicacion construido con `APP_PUBLIC_BASE_URL`.
- RF-151: El sistema debe generar una URL prefirmada de S3 solo al momento del clic sobre el link intermediado y con expiracion inicial de 5 segundos.
- RF-152: El sistema debe permitir parametrizar `APP_PUBLIC_BASE_URL`, `REPORT_DOWNLOAD_PRESIGNED_TTL_SECONDS`, `REPORT_LINK_TOKEN_TTL_HOURS` y politica de retencion de archivos.
- RF-153: El modulo de reportes debe mostrar jobs solicitados, estados, errores funcionales y descargas disponibles segun permisos, sin obligar al usuario a volver al modulo para descargar desde correo.
- RF-154: ROOT puede consultar jobs de cualquier empresa; administradores empresariales solo jobs de su empresa; usuarios normales solo sus propios jobs salvo permiso delegado.
- RF-155: Cada solicitud, procesamiento, fallo, expiracion, revocacion, envio de correo y descarga de reporte pesado debe quedar auditado sin datos sensibles.

## Requisitos cierre DIAN real parametrizable por empresa

- RF-156: El cierre de DIAN real debe permanecer dentro de la Fase 20 antes de ejecutar reportes asincronos avanzados, sin crear una fase nueva para el envio real.
- RF-157: `dian-provider-service` debe generar XML UBL 2.1 para factura electronica de venta, documento equivalente electronico POS y notas fiscales usando la version/anexo DIAN vigente parametrizado.
- RF-158: El sistema debe calcular CUFE/CUDE y contenido QR de forma deterministica segun tipo de documento, ambiente, numeracion, emisor, adquirente, totales, impuestos y claves tecnicas configuradas por empresa.
- RF-159: El sistema debe firmar los XML con certificado digital de la empresa facturadora, referenciado desde gestor de secretos, sin exponer certificado, PIN ni claves en base de datos, logs, auditoria o respuestas.
- RF-160: El sistema debe validar XSD, Schematron y listas de codigos antes de transmitir a DIAN y debe bloquear el envio real cuando exista una falla tecnica.
- RF-161: El sistema debe soportar transporte real DIAN para habilitacion y produccion por empresa, con URLs, credenciales, ambiente y estado de pruebas configurables por `company_id`.
- RF-162: El sistema debe registrar respuestas DIAN, `ApplicationResponse`, tracking, rechazos, errores, reintentos e idempotencia sin duplicar documentos ni repetir efectos de inventario o contabilidad.
- RF-163: El sistema debe almacenar artefactos fiscales reales de forma segura: XML firmado, AttachedDocument/ZIP cuando aplique, QR, representacion grafica, hash, metadata y respuesta DIAN.
- RF-164: El modo `MOCK` debe permanecer disponible para E2E local, pero separado del modo real; un envio real nunca debe degradar silenciosamente a mock.
- RF-165: La documentacion DIAN debe citar fuentes oficiales y separar requisito normativo, decision tecnica, supuesto pendiente y validacion requerida.
- RF-166: Antes de habilitar produccion DIAN real deben existir pruebas unitarias, de integracion y E2E con fixtures sanitizados del anexo tecnico vigente.
- RF-192: La pantalla operativa de ventas debe permitir cerrar una venta en una sola accion de usuario, creando la venta, emitiendo el documento fiscal, aplicando inventario/contabilidad y abriendo comprobante imprimible sin exigir un paso visible previo de "crear venta".
