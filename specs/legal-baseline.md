# Linea base normativa

## Control

- Fecha de consulta: 2026-09-09.
- Alcance: referencias oficiales para diseno y validacion del producto; no constituye concepto juridico, tributario o contable.
- Regla: una URL o una fecha nominal no prueba vigencia efectiva. Antes de publicar reglas se validan modificaciones, derogatorias, suspensiones, efectos temporales y jurisdiccion.
- Estados: `CURRENT_REFERENCE`, `HISTORICAL_SOURCE`, `REQUIRES_REVIEW`.

## Fuentes nacionales

| Fuente | Estado documental | Uso en NexoFiscal |
|---|---|---|
| [Estatuto Tributario](https://www.funcionpublica.gov.co/eva/gestornormativo/norma_pdf.php?i=6533) | CURRENT_REFERENCE | Obligacion de facturar, IVA, retenciones y deberes formales; siempre consultar texto compilado y concordancias aplicables |
| [Decreto 1625 de 2016](https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=83233) | CURRENT_REFERENCE | DUR tributario y reglas reglamentarias por sujeto, concepto, base y fecha |
| [Resolucion DIAN 000227 de 2025](https://www.dian.gov.co/normatividad/Paginas/Resolucion-000227-del-23092025.aspx) | CURRENT_REFERENCE | Resolucion unica DIAN; su Titulo 5 compila el sistema de facturacion |
| [Resolucion DIAN 000165 de 2023](https://normograma.dian.gov.co/dian/compilacion/docs/resolucion_dian_0165_2023.htm) | HISTORICAL_SOURCE | Fuente del regimen y anexos tecnicos de facturacion compilados posteriormente en la Resolucion 227 de 2025 |
| [Normatividad del sistema de facturacion](https://micrositios.dian.gov.co/sistema-de-facturacion-electronica/normatividad/) | CURRENT_REFERENCE | Indice oficial de resoluciones, modificaciones y anexos tecnicos |
| [Decreto 2420 de 2015](https://www.funcionpublica.gov.co/eva/gestornormativo/norma.php?i=76745) | CURRENT_REFERENCE | DUR de contabilidad e informacion financiera para Grupos 1, 2 y 3; no define tarifas tributarias |
| [Decreto 2650 de 1993](https://suin-juriscol.gov.co/viewDocument.asp?id=1772403) | HISTORICAL_SOURCE | PUC historico util como plantilla y lenguaje operativo, no como plan NIIF universal |
| [Decreto 572 de 2025](https://normograma.dian.gov.co/dian/compilacion/docs/decreto_0572_2025.htm) | REQUIRES_REVIEW | Sus efectos temporales y cualquier suspension/reactivacion deben resolverse por eventos juridicos antes de publicar tarifas |

## Fuentes territoriales

- ReteICA no se deriva de una tarifa nacional unica.
- Cada paquete territorial requiere acuerdo, decreto, resolucion, calendario o acto oficial de la jurisdiccion correspondiente.
- La fuente debe registrar municipio DIVIPOLA, autoridad, articulos, fecha de publicacion, fecha efectiva, URL oficial y eventos juridicos posteriores.
- La ausencia de paquete verificado produce `BLOCKED`; no autoriza tarifa cero ni uso automatico del municipio de domicilio.

## Fuentes tecnicas y catalogos

- Los anexos tecnicos DIAN se toman desde el micrositio oficial y se versionan por tipo documental.
- DIVIPOLA y CIIU deben conservar fuente, version y vigencia; una nueva revision publicada no reemplaza automaticamente la clasificacion adoptada para el RUT.
- Los fixtures DIAN deben ser oficiales o sanitizados y no pueden contener certificados, claves, datos personales o credenciales reales.

## Compuerta de publicacion fiscal

1. Registrar fuente oficial y hash/referencia verificable.
2. Registrar eventos juridicos y sus fechas efectivas.
3. Revisar sujeto, concepto, base, tarifa, umbral, territorio, excepciones y momento de causacion.
4. Ejecutar pruebas de frontera y viaje temporal.
5. Obtener aprobacion ROOT y auditoria reforzada.
6. Publicar una nueva version; nunca modificar silenciosamente la historia aplicada.

## Pendientes vinculados

- TASK-309: temporalidad y eventos juridicos.
- TASK-310/TASK-311: perfiles, reglas nacionales, lineas y acumulaciones.
- TASK-312: paquetes ReteICA.
- TASK-313/TASK-314: conciliacion, reversos, cierres y certificados.
- TASK-315: experiencia, observabilidad y E2E.
