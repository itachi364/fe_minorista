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

## Supuestos

- Se usara proveedor tecnologico DIAN para emision y validacion de documentos.
- El negocio emitira POS electronico.
- El backend se evolucionara hacia microservicios, manteniendo compatibilidad temporal con el proyecto actual.
- La correccion de credenciales hardcodeadas queda incluida como tarea aprobada para fase de implementacion.
- Mientras no exista proveedor tecnologico, contrato tecnico, certificado y credenciales reales, la integracion DIAN se implementara con un adaptador dummy local sin llamadas externas.
- La migracion fisica a microservicios se hara por bounded context para mantener un balance entre independencia de despliegue y complejidad operacional.
- En local se usara Docker Compose con contenedores por microservicio; la separacion de bases de datos podra iniciar con esquemas o bases separadas en PostgreSQL y evolucionar a instancias independientes.

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

## Criterios de aceptacion

Los criterios detallados se encuentran en `specs/acceptance-criteria.md`.

## Trazabilidad

Cada tarea de `specs/tasks.md` debe enlazar uno o mas requisitos funcionales, no funcionales y criterios de aceptacion.
