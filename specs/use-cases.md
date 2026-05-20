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
5. El sistema envia al proveedor tecnologico DIAN.
6. El sistema registra CUFE, QR, artefactos y estado.
7. El sistema actualiza inventario y contabilidad.

Acceptance criteria: AC-001, AC-002, AC-003, AC-004, AC-010, AC-014.

## UC-004: Emitir POS electronico

Actor: Cajero.

Flujo principal:
1. El cajero registra productos y pago.
2. El sistema valida disponibilidad y datos minimos.
3. El sistema genera documento equivalente electronico POS.
4. El sistema envia al proveedor tecnologico.
5. El sistema registra CUDE, QR, artefactos y estado.
6. El sistema actualiza inventario y contabilidad.

Acceptance criteria: AC-007, AC-009, AC-010, AC-014.

## UC-005: Generar nota credito o debito

Actor: Administrador o vendedor autorizado.

Flujo principal:
1. El actor selecciona factura validada.
2. El sistema valida que el ajuste sea permitido.
3. El sistema calcula valores de la nota.
4. El sistema envia la nota al proveedor tecnologico.
5. El sistema registra estado y afecta inventario/contabilidad si corresponde.

Acceptance criteria: AC-005, AC-013, AC-014.

## UC-006: Generar nota de ajuste POS

Actor: Administrador o cajero autorizado.

Flujo principal:
1. El actor selecciona POS electronico.
2. El sistema valida motivo de anulacion o correccion.
3. El sistema genera nota de ajuste.
4. El sistema envia la nota al proveedor tecnologico.
5. El sistema registra estado y trazabilidad.

Acceptance criteria: AC-008.

## UC-007: Registrar compra e incrementar inventario

Actor: Administrador.

Flujo principal:
1. El actor registra proveedor, productos, cantidades y valores.
2. El sistema valida proveedor y productos.
3. El sistema registra compra.
4. El sistema incrementa stock y registra movimiento de entrada.
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

## UC-015: Registrar compra de bienes o insumos

Actor: Administrador.

Flujo principal:
1. El actor selecciona proveedor, fecha, lineas, impuestos y forma de pago.
2. El sistema crea la compra en estado pendiente.
3. Al confirmar, el sistema incrementa stock cuando las lineas son inventariables.
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
1. El actor crea usuario o invita usuario existente.
2. El sistema asigna rol y permisos dentro de una empresa.
3. El sistema evalua permisos antes de comandos protegidos.

Acceptance criteria: AC-048.

## UC-021: Administrar licencia de empresa

Actor: Administrador de plataforma.

Flujo principal:
1. El actor registra plan, estado, vigencia y limites de licencia para una empresa.
2. El sistema evalua la licencia antes de crear transacciones o emitir documentos.
3. El sistema bloquea operaciones no permitidas con error estructurado.

Acceptance criteria: AC-049.
