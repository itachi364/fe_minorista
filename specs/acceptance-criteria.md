# Acceptance Criteria

## Facturacion electronica

- AC-001: Dado un emisor configurado con resolucion vigente, cuando se cree una factura valida, entonces el sistema debe asignar prefijo y numero autorizado sin duplicados.
- AC-002: Dada una factura con productos, cantidades, impuestos y descuentos, cuando se calcule el documento, entonces subtotal, impuestos, descuentos, cargos y total deben cuadrar exactamente.
- AC-003: Dada una factura lista para emitir, cuando se envie al proveedor tecnologico, entonces el sistema debe registrar solicitud, respuesta, identificadores, estado y errores si existen.
- AC-004: Dada una respuesta exitosa del proveedor tecnologico, entonces el sistema debe almacenar CUFE, QR, XML, representacion grafica y estado validado o equivalente.
- AC-005: Dada una factura validada, cuando el usuario intente editar valores fiscales, entonces el sistema debe rechazar la modificacion y exigir nota credito/debito.
- AC-006: Dada una factura rechazada por proveedor tecnologico, entonces el sistema debe conservar el rechazo, permitir correccion segun estado y registrar auditoria.

## POS electronico

- AC-007: Dado un punto de venta configurado, cuando se emita una venta POS, entonces el sistema debe generar documento equivalente electronico POS con numeracion, CUDE, QR y totales.
- AC-008: Dado un POS electronico emitido, cuando deba corregirse o anularse, entonces el sistema debe generar nota de ajuste y no reutilizar el numero del documento original.
- AC-009: Dado un adquirente que requiere soporte fiscal, cuando se emita POS electronico, entonces el sistema debe permitir registrar nombre o razon social e identificacion del adquirente.

## Inventario

- AC-010: Dada una venta con productos inventariables, cuando el documento se confirme, entonces el stock debe disminuir y quedar registrado un movimiento de salida.
- AC-011: Dada una compra registrada, cuando se confirme, entonces el stock debe aumentar y quedar registrado un movimiento de entrada.
- AC-012: Dado un producto sin stock suficiente, cuando se intente vender una cantidad mayor al disponible, entonces el sistema debe rechazar la operacion salvo configuracion aprobada.
- AC-013: Dado cualquier movimiento de inventario, entonces debe existir trazabilidad de producto, cantidad, documento origen, usuario, fecha y tipo de movimiento.

## Contabilidad

- AC-014: Dado un documento fiscal validado o confirmado, cuando se contabilice, entonces el sistema debe generar asiento balanceado con debitos y creditos equivalentes.
- AC-015: Dado un asiento contable, cuando se consulte el libro diario, entonces debe aparecer con fecha, descripcion, cuentas, tercero, debitos, creditos y documento origen.
- AC-016: Dado un periodo contable, cuando se consulte el libro mayor, entonces el sistema debe agrupar movimientos por cuenta.

## Seguridad, configuracion y observabilidad

- AC-017: Dado el archivo `application.properties`, entonces no debe contener usuario, password, tokens, certificados ni secretos reales en texto plano.
- AC-018: Dada una operacion fiscal sensible, entonces debe registrarse auditoria con usuario, fecha, accion, recurso y resultado.
- AC-019: Dado un error de integracion externa, entonces el sistema debe responder con error estructurado sin exponer secretos ni detalles internos sensibles.
- AC-020: Dada una peticion HTTP, entonces los logs deben incluir correlation ID o request ID.

## Pruebas

- AC-021: Cada caso de uso nuevo debe tener pruebas unitarias.
- AC-022: Cada controlador nuevo debe tener prueba HTTP o de capa web.
- AC-023: Cada adaptador externo al proveedor tecnologico debe probarse con mocks o test doubles.
- AC-024: Los criterios de aceptacion criticos deben quedar cubiertos por pruebas automatizadas o checklist operacional documentado.

## Refactorizacion arquitectonica

- AC-025: Dado un modulo CRUD existente, cuando se refactorice a Clean Architecture, entonces sus endpoints publicos, codigos de respuesta y DTOs compatibles deben mantenerse salvo cambio aprobado en specs.
- AC-026: Dado un modulo refactorizado, entonces su dominio y casos de uso no deben depender de Spring MVC, Spring Data JPA, anotaciones JPA, controladores HTTP ni detalles de infraestructura.
- AC-027: Dado un modulo refactorizado, entonces los controladores deben depender de puertos de entrada o casos de uso, y la persistencia debe quedar detras de puertos de salida y adaptadores.
- AC-028: Dado un modulo refactorizado, entonces debe tener pruebas unitarias de casos de uso y pruebas de controlador o integracion que demuestren compatibilidad del comportamiento existente.
- AC-029: Dada la migracion incremental, entonces solo debe refactorizarse un modulo o bounded context por iteracion para reducir riesgo de regresion.

## Microservicios fisicos y flujo end-to-end

- AC-030: Dado un bounded context aprobado, cuando se extraiga a microservicio fisico, entonces debe tener artefacto Maven independiente, Dockerfile propio, configuracion propia, healthcheck y pruebas automatizadas.
- AC-031: Dado el despliegue local con Docker Compose, cuando se levante la plataforma, entonces cada microservicio debe ejecutarse en su propio contenedor y comunicarse con los demas mediante contratos REST versionados o eventos aprobados.
- AC-032: Dado un dato de negocio multiempresa, cuando cualquier microservicio lo persista o consulte, entonces debe aplicar aislamiento por `company_id` y no retornar datos de otra empresa.
- AC-033: Dado un flujo POS/factura validado por el proveedor DIAN mock, cuando el documento quede aceptado, entonces el sistema debe descontar inventario y generar asiento contable automaticamente sin duplicar efectos ante reintentos idempotentes.
- AC-034: Dado un producto comprado o ajustado inicialmente, cuando se consulte el stock o kardex, entonces debe reflejar costo, cantidad, documento origen y trazabilidad del movimiento.
- AC-035: Dado el flujo end-to-end local desde cero, cuando se creen empresa, configuraciones, inventario, venta y documento electronico, entonces los datos deben quedar persistidos y verificables por API y por consultas PostgreSQL.
- AC-036: Dado un inventario de codigo y tablas legacy, cuando se ejecute la depuracion, entonces solo deben eliminarse elementos demostrados como reemplazados, no usados y cubiertos por pruebas o checklist de migracion.

## Backend core pendiente antes de depuracion

- AC-037: Dado un tercero con tipo de documento NIT, cuando se registre o actualice su numero base, entonces el sistema debe calcular automaticamente el digito de verificacion y retornarlo en la respuesta.
- AC-038: Dado un tercero con tipo de documento distinto a NIT, cuando se registre o actualice, entonces el digito de verificacion debe quedar nulo o vacio.
- AC-039: Dado un mismo numero de identificacion por empresa, cuando el tercero sea cliente y proveedor, entonces el sistema debe permitir ambos roles sin duplicar la identidad fiscal.
- AC-040: Dado un bien fisico con stock controlado, cuando se venda y el documento quede efectivo, entonces el sistema debe descontar su propio stock.
- AC-041: Dado un servicio o intangible facturable, cuando se venda y facture, entonces el sistema debe generar la linea fiscal sin descontar automaticamente insumos asociados.
- AC-042: Dado un insumo usado en un servicio, cuando el usuario registre consumo, desperdicio o ajuste manual, entonces el sistema debe actualizar stock y kardex con motivo, cantidad, origen, usuario y empresa.
- AC-043: Dada una compra confirmada de productos o insumos, entonces debe incrementar stock, registrar kardex y generar contabilizacion segun reglas PUC de la empresa.
- AC-044: Dado un gasto sin inventario, cuando se registre y confirme, entonces no debe afectar stock y debe generar cuenta por pagar o pago contable segun la forma de pago.
- AC-045: Dada una cuenta por pagar, cuando se registre un pago parcial o total, entonces el saldo debe disminuir y quedar trazabilidad contable.
- AC-046: Dada una venta de bienes o servicios, cuando se emita POS electronico o factura electronica, entonces el documento debe conservar snapshot fiscal de tercero, lineas, impuestos, totales, prefijo y consecutivo.
- AC-047: Dado un reporte operativo o contable, cuando se consulte por empresa y periodo, entonces solo debe incluir datos de esa empresa y debe provenir del modelo Clean Architecture activo.
- AC-048: Dado un usuario con roles por empresa, cuando intente ejecutar una accion protegida, entonces el sistema debe permitirla o rechazarla segun permisos configurados.
- AC-049: Dada una empresa con licencia suspendida o vencida, cuando intente emitir documentos o crear nuevas transacciones, entonces el sistema debe bloquear la operacion con error estructurado.
- AC-050: Dado el flujo completo despues de migrar legacy pendiente, cuando se ejecute la prueba E2E desde cero, entonces debe cubrir empresa, licencia, usuario, configuracion fiscal, terceros, items, compras/gastos, inventario, venta, documento electronico, DIAN mock, contabilidad, reportes y auditoria.
- AC-051: Dada la tarea de limpieza legacy, entonces debe existir matriz de reemplazo con codigo, endpoints, tablas, datos a migrar, pruebas y decision de eliminar o conservar por cada componente.
- AC-052: Dada una venta o documento fiscal configurado a credito, cuando quede efectivo, entonces debe crear o actualizar una cuenta por cobrar por cliente con saldo, vencimiento, estado y trazabilidad de documento origen.
- AC-053: Dada una cuenta por cobrar abierta, cuando se registre un pago parcial o total, entonces el saldo debe disminuir, el estado debe actualizarse y debe quedar trazabilidad contable y operativa sin permitir sobrepago.


- AC-054: La documentacion de arquitectura cloud identifica CloudFront/S3 para frontend, API Gateway/BFF para entrada publica, ECS Fargate para microservicios de larga vida, Lambda para procesos event-driven y RDS/Aurora PostgreSQL para persistencia.
- AC-055: Ningun microservicio de negocio queda definido como exposicion directa al navegador; el contrato publico del frontend pasa por BFF/API Gateway.
- AC-056: Los procesos event-driven quedan clasificados con contratos de evento, idempotencia, Outbox/Inbox y DLQ/reintentos antes de implementar infraestructura.
- AC-057: La IaC productiva propuesta no incluye contenedores, artefactos, rutas ni servicios legacy eliminados.
- AC-058: La arquitectura productiva no incluye brokers self-hosted; la mensajeria objetivo es AWS administrada con EventBridge/SQS + Lambda.
