# Acceptance Criteria

## Facturacion electronica

- AC-001: Dado un emisor configurado con resolucion vigente, cuando se cree una factura valida, entonces el sistema debe asignar prefijo y numero autorizado sin duplicados.
- AC-002: Dada una factura con productos, cantidades, impuestos y descuentos, cuando se calcule el documento, entonces subtotal, impuestos, descuentos, cargos y total deben cuadrar exactamente.
- AC-003: Dada una factura lista para emitir, cuando se envie mediante la conexion DIAN configurada para la empresa, entonces el sistema debe registrar solicitud, respuesta, identificadores, estado y errores si existen.
- AC-004: Dada una respuesta exitosa de la conexion DIAN configurada, entonces el sistema debe almacenar CUFE/CUDE cuando aplique, QR, XML, representacion grafica y estado validado o equivalente.
- AC-005: Dada una factura validada, cuando el usuario intente editar valores fiscales, entonces el sistema debe rechazar la modificacion y exigir nota credito/debito.
- AC-006: Dada una factura rechazada por la conexion DIAN configurada, entonces el sistema debe conservar el rechazo, permitir correccion segun estado y registrar auditoria.

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
- AC-023: Cada adaptador externo de conexion DIAN debe probarse con mocks o test doubles.
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
- AC-033: Dado un flujo POS/factura validado por el conector DIAN mock, cuando el documento quede aceptado, entonces el sistema debe descontar inventario y generar asiento contable automaticamente sin duplicar efectos ante reintentos idempotentes.
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
- AC-066: Dado el entorno local Docker, cuando `IDENTITY_ROOT_USER_SEED_ENABLED=true`, entonces `identity-service` debe crear o asegurar un usuario `ROOT` dummy activo, retornar `globalRoles` en login y permitir entrada al panel global sin empresa ni licencia.
- AC-067: Dado un usuario `ROOT` autenticado, cuando ingrese a la SPA, entonces debe ver todos los modulos disponibles y operar configuraciones usando una empresa activa creada o seleccionada.
- AC-068: Dado un usuario `ROOT` autenticado y una empresa contratante creada, cuando registre el administrador inicial con email, nombre y contrasena, entonces el sistema debe crear el usuario y asignarle rol empresarial `OWNER` para esa empresa.
- AC-069: Dado el flujo de creacion de empresa, cuando se envie el request, entonces debe contener `identificationTypeCode` numerico DIAN y no debe contener `identificationTypeId` UUID.
- AC-070: Dado un codigo de tipo de documento, cuando no pertenezca a la tabla DIAN soportada, entonces el backend debe rechazarlo con error funcional claro.
- AC-071: Dada la tabla `tenant.company`, cuando persista identificacion de empresa, entonces debe guardar `identification_type_code` entero con restriccion de codigos permitidos.
- AC-072: Dado el frontend de empresa, cuando el usuario seleccione tipo de documento, entonces debe mostrar nombres de tipos DIAN y enviar el codigo numerico seleccionado.
- AC-073: Dado `thirdparty-service`, cuando reciba, devuelva o persista tipos de documento, entonces debe usar `identificationTypeCode` entero DIAN.
- AC-074: Dado cualquier formulario de identificacion en frontend, entonces no debe permitir aliases textuales o UUID para tipos de documento; debe mostrar etiquetas en espanol y enviar codigos numericos.
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
- AC-095: Dada una sesion autenticada en modo local/transitorio, cuando el usuario refresque la pagina antes de 5 minutos de inactividad, entonces la SPA debe restaurar la sesion desde `sessionStorage` sin volver a login. En produccion este criterio queda reemplazado por sesion BFF con cookie `HttpOnly`.
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

## Catalogos desde base de datos, UX operativa, contabilidad y nomina

- AC-132: Dada la SPA, cuando se compile para produccion, entonces no debe contener `initialState` con datos demo de empresa, tercero, inventario, fiscal, venta, usuarios, roles ni catalogos de negocio.
- AC-133: Dada una pantalla que depende de catalogos, cuando `catalog-service` o BFF no entregue los catalogos requeridos, entonces la pantalla debe mostrar error controlado y bloquear la accion sin inventar opciones locales.
- AC-134: Dado el entorno local inicial, cuando se levante la plataforma, entonces el unico usuario semilla permitido es `ROOT`; empresas, administradores, terceros, productos y ventas se crean por API o E2E.
- AC-135: Dado un flujo de login con credenciales invalidas, cuando el backend responda 401 o 403, entonces el modal debe indicar credenciales incorrectas sin mostrar errores tecnicos.
- AC-136: Dada una accion de negocio exitosa, cuando el backend responda OK, entonces el modal de proceso debe mostrar exito y cerrarse automaticamente.
- AC-137: Dada una accion fallida por error 5xx, timeout o fallo de red, entonces el modal debe permanecer visible con mensaje generico de fallo interno y referencia a Logs/Auditoria.
- AC-138: Dado el modulo Logs/Auditoria, cuando se abra, entonces debe cargar por defecto los eventos del dia actual.
- AC-139: Dado el modulo Logs/Auditoria, cuando el usuario filtre, entonces solo debe poder filtrar por rango de fechas y `resourceType` opcional cargado desde backend.
- AC-140: Dado el modulo Logs/Auditoria, entonces no debe mostrar filtro manual por `resourceId`.
- AC-141: Dado el formulario de inventario, cuando el usuario configure el uso del item, entonces debe elegir opciones guiadas en espanol y el frontend debe mapearlas a `saleEnabled`, `purchaseEnabled` y `stockTracked`.
- AC-142: Dado un producto vendible, cuando se cree o actualice, entonces el impuesto debe seleccionarse desde catalogo persistido `SALES_TAX` y no desde una constante frontend.
- AC-143: Dado el modulo contable, cuando se consulten ingresos, egresos, costos, activos, cuentas por cobrar o cuentas por pagar, entonces los datos deben estar aislados por empresa y provenir del modelo contable activo.
- AC-144: Dada una venta, compra, gasto o pago de nomina contabilizable, cuando quede confirmado, entonces debe generar asiento contable balanceado o dejar error estructurado si no existe regla PUC configurada.
- AC-145: Dado el modulo de nomina, cuando se registre un empleado o contrato, entonces debe persistirse por empresa y auditarse.
- AC-146: Dado un pago diario verbal, cuando se registre, entonces debe conservar fecha, actividad, jornada/horas, valor acordado, valor pagado, medio de pago, observaciones, evidencia opcional y clasificacion laboral/contractual.
- AC-147: Dado un pago diario verbal, cuando se confirme, entonces debe mostrar advertencia legal configurable y exigir confirmacion administrativa auditada.
- AC-148: Dada una empresa con nomina electronica desactivada, cuando registre nomina interna, entonces no debe generar documento soporte electronico mock.
- AC-149: Dada una empresa con nomina electronica activada, cuando cierre un periodo de nomina, entonces debe poder generar documento soporte de nomina electronica mock y registrar estado.
- AC-150: Dado un pago clasificado como contratista independiente, cuando se contabilice, entonces debe registrarse como egreso/proveedor o gasto operativo y no como empleado de nomina formal.
- AC-151: Dada la suite E2E actualizada, cuando se ejecute desde cero, entonces debe crear empresa, administrador, catalogos requeridos, tercero, inventario, venta, factura mock, contabilidad, logs y un flujo minimo de nomina/pago diario sin depender de datos demo del frontend.

## Productizacion operativa y cierre funcional

- AC-152: Dado un entorno local limpio con solo ROOT semilla, cuando se ejecute el E2E desde cero, entonces debe crear empresa, licencia, OWNER, tercero, producto, stock, venta POS, factura electronica mock, descuento de inventario, asiento contable y auditoria.
- AC-153: Dada una compra confirmada, entonces debe incrementar stock, conservar costo, registrar proveedor, generar egreso/cuenta por pagar segun configuracion y crear asiento contable balanceado.
- AC-154: Dado un servicio facturable con insumos sugeridos, cuando se confirme consumo manual, entonces debe crear movimientos `CONSUMPTION_OUT` idempotentes, auditados y asociados al documento origen.
- AC-155: Dado cualquier listado operativo, cuando se abra, entonces debe cargar datos paginados de la empresa activa, permitir busqueda/estado y no mostrar datos de otra empresa.
- AC-156: Dado un usuario sin permiso o sin modulo licenciado, cuando invoque una accion protegida directamente por API, entonces backend/BFF debe rechazarla aunque la SPA no muestre la accion.
- AC-157: Dado ROOT, cuando consulte uso de licencia, entonces debe ver usuarios activos, documentos del mes, modulos, estado y vigencia por empresa.
- AC-158: Dada una accion mutable exitosa o fallida, entonces debe existir evento de auditoria sin secretos ni payload sensible.
- AC-159: Dada una sesion vigente, cuando se refresque la pagina, entonces se restaura; dada una sesion inactiva por 5 minutos o expirada, entonces se cierra y vuelve a login.
- AC-160: Dada una regla contable PUC ausente para un evento confirmado, entonces el proceso debe fallar con error funcional y auditoria sin crear asiento desbalanceado.
- AC-161: Dado un asiento generado automaticamente, entonces la suma de debitos debe ser igual a la suma de creditos y debe estar asociado a empresa, evento y documento origen.
- AC-162: Dado el modulo de reportes, entonces debe consultar estado de resultados, balance basico, libro diario, cuentas por cobrar/pagar, inventario valorizado y uso de licencia con aislamiento empresarial.
- AC-163: Dadas las rutas criticas del BFF, entonces las pruebas de contrato deben validar metodo, ruta, headers, payload y preservacion de errores funcionales.
- AC-164: Dado el E2E multiempresa, cuando dos empresas creen datos similares, entonces ninguna consulta o accion empresarial debe cruzar datos.
- AC-165: Dado Terraform AWS, cuando se ejecute `terraform fmt`, `terraform init -backend=false` y `terraform validate`, entonces no debe fallar.
- AC-166: Dado un evento asincrono productivo, cuando se procese por Lambda, entonces debe ser idempotente, tener DLQ/reintento y no bloquear el microservicio productor.
- AC-167: Dado el modulo administrativo de usuarios y roles, cuando se listen registros, entonces las tablas deben mostrar datos principales, estado, permisos y acciones con presentacion profesional, busqueda estable y textos visibles en espanol.

## Configuracion DIAN parametrizable por empresa

- AC-168: Dada la documentacion del producto, cuando describa la emision electronica, entonces debe indicar que la plataforma es software parametrizable por empresa y no presta servicio de proveedor tecnologico DIAN.
- AC-169: Dada una empresa, cuando configure conexion DIAN, entonces la configuracion debe quedar aislada por `company_id` y no debe poder usarse desde otra empresa.
- AC-170: Dado un certificado, PIN tecnico, clave tecnica o credencial DIAN, cuando se configure, entonces el sistema debe almacenar solo una referencia segura y nunca retornar ni auditar el valor secreto.
- AC-171: Dado un usuario ROOT o administrador empresarial autorizado, cuando cree, actualice, pruebe, active o inactive configuracion DIAN, entonces debe generarse auditoria segura sin secretos.
- AC-172: Dada una empresa con configuracion DIAN incompleta, vencida, inactiva o no probada, cuando intente emitir en modo real, entonces el backend debe rechazar la emision con error funcional claro.
- AC-173: Dado el modo `MOCK`, cuando se ejecute E2E local, entonces debe permitir pruebas internas sin llamadas externas y sin afirmar cumplimiento tecnico DIAN productivo.
- AC-174: Dada la UI de Configuracion DIAN, cuando se active modo real, entonces debe mostrar declaracion de responsabilidad de la empresa facturadora y exigir confirmacion explicita.
- AC-175: Dado el flujo futuro de integracion real, cuando se implemente, entonces debe validar XML UBL, firma, CUFE/CUDE, QR, XSD/Schematron y respuesta DIAN segun anexo vigente antes de habilitar produccion.
- AC-217: Dada la Fase 20 DIAN, cuando se revise la documentacion, entonces debe citar fuentes oficiales DIAN vigentes y declarar la version/anexo tecnico usado para cada decision.
- AC-218: Dado un documento fiscal soportado, cuando `dian-provider-service` genere el XML real, entonces debe producir UBL 2.1 valido para factura electronica, documento equivalente electronico POS o notas fiscales segun tipo.
- AC-219: Dado el mismo snapshot fiscal, cuando se calcule CUFE/CUDE/QR, entonces el resultado debe ser deterministico y estar cubierto por pruebas con fixtures sanitizados.
- AC-220: Dado un certificado empresarial configurado, cuando se firme un XML, entonces la firma debe usar solo secretos de la empresa y no debe exponer certificado, PIN ni claves en logs, auditoria o respuesta API.
- AC-221: Dado un XML con error XSD, Schematron o lista de codigos, cuando se intente transmitir en modo real, entonces el sistema debe bloquear el envio y registrar error tecnico sanitizado.
- AC-222: Dada una empresa con modo DIAN real, cuando transmita en habilitacion o produccion, entonces debe usar URLs, ambiente y credenciales propias de esa empresa, sin configuracion global compartida.
- AC-223: Dada una respuesta DIAN o `ApplicationResponse`, cuando llegue al sistema, entonces debe persistirse estado, tracking, codigos/mensajes sanitizados y correlacion con el documento fiscal.
- AC-224: Dado un error temporal DIAN, cuando se ejecute reintento, entonces debe ser idempotente y no duplicar documento fiscal, descuento de inventario ni asiento contable.
- AC-225: Dado un documento validado o rechazado, cuando se almacenen artefactos fiscales, entonces deben quedar en storage privado con hash, metadata y acceso controlado por BFF/RBAC.
- AC-226: Dado `DIAN_PROVIDER_MODE=REAL`, cuando falle una validacion o transporte real, entonces el sistema no debe cambiar automaticamente a mock.
- AC-227: Dado el cierre de DIAN real, cuando se solicite habilitar produccion, entonces debe existir evidencia de pruebas unitarias, integracion y E2E con fixtures sanitizados del anexo tecnico vigente.

## Autenticacion productiva, sesion segura y proteccion del navegador

- AC-176: Dado un ambiente productivo, cuando el usuario inicie sesion, entonces debe ser redirigido a Cognito Hosted UI con Authorization Code Grant + PKCE, no a un formulario de password propio de la SPA.
- AC-177: Dado el callback OAuth, cuando Cognito devuelva `code` y `state`, entonces el BFF debe validar `state`, intercambiar el codigo por tokens y crear una sesion server-side sin retornar tokens al navegador.
- AC-178: Dada una sesion productiva creada, entonces la respuesta del BFF debe establecer cookie `HttpOnly`, `Secure`, `SameSite=Lax` o `Strict`, con expiracion controlada.
- AC-179: Dada la SPA productiva autenticada, entonces `localStorage`, `sessionStorage`, IndexedDB y estado serializado no deben contener `accessToken`, `refreshToken`, `idToken`, bearer token, password ni cookie de sesion.
- AC-180: Dado cualquier request de la SPA hacia `/api/v1/**`, entonces la autorizacion debe derivarse de la cookie segura y de la sesion server-side; la SPA no debe enviar header `Authorization` construido en JavaScript.
- AC-181: Dado un logout, entonces el BFF debe invalidar la sesion server-side, limpiar la cookie y revocar tokens Cognito cuando aplique.
- AC-182: Dado un usuario ROOT o administrador en produccion, cuando intente iniciar sesion u operar acciones criticas sin MFA, entonces el sistema debe bloquear la operacion.
- AC-183: Dado un endpoint mutable autenticado por cookie, cuando falte o sea invalido el token CSRF, entonces el BFF debe rechazar la solicitud con error seguro.
- AC-184: Dada la build productiva de la SPA, entonces no debe contener `console.log`/`console.debug` con payloads, credenciales, tokens, headers sensibles o respuestas completas.
- AC-185: Dado CloudFront/BFF productivo, entonces las respuestas deben incluir HSTS, CSP, `X-Content-Type-Options`, proteccion de frame y `Referrer-Policy`.
- AC-186: Dado el alta de una empresa por ROOT, cuando se creen secretos AWS por empresa, entonces la operacion debe usar prefijo controlado por ambiente/empresa, KMS/IAM minimo, idempotencia y auditoria sin exponer valores.
- AC-187: Dado un ambiente productivo, cuando `POST /api/v1/auth/login` dummy sea invocado desde la SPA, entonces debe estar deshabilitado o no expuesto publicamente.
- AC-188: Dado un analisis automatico o manual de seguridad, entonces no deben encontrarse passwords, tokens, certificados, PIN ni claves en logs, auditoria, errores publicos, storage del navegador ni sourcemaps publicos.
- AC-189: Dado el flujo SDD vigente, cuando se revise la documentacion, entonces no deben existir IDs de requisitos duplicados, secciones vacias de tareas DONE ni decisiones vigentes sin reflejo en requisitos, diseno, arquitectura, infraestructura, contratos API, modelo/diccionario de datos, tareas y README cuando aplique.
- AC-190: Dados los diagramas Mermaid en `specs/diagrams`, cuando se comparen con la arquitectura y el modelo vigente, entonces deben representar BFF, Cognito, microservicios privados, AWS administrado, EventBridge/SQS/Lambda con DLQ, RDS/RDS Proxy, Secrets/KMS, DIAN parametrizable por empresa, RBAC, licencias, catalogos DB-only, terceros consolidados, inventario, billing, contabilidad, nomina, auditoria, sesiones BFF y Outbox/Inbox.
- AC-191: Dada la documentacion SDD, cuando una capacidad aparezca como objetivo productivo o backlog, entonces debe estar marcada explicitamente como `objetivo`, `pendiente` o `transitorio`, sin confundirse con componentes implementados y desplegables actualmente.
- AC-192: Dada la limpieza final legacy antes de nuevas mejoras, cuando se audite repositorio, Docker local y PostgreSQL, entonces no deben quedar artefactos generados/IDE ignorados, guias historicas obsoletas ni tablas `public.*` vacias; las tablas `public.*` con datos deben quedar documentadas como pendientes de migracion/respaldo o descarte aprobado.

## Marca NexoFiscal, branding empresarial, reportes avanzados e impresion POS

- AC-193: Dada la SPA, cuando se abra login, shell autenticado, sidebar, encabezado o titulo del navegador, entonces debe presentarse como `NexoFiscal` y no como `Factura Electronica`.
- AC-194: Dada una empresa activa, cuando tenga branding configurado, entonces la SPA debe mostrar logo de login, logo de encabezado y favicon empresarial; si no existe configuracion, debe usar fallback visual de `NexoFiscal`.
- AC-195: Dado un usuario ROOT o administrador empresarial autorizado, cuando cargue o actualice logos, entonces el backend debe validar tamano, tipo MIME, extension, dimensiones y contenido permitido antes de almacenar metadata y archivo.
- AC-196: Dado un archivo de branding, cuando no cumpla la lista permitida de formatos seguros, entonces el backend debe rechazarlo con error funcional sin conservar el archivo.
- AC-197: Dada una mutacion de branding empresarial, cuando finalice exitosa o fallida, entonces debe generar auditoria con empresa, usuario, accion, resultado y correlation ID sin guardar contenido binario ni datos sensibles.
- AC-198: Dado el modulo avanzado de reportes, cuando el usuario seleccione un tipo de reporte, entonces la UI debe cargar filtros dinamicos, opciones de datos y tipos de grafico permitidos para ese reporte desde backend.
- AC-199: Dado un reporte con rango de fechas obligatorio, cuando el usuario intente generarlo sin fechas validas, entonces la UI y el backend deben bloquear la consulta con mensaje funcional.
- AC-200: Dado el reporte de ventas por vendedor, cuando se carguen opciones de vendedor, entonces solo deben aparecer usuarios empresariales activos con rol o permiso efectivo de ventas, no cualquier usuario de la empresa.
- AC-201: Dado cualquier reporte, cuando se consulte por un usuario empresarial, entonces los datos deben estar aislados por `company_id`, RBAC y licencia; ROOT solo puede consultar datos de la empresa activa seleccionada.
- AC-202: Dado un reporte historico o tabular, cuando el usuario solicite exportacion, entonces el sistema debe generar al menos CSV/Excel y conservar auditoria de solicitud, finalizacion, descarga o error.
- AC-203: Dado un reporte con visualizacion grafica, cuando el usuario seleccione tipo de grafico, entonces el backend debe validar que `TABLE`, `BAR`, `LINE`, `PIE` o `KPI` aplique al reporte solicitado.
- AC-204: Dado que los reportes requieran agregaciones transversales o exportaciones pesadas, entonces debe implementarse `reporting-service` como microservicio fisico, y el BFF solo debe enrutar, autorizar y normalizar errores.
- AC-205: Dado un documento POS confirmado, entonces el sistema debe generar y conservar artefactos consultables: representacion imprimible, metadata fiscal, QR cuando aplique, hash, tipo de contenido y referencia de almacenamiento.
- AC-206: Dado un comprobante POS, cuando el usuario solicite impresion o reimpresion, entonces la fase inicial debe usar impresion web 58/80 mm y registrar intento/resultado; conectores ESC/POS, WebUSB, WebSerial o agente local requieren tarea posterior con hardware aprobado.
- AC-207: Dado el historico de ventas/documentos, cuando el usuario consulte por fecha, vendedor, cliente, estado fiscal o metodo de pago, entonces debe ver ventas emitidas, detalle, items, totales, estado DIAN/mock, artefactos, descargas y reimpresiones segun permisos.
- AC-208: Dado un negocio que requiere reportes de compras, inventario, rentabilidad, cuentas, nomina o licencia, cuando el modulo de reportes este implementado, entonces debe ofrecer esos reportes como opciones parametrizadas y exportables segun alcance licenciado.

## Reportes asincronos avanzados con S3 y notificacion

- AC-209: Dado un reporte marcado como pesado, cuando el usuario solicite exportacion, entonces el API debe crear un job `PENDING` y responder sin esperar la generacion del archivo.
- AC-210: Dado un job de reporte, cuando el worker lo procese, entonces debe pasar por `PROCESSING` y terminar en `READY` con referencia privada de S3 o en `FAILED` con error sanitizado.
- AC-211: Dado un reporte listo, cuando se envie correo al usuario, entonces el link debe construirse con `APP_PUBLIC_BASE_URL` y no debe contener URL directa de S3, bucket, key interna ni credenciales.
- AC-212: Dado un link de descarga valido, cuando el usuario haga clic, entonces el BFF debe validar token, empresa, usuario, estado del job y auditoria antes de generar una URL prefirmada de S3.
- AC-213: Dada la URL prefirmada generada por el BFF, entonces debe expirar inicialmente a los 5 segundos desde el clic mediante `REPORT_DOWNLOAD_PRESIGNED_TTL_SECONDS`.
- AC-214: Dado un token vencido, revocado, reutilizado fuera de politica o asociado a un job no disponible, entonces la aplicacion debe mostrar mensaje funcional claro sin exponer detalles de S3 ni secretos.
- AC-215: Dado ROOT, administrador empresarial o usuario normal, cuando consulte jobs de reportes, entonces solo debe ver los jobs permitidos por alcance, empresa, RBAC y licencia.
- AC-216: Dada cualquier solicitud, procesamiento, fallo, expiracion, revocacion, envio de correo o descarga de reporte pesado, entonces debe existir auditoria segura con correlation ID y sin filtros sensibles completos.

## Ajustes QA RBAC, POS e i18n

- AC-228: Dado `ROOT`, cuando cree el administrador inicial de una empresa con rol `OWNER`, entonces debe existir un rol empresarial activo `OWNER` para esa empresa con todos los permisos `COMPANY`, asignado al administrador y visible en el panel de Roles.
- AC-229: Dado un administrador empresarial `OWNER`, cuando intente confirmar una venta POS sin emisor fiscal activo o resolucion activa, entonces el backend debe responder un error funcional en espanol que indique la configuracion faltante y la SPA debe mostrarlo sin tratarlo como falta de permisos.
- AC-230: Dado el catalogo de permisos RBAC vigente, cuando la SPA muestre permisos o modulos, entonces todos los codigos deben resolverse desde `i18next` a etiquetas/descripciones en espanol; ningun permiso vigente debe mostrarse como texto derivado en ingles.
- AC-231: Dada una empresa con varios emisores fiscales, cuando se cree o active un emisor, entonces todos los demas emisores de la empresa quedan inactivos y la tabla fiscal muestra el nuevo estado.
- AC-232: Dada una empresa con varias resoluciones para el mismo tipo documental y ambiente, cuando se cree o active una resolucion, entonces las demas resoluciones del mismo alcance quedan inactivas.
- AC-233: Dada una venta POS sin resolucion activa, cuando el backend responda el mensaje historico en ingles o el nuevo mensaje en espanol, entonces la SPA debe mostrar un error funcional de configuracion fiscal y llevar al usuario al modulo Fiscal.
- AC-234: Dado `ROOT`, cuando ingrese a Configuracion > Empresa, entonces el formulario principal debe permanecer vacio para crear empresa y no debe precargarse por la empresa activa.
- AC-235: Dado un intento de login con BFF no disponible, entonces la SPA debe informar indisponibilidad del servicio de autenticacion y no presentar el problema como credenciales invalidas.
- AC-236: Dado `ROOT`, cuando consulte Configuracion > Empresa, entonces debe ver una tabla de empresas registradas con acciones por fila para actualizar, activar/inactivar, crear administrador y crear marca empresarial.
- AC-237: Dado `ROOT`, cuando haga clic en `Actualizar` sobre una empresa, entonces el formulario principal debe cargarse con esa empresa y el boton principal debe cambiar a `Actualizar empresa`.
- AC-238: Dado `ROOT`, cuando abra el modal de administrador inicial o marca empresarial desde una fila, entonces solo el campo de empresa debe estar bloqueado y los demas campos deben permanecer editables.
- AC-239: Dado `ROOT`, cuando ejecute crear administrador o guardar marca empresarial desde un modal, entonces el request debe usar el `company_id` de la empresa seleccionada en la fila y no depender de datos escritos manualmente por el usuario.
