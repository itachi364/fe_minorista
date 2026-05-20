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

- Frontend web o movil.
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
- RF-023: El sistema debe registrar compras, gastos, cuentas por pagar y pagos basicos asociados a proveedores, inventario y contabilidad.
- RF-024: El sistema debe generar factura electronica o POS electronico tanto para bienes fisicos como para servicios, conservando snapshot fiscal de lineas, impuestos y tercero adquirente.
- RF-025: El sistema debe exponer reportes operativos minimos de ventas, inventario/kardex, compras/gastos, documentos electronicos, cuentas por cobrar/pagar y libros contables.
- RF-026: El sistema debe implementar usuarios, roles y permisos por empresa antes de operar en escenarios reales multiempresa.
- RF-027: El sistema debe implementar licenciamiento por empresa para habilitar, suspender o limitar el uso de la plataforma segun condiciones comerciales.

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
- RNF-014: La mensajeria asincrona objetivo usara NATS JetStream con patron Outbox/Inbox cuando el flujo funcional core este completo, probado y depurado.
- RNF-015: Los casos de uso de identificacion fiscal deben ser deterministas y probados con datos de NIT validos e invalidos.
- RNF-016: Los reportes iniciales deben ejecutarse por empresa, rango de fechas y filtros basicos sin exponer informacion de otros tenants.
- RNF-017: Las reglas de licenciamiento deben evaluarse antes de ejecutar comandos de negocio que creen documentos, usuarios o transacciones segun el plan contratado.
- RNF-018: La limpieza legacy debe ejecutarse solo despues de una matriz de reemplazo, prueba E2E aprobada y verificacion de referencias con compilacion completa.

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
- RN-024: Las compras de productos o insumos incrementan inventario al confirmarse; los gastos sin inventario no deben crear stock.
- RN-025: Las compras, gastos y cuentas por pagar deben contabilizarse con reglas parametrizables por empresa y cuentas PUC aprobadas.
- RN-026: Ninguna venta, compra, gasto, movimiento o reporte puede operar sin `company_id`.
- RN-027: Una empresa con licencia suspendida no debe poder emitir documentos fiscales ni crear nuevas transacciones de negocio, salvo consultas y acciones administrativas permitidas.
- RN-028: Los reportes deben basarse en datos persistidos por los microservicios activos, no en tablas legacy que esten pendientes de eliminacion.
- RN-029: La depuracion de tablas legacy debe ocurrir despues de migrar clientes, proveedores, productos, servicios, compras, gastos, facturas, auditoria y datos contables necesarios al modelo Clean Architecture.
- RN-030: La integracion NATS JetStream se implementara despues de cerrar la logica backend core, migrar legacy pendiente y aprobar la depuracion.

## Supuestos

- Se usara proveedor tecnologico DIAN para emision y validacion de documentos.
- El negocio emitira POS electronico.
- El backend se evolucionara hacia microservicios, manteniendo compatibilidad temporal con el proyecto actual.
- La correccion de credenciales hardcodeadas queda incluida como tarea aprobada para fase de implementacion.
- Mientras no exista proveedor tecnologico, contrato tecnico, certificado y credenciales reales, la integracion DIAN se implementara con un adaptador dummy local sin llamadas externas.
- La migracion fisica a microservicios se hara por bounded context para mantener un balance entre independencia de despliegue y complejidad operacional.
- En local se usara Docker Compose con contenedores por microservicio; la separacion de bases de datos podra iniciar con esquemas o bases separadas en PostgreSQL y evolucionar a instancias independientes.
- NATS JetStream se adopta como opcion objetivo para mensajeria asincrona por ser open source, liviano y viable para un producto vendible mediante licencias de uso.

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
- No introducir broker, login ni infraestructura adicional antes de completar y validar el flujo core de negocio por API, persistencia PostgreSQL y prueba end-to-end desde cero.

## Criterios de aceptacion

Los criterios detallados se encuentran en `specs/acceptance-criteria.md`.

## Trazabilidad

Cada tarea de `specs/tasks.md` debe enlazar uno o mas requisitos funcionales, no funcionales y criterios de aceptacion.
