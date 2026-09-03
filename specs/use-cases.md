# Use Cases

## UC-001: Configurar emisor

Actor: Administrador.

Flujo principal:
1. El administrador registra datos fiscales del emisor.
2. El sistema valida NIT, razon social, responsabilidades fiscales y datos de contacto.
3. El sistema guarda la configuracion versionada.

Acceptance criteria: AC-001, AC-017, AC-018.

## UC-002: Configurar resolucion de numeracion

Actor: Administrador.

Flujo principal:
1. El administrador registra prefijo, rango, vigencia, tipo de documento y ambiente.
2. El sistema valida que no exista solapamiento de rango activo.
3. El sistema deja la resolucion disponible para emision.

Acceptance criteria: AC-001.

## UC-003: Emitir factura electronica de venta

Actor: Vendedor o sistema POS.

Flujo principal:
1. El actor crea una venta con cliente, productos, cantidades, impuestos y metodo de pago.
2. El sistema valida datos y stock.
3. El sistema calcula totales.
4. El sistema asigna numeracion.
5. El sistema envia mediante la conexion DIAN configurada de la empresa.
6. El sistema registra CUFE, QR, artefactos y estado.
7. El sistema actualiza inventario y contabilidad.

Acceptance criteria: AC-001, AC-002, AC-003, AC-004, AC-010, AC-014.

## UC-004: Emitir POS electronico

Actor: Cajero.

Flujo principal:
1. El cajero registra productos y pago.
2. El sistema valida disponibilidad y datos minimos.
3. El sistema genera documento equivalente electronico POS.
4. El sistema envia mediante la conexion DIAN configurada.
5. El sistema registra CUDE, QR, artefactos y estado.
6. El sistema actualiza inventario y contabilidad.

Acceptance criteria: AC-007, AC-009, AC-010, AC-014.

## UC-005: Generar nota credito o debito

Actor: Administrador o vendedor autorizado.

Flujo principal:
1. El actor selecciona factura validada.
2. El sistema valida que el ajuste sea permitido.
3. El sistema calcula valores de la nota.
4. El sistema envia la nota mediante la conexion DIAN configurada.
5. El sistema registra estado y afecta inventario/contabilidad si corresponde.

Acceptance criteria: AC-005, AC-013, AC-014.

## UC-006: Generar nota de ajuste POS

Actor: Administrador o cajero autorizado.

Flujo principal:
1. El actor selecciona POS electronico.
2. El sistema valida motivo de anulacion o correccion.
3. El sistema genera nota de ajuste.
4. El sistema envia la nota mediante la conexion DIAN configurada.
5. El sistema registra estado y trazabilidad.

Acceptance criteria: AC-008.

## UC-007: Registrar entrada fisica de inventario

Actor: Administrador.

Flujo principal:
1. El actor registra producto o insumo, cantidad, costo y documento soporte cuando aplique.
2. El sistema valida item y reglas de inventario.
3. El sistema registra movimiento de entrada.
4. El sistema incrementa stock y registra kardex.
5. El sistema genera asiento contable si aplica.

Acceptance criteria: AC-011, AC-013, AC-014.

## UC-008: Ajustar inventario

Actor: Administrador.

Flujo principal:
1. El actor solicita ajuste de inventario con motivo.
2. El sistema valida permisos y producto.
3. El sistema registra movimiento de ajuste.
4. El sistema actualiza existencia y deja auditoria.

Acceptance criteria: AC-013, AC-018.

## UC-009: Consultar libro diario y mayor

Actor: Contador.

Flujo principal:
1. El contador consulta un periodo.
2. El sistema lista asientos en libro diario.
3. El sistema agrupa saldos por cuenta en libro mayor.

Acceptance criteria: AC-015, AC-016.

## UC-010: Registrar cliente o adquirente fiscal

Actor: Administrador o vendedor autorizado.

Flujo principal:
1. El actor registra tipo de persona, tipo de documento, numero de documento, nombre completo o razon social y datos de contacto.
2. El sistema calcula digito de verificacion cuando el tipo de documento es NIT.
3. El sistema guarda el tercero con rol cliente dentro de la empresa.

Acceptance criteria: AC-037, AC-038, AC-039.

## UC-011: Registrar proveedor

Actor: Administrador.

Flujo principal:
1. El actor registra datos fiscales y comerciales del proveedor.
2. El sistema calcula digito de verificacion cuando aplique.
3. El sistema guarda el tercero con rol proveedor dentro de la empresa.

Acceptance criteria: AC-037, AC-038, AC-039.

## UC-012: Crear bien fisico vendible

Actor: Administrador.

Flujo principal:
1. El actor crea un item tipo bien fisico con SKU, nombre, precio, costo, impuesto y control de stock.
2. El sistema valida unicidad por empresa.
3. El sistema deja el item disponible para compra, venta e inventario.

Acceptance criteria: AC-034, AC-040.

## UC-013: Crear servicio o intangible facturable

Actor: Administrador.

Flujo principal:
1. El actor crea un item tipo servicio con nombre, precio, impuesto y configuracion de venta.
2. El sistema permite asociar insumos sugeridos solo como referencia operativa.
3. El sistema deja el servicio disponible para facturacion sin control automatico de stock.

Acceptance criteria: AC-041, AC-046.

## UC-014: Registrar consumo o desperdicio manual de insumos

Actor: Administrador o responsable de inventario.

Flujo principal:
1. El actor selecciona insumo, cantidad, tipo de movimiento y motivo.
2. El sistema valida existencia suficiente para salidas.
3. El sistema actualiza stock y kardex sin asociarlo automaticamente a una venta.

Acceptance criteria: AC-042.

## UC-015: Registrar compra documental de proveedor

Actor: Administrador.

Flujo principal:
1. El actor selecciona proveedor, fecha, conceptos, impuestos y forma de pago.
2. El sistema crea la compra en estado pendiente.
3. Al confirmar, el sistema conserva soporte financiero/documental sin mover inventario.
4. El sistema genera asiento contable o cuenta por pagar segun reglas de la empresa.

Acceptance criteria: AC-043.

## UC-016: Registrar gasto sin inventario

Actor: Administrador o contador.

Flujo principal:
1. El actor registra proveedor, concepto, valores, impuestos, evidencia y forma de pago.
2. El sistema confirma el gasto sin crear movimientos de inventario.
3. El sistema genera asiento contable y cuenta por pagar cuando aplique.

Acceptance criteria: AC-044.

## UC-017: Pagar cuenta por pagar

Actor: Administrador o contador.

Flujo principal:
1. El actor selecciona cuenta por pagar y registra valor, fecha y medio de pago.
2. El sistema valida saldo disponible.
3. El sistema disminuye saldo y genera trazabilidad contable.

Acceptance criteria: AC-045.

## UC-018: Emitir venta mixta de bienes y servicios

Actor: Cajero o vendedor.

Flujo principal:
1. El actor selecciona cliente, bienes fisicos, servicios, cantidades y medio de pago.
2. El sistema valida stock solo para bienes con control de inventario.
3. El sistema emite POS electronico o factura electronica.
4. El sistema descuenta bienes fisicos cuando el documento queda efectivo y conserva servicios sin descuento automatico de insumos.

Acceptance criteria: AC-040, AC-041, AC-046.

## UC-019: Consultar reportes operativos y fiscales

Actor: Administrador, contador o auditor.

Flujo principal:
1. El actor selecciona empresa, rango de fechas y reporte.
2. El sistema consulta datos del modelo activo.
3. El sistema retorna informacion aislada por empresa.

Acceptance criteria: AC-047.

## UC-020: Administrar usuarios, roles y permisos

Actor: Propietario o administrador.

Flujo principal:
1. El actor crea usuario con correo, nombre completo y password.
2. El sistema persiste password con hash PBKDF2 y registra auditoria de creacion.
3. El actor inicia sesion y recibe token opaco Bearer con expiracion.
4. El actor asigna roles dentro de una empresa.
5. El sistema deriva permisos por rol y empresa.
6. El sistema rechaza cambios de roles cuando el usuario autenticado no tiene `ROLES_MANAGE`.
7. El sistema registra auditoria de login y cambios de roles.

Acceptance criteria: AC-048, AC-018, AC-032.

## UC-021: Administrar licencia de empresa

Actor: Administrador de plataforma.

Flujo principal:
1. El actor registra plan, vigencia y limites de licencia para una empresa.
2. El actor consulta, activa o suspende la licencia cuando cambian las condiciones comerciales.
3. Un servicio consumidor solicita validacion de licencia antes de crear usuarios, transacciones o documentos fiscales.
4. El sistema responde `allowed=true` cuando la licencia esta activa y vigente.
5. El sistema responde `allowed=false` con `reasonCode` y `message` cuando la licencia esta suspendida, vencida o cancelada.

Flujos alternos:
- Si la empresa no existe, el sistema responde `RESOURCE_NOT_FOUND`.
- Si no existe licencia configurada, el sistema responde `RESOURCE_NOT_FOUND`.
- Las consultas, exportaciones y acciones administrativas de recuperacion quedan permitidas en esta fase sin consumir validacion de licencia.

Acceptance criteria: AC-049.

Nota UC-021 consumidores implementados en TASK-058: `billing-service` valida `CREATE_TRANSACTION` e `ISSUE_FISCAL_DOCUMENT`; `identity-service` valida `CREATE_USER` al asociar usuarios a una empresa mediante membresia/roles.
## UC-022: Registrar cuenta por cobrar

Actor: Cajero, administrador o proceso de facturacion.

Flujo principal:
1. El actor o el proceso de venta identifica una venta/documento fiscal con condicion de pago a credito.
2. El sistema valida empresa, cliente, documento origen, fecha de emision, fecha de vencimiento y total.
3. El sistema crea una cuenta por cobrar abierta con saldo inicial igual al total del documento.
4. El sistema deja trazabilidad hacia la venta/documento fiscal y permite consulta por cliente, estado y periodo.

Acceptance criteria: AC-052, AC-047.

## UC-023: Registrar pago de cuenta por cobrar

Actor: Cajero, administrador o contador.

Flujo principal:
1. El actor selecciona una cuenta por cobrar abierta o parcialmente pagada.
2. El actor registra fecha, valor, metodo de pago, referencia y usuario responsable.
3. El sistema valida que el pago no exceda el saldo pendiente.
4. El sistema disminuye el saldo, actualiza estado y registra trazabilidad contable y operativa.

Acceptance criteria: AC-053, AC-014, AC-015.

## UC-024: Crear empresa contratante y administrador inicial

Actor: Usuario `ROOT`.

Precondiciones:
- El actor tiene rol global `ROOT`.
- El actor no requiere `company_id` activo para operar el panel global.

Flujo principal:
1. `ROOT` registra la empresa que compra la licencia del software.
2. El sistema crea la empresa en `tenant-service` y su licencia inicial segun plan contratado.
3. `ROOT` crea o selecciona el usuario administrador inicial de esa empresa.
4. El sistema crea la membresia del administrador en la empresa.
5. El sistema asigna un rol empresarial inicial con permisos administrativos permitidos para esa empresa.
6. El sistema registra auditoria de empresa, licencia, usuario y asignacion de rol.

Reglas:
- `ROOT` no queda como usuario operativo de la empresa por defecto.
- La empresa no puede acceder a roles ni usuarios de otras empresas.
- El administrador inicial no recibe permisos `GLOBAL_*`.

Acceptance criteria: AC-059, AC-060, AC-061, AC-063, AC-018, AC-032.

## UC-025: Crear rol empresarial configurable

Actor: Administrador empresarial con `COMPANY_ROLES_MANAGE`.

Precondiciones:
- El actor pertenece a la empresa activa.
- La licencia de la empresa permite administracion de usuarios/roles segun politica comercial.

Flujo principal:
1. El actor define nombre, descripcion y permisos del rol.
2. El sistema calcula permisos efectivos del actor en esa empresa.
3. El sistema rechaza permisos `GLOBAL_*`.
4. El sistema valida que los permisos seleccionados sean subconjunto estricto de los permisos efectivos del actor.
5. El sistema crea el rol asociado a `company_id`.
6. El sistema registra auditoria del cambio.

Flujos alternos:
- Si el rol intenta tener permisos iguales a los del actor, se rechaza.
- Si el rol intenta tener permisos que el actor no posee, se rechaza.
- Si otra empresa intenta consultar o asignar el rol, se rechaza por aislamiento multiempresa.

Acceptance criteria: AC-061, AC-062, AC-063, AC-064, AC-018, AC-032.

## UC-026: Asignar roles empresariales a usuarios

Actor: Administrador empresarial con `COMPANY_USERS_MANAGE` y permisos suficientes.

Flujo principal:
1. El actor selecciona usuario de la empresa o crea uno nuevo.
2. El actor selecciona uno o varios roles empresariales activos.
3. El sistema calcula permisos efectivos resultantes de los roles seleccionados.
4. El sistema valida que el actor pueda delegar esos permisos con la regla estrictamente menor.
5. El sistema guarda la asignacion y recalcula permisos efectivos del usuario.
6. El sistema registra auditoria.

Acceptance criteria: AC-061, AC-062, AC-064, AC-018, AC-032.

## UC-027: Registrar producto con impuesto fiscal

Actor: Administrador o responsable de inventario.

Flujo principal:
1. El actor escanea o digita el codigo de barras en el campo dedicado.
2. El actor selecciona el impuesto de venta desde el catalogo fiscal visible en espanol.
3. El sistema guarda producto, precio, costo, stock inicial y snapshot fiscal del impuesto.
4. El sistema deja el producto disponible para venta POS si `saleEnabled=true`.

Acceptance criteria: AC-116, AC-120, AC-123.

## UC-028: Vender por POS con scanner y consumidor final

Actor: Cajero o vendedor.

Flujo principal:
1. El actor decide si el comprador desea factura electronica nominada.
2. Si el comprador desea identificarse, el actor busca y selecciona cliente por documento.
3. Si el comprador no desea identificarse, el sistema usa perfil fiscal de consumidor final configurado.
4. El actor escanea codigos de barras en el campo dedicado.
5. El sistema busca cada producto automaticamente, agrega linea o incrementa cantidad, y calcula precio/impuesto desde inventario.
6. El actor selecciona medio de pago y crea/confirmar venta POS.
7. El sistema emite documento equivalente electronico POS mock, descuenta inventario y contabiliza efectos aprobados.

Acceptance criteria: AC-117, AC-118, AC-119, AC-121, AC-122, AC-033.

## UC-029: Iniciar sesion productiva sin exponer tokens al navegador

Actor: Usuario ROOT, administrador empresarial o usuario operativo.

Precondiciones:
- El ambiente productivo tiene Cognito configurado.
- El BFF tiene dominio/callback OAuth autorizado.
- El usuario existe en Cognito y esta vinculado a identidad local.

Flujo principal:
1. El usuario abre la SPA.
2. La SPA solicita al BFF la URL de login productiva.
3. El BFF genera `state`, `nonce` y PKCE, y redirige a Cognito Hosted UI.
4. El usuario ingresa credenciales y completa MFA cuando aplique.
5. Cognito redirige al callback del BFF con `code` y `state`.
6. El BFF valida `state`, intercambia el codigo por tokens y solicita a `identity-service` una sesion interna para el usuario local activo asociado.
7. El BFF emite cookie opaca `HttpOnly`, `Secure`, `SameSite`.
8. La SPA consulta `/api/v1/auth/session` y recibe identidad resumida y CSRF, sin tokens.
9. La SPA carga empresas, licencia y permisos mediante requests al BFF sin construir `Authorization` en JavaScript.
10. Las solicitudes posteriores usan la cookie segura y el BFF propaga `Authorization` interno y `X-User-Id` a microservicios.

Flujos alternos:
- Si `state` no coincide o expiro, el BFF rechaza el callback y registra auditoria.
- Si el usuario ROOT/admin no completa MFA, el BFF bloquea acciones criticas.
- Si la sesion expira, el BFF limpia cookie y la SPA vuelve a login.

Reglas:
- La SPA productiva no captura password ni conserva tokens en storage.
- El BFF es responsable de refresh, logout, revocacion y auditoria.

Acceptance criteria: AC-176, AC-177, AC-178, AC-179, AC-180, AC-181, AC-182.

## UC-030: Crear secretos AWS por empresa al crear tenant

Actor: Usuario ROOT.

Precondiciones:
- `ROOT` esta autenticado con MFA.
- El rol IAM del backend permite crear secretos solo bajo el prefijo del ambiente y empresa.

Flujo principal:
1. `ROOT` crea una empresa contratante.
2. `tenant-service` registra la empresa y emite evento/solicitud de provisionamiento seguro.
3. El backend autorizado crea secretos deterministas por empresa para configuraciones sensibles futuras.
4. El sistema etiqueta los secretos con ambiente, `companyId` y proposito.
5. La base de datos guarda solo referencias no sensibles.
6. Auditoria registra resultado sin valores secretos.

Flujos alternos:
- Si el secreto ya existe, el proceso es idempotente y retorna la referencia existente.
- Si AWS Secrets Manager falla, la empresa puede quedar creada con estado de provisionamiento pendiente y reintento auditable.

Acceptance criteria: AC-170, AC-171, AC-186, AC-188.

## UC-031: Emitir documento fiscal real ante DIAN

Actor: Sistema, iniciado por vendedor/cajero o proceso fiscal autorizado.

Precondiciones:
- La empresa tiene configuracion DIAN real activa, completa, vigente y probada.
- Existe resolucion de numeracion vigente para el tipo documental.
- El documento fiscal tiene snapshot canonico, totales, impuestos y adquirente/consumidor final resueltos.

Flujo principal:
1. `billing-service` confirma el documento y genera una clave de idempotencia estable.
2. `billing-service` solicita envio real a `dian-provider-service`.
3. `dian-provider-service` resuelve configuracion y secretos de la empresa.
4. El sistema genera XML UBL 2.1, CUFE/CUDE y QR.
5. El sistema firma el XML con certificado empresarial.
6. El sistema valida XSD, Schematron y listas de codigos.
7. El sistema transmite a DIAN en ambiente de habilitacion o produccion.
8. El sistema registra respuesta, tracking, artefactos y estado.
9. `billing-service` actualiza el documento sin duplicar inventario ni contabilidad.

Flujos alternos:
- Si falta configuracion o certificado, el sistema falla cerrado antes de generar/transmitir.
- Si falla validacion tecnica, no transmite a DIAN y registra error sanitizado.
- Si DIAN rechaza, el documento queda `REJECTED` y requiere gestion fiscal autorizada.
- Si el fallo es temporal, se programa reintento idempotente.

Acceptance criteria: AC-218, AC-219, AC-220, AC-221, AC-222, AC-223, AC-224, AC-226, AC-227.

## UC-032: Consultar artefactos fiscales reales

Actor: ROOT, administrador empresarial, contador o usuario con permiso fiscal/documental.

Precondiciones:
- El documento fiscal existe en la empresa activa.
- El usuario tiene permiso y modulo licenciado.

Flujo principal:
1. El usuario consulta historico de ventas o documentos.
2. El BFF solicita metadata de artefactos al servicio dueno.
3. El sistema valida empresa, permiso, licencia y estado del documento.
4. El sistema entrega links controlados por BFF para XML firmado, representacion grafica, QR, ZIP/AttachedDocument o respuesta DIAN segun disponibilidad.
5. Cada descarga queda auditada.

Flujos alternos:
- Si el artefacto no existe o no esta listo, se muestra mensaje funcional.
- Si el usuario no tiene permiso, el backend rechaza aunque la UI oculte la accion.

Acceptance criteria: AC-225, AC-158, AC-201, AC-207.

## UC-033: Configurar politica fiscal por empresa

Actor: Administrador empresarial o ROOT.

Precondiciones:
- La empresa tiene licencia activa para facturacion.
- El usuario tiene permiso de configuracion fiscal.

Flujo principal:
1. El usuario abre Configuracion > Fiscal.
2. El sistema muestra la politica fiscal vigente de la empresa.
3. El usuario selecciona documento fiscal por defecto para ventas POS: factura electronica de venta o documento equivalente POS.
4. El sistema valida que exista o se configure resolucion activa compatible por tipo documental y ambiente.
5. El sistema guarda la politica, registra auditoria y no modifica ventas ya emitidas.

Flujos alternos:
- Si no existe resolucion compatible, el sistema permite guardar politica en borrador o bloquea activacion segun regla aprobada.
- Si el usuario no tiene permiso, el backend rechaza aunque la UI oculte la opcion.

Acceptance criteria: AC-244, AC-245, AC-252, AC-253.

## UC-034: Autorizar cambio de tipo documental con PIN

Actor: Vendedor y administrador/supervisor autorizador.

Precondiciones:
- El vendedor tiene una venta POS en borrador.
- La empresa permite override de tipo documental.
- El autorizador pertenece a la empresa, tiene permiso `SALES_DOCUMENT_TYPE_OVERRIDE` y PIN operacional activo.

Flujo principal:
1. El vendedor solicita cambiar el tipo fiscal de la venta.
2. La SPA abre modal de autorizacion sin cerrar la sesion del vendedor.
3. El autorizador ingresa correo o selecciona su usuario, PIN de 6 digitos y motivo.
4. El backend valida usuario, permiso, PIN, estado del PIN, politica, licencia y resolucion activa.
5. El sistema registra el override solo para esa venta.
6. Auditoria registra vendedor, autorizador, motivo, tipo anterior, tipo nuevo y correlation ID.
7. La venta continua en la sesion del vendedor con el nuevo tipo fiscal.

Flujos alternos:
- Si el PIN falla 3 veces, queda bloqueado.
- Si el PIN esta `CHANGE_REQUIRED`, se rechaza la autorizacion.
- Si no hay resolucion activa para el tipo destino, se rechaza con mensaje funcional.

Acceptance criteria: AC-246, AC-247, AC-248, AC-249, AC-250, AC-251, AC-256.

## UC-035: Emitir notas fiscales independientes

Actor: Contador, administrador o usuario con permiso fiscal especifico.

Precondiciones:
- Existe documento fiscal origen validado o gestionable.
- Existe resolucion activa para el tipo de nota correspondiente.
- El usuario tiene permiso para el modulo de nota.

Flujo principal:
1. El usuario abre Facturacion > Nota credito, Nota debito o Nota de ajuste POS.
2. El sistema busca y valida el documento origen dentro de la empresa.
3. El usuario captura motivo, valores/lineas y observaciones permitidas.
4. El backend asigna numeracion desde la resolucion del tipo de nota.
5. El conector DIAN mock/real procesa la nota segun modo de empresa.
6. El sistema registra estado, CUFE/CUDE, artefactos, auditoria y efectos contables cuando aplique.

Flujos alternos:
- Nota de ajuste POS solo aplica sobre documento equivalente electronico POS.
- Nota credito/debito aplica sobre factura electronica de venta segun reglas aprobadas.

Acceptance criteria: AC-254, AC-255, AC-256.

## UC-036: Actualizar o inactivar producto

Actor: Administrador o responsable de inventario.

Precondiciones:
- Existe empresa activa.
- El usuario tiene permiso de inventario.

Flujo principal:
1. El actor abre `Inventario`.
2. El sistema carga automaticamente productos de la empresa.
3. El actor hace clic en `Actualizar` sobre un producto.
4. El sistema carga el formulario con los datos del producto y cambia la accion principal a `Actualizar item`.
5. El actor modifica datos maestros permitidos.
6. El sistema valida unicidad de SKU/codigo de barras, guarda cambios, audita y refresca la tabla.

Flujo alterno:
- Si el actor hace clic en `Inactivar`, el producto queda fuera de ventas nuevas, pero permanece disponible para historicos y reportes.
- Si el actor digita o escanea un codigo de barras existente en el formulario, el sistema carga ese producto y pasa a modo actualizacion.

Acceptance criteria: AC-359, AC-360, AC-361, AC-362.

## UC-037: Registrar compra documental con evidencia opcional

Actor: Administrador, contador o usuario con permiso de compras.

Flujo principal:
1. El actor abre `Compras`.
2. El sistema carga compras recientes y proveedores activos.
3. El actor captura proveedor, fecha, concepto, condicion de pago, vencimiento si aplica y total de factura.
4. El actor opcionalmente selecciona evidencia: ninguna, PDF unico o URL.
5. Si selecciona PDF, la aplicacion valida un solo archivo PDF y lo envia al storage empresarial.
6. El sistema guarda la compra documental sin aumentar inventario y registra auditoria.

Acceptance criteria: AC-363, AC-365, AC-366, AC-367, AC-368, AC-369.

## UC-038: Registrar gasto total-only

Actor: Administrador, contador o usuario con permiso de gastos.

Flujo principal:
1. El actor abre `Gastos`.
2. El sistema carga gastos recientes y proveedores activos.
3. El actor captura concepto, tipo de egreso, proveedor opcional, fecha, condicion de pago, vencimiento si aplica y total.
4. El actor opcionalmente adjunta PDF o URL de soporte.
5. El sistema guarda el gasto sin inventario, contabiliza segun regla empresarial y audita.

Acceptance criteria: AC-364, AC-365, AC-366, AC-367.

## UC-039: Crear deudor sin error interno

Actor: Administrador, contador o usuario con permiso de deudores.

Flujo principal:
1. El actor abre `Deudores`.
2. El sistema carga terceros y cuentas por cobrar recientes.
3. El actor selecciona tercero deudor, captura concepto, fecha, vencimiento y monto.
4. El backend crea cuenta por cobrar manual, saldo inicial, asiento contable y auditoria.

Flujos alternos:
- Si falta tercero, monto o regla contable, el backend responde error funcional `400/409`.
- Ningun error esperado de validacion o parametrizacion debe responder `500`.

Acceptance criteria: AC-370, AC-371.

## UC-040: Imprimir comprobante POS con QR parametrizable

Actor: Cajero, vendedor o usuario autorizado para consultar venta.

Precondiciones:
- Existe venta confirmada con documento fiscal mock o real.

Flujo principal:
1. El actor cierra venta o solicita reimpresion.
2. `billing-service` resuelve contenido QR del documento.
3. En modo mock, construye URL de comprobante desde parametro de URL base.
4. En modo DIAN real, usa QR/URL entregado por DIAN.
5. El comprobante renderiza QR grafico escaneable con subtotal, IVA y total.
6. El sistema audita la generacion/impresion.

Acceptance criteria: AC-372, AC-373, AC-374, AC-376.

## UC-041: Configurar colores de marca empresarial

Actor: ROOT o administrador empresarial autorizado.

Flujo principal:
1. El actor abre marca empresarial.
2. El sistema muestra explicacion del color principal y color de acento.
3. El actor selecciona colores con color picker.
4. El sistema valida hexadecimal, guarda branding y aplica la vista previa.

Acceptance criteria: AC-375, AC-376.

## UC-042: Revisar puesta en marcha empresarial

Actor: ROOT o administrador empresarial.

Flujo principal:
1. El actor abre el asistente de puesta en marcha.
2. El sistema consulta el estado de licencia, usuarios, roles, DIAN, emisor fiscal, resoluciones, contabilidad, PIN, inventario, branding, reportes y storage.
3. El sistema muestra estado general y checks por modulo con accion sugerida.
4. El actor abre el modulo sugerido para corregir la configuracion faltante.

Flujo alterno:
- Si la empresa esta bloqueada para vender, facturar o reportar, el sistema muestra el motivo funcional antes de intentar la operacion.

Acceptance criteria: AC-379, AC-380.

## UC-043: Validar seguridad productiva antes de salida comercial

Actor: ROOT tecnico o administrador de plataforma.

Flujo principal:
1. El actor despliega ambiente productivo.
2. El sistema valida autenticacion real, secretos, cookies seguras, CSRF, headers, rate limiting y MFA cuando aplique.
3. Si alguna configuracion critica falta, el servicio falla cerrado durante arranque o bloquea operacion administrativa.

Acceptance criteria: AC-381.

## UC-044: Emitir documento DIAN real por empresa

Actor: Empresa habilitada ante DIAN.

Flujo principal:
1. La empresa configura sus parametros DIAN, certificado y resoluciones.
2. Un usuario autorizado cierra una venta o emite documento fiscal.
3. El sistema genera XML UBL, firma, CUFE/CUDE y QR.
4. El sistema transmite a DIAN, procesa ApplicationResponse y registra artefactos.
5. El sistema conserva auditoria, estado fiscal y comprobante consultable.

Flujo alterno:
- Si DIAN rechaza o falla una validacion tecnica, el sistema conserva estado/error funcional y no repite inventario ni contabilidad.

Acceptance criteria: AC-382.

## UC-045: Completar configuracion contable guiada

Actor: Administrador empresarial o contador.

Flujo principal:
1. El actor abre configuracion contable.
2. El sistema muestra reglas y cuentas faltantes por modulo.
3. El actor crea o completa cuentas/reglas requeridas.
4. El sistema actualiza readiness contable y habilita operaciones dependientes.

Acceptance criteria: AC-383.

## UC-046: Consultar reportes gerenciales normalizados

Actor: Administrador empresarial, contador o usuario con permiso de reportes.

Flujo principal:
1. El actor abre Reportes.
2. Selecciona reporte, filtros, rango de fechas, visualizacion y formato.
3. El sistema genera dataset normalizado en espanol.
4. El actor visualiza tabla/grafica o descarga exportacion.

Acceptance criteria: AC-384.

## UC-047: Consultar auditoria operativa

Actor: ROOT o administrador empresarial.

Flujo principal:
1. El actor abre Logs/Auditoria.
2. Filtra por empresa, usuario, modulo, resultado, correlation ID y fechas.
3. El sistema muestra eventos auditables sanitizados.

Acceptance criteria: AC-385.

## UC-048: Gestionar finanzas diarias

Actor: Administrador empresarial o contador.

Flujo principal:
1. El actor abre gestion financiera diaria.
2. El sistema consolida ventas, gastos, compras, deudores, cuentas por pagar, pagos diarios, vencimientos y alertas.
3. El actor revisa utilidad/perdida, liquidez esperada y obligaciones.

Acceptance criteria: AC-388.
