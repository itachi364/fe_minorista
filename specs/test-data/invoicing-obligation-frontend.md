# Datos de prueba: obligacion de facturar desde frontend

Fecha de referencia: 2026-09-09. UVT configurada para 2026: COP 52.374. Umbral de 3.500 UVT: COP 183.309.000.

## Preparacion comun

1. Ingresar como ROOT y abrir `Empresas > Empresa y configuracion`.
2. Crear una empresa distinta para cada escenario y cargar un PDF de prueba como RUT.
3. Usar NIT distintos, correo unico, tipo de operacion `Venta de bienes gravados` y fecha de RUT `2026-09-01`.
4. Cuando un campo monetario se indique como bajo, registrar `100000000`.

## Escenario A: persona natural no obligada verificada

- Empresa: `Comercial Natural No Obligada`
- Tipo de persona: `Persona natural`
- Regimen tributario: `Ordinario`
- Responsabilidad RUT: `49 - No responsable de IVA`
- Establecimientos: `1`
- Usuario aduanero: desactivado
- Explota intangibles: desactivado
- Todos los siete valores monetarios: `100000000`
- Resultado esperado: `NOT_OBLIGATED_VERIFIED`
- Prueba de venta: la politica `Venta interna no fiscal` se puede guardar y la venta se confirma sin resolucion, CUFE ni envio DIAN.

## Escenario B: umbral alcanzado

- Repetir el escenario A.
- Ingresos actividad ano actual: `183309000`
- Resultado esperado: `OBLIGATED`, porque alcanzar exactamente el umbral no cuenta como estar por debajo.
- Prueba de venta: `Venta interna no fiscal` debe ser rechazada con el mensaje de clasificacion vigente.

## Escenario C: persona juridica vendedora

- Empresa: `Prueba Juridica Obligada SAS`
- Tipo de persona: `Persona juridica`
- Regimen tributario: `Ordinario`
- Responsabilidad RUT: `53 - Persona juridica no responsable de IVA`
- Establecimientos: `1`
- Valores monetarios: `100000000`
- Resultado esperado: `OBLIGATED`; el codigo 53 no es una exoneracion automatica de facturar.

## Escenario D: regimen SIMPLE

- Empresa: `Prueba Simple Obligada SAS`
- Tipo de persona: `Persona natural`
- Regimen tributario: `SIMPLE`
- Responsabilidad RUT: `47 - Regimen simple de tributacion`
- Resultado esperado: `OBLIGATED`.

## Escenario E: informacion incompleta

- Empresa: `Comercial Revision Fiscal`
- Tipo de persona: `Persona natural`
- Regimen tributario: `Ordinario`
- Responsabilidad RUT: `49 - No responsable de IVA`
- Dejar vacio `Mayor contrato gravado ano actual`.
- Resultado esperado: `REVIEW_REQUIRED`.
- Prueba de venta: la venta interna no fiscal permanece bloqueada.

## Comprobante esperado

Una venta confirmada para el escenario A imprime `Comprobante interno de venta`, sin CUFE/CUDE fiscal, y muestra de forma visible:

```text
NO ES FACTURA DE VENTA NI DOCUMENTO EQUIVALENTE
NO VALIDO COMO SOPORTE FISCAL
```
