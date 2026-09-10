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
- AC-043: Dada una entrada fisica de productos o insumos registrada desde `Inventario`, entonces debe incrementar stock, registrar kardex y generar contabilizacion segun reglas PUC de la empresa.
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
- AC-153: Dada una compra documental confirmada, entonces debe conservar proveedor, conceptos, valores, generar egreso/cuenta por pagar segun configuracion y crear asiento contable balanceado sin incrementar stock.
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
- AC-391: Dada una empresa en ambiente de habilitacion DIAN, cuando envie una factura electronica de prueba, entonces `dian-provider-service` debe consumir SOAP WCF contra `https://vpfe-hab.dian.gov.co/WcfDianCustomerServices.svc` usando `SendTestSetAsync` y el `testSetId` configurado.
- AC-392: Dado un documento enviado por SOAP a DIAN, cuando DIAN responda `UploadDocumentResponse`, `DianResponse` o `ApplicationResponse`, entonces el sistema debe persistir tracking/zipKey, codigo, descripcion, mensaje, validez, CUFE/CUDE y artefactos sin exponer XML completo sensible en logs publicos.
- AC-393: Dado un error SOAP, timeout, WSDL no disponible o respuesta no parseable, cuando ocurra en modo real, entonces el backend debe responder error funcional DIAN sanitizado y no debe degradar a mock.
- AC-394: Dado el modulo de Configuracion DIAN, cuando una empresa configure certificado real, entonces la UI debe permitir seleccionar un unico archivo `.p12` o `.pfx`, capturar password como campo secreto y no ofrecer textarea para pegar el certificado.
- AC-395: Dado un archivo de certificado subido, cuando el backend lo reciba, entonces debe validar extension, tamano, tipo/estructura PKCS#12, password, alias, fingerprint y vencimiento; la base de datos solo persiste referencia segura y metadata no sensible.
- AC-396: Dada la caja de herramientas DIAN local, cuando se implemente o valide el flujo, entonces deben usarse XSD, Schematron, XSL/listas de codigos y XML de ejemplo como fixtures sanitizados, sin versionar artefactos innecesarios como `.DS_Store`, `__MACOSX` o jars no usados.
- AC-397: Dado un documento fiscal de una empresa, cuando se emita en modo DIAN real, entonces `dian-provider-service` debe usar exclusivamente el certificado asociado al mismo `company_id`; si la referencia pertenece a otra empresa, ROOT o una configuracion global, la emision debe fallar cerrada con error funcional.
- AC-398: Dada una empresa sin configuracion DIAN real propia activa, probada exitosamente y con certificado configurado, cuando intente crear o activar una resolucion electronica, entonces `billing-service` debe rechazar la operacion con error funcional y no debe persistir la resolucion activa.
- AC-399: Dada una empresa configurada con `NON_FISCAL_SALE`, cuando cierre una venta, entonces `billing-service` debe confirmar la venta, liquidar impuestos desde las lineas, aplicar inventario/contabilidad de forma idempotente y no generar documento electronico, CUFE/CUDE, QR DIAN ni envio a `dian-provider-service`.

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
- AC-240: Dado un usuario con acceso a ventas, cuando abra `Ventas`, entonces no debe ver el historico de ventas en esa pantalla; solo debe ver el flujo operativo de comprador, pago, scanner, lineas, creacion y confirmacion POS.
- AC-241: Dado un usuario con acceso a ventas, cuando abra `Registro de Ventas`, entonces debe poder filtrar y consultar ventas registradas sin acciones de modificacion, eliminacion, inactivacion, anulacion, reemision o reimpresion.
- AC-242: Dada una venta registrada con documento electronico, cuando el usuario haga clic en `Ver detalle`, entonces la SPA debe mostrar detalle de venta, lineas, estado fiscal, numero de documento, tracking y CUFE/CUDE cuando exista.
- AC-243: Dado `ROOT`, cuando abra modales de administrador inicial o marca empresarial, entonces los modales deben mantenerse centrados, responsivos, sin desbordar campos ni superponer contenido, y con scroll interno solo cuando el alto disponible lo requiera.

## Politica fiscal configurable, PIN operacional y documentos fiscales

- AC-244: Dada una empresa con politica fiscal por defecto `ELECTRONIC_INVOICE`, cuando un vendedor confirme una venta POS sin override, entonces `billing-service` debe asignar numeracion de factura electronica de venta y enviar el documento al flujo de factura electronica.
- AC-245: Dada una empresa con resoluciones activas para varios tipos documentales, cuando se cree o active una resolucion, entonces solo se inactivan resoluciones del mismo `company_id`, `document_type` y `environment`, sin afectar otros tipos documentales.
- AC-246: Dado un vendedor sin permiso de override, cuando intente cambiar una venta de factura electronica a documento equivalente POS, entonces el sistema debe exigir autorizacion operacional y no debe cambiar la venta sin aprobacion.
- AC-247: Dado un administrador/supervisor con permiso `SALES_DOCUMENT_TYPE_OVERRIDE`, cuando ingrese PIN valido y motivo, entonces el backend debe autorizar el cambio solo para esa venta y registrar auditoria con vendedor y autorizador.
- AC-248: Dado un PIN operacional, cuando se cree o cambie, entonces debe aceptar exactamente 6 digitos numericos y persistirse solo como hash.
- AC-249: Dado un PIN operacional activo, cuando falle 3 veces consecutivas, entonces debe quedar `LOCKED`, rechazar nuevas autorizaciones y generar auditoria de bloqueo.
- AC-250: Dado un PIN bloqueado, cuando un administrador autorizado lo desbloquee, entonces debe pasar a `CHANGE_REQUIRED` y el titular debe cambiarlo antes de autorizar operaciones.
- AC-251: Dado un usuario con PIN en `CHANGE_REQUIRED`, cuando intente autorizar un override, entonces el sistema debe rechazar la autorizacion y solicitar cambio de PIN.
- AC-252: Dada una venta POS que emite factura electronica de venta, cuando se genere tirilla, entonces debe titularse como representacion grafica de factura electronica de venta e incluir CUFE cuando exista.
- AC-253: Dada una venta POS que emite documento equivalente electronico POS, cuando se confirme, entonces debe usar resolucion `ELECTRONIC_POS`, CUDE y reglas propias de documento equivalente POS.
- AC-254: Dado un usuario sin permisos fiscales de notas, cuando abra la aplicacion, entonces no debe ver ni ejecutar Nota credito, Nota debito ni Nota de ajuste POS.
- AC-255: Dado un usuario autorizado para notas, cuando cree Nota credito, Nota debito o Nota de ajuste POS, entonces el sistema debe usar la resolucion activa del tipo documental correspondiente y auditar la operacion.
- AC-256: Dado cualquier uso exitoso o fallido de PIN/override/notas fiscales, entonces auditoria debe conservar correlation ID y no debe incluir PIN, hashes de PIN, contrasenas ni payloads fiscales completos.

## Cierre de venta, permisos y configuracion contable

- AC-257: La pantalla Ventas muestra `Cerrar venta` como accion principal y no exige `Crear venta` seguido de `Emitir documento fiscal`.
- AC-258: `POST /api/v1/sales/close` crea y confirma la venta con una sola idempotency key.
- AC-259: El cierre exitoso abre el comprobante imprimible y reinicia el formulario operativo.
- AC-260: Faltantes de emisor fiscal/resolucion muestran mensaje funcional y llevan al modulo Fiscal.
- AC-261: Fallos del conector DIAN se mapean como `EXTERNAL_PROVIDER_ERROR`, no como `INTERNAL_ERROR` generico.
- AC-262: `identity.permission_catalog` no debe contener codigos activos ausentes de `PermissionCode`.
- AC-263: `GET /api/v1/platform/permissions` es root-only y no autentica falsamente usuarios empresariales como ROOT.
- AC-264: ROOT puede leer catalogos globales via BFF sin `X-Company-Id`.
- AC-265: El permiso `OPERATIONAL_PIN_MANAGE` aparece en el catalogo de permisos y no rompe `/platform/permissions`.
- AC-266: Guardar una licencia empresarial no debe fallar al refrescar el tablero de uso por consulta de documentos electronicos sin filtros opcionales.
- AC-267: `GET /api/v1/reports/electronic-documents?from=...&to=...` debe responder `200` con lista vacia o documentos reales cuando solo recibe empresa y rango de fechas.
- AC-268: Al iniciar sesion como `ROOT`, la SPA debe mantener el selector de empresa en blanco y no hidratar formularios con la primera empresa registrada.
- AC-269: En el modulo `Licencias`, el boton `Cargar licencia` debe quedar deshabilitado despues de cargar o guardar la licencia de la empresa seleccionada.
- AC-270: `billing-service` valida que exista regla contable activa `SALE_CONFIRMED` antes de asignar numeracion fiscal o enviar documento a DIAN/mock.
- AC-271: Si falta configuracion contable, la venta permanece sin confirmar, no se consume consecutivo fiscal, no se descuenta inventario y no se registra asiento.
- AC-272: `accounting-service` transforma la ausencia de regla contable en `400 BUSINESS_RULE_VIOLATION`, no en `500 INTERNAL_ERROR`.
- AC-273: La SPA muestra mensaje funcional y lleva al usuario a `Configuracion contable`.
- AC-274: `Desde` y `Hasta` deben ser enteros mayores a cero y `Hasta >= Desde`.
- AC-275: `Vigencia hasta` no puede ser menor que `Vigencia desde`.
- AC-276: Los campos invalidos se resaltan en rojo con mensaje local y el formulario no ejecuta POST mientras exista error.
- AC-277: La SPA muestra `Configuracion contable` como modulo independiente; su ubicacion vigente bajo `Configuracion` se valida en AC-458.
- AC-278: El modulo permite inicializar contabilidad basica por empresa mediante `POST /api/v1/accounting-setup/basic`.
- AC-279: El modulo lista plan de cuentas y reglas contables existentes por empresa.
- AC-280: El acceso depende de licencia `ACCOUNTING` y permisos `ACCOUNTING_VIEW` o `ACCOUNTING_MANAGE`.
- AC-281: El boton de configuracion contable no crea datos automaticamente sin mostrar previamente el formulario/asistente.
- AC-282: El asistente permite agregar, editar y quitar varias cuentas PUC antes de enviar una sola creacion batch.
- AC-283: El asistente permite agregar, editar y quitar varias reglas contables antes de enviar una sola creacion batch.
- AC-284: Cada regla permite agregar multiples `movimientos contables` con cuenta, naturaleza debito/credito, tipo de monto y descripcion.
- AC-285: El backend valida que todas las cuentas referenciadas existan o vengan en el mismo lote, y que cada regla quede balanceable segun partida doble.
- AC-286: El guardado batch es transaccional: si una cuenta, regla o movimiento falla, no se persiste ningun registro del lote.
- AC-287: La opcion `Completar plantilla basica` usa `POST /api/v1/accounting-setup/basic` para completar faltantes sin duplicar parametros existentes.
- AC-288: Cuentas o reglas ya usadas por asientos no se eliminan fisicamente; se inactivan o versionan conservando trazabilidad.
- AC-289: Toda creacion, actualizacion, inactivacion o aplicacion de plantilla contable queda auditada con empresa, usuario, recurso, resultado y correlation ID.
- AC-290: El plan de cuentas expone `used` y `usageCount` calculados desde asientos contables reales.
- AC-291: Las reglas contables exponen `used` y `usageCount` calculados desde `accounting_entry.accounting_rule_id`.
- AC-292: Los asientos nuevos guardan el `accounting_rule_id` de la regla usada para generar el asiento.
- AC-293: Las reglas anteriores sin `accounting_rule_id` quedan como historico no trazado; no se inventa historial contable.
- AC-294: Una cuenta sin uso puede actualizar nombre/cuenta padre o inactivarse; una cuenta usada no permite cambios estructurales ni inactivacion.
- AC-295: Una regla sin uso puede actualizarse o inactivarse; una regla usada no permite cambios estructurales ni inactivacion.
- AC-296: La SPA muestra columna `Uso` en tablas de reglas y plan de cuentas.
- AC-297: La SPA muestra acciones `Actualizar` e `Inactivar` solo cuando el recurso no ha sido usado.
- AC-298: En inventario, el usuario ingresa `Precio final` y la SPA calcula automaticamente `Precio sin IVA` y `Valor IVA`.
- AC-299: `Tarifa impuesto` deja de aparecer como campo principal visible/editable; la tarifa se deriva del catalogo `SALES_TAX`.
- AC-300: El payload `POST /api/v1/products` envia `salePrice` como precio unitario sin IVA calculado desde `Precio final`.
- AC-301: En ventas, cada linea muestra valores fiscales derivados y el formulario muestra resumen `Subtotal`, `IVA` y `Total` antes de cerrar.
- AC-302: La representacion imprimible conserva `Subtotal`, `IVA` y `Total` desde `SaleResponse`.
- AC-303: El campo `Codigo de barras` de inventario acepta escritura manual o lector USB HID sin boton adicional.

## Bugs UX formularios y PIN operacional

- AC-304: Dado un producto creado correctamente, cuando el backend responde exito, entonces el formulario de inventario se limpia y el producto queda visible en la lista.
- AC-305: Dado un error al crear producto, cuando el backend rechaza la solicitud, entonces el formulario conserva los datos digitados para correccion.
- AC-306: Dado un emisor fiscal guardado correctamente, cuando se recarga la configuracion fiscal, entonces se limpian campos capturados y se mantienen visibles los datos informativos de empresa activa como solo lectura.
- AC-307: Dada una resolucion fiscal guardada correctamente, cuando se recarga la configuracion fiscal, entonces el formulario de resolucion queda listo para una nueva captura.
- AC-308: Dado un usuario autorizado, cuando abre Configuracion > PIN operacional, entonces puede consultar estado, crear/cambiar PIN de 6 digitos y desbloquearlo si esta bloqueado.
- AC-309: Dado cualquier flujo de PIN operacional, entonces la SPA nunca muestra ni persiste el PIN o hash recibido; solo muestra estado, intentos restantes y fecha de actualizacion.
- AC-310: La SPA muestra `PIN operacional` en Configuracion solo a usuarios autorizados por rol/permiso/licencia.
- AC-311: La pantalla permite consultar estado del PIN, crear/cambiar PIN de exactamente 6 digitos y limpiar el input despues de guardar.
- AC-312: La pantalla permite desbloquear un PIN bloqueado sin mostrar PIN ni hash.
- AC-313: El request de configuracion envia solo `{ "pin": "123456" }` por `PUT /api/v1/companies/{companyId}/operational-pin`.
- AC-314: Dado el modulo Ventas, cuando se renderiza el formulario, entonces el encabezado no muestra botones de accion y solo existe un boton principal `Cerrar venta`.
- AC-315: Dado que la politica fiscal permite override y existen tipos fiscales alternos, cuando el vendedor abre Ventas, entonces puede solicitar cambio de documento fiscal desde un modal con PIN operacional y motivo.
- AC-316: Dada una venta sin borrador creado, cuando se autoriza el cambio de documento fiscal, entonces la SPA crea primero la venta en borrador y envia el override a `/api/v1/sales/{saleId}/document-type-override`.
- AC-317: Dado un override autorizado sobre una venta en borrador, cuando el usuario cierre la venta, entonces la SPA confirma ese mismo `saleId` en lugar de crear una venta nueva.
- AC-318: Dado el reporte `SALES_BY_PRODUCT`, cuando se consulte con `from`, `to` y `productId` opcional, entonces `billing-service` debe responder `200` con ventas filtradas por producto o lista vacia, sin `500` por parametros nulos de PostgreSQL.
- AC-319: Dado el reporte `SALES_BY_SELLER`, cuando se consulte con `sellerId` opcional, entonces `billing-service` debe filtrar por vendedor sin afectar otros filtros de fecha, estado, cliente, metodo de pago o estado documental.
- AC-320: Dado el modulo Ventas, cuando se escanee un codigo de barras valido, entonces la SPA debe agregar o incrementar la linea y no debe abrir modal de exito.
- AC-321: Dado el modulo Ventas, cuando el scanner agregue una linea, entonces el campo de scanner queda vacio y enfocado para el siguiente codigo.
- AC-322: Dado el modulo Ventas, entonces no debe existir boton manual `Agregar linea`; las lineas se originan por scanner o busqueda de producto aprobada.
- AC-323: Dado el modal de cambio documental, cuando existan usuarios autorizadores, entonces debe mostrarse un selector/buscador por correo/nombre que envie `authorizedBy` como `userId`; si el actor actual autoriza, el campo puede quedar vacio.

## Reportes normalizados y visualizacion de negocio

- AC-324: Dado un reporte `SALES_BY_PRODUCT` con datos de ventas, cuando el usuario seleccione visualizacion `Barras`, entonces la SPA debe mostrar barras agregadas por producto/servicio y no una tabla cruda de ventas/documentos.
- AC-325: Dado un reporte `SALES_BY_PRODUCT`, cuando se muestre tabla o se exporte, entonces las columnas visibles deben ser funcionales y en espanol: `Producto`, `Cantidad vendida`, `Subtotal`, `IVA`, `Total` y `Ventas` o `Documentos` si aplica.
- AC-326: Dado un reporte `SALES_BY_SELLER` con datos de ventas, cuando el usuario seleccione visualizacion `Barras`, entonces la SPA debe mostrar barras agregadas por vendedor y no registros transaccionales individuales.
- AC-327: Dado cualquier reporte normalizado, cuando se renderice tabla o grafica, entonces no deben aparecer columnas tecnicas no solicitadas como `Company Id`, `Idempotency Key`, `Created By`, rutas anidadas tipo `Electronic Document / ...` ni columnas numeradas como `1 / Id`, `2 / Id`.
- AC-328: Dado un reporte sin datos agregables, cuando el usuario seleccione grafica, entonces debe mostrarse un estado vacio funcional y no una tabla deformada o una grafica con datos incorrectos.
- AC-329: Dado un reporte tabular historico que requiera detalle transaccional, cuando se muestre informacion tecnica necesaria como CUFE/CUDE, entonces debe tener etiqueta funcional en espanol y estar limitado a las columnas aprobadas para ese reporte.
- AC-330: Dado un reporte exportado a CSV o Excel, cuando el archivo se genere, entonces debe usar el mismo dataset normalizado que la UI y no el JSON crudo de microservicios.
- AC-331: Dado el flujo SDD vigente, cuando se implemente normalizacion de reportes, entonces deben existir pruebas backend y frontend que validen columnas, series graficas, ausencia de campos tecnicos y compatibilidad con exportacion.

## Bugs fiscales, compras y contabilidad diaria

- AC-332: Dada una resolucion fiscal no usada, cuando ROOT o un administrador autorizado solicite eliminarla desde UI/API, entonces se elimina fisicamente y queda auditoria de la accion.
- AC-333: Dada una resolucion fiscal usada por documentos, cuando se solicite quitarla de operacion, entonces el backend debe rechazar eliminacion fisica y permitir inactivacion conservando historial.
- AC-334: Dada una resolucion fiscal con error operativo, cuando quede inactiva, entonces no puede ser seleccionada por el cierre de venta FE/POS.
- AC-335: Dado el modulo `Compras`, cuando existan compras de la empresa activa, entonces la SPA debe cargar automaticamente la tabla de compras reales desde `/api/v1/purchases`; si no existen, debe mostrar estado vacio funcional.
- AC-336: Dado un pago diario a empleado/jornalero, cuando se confirme, entonces debe generar asiento contable como egreso/gasto operacional mediante regla `DAILY_PAYROLL_PAID`.
- AC-337: Dado que falte la regla contable de pago diario, cuando se intente confirmar el pago, entonces el backend debe responder error funcional indicando configurar contabilidad.
- AC-338: Dado el reporte diario de ganancias y gastos, cuando se consulte una fecha, entonces debe mostrar ingresos por ventas, costos de venta, gastos operativos, pagos diarios y utilidad/perdida neta.
- AC-339: Dado un reporte diario sin informacion, cuando se consulte, entonces debe responder ceros funcionales y no error interno.

## Separacion financiera operativa

- AC-340: Dada una compra documental, cuando se confirme, entonces no incrementa stock y registra asiento contable de compra/proveedor o caja/banco mediante `PURCHASE_CONFIRMED`.
- AC-341: Dada una compra clasificada como activo, cuando se confirme, entonces no incrementa stock vendible y registra activo/caja, banco o cuenta por pagar.
- AC-342: Dado un gasto operativo, cuando se confirme, entonces no incrementa stock y registra gasto por valor total no discriminado, caja/banco o cuenta por pagar.
- AC-343: Dado un deudor/cuenta por cobrar, cuando se registre una obligacion y abonos, entonces el sistema mantiene saldo pendiente, estado y asientos contables correspondientes.
- AC-344: Dadas compras, gastos, deudores y pagos diarios, cuando se consulten reportes, entonces todos los resultados deben estar aislados por empresa, permisos y licencia.

## UX operativa y carga automatica

- AC-345: La barra lateral muestra como entradas principales `Ventas`, `Reportes`, `Contabilidad` y `Configuracion`; los submodulos no quedan desplegados permanentemente hacia abajo.
- AC-346: Al pasar el mouse o enfocar con teclado un menu con submodulos, se muestra un menu flotante lateral con opciones autorizadas por licencia, rol y permisos.
- AC-347: La opcion activa conserva resaltado visible tanto en el menu principal como en el submodulo seleccionado.
- AC-348: Al abrir `Terceros`, `Inventario`, `Compras`, `Gastos`, `Deudores`, `Registro de Ventas` o `Nomina`, la SPA carga automaticamente datos de tabla y listas requeridas sin exigir botones `Consultar` o `Cargar`.
- AC-349: Las tablas historicas usan por defecto rango desde ayer hasta hoy; las listas desplegables cargan todos los registros activos requeridos para operar.
- AC-350: Los botones manuales de consulta/carga usados como prerequisito se eliminan de los modulos indicados y las pruebas frontend validan la carga por entrada a modulo.
- AC-351: Dado el modulo `Compras`, cuando se cree o confirme una compra/factura de proveedor, entonces no debe crear movimientos `PURCHASE_IN` ni aumentar stock.
- AC-352: Dado el formulario de `Compras`, cuando el usuario registre una factura, entonces captura conceptos libres y no selecciona `Producto/Insumo`.
- AC-353: Dado el formulario de `Inventario`, cuando se seleccione uso de item, entonces no se ofrece la opcion `Compra sin inventario`; los egresos sin stock se registran en `Gastos` o `Compras` segun corresponda.
- AC-354: Dado un tercero creado correctamente, cuando el backend responda OK, entonces el formulario se limpia para capturar otro cliente/proveedor sin arrastrar datos.
- AC-355: Dado un menu principal con flyout, cuando el usuario mueva el mouse hacia una opcion, entonces el menu no debe cerrarse por el titulo o por un espacio entre barra y panel.
- AC-356: Dado el modulo `Compras`, cuando el BFF autoriza lectura o escritura, entonces acepta permisos funcionales de compras/contabilidad y no exige permiso de inventario.

## Gobierno documental del repositorio

- AC-357: Dado el README del repositorio, cuando se revise su contenido, entonces debe servir como guia practica de instalacion, ejecucion, despliegue local, pruebas, SonarQube, Swagger, seguridad y estructura tecnica, sin mezclar planeacion funcional.
- AC-358: Dado el flujo SDD, cuando se actualice documentacion transversal, entonces requisitos, diseno, criterios, infraestructura, diccionario, arquitectura, diagramas y casos de uso deben quedar sin numeraciones duplicadas, estados contradictorios o modelos desalineados con el comportamiento vigente.

## Fase 34: Inventario editable, evidencias documentales y QR fiscal

- AC-359: Dado un producto activo o inactivo de la empresa, cuando el usuario autorizado haga clic en `Actualizar`, entonces el formulario de inventario debe cargarse con sus datos y el boton principal debe cambiar a `Actualizar item`.
- AC-360: Dado un codigo de barras existente, cuando se digite o escanee en el formulario de inventario, entonces la SPA debe cargar el producto relacionado y pasar a modo actualizacion sin crear duplicados.
- AC-361: Dado un producto usado historicamente, cuando se inactive, entonces no debe aparecer como opcion activa en ventas nuevas, pero los reportes y documentos historicos deben seguir mostrando nombre, SKU, impuestos y totales sin error.
- AC-362: Dado un producto sin uso bloqueante, cuando se actualice, entonces el backend debe conservar trazabilidad, validar unicidad de SKU/codigo de barras por empresa y no alterar movimientos historicos de stock.
- AC-363: Dado el modulo `Compras`, cuando se registre una factura de proveedor, entonces el formulario no debe solicitar cantidad, costo unitario, subtotal ni IVA; solo debe capturar proveedor, fecha, concepto, condicion de pago, vencimiento cuando aplique, total y evidencia opcional.
- AC-364: Dado el modulo `Gastos`, cuando se registre un egreso, entonces el formulario no debe solicitar subtotal ni IVA; solo debe capturar concepto, tipo de gasto/activo cuando aplique, proveedor opcional, fecha, condicion de pago, vencimiento cuando aplique, total y evidencia opcional.
- AC-365: Dado que el usuario seleccione evidencia `PDF`, cuando elija archivo, entonces la UI debe aceptar un solo PDF y el backend debe rechazar archivos no PDF, multiples archivos o archivos fuera de limite con error funcional.
- AC-366: Dado que el usuario seleccione evidencia `URL`, cuando capture soporte, entonces la UI y el backend deben validar URL `http/https`; si el usuario no selecciona evidencia, el soporte queda nulo sin error.
- AC-367: Dado un archivo empresarial subido correctamente, cuando se almacene, entonces debe quedar bajo prefijo/carpeta de empresa y categoria funcional de negocio, con metadata, hash, tamano, tipo MIME, usuario, fecha y referencia privada.
- AC-368: Dado ambiente local, cuando se suba el primer archivo de una empresa, entonces el storage de desarrollo debe crear la carpeta/prefijo de la empresa y las subcarpetas funcionales necesarias sin requerir Terraform.
- AC-369: Dado ambiente productivo AWS, cuando se almacene evidencia o asset empresarial, entonces debe usarse S3 privado cifrado con KMS o equivalente aprobado; el navegador no debe recibir bucket, key interna ni credenciales.
- AC-370: Dado un deudor/cuenta por cobrar con datos validos, cuando se registre, entonces el backend debe responder exito y crear saldo inicial/asiento segun regla contable vigente.
- AC-371: Dado un intento invalido de crear deudor, cuando falte tercero, monto, fuente o regla contable, entonces el backend debe responder `400/409` funcional y nunca `500 INTERNAL_ERROR`.
- AC-372: Dado un comprobante POS mock, cuando se renderice para impresion, entonces debe mostrar un QR grafico escaneable apuntando a una URL interna parametrizada del comprobante.
- AC-373: Dado un documento fiscal enviado en modo real DIAN, cuando DIAN retorne contenido o URL QR, entonces el comprobante debe usar ese valor sin reemplazarlo por el QR mock.
- AC-374: Dado que `APP_PUBLIC_BASE_URL` o parametro equivalente no este configurado en ambiente desplegado, entonces el servicio debe fallar cerrado para QR/link publico y mostrar error funcional de configuracion.
- AC-375: Dado el modulo de marca empresarial, cuando el usuario configure colores, entonces debe ver explicacion de efecto y seleccionar color principal/acento con controles de color, conservando validacion hexadecimal en backend.
- AC-376: Dada cualquier mutacion de producto, evidencia, archivo empresarial, QR/comprobante o branding, entonces debe existir auditoria sin contenido binario, secretos, PIN, credenciales ni URLs privadas persistentes.
- AC-377: Dada una empresa con contabilidad basica inicializada antes de una nueva plantilla, cuando se ejecute nuevamente `Completar plantilla basica`, entonces se crean solo las cuentas/reglas faltantes y se conservan reglas activas existentes.
- AC-378: Dado un deudor manual con datos validos pero sin regla `ACCOUNT_RECEIVABLE_REGISTERED`, cuando se intente registrar, entonces el backend responde `400 BUSINESS_RULE_VIOLATION` con mensaje de regla faltante para cuenta por cobrar, no con mensaje de cerrar venta ni `500`.

## Fase 35: Mejoras priorizadas para salida comercial

- AC-379: Dada una empresa nueva o incompleta, cuando ROOT o un administrador abra el asistente de puesta en marcha, entonces debe ver cada prerequisito operativo con estado, descripcion, responsable, accion sugerida y bloqueo asociado.
- AC-380: Dada una empresa sin configuracion minima para vender o facturar, cuando intente ejecutar una operacion dependiente, entonces el sistema debe mostrar bloqueo funcional guiado y no un error generico.
- AC-381: Dado ambiente productivo, cuando arranquen BFF y microservicios, entonces debe quedar deshabilitada cualquier autenticacion dummy, usar cookies/CSRF/headers seguros y rechazar configuraciones sin secretos obligatorios.
- AC-382: Dada una empresa con DIAN en modo real, cuando emita un documento fiscal, entonces debe generar XML firmado, CUFE/CUDE, QR, registrar ApplicationResponse, persistir artefactos y auditar resultado sin repetir efectos de negocio.
- AC-383: Dada una empresa con contabilidad incompleta, cuando abra configuracion contable, entonces debe ver faltantes por modulo y poder completarlos antes de operar ventas, compras, gastos, nomina, deudores o pagos.
- AC-384: Dado el modulo de reportes, cuando el usuario seleccione un reporte gerencial, entonces las columnas, graficas y exportaciones deben salir normalizadas en espanol y orientadas a decision de negocio, no como JSON tecnico.
- AC-385: Dada una accion mutable, error funcional o intento denegado, cuando se consulte auditoria operativa, entonces debe poder filtrarse por empresa, usuario, modulo, resultado, correlation ID y rango de fechas sin exponer secretos.
- AC-386: Dado el pipeline de calidad, cuando se ejecute en local o CI, entonces debe correr pruebas relevantes, cobertura, SonarQube, validacion Docker, migraciones Flyway y revision basica de secretos/dependencias con resultado reproducible.
- AC-387: Dado un punto de venta con impresora termica soportada, cuando se cierre o reimprima una venta, entonces el comprobante debe imprimirse en formato 58/80 mm con QR fiscal y auditoria de impresion/reimpresion.
- AC-388: Dadas cuentas por cobrar, pagar, gastos, compras, pagos diarios y ventas, cuando el administrador consulte gestion financiera diaria, entonces debe ver vencimientos, saldos, alertas y utilidad/perdida esperada.
- AC-389: Dado un archivo empresarial subido en local o produccion, cuando se almacene o descargue, entonces debe usar prefijos por empresa/categoria, cifrado productivo, metadata auditable y links controlados sin exponer credenciales ni rutas privadas.
- AC-390: Dado cualquier microservicio desplegado, cuando se consulte monitoreo, entonces debe exponer health liveness/readiness, metricas y logs correlacionables; las alertas deben identificar degradacion de DIAN, storage, jobs y errores funcionales recurrentes.
- AC-400: Dado ROOT en el modulo `Licencias`, cuando abra `Tipo de licencia`, entonces no debe aparecer la opcion `Basico`.
- AC-401: Dado ROOT en el modulo `Licencias`, cuando seleccione `Completo`, entonces todos los modulos licenciables quedan seleccionados y la seleccion manual de checks queda bloqueada por ser un preset.
- AC-402: Dado ROOT en el modulo `Licencias`, cuando seleccione `POS y facturacion`, entonces quedan seleccionados solamente `COMPANY`, `INVENTORY`, `BILLING`, `REPORTS`, `THIRDPARTY`, `ACCOUNTING` y `USERS`.
- AC-403: Dado ROOT en el modulo `Licencias`, cuando seleccione `Personalizado`, entonces puede marcar o desmarcar manualmente los modulos que quiere incluir en la licencia.

## Fase 36: Modulo de contadores, reglas fiscales y notificaciones

- AC-404: Dado un usuario contador autenticado, cuando abra el portal `Contadores`, entonces solo debe ver empresas con asociacion activa a su usuario y nunca empresas no vinculadas.
- AC-405: Dada una empresa con contador activo, cuando ROOT intente asociar otro contador activo a la misma empresa, entonces el backend debe rechazar la operacion con error funcional o exigir reemplazo explicito auditado.
- AC-406: Dado ROOT, cuando asocie un contador a una empresa, entonces la asociacion debe quedar auditada con contador, empresa, actor, estado, fecha y correlation ID.
- AC-407: Dado un contador vinculado a varias empresas, cuando seleccione una empresa en su portal, entonces puede consultar ventas, compras, gastos, nomina, ingresos, egresos, cartera, cuentas por pagar y reportes contables solo de esa empresa.
- AC-408: Dado un contador sin permiso delegado de escritura, cuando intente crear, modificar, anular, emitir o configurar informacion de una empresa vinculada, entonces el sistema debe rechazar la accion y conservar auditoria de denegacion.
- AC-409: Dado ROOT creando credenciales de contador o administrador empresarial, cuando se genere contrasena temporal, entonces el usuario debe quedar con `passwordChangeRequired=true`, expiracion configurada y acceso funcional bloqueado hasta cambiarla.
- AC-410: Dado un usuario con contrasena temporal vigente, cuando inicie sesion, entonces debe poder autenticarse solo para completar cambio de clave y no para operar modulos antes del cambio exitoso.
- AC-411: Dado que ROOT cree credenciales temporales, cuando la transaccion finalice, entonces se debe emitir evento de notificacion y enviar correo al usuario con instrucciones, vigencia y obligatoriedad de cambio de clave.
- AC-412: Dado el formulario de cliente/proveedor, cuando se registre o actualice un tercero proveedor, entonces debe permitir capturar `ciiuCode` valido desde catalogo CIIU y persistirlo junto con municipio, regimen y responsabilidades fiscales existentes.
- AC-413: Dado un proveedor con regimen, responsabilidades, municipio y CIIU registrados, cuando se cree una compra o gasto sujeto a retenciones, entonces el backend debe calcular retenciones usando reglas versionadas y devolver desglose por concepto, base, tarifa, valor y regla aplicada.
- AC-414: Dada una regla especial por regimen SIMPLE, no responsable, autorretenedor, gran contribuyente u otra responsabilidad fiscal, cuando aplique a la operacion, entonces el motor debe excluir, ajustar o marcar la retencion segun la regla vigente y explicar la decision funcionalmente.
- AC-415: Dada una compra o gasto con retenciones calculadas, cuando se confirme, entonces debe conservar valor bruto, impuestos, retenciones, valor neto a pagar, tercero, municipio, CIIU, version de regla y asiento contable asociado.
- AC-416: Dada una compra, gasto o pago que requiere retencion pero carece de configuracion fiscal/contable obligatoria, cuando se confirme, entonces debe responder `400/409` funcional y no generar asiento parcial ni cuenta por pagar inconsistente.
- AC-417: Dado un producto con umbral de desabastecimiento configurado, cuando un movimiento o venta deje su stock igual o menor al umbral, entonces el sistema debe emitir notificacion de inventario bajo por correo sin duplicarla dentro de la ventana configurada.
- AC-418: Dado un reporte pesado solicitado con notificacion por correo, cuando el job pase a `READY`, entonces el sistema debe enviar correo con link intermediado de descarga y auditar intento/resultado sin exponer URL directa de storage.
- AC-419: Dada cualquier notificacion por correo, cuando falle el proveedor SMTP/SES o equivalente, entonces el sistema debe registrar error sanitizado, permitir reintento controlado y no revertir el hecho de negocio ya confirmado salvo que la notificacion sea requisito explicito de seguridad.
- AC-420: Dada una operacion fechada en 2026 con base expresada en UVT, cuando el motor la evalua, entonces usa la UVT 2026 de `$52.374` y conserva el parametro/version usados en el resultado.
- AC-421: Dadas varias reglas compatibles del mismo tipo, cuando se calcula una retencion, entonces solo se aplica la regla activa mas especifica y prioritaria; no se duplican valores por coincidencias globales y empresariales.
- AC-422: Dada una exencion o exclusion vigente, cuando coincide con empresa, tercero, concepto y tipo, entonces prevalece sobre la tarifa y devuelve `EXEMPT` o `NOT_APPLIED` con fuente y razon.
- AC-423: Dada una empresa autorretenedora, cuando se calcula autorretencion, entonces la tarifa se resuelve por el CIIU propio y la vigencia de la tabla del Decreto 572 de 2025, no por el CIIU del proveedor.
- AC-424: Dada una empresa obligada a practicar ReteICA, cuando no existe catalogo publicado para el municipio y fecha, entonces el resultado es `BLOCKED` y no se genera valor ni asiento parcial.
- AC-425: Dada una regla publicada, cuando ROOT necesite cambiar tarifa, base o condiciones, entonces crea una nueva version o cierra la vigencia anterior; el historial aplicado permanece inmutable.
- AC-426: Dado un usuario sin alcance ROOT ni permiso contable de empresa, cuando intente mutar catalogos fiscales, entonces la API rechaza la accion y no filtra informacion de otras empresas.

## CIIU multiactividad y vencimientos de credito

- AC-427: Dado un tercero persona juridica o con rol proveedor, cuando se registre, entonces puede seleccionar uno o varios codigos CIIU desde una lista dual con busqueda y la API conserva todos los codigos.
- AC-428: Dado un tercero cuyo unico rol es cliente y cuya persona es natural, cuando se cree o actualice, entonces la UI no muestra CIIU y el backend persiste una coleccion vacia aunque llegue un valor residual.
- AC-429: Dado un tercero historico con `ciiu_code`, cuando se aplique la migracion, entonces ese codigo queda disponible en `ciiuCodes` sin perdida y la respuesta mantiene temporalmente `ciiuCode` como actividad principal compatible.
- AC-430: Dada una regla fiscal condicionada por CIIU, cuando cualquiera de los codigos del tercero coincide, entonces la regla es candidata; si ninguno coincide, no aplica.
- AC-431: Dada una compra o gasto de contado, cuando se edite el formulario, entonces no aparece fecha limite de pago y el payload la envia nula; al elegir credito aparece `Fecha limite de pago` y el backend conserva su validacion obligatoria.
- AC-432: Dado un usuario autorizado, cuando abra `Configuracion`, entonces encuentra `Catalogos` con la clasificacion CIIU oficial y `Reglas fiscales` como opcion hermana.

## Integracion operativa del motor fiscal

- AC-433: Dada una empresa, cuando su administrador configure el perfil fiscal, entonces regimen, responsabilidades, agentes de retencion, municipio ICA y multiples CIIU quedan persistidos y vuelven a cargarse al abrir la configuracion.
- AC-434: Dada una compra o gasto pendiente con proveedor, concepto fiscal, subtotal e IVA validos, cuando el usuario solicite calcular retenciones, entonces ve el desglose autoritativo sin confirmar el documento ni generar asiento o cuenta por pagar.
- AC-435: Dado un proveedor registrado con regimen, responsabilidades, municipio y multiples CIIU, cuando se calcule o confirme una operacion, entonces el backend consulta ese perfil persistido y aplica la regla mas especifica vigente.
- AC-436: Dada una operacion cuyo calculo contiene `BLOCKED`, cuando el usuario intente confirmarla, entonces la API rechaza la confirmacion y el documento permanece pendiente sin efectos contables parciales.
- AC-437: Dada una operacion sin bloqueos, cuando se confirme, entonces persiste snapshots por tipo de retencion, genera asiento con ReteFuente/ReteIVA/ReteICA y crea cuenta por pagar por `netPayable` cuando sea a credito.
- AC-438: Dada una compra o gasto confirmado, cuando el usuario abra su detalle fiscal despues de recargar la SPA, entonces ve importes, decisiones y evidencia normativa del calculo guardado.
- AC-439: Dado un cliente que intente enviar retenciones calculadas o modificar el neto, cuando confirme, entonces el backend ignora esos valores y recalcula desde datos persistidos.
- AC-440: Dado un documento con `subtotal + taxTotal != total`, proveedor ausente o inactivo, cuando se calcule o confirme, entonces recibe error funcional y no se crean efectos financieros.
- AC-441: Dado ROOT sin empresa activa, cuando abra el formulario de empresa, entonces ve regimen, responsabilidades RUT, CIIU y los indicadores de IVA, retefuente, reteIVA, reteICA, gran contribuyente y autorretencion.
- AC-442: Dado ROOT creando una empresa, cuando guarde datos generales y un regimen valido, entonces empresa y perfil fiscal se persisten en una unica transaccion; si el perfil es invalido no queda una empresa parcial.
- AC-443: Dado ROOT editando una empresa distinta de la activa, cuando pulse `Actualizar`, entonces se carga exclusivamente el perfil fiscal de esa empresa y el envio actualiza ambos agregados sin mezclar estados.
- AC-444: Dada una compra o gasto, cuando se presenten retenciones, entonces sus valores son resultados de solo lectura calculados por el backend y no campos editables del documento.

## Catalogo fiscal controlado y soportes de exencion

- AC-445: Dado el formulario de regla fiscal, cuando se capture una condicion por municipio, CIIU, regimen, responsabilidad o concepto, entonces el valor se selecciona desde el catalogo vigente y puede dejarse sin restriccion sin seleccionar implicitamente el primer item.
- AC-446: Dada una regla empresarial, cuando se seleccione un tercero especifico, entonces el selector solo muestra terceros activos de la empresa activa y envia su UUID sin exponerlo como dato editable.
- AC-447: Dada una exencion dirigida a un tercero, cuando se publique, entonces exige un PDF de maximo 5 MB, valida extension, MIME, firma PDF y contenido inseguro, y conserva una referencia privada al archivo empresarial.
- AC-448: Dado un archivo de soporte fiscal, cuando se almacene o descargue, entonces queda aislado por empresa, registra nombre, tamano, hash, usuario y fecha, y no expone bucket, key ni URL publica permanente.
- AC-449: Dado ROOT sin empresa activa, cuando la regla no sea nacional global, entonces no puede publicarla; una regla nacional global limpia y rechaza tercero y soporte empresarial.
- AC-450: Dado que el usuario cambie la decision desde `EXEMPT` o retire el tercero especifico, entonces el formulario limpia el archivo seleccionado y no envia una evidencia incompatible.
- AC-451: Dada una regla que exige que la empresa sea responsable de IVA, cuando se publique, entonces el formulario envia `requiresCompanyVatResponsible=true` y el motor la filtra contra el perfil fiscal empresarial.

## Permisos y experiencia de configuracion fiscal

- AC-452: Dado un selector fiscal con busqueda, cuando el usuario lo abre, entonces la busqueda aparece dentro del desplegable y permite filtrar y seleccionar con mouse o teclado sin renderizar otro campo externo.
- AC-453: Dada una empresa activa con proveedores activos, cuando un usuario autorizado abre `Reglas fiscales`, entonces `Tercero exento` permite encontrarlos por nombre o documento; si la carga falla o no hay resultados muestra un estado controlado.
- AC-454: Dado un usuario sin `FISCAL_SETTINGS_MANAGE`, cuando consulta o intenta mutar configuracion fiscal, entonces la SPA oculta o bloquea las opciones y el BFF rechaza la operacion, aunque el usuario tenga `ACCOUNTING_MANAGE` o un nombre de rol administrativo.
- AC-455: Dado un rol personalizado con `FISCAL_SETTINGS_MANAGE`, cuando opera sobre su empresa activa, entonces puede administrar perfil y reglas fiscales, soportes, emisor, politica, resoluciones y conexion DIAN, pero no emitir documentos sin `FISCAL_DOCUMENTS_ISSUE`.
- AC-456: Dado el administrador inicial creado por ROOT, cuando se materializa su rol OWNER, entonces recibe todos los permisos empresariales vigentes; una migracion agrega `FISCAL_SETTINGS_MANAGE` a roles OWNER existentes y nunca agrega permisos globales.
- AC-457: Dado un OWNER con `COMPANY_SETTINGS_MANAGE`, cuando actualiza datos generales de su empresa activa, entonces el BFF permite la operacion; si intenta actualizar otra empresa, la autorizacion por contexto la rechaza.
- AC-458: Dado el menu lateral, cuando se renderiza la configuracion, entonces muestra `Reglas fiscales` y `Configuracion contable` dentro del grupo `Configuracion`, sin mostrar el nombre anterior `Catalogo fiscal`.

## Licencias por capacidades

- AC-459: Dado ROOT administrando una licencia, cuando selecciona `POS y facturacion`, entonces backend y SPA establecen exactamente los modulos y capacidades basicas aprobados y bloquean su edicion manual.
- AC-460: Dada una empresa POS, cuando inicia sesion, entonces puede vender, administrar clientes/productos, emitir documentos configurados y usar reportes sincronicos, pero no ve contabilidad avanzada, proveedores, compras, gastos, deudores, nomina, reglas fiscales avanzadas ni jobs de reporte.
- AC-461: Dada una licencia POS, cuando se intenta invocar directamente una capacidad avanzada, entonces el backend responde licencia no incluida sin ejecutar efectos de negocio.
- AC-462: Dado ROOT seleccionando `Completo`, cuando guarda la licencia, entonces se habilitan todos los modulos y capacidades estandar implementados y los checks del preset permanecen bloqueados.
- AC-463: Dado ROOT seleccionando `Personalizable`, cuando edita la licencia, entonces puede activar o desactivar modulos y capacidades compatibles y el backend rechaza combinaciones incoherentes.
- AC-464: Dada una venta POS con licencia POS, cuando se confirma, entonces la contabilizacion automatica interna permanece disponible aunque las pantallas contables avanzadas no esten licenciadas.
- AC-465: Dado un reporte con licencia POS, cuando se consulta o exporta de forma sincrona funciona; cuando se solicita un job asincrono, el backend lo bloquea por falta de `REPORTS_ASYNC`.
- AC-466: Dada una licencia preexistente al aplicar la migracion, cuando se consulta, entonces conserva acceso equivalente mediante un `planCode`, `enabledModules` y `enabledFeatures` normalizados.
- AC-467: Dado un usuario empresarial con permisos RBAC pero sin capacidad licenciada, cuando intenta operar, entonces prevalece el bloqueo de licencia; ROOT conserva sus funciones administrativas globales.
- AC-468: Dada la respuesta de licencia, cuando la SPA construye navegacion y controles, entonces deriva visibilidad desde `enabledFeatures` sin duplicar estado local mutable.

## Contabilidad basica automatica para POS

- AC-469: Dada una empresa con licencia `POS` y sin regla activa `SALE_CONFIRMED`, cuando confirma su primera venta, entonces la plataforma inicializa la plantilla contable basica, vuelve a validar la regla y permite continuar sin intervencion manual.
- AC-470: Dada una empresa POS cuya configuracion contable basica ya existe, cuando confirma una venta, entonces no se vuelve a inicializar ni se reemplazan sus reglas activas.
- AC-471: Dada una licencia `FULL` o `CUSTOM` sin configuracion contable, cuando intenta confirmar una venta, entonces conserva el error funcional de configuracion requerida y no recibe una plantilla automatica.
- AC-472: Dado un fallo del servicio contable o una inicializacion que no produce `SALE_CONFIRMED`, cuando se confirma una venta POS, entonces no se asigna numeracion, no se invoca DIAN, no se descuenta inventario, no se registra asiento y la venta permanece sin confirmar.

## Carga automatica de licencia ROOT

- AC-473: Dado ROOT en `Licencias`, cuando selecciona una empresa con licencia configurada, entonces el formulario completa automaticamente plan, vigencia, limites, modulos y funcionalidades.
- AC-474: Dada una licencia cargada automaticamente, cuando termina la consulta, entonces el resumen muestra su estado y el consumo comercial corresponde a la misma empresa.
- AC-475: Dado que ROOT cambia a una empresa sin licencia, cuando el backend responde `404`, entonces se limpian licencia, uso y selecciones anteriores, se conservan valores iniciales para crearla y no se muestra un error tecnico.
- AC-476: Dadas dos selecciones consecutivas, cuando la respuesta de la primera llega despues, entonces no puede sobrescribir el formulario ni el uso de la empresa seleccionada actualmente.

## Clasificacion de obligacion de facturar

- AC-477: Dado ROOT creando una empresa, cuando completa la configuracion inicial, entonces registra persona, RUT, regimen, responsabilidades, CIIU y cuestionario complementario sin encontrar un selector editable para el resultado de obligacion.
- AC-478: Dado un RUT con responsabilidad `52`, cuando se clasifica la empresa, entonces el resultado es `OBLIGATED` o `VOLUNTARY_ELECTRONIC` segun la evidencia de habilitacion y `NON_FISCAL_SALE` queda bloqueado.
- AC-479: Dada una empresa SIMPLE, responsable de IVA o responsable de impuesto nacional al consumo, cuando se clasifica, entonces cualquier marca de no responsable o declaracion manual contradictoria no evita `OBLIGATED`.
- AC-480: Dada una persona juridica que vende bienes o presta servicios, cuando no existe una excepcion legal por entidad y operacion validada, entonces el resultado es `OBLIGATED` aunque figure como no responsable de IVA.
- AC-481: Dada una persona natural con responsabilidad `49`, cuando falta una sola condicion del articulo 437 o existe otra causal de obligacion, entonces no obtiene `NOT_OBLIGATED_VERIFIED`.
- AC-482: Dada una persona natural con responsabilidad `49` y todas las condiciones vigentes del articulo 437 acreditadas, cuando no existe causal prevalente, entonces puede obtener `NOT_OBLIGATED_VERIFIED` con motivos y evidencia.
- AC-483: Dada una persona natural de restaurante o bar con responsabilidad `50`, cuando sus ingresos del periodo anterior son inferiores a 3.500 UVT, tiene maximo un establecimiento y no existe causal prevalente, entonces la excepcion solo aplica al alcance de esa actividad.
- AC-484: Dada una persona natural que declara vender exclusivamente bienes excluidos o servicios no gravados, cuando sus ingresos alcanzan 3.500 UVT o aparece una operacion gravada, entonces deja de cumplir la excepcion y se bloquean nuevas ventas no fiscales.
- AC-485: Dado un banco, cooperativa, transporte publico urbano u otra excepcion especial, cuando tambien realiza operaciones no cubiertas, entonces la clasificacion conserva el alcance por operacion y no habilita una excepcion general.
- AC-486: Dado un dato requerido ausente, contradictorio, evidencia vencida o RUT no revisado, cuando se calcula la clasificacion, entonces queda `REVIEW_REQUIRED` y no puede configurarse ni confirmarse `NON_FISCAL_SALE`.
- AC-487: Dado un RUT PDF cargado durante la creacion, cuando se guarda, entonces queda privado, versionado, asociado a la empresa y protegido con hash; no se requiere OCR para completar el alta inicial.
- AC-488: Dado un usuario que modifica datos fuente, cuando guarda, entonces backend crea un snapshot nuevo, recalcula y conserva el resultado anterior con actor, instante, motivos y version normativa.
- AC-489: Dada una politica u override hacia `NON_FISCAL_SALE`, cuando la clasificacion vigente no es `NOT_OBLIGATED_VERIFIED`, entonces backend rechaza la solicitud aunque el PIN sea valido o el frontend sea omitido.
- AC-490: Dada una empresa `NOT_OBLIGATED_VERIFIED`, cuando confirma una venta interna, entonces no exige emisor ni resolucion y no genera documento electronico, CUFE, CUDE o QR DIAN.
- AC-491: Dado el comprobante interno de una venta no fiscal, cuando se imprime, entonces muestra `NO ES FACTURA DE VENTA NI DOCUMENTO EQUIVALENTE` y `NO VALIDO COMO SOPORTE FISCAL` de forma visible.
- AC-492: Dada una clasificacion no obligada, cuando se consulta readiness, entonces emisor y resolucion aparecen `NOT_APPLICABLE`; para los demas estados conservan el bloqueo correspondiente.
- AC-493: Dado que ingresos u otra condicion operacional cruza un umbral vigente, cuando el sistema reevalua, entonces registra `TRANSITION_TO_OBLIGATED`, bloquea nuevas ventas no fiscales y no modifica documentos historicos.
- AC-494: Dadas responsabilidades RUT `O-07`, `O-13`, `O-15`, `O-23`, `O-47`, `O-48` u `O-59`, cuando se guarda el perfil, entonces backend deriva los indicadores nacionales correspondientes e ignora booleanos enviados en contradiccion.
- AC-495: Dada una designacion municipal de ReteICA, cuando se guarda el perfil, entonces se conserva como el unico indicador editable separado de las responsabilidades nacionales y la interfaz explica su alcance municipal.
- AC-496: Dadas responsabilidades `O-48` y `O-49`, o `R-99-PN` junto con otra responsabilidad, cuando se intenta guardar, entonces backend rechaza el perfil sin persistirlo.
- AC-497: Dada una excepcion especial seleccionada sin verificacion dedicada, cuando se clasifica la obligacion, entonces el resultado es `REVIEW_REQUIRED` y no habilita `NON_FISCAL_SALE`.
- AC-498: Dado un PDF real de RUT o soporte fiscal permitido, cuando se carga dentro de los limites, entonces la restriccion de persistencia admite su categoria; un archivo renombrado sin firma PDF continua siendo rechazado.
- AC-499: Dada la pantalla de reglas fiscales, cuando se definen requisitos de una regla, entonces los textos indican que se evalua la empresa activa y no el tercero; las condiciones del tercero permanecen en sus campos propios.
- AC-500: Dado un PDF RUT valido menor de 5 MB enviado por la SPA, cuando atraviesa el BFF, entonces el tenant recibe exactamente el cuerpo multipart y su boundary y responde `201` sin corrupcion del archivo.

## Culminacion del motor fiscal colombiano

- AC-501: Dadas dos versiones normativas con periodos distintos, cuando se calculan operaciones en cada fecha, entonces cada una usa exclusivamente la version efectiva y conserva su fuente en el snapshot.
- AC-502: Dada una norma suspendida judicialmente, cuando la fecha de operacion cae dentro de la suspension, entonces ninguna regla dependiente de sus articulos se selecciona aunque figure publicada en base de datos.
- AC-503: Dado el Decreto 572 de 2025 y una fecha posterior al 8 de mayo de 2026 sin reactivacion oficial registrada, cuando se intenta publicar o aplicar sus articulos 2 a 8, entonces el sistema lo bloquea como vigencia no verificada.
- AC-504: Dado un cambio normativo posterior, cuando se consulta un documento confirmado anteriormente, entonces importes, motivos y version permanecen iguales.
- AC-505: Dada una empresa que no es agente del tipo evaluado, cuando registra una compra, entonces el motor no aplica esa retencion y explica la calidad faltante.
- AC-506: Dado un proveedor SIMPLE, cuando se evalua una compra, entonces cada impuesto aplica su propia regla y la exclusion de renta no elimina automaticamente ReteIVA, ICA u obligaciones laborales.
- AC-507: Dado un proveedor autorretenedor, cuando se evalua retefuente, entonces solo se omite la retencion del comprador si una regla vigente demuestra que la designacion cubre ese impuesto y concepto.
- AC-508: Dado un documento con lineas de compra general, servicio y honorarios, cuando se calcula, entonces cada linea obtiene concepto, base, tarifa y decision propios y los totales coinciden con su suma.
- AC-509: Dadas operaciones cuya norma exige acumulacion diaria con el mismo proveedor, cuando se registra una nueva operacion, entonces el umbral usa las operaciones validas del dia sin duplicar reintentos ni incluir anulaciones.
- AC-510: Dada una base exactamente igual al umbral, cuando la regla usa `GT` o `GTE`, entonces el resultado respeta el operador normativo y cubre ambos limites con pruebas.
- AC-511: Dado IVA cero, proveedor no responsable o concepto excluido, cuando se evalua ReteIVA, entonces no se aplica y se informa la causal concreta; si hay IVA y comprador agente se evalua la regla vigente.
- AC-512: Dada una operacion entre agentes de ReteIVA o con proveedor exterior, cuando se calcula, entonces se usa la excepcion o tarifa especial que corresponda y no la tarifa general por defecto.
- AC-513: Dada una operacion sujeta a ICA, cuando se informa el lugar real de realizacion, entonces se selecciona el paquete de ese municipio aunque difiera del domicilio del proveedor.
- AC-514: Dado un municipio sin paquete vigente, cuando se confirma una operacion que exige ReteICA, entonces queda pendiente y no se crean snapshots, asiento ni cuenta por pagar parciales.
- AC-515: Dadas dos reglas aplicables del mismo tipo, cuando una representa exencion documentada y vigente, entonces prevalece de forma deterministica; al vencer vuelve a evaluarse la regla general.
- AC-516: Dado un dato fiscal obligatorio ausente o contradictorio, cuando se calcula, entonces el resultado es `BLOCKED` e identifica exactamente el dato, fuente o catalogo faltante.
- AC-517: Dada una nota credito o anulacion, cuando afecta una retencion confirmada, entonces se crea un reverso enlazado, se ajustan acumulados y auxiliares y el snapshot original no cambia.
- AC-518: Dado el cierre mensual, cuando se genera el auxiliar nacional, entonces sus bases y valores concilian con los snapshots por concepto y casillas del Formulario 350 sin marcar la declaracion como presentada.
- AC-519: Dado un proveedor con retenciones practicadas, cuando se genera su certificado, entonces incluye periodo, identidades, pagos, conceptos y cuantias trazables a los documentos fuente.
- AC-520: Dada una empresa Grupo 1, 2 o 3, cuando configura contabilidad, entonces el sistema conserva marco, vigencia y politicas sin cambiar tarifas fiscales por el grupo NIIF.
- AC-521: Dada una empresa con plan propio, cuando contabiliza una retencion, entonces utiliza su mapeo aprobado; la ausencia de una cuenta requerida bloquea el asiento sin imponer automaticamente un codigo PUC historico.
- AC-522: Dada una plantilla basada en PUC, cuando se activa, entonces identifica sus codigos como referencia configurable y mantiene un mapeo separado a rubros de estados financieros.
- AC-523: Dado un fallo despues del calculo fiscal durante una confirmacion distribuida, cuando termina la operacion, entonces el documento no aparece confirmado con efectos faltantes y el reintento es idempotente.
- AC-524: Dado un usuario empresarial, cuando intenta publicar una regla nacional o cambiar el estado juridico de una fuente, entonces recibe `403`; ROOT deja auditoria de cualquier publicacion, suspension o reactivacion.
- AC-525: Dado un ROOT que registra manualmente o importa por CSV un paquete ReteICA, cuando se valida antes de publicar, entonces cada codigo municipal existe en DIVIPOLA, sus CIIU, tarifas, vigencias y fuentes son validos, no se crean municipios ni paquetes automaticamente y cualquier fila invalida rechaza atomicamente toda la importacion.
- AC-526: Dada una version normativa proxima a vencer o sin revision vigente, cuando un usuario fiscal abre el panel, entonces recibe una advertencia sin que el sistema cambie reglas silenciosamente.

## Gobierno documental SDD

- AC-527: Dado el repositorio actual, cuando se consulta `sdd-status.md`, entonces cada capacidad principal aparece clasificada como `IMPLEMENTED`, `PARTIAL`, `TARGET`, `HISTORICAL` o `RETIRED` y tiene una referencia verificable.
- AC-528: Dadas las tareas publicadas, cuando se valida su trazabilidad, entonces no existe ninguna referencia RF o AC indefinida y los rangos historicos omitidos se encuentran declarados como reservados.
- AC-529: Dadas arquitectura, infraestructura, contratos, persistencia y diagramas, cuando se comparan con codigo, migraciones y Docker Compose, entonces no presentan componentes objetivo como si estuvieran desplegados.
- AC-530: Dado `tasks.md`, cuando se localiza `Context7 evidence`, entonces no existe ninguna fase ni `TASK-*` despues de esa seccion.
- AC-531: Dada la documentacion operativa, cuando se valida `docker compose config`, entonces README, puertos y variables de `.env.example` corresponden con la configuracion vigente sin contener secretos reales.
- AC-532: Dado el cierre de la auditoria, cuando se ejecutan las validaciones documentales, entonces referencias, formato, enlaces locales y diff no presentan errores conocidos; cualquier prueba no ejecutada queda declarada expresamente.
