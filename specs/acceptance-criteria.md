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

## RBAC modular y experiencia frontend profesional

- AC-059: Dado un usuario `ROOT`, cuando inicie sesion, entonces el sistema debe permitir acceso global sin requerir `company_id`, membresia empresarial ni licencia empresarial activa.
- AC-060: Dado un usuario `ROOT`, cuando cree una empresa contratante, entonces debe poder crear o asignar el administrador inicial de esa empresa sin exponer informacion de otras empresas.
- AC-061: Dado un rol distinto de `ROOT`, cuando se cree o actualice, entonces debe pertenecer obligatoriamente a una sola empresa mediante `company_id` y no debe ser visible ni asignable desde otra empresa.
- AC-062: Dado un administrador empresarial, cuando cree o asigne un rol, entonces el conjunto de permisos del rol debe ser subconjunto estricto de sus permisos efectivos y nunca igual ni superior.
- AC-063: Dado cualquier rol empresarial, cuando se configure permisos, entonces el sistema debe rechazar permisos globales `GLOBAL_*` y registrar auditoria segura del intento.
- AC-064: Dado un usuario con permisos efectivos, cuando acceda a un modulo o ejecute una accion, entonces backend y frontend deben permitirla o rechazarla segun permisos persistidos, no segun nombres de roles hardcodeados.
- AC-065: Dada la SPA, cuando se use en escritorio o movil, entonces login, shell, navegacion, formularios, modales y paneles de respuesta deben mantener una presentacion profesional, consistente, responsive y sin solapamientos visuales.
- AC-066: Dado el entorno local Docker, cuando `IDENTITY_ROOT_USER_SEED_ENABLED=true`, entonces `identity-service` debe crear o asegurar un usuario `ROOT` dummy activo, retornar `globalRoles` en login y permitir entrada al panel global sin empresa ni licencia.- AC-067: Dado un usuario `ROOT` autenticado, cuando ingrese a la SPA, entonces debe ver todos los modulos disponibles y operar configuraciones usando una empresa activa creada o seleccionada.
- AC-068: Dado un usuario `ROOT` autenticado y una empresa contratante creada, cuando registre el administrador inicial con email, nombre y contraseña, entonces el sistema debe crear el usuario y asignarle rol empresarial `OWNER` para esa empresa.
- AC-075: Dado el formulario de terceros, cuando el usuario seleccione cliente/proveedor, entonces la UI debe mostrar `Tipo de tercero` en espanol y el request debe enviar `roles` con valores tecnicos `CUSTOMER`, `SUPPLIER` o ambos.
- AC-076: Dado un campo de municipio en terceros o emisor fiscal, cuando el usuario lo edite, entonces debe seleccionar departamento y municipio por nombre en orden alfabetico, mientras el backend recibe y persiste `municipalityCode` DANE/DIVIPOLA.
- AC-077: Dado un usuario `ROOT`, cuando cree o seleccione una empresa, entonces la SPA debe mostrar las empresas disponibles en una lista desplegable y usar el `companyId` seleccionado para operar configuraciones empresariales.
- AC-078: Dado un usuario `ROOT` con empresa activa, cuando cree administrador inicial, entonces debe abrir un modal, capturar nombre/email/password y asignar automaticamente `OWNER` sin permisos globales.
- AC-079: Dado un administrador empresarial o ROOT, cuando asigne roles, entonces debe abrir un modal, buscar o seleccionar usuario por correo, seleccionar un rol empresarial por nombre y enviar al backend `userId` y `roleIds`.
- AC-080: Dada cualquier etiqueta visible de la SPA, entonces debe presentarse en espanol profesional, aunque los contratos y base de datos conserven valores tecnicos en ingles documentados.

- AC-081: `App.jsx` no contiene catalogos estaticos, formularios de feature ni componentes visuales reutilizables; estos viven en `src/data`, `src/components` y `src/features`.
- AC-082: La modularizacion no cambia payloads enviados al backend ni endpoints consumidos por la SPA.
- AC-083: Las pruebas frontend y build productivo pasan despues del refactor.
- AC-084: El selector de municipio usa el catalogo completo DIVIPOLA agrupado por departamento.
- AC-085: Los municipios se muestran por nombre y se envia al backend el codigo DIVIPOLA/DANE de 5 digitos.
- AC-086: La fuente y fecha/corte del dataset DIVIPOLA quedan documentadas en `src/data/divipola.js` o specs.

- AC-087: La SPA debe mostrar responsabilidades fiscales como lista seleccionable con codigo DIAN y significado, sin requerir que el usuario memorice codigos.
- AC-088: La responsabilidad `R-99-PN` debe ser excluyente frente a cualquier otra responsabilidad fiscal en frontend y backend.
- AC-089: La SPA debe mostrar regimen tributario como lista desplegable controlada y enviar el codigo tecnico al backend.
- AC-090: `thirdparty-service` debe persistir y devolver `taxResponsibilities` y `taxRegime` para clientes/proveedores.
- AC-091: Las ventas POS deben recibir y persistir `paymentMethodCode` con valores controlados: `CASH`, `DEBIT_CARD`, `CREDIT_CARD`, `BREB_KEY`, `BANK_TRANSFER`, `VIRTUAL_WALLET`.
- AC-092: `virtualWalletCode` debe ser obligatorio solo cuando `paymentMethodCode = VIRTUAL_WALLET`; para otros medios debe estar ausente o nulo.
- AC-093: La SPA debe mostrar responsabilidades fiscales en doble lista `Disponibles`/`Seleccionadas`, permitiendo mover codigos sin escritura manual.
- AC-094: La responsabilidad `R-99-PN` debe mantenerse excluyente en la doble lista: al seleccionarla limpia las demas y al seleccionar otra responsabilidad reemplaza `R-99-PN`.
- AC-095: Dada una sesion autenticada, cuando el usuario refresque la pagina antes de 5 minutos de inactividad, entonces la SPA debe restaurar la sesion desde `sessionStorage` sin volver a login.
- AC-096: Dada una sesion autenticada, cuando pasen 5 minutos sin actividad de usuario, entonces la SPA debe cerrar la sesion, limpiar el almacenamiento local de sesion y mostrar solo login con modal informativo.
- AC-097: Dado el formulario de login, entonces email y contrasena deben iniciar vacios, con placeholders, sin credenciales dummy precargadas.
- AC-098: Dada una venta POS creada, entonces el identificador tecnico de venta debe mostrarse como estado no editable y solo debe habilitar la confirmacion POS; la fecha de venta la asigna el backend.
- AC-099: Dado el formulario de venta POS, cuando el usuario escriba al menos dos caracteres del numero de documento del cliente, entonces la SPA debe consultar clientes activos de la empresa y mostrar coincidencias por prefijo de documento.
- AC-100: Dado un cliente seleccionado desde el buscador POS, cuando se cree la venta, entonces el request debe enviar `customerId` y no un numero de documento libre.
- AC-101: Dado un tercero con `identificationTypeCode=31`, cuando se registre NIT, entonces el numero debe capturarse sin DV, solo con digitos, y el backend debe calcular el DV separado segun algoritmo DIAN.
- AC-102: Dado un tipo de documento distinto a NIT, cuando se registre un tercero, entonces el DV no debe enviarse como valor manual editable y debe quedar nulo o vacio.
- AC-103: Dado el frontend de empresa o tercero, cuando el tipo de documento sea NIT, entonces el DV debe mostrarse como campo informativo de solo lectura calculado desde el numero; cuando no sea NIT debe mostrarse vacio.
- AC-104: Dado un tercero `CUSTOMER` con `personType=NATURAL`, cuando se registre desde la SPA, entonces la responsabilidad fiscal debe fijarse automaticamente en `R-99-PN`, el regimen en `NO_RESPONSABLE_IVA`, el tipo de documento no puede ser NIT y el DV no debe existir.
- AC-105: Dado un cliente natural simple, cuando se diligencie el formulario, entonces razon social, nombre comercial, responsabilidades fiscales y regimen tributario no deben ser editables.
- AC-106: Dado un cliente natural simple sin direccion, cuando se guarde, entonces debe usar automaticamente el municipio de la empresa/emisor fiscal activo; si el usuario diligencia direccion, debe poder seleccionar departamento y municipio.
- AC-107: Dado un request directo al backend para cliente natural simple, cuando incluya NIT, DV, razon social, nombre comercial, responsabilidades distintas a `R-99-PN` o regimen distinto a `NO_RESPONSABLE_IVA`, entonces `thirdparty-service` debe rechazarlo.
- AC-108: Dado un catalogo oficial o parametrizable, cuando se disene su administracion, entonces debe diferenciar catalogos regulatorios de solo lectura/versionados y catalogos operativos configurables por empresa.
- AC-109: Dado un usuario con permisos de administracion de catalogos, cuando abra el modulo `Catalogos`, entonces debe seleccionar un catalogo por nombre en espanol y la SPA debe consultar sus registros desde BFF/catalog-service, sin usar catalogos locales como fallback.
- AC-110: Dado un catalogo seleccionado, cuando se listen sus registros, entonces la UI debe mostrar codigo tecnico, etiqueta en espanol, descripcion, origen, version, estado y si es regulatorio.
- AC-111: Dado un usuario `ROOT`, cuando administre catalogos globales, entonces puede crear, actualizar o inactivar items permitidos; los codigos regulatorios oficiales no se editan por administradores empresariales.
- AC-112: Dado un administrador empresarial con permiso delegado, cuando administre catalogos de su empresa, entonces solo puede activar/inactivar opciones permitidas o crear extensiones operativas aprobadas dentro de su `company_id`.
- AC-113: Dada la SPA, cuando cargue formularios que dependen de catalogos, entonces no debe importar datos de catalogo regulatorios u operativos desde `src/data`; debe consumirlos desde base de datos por BFF y mostrar error controlado si no estan disponibles.
- AC-114: Dado el reporte de auditoria de tablas legacy, cuando se clasifique una tabla como candidata a eliminar, entonces debe demostrar que no tiene referencias JPA/repositorio/SQL/runtime, que el flujo E2E no la usa, y que sus datos estan vacios o migrados/respaldados.
- AC-115: Dada una eliminacion aprobada de tablas legacy, cuando se ejecute Flyway sobre una base limpia y la base local actual, entonces ambas deben quedar alineadas con el modelo vigente y la suite completa debe pasar sin referencias a tablas eliminadas.

## POS con impuestos por producto, scanner y consumidor final

- AC-116: Dado un producto vendible, cuando se cree en inventario, entonces debe guardar `taxCategoryCode`, `taxCode`, `taxLabel` y `taxRate` provenientes de catalogo fiscal activo.
- AC-117: Dada una venta POS, cuando se agregue una linea, entonces `billing-service` debe calcular impuesto y tarifa desde el snapshot de inventario y no desde campos enviados por frontend.
- AC-118: Dada la SPA de Venta POS, entonces no debe mostrar campos editables de canal, impuesto ni tasa; solo debe mostrar informacion calculada o derivada del producto cuando exista.
- AC-119: Dado un codigo de barras escaneado en POS, cuando el campo dedicado reciba el codigo, entonces la SPA debe consultar producto por barcode automaticamente, agregarlo como linea o incrementar cantidad si ya existe, limpiar el campo y dejarlo listo para el siguiente escaneo.
- AC-120: Dado el formulario de inventario, cuando se escanee un codigo de barras en su campo dedicado, entonces debe quedar capturado en `barcode` sin requerir drivers, clic adicional ni escritura en otros campos de negocio.
- AC-121: Dada una venta POS donde el comprador no desea identificarse para factura electronica nominada, cuando se cree la venta, entonces la SPA debe enviar `buyerIdentificationMode=FINAL_CONSUMER` y `billing-service` debe resolver el adquirente desde configuracion persistida, sin crear tercero `thirdparty`.
- AC-122: Dada una venta POS donde el comprador si desea factura electronica nominada, cuando se cree la venta, entonces debe exigir `customerId` seleccionado por buscador y conservar snapshot fiscal del tercero.
- AC-123: Dado el catalogo fiscal de impuestos, cuando cambie una tarifa o impuesto permitido, entonces `ROOT` puede parametrizarlo en base de datos y los productos nuevos deben usar el catalogo actualizado sin despliegue frontend.

## Auditoria transversal y UX operativa

- AC-124: Dado un usuario ROOT autenticado, cuando actualiza un item de catalogo global/regulatorio, entonces el backend persiste el cambio y registra auditoria `CATALOG_ITEM/UPDATE_CATALOG_ITEM/SUCCESS`.
- AC-125: Dado un usuario ROOT autenticado, cuando activa o inactiva un item de catalogo global/regulatorio, entonces el backend persiste el estado y registra auditoria `CATALOG_ITEM/SET_CATALOG_ITEM_ACTIVE/SUCCESS`.
- AC-126: Dado un usuario no ROOT sin permiso de administracion global, cuando intenta administrar catalogos globales, entonces la UI no habilita la accion y el backend conserva restricciones de dominio.
- AC-127: Dado que una accion inicia desde la UI, cuando esta en proceso, entonces se muestra un modal con barra de carga y texto de estado sin mostrar JSON tecnico persistente.
- AC-128: Dado que una accion finaliza correctamente, cuando el backend responde OK, entonces el modal informa exito y permite cerrarlo.
- AC-129: Dado que una accion falla, cuando el backend responde error o no responde, entonces el modal informa error generico y referencia revisar Logs/Auditoria, conservando `correlationId` si existe.
- AC-130: Dado un usuario ROOT o administrador de empresa, cuando entra al modulo Logs/Auditoria, entonces puede consultar eventos autorizados de auditoria.
- AC-131: Dado un usuario sin permisos de auditoria, cuando usa la aplicacion, entonces no ve el modulo Logs/Auditoria.
