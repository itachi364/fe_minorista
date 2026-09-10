# Roadmap SDD activo

## Regla de uso

Este archivo ordena exclusivamente trabajo pendiente. No reemplaza requisitos, criterios ni tareas. Una fase requiere nueva aprobacion MUT-001 antes de modificar codigo, datos, infraestructura o sistemas externos.

## Fase A: Validez juridica del catalogo fiscal

- Prioridad: P0.
- Tarea: TASK-309.
- Objetivo: modelar fuentes y eventos juridicos temporales, bloquear versiones no verificadas y preservar snapshots historicos.
- Dependencia externa: confirmar providencias, comunicaciones y compilaciones oficiales aplicables a cada fecha.
- Salida: catalogo que no confunde fecha nominal con vigencia juridica efectiva.

## Fase B: Integracion DIAN productiva

- Prioridad: P0.
- Tareas: TASK-273, TASK-274, TASK-276 y TASK-264, en ese orden.
- Objetivo: transporte SOAP WCF firmado con certificado propio por empresa, respuestas normalizadas, fixtures sanitizados y validacion real E2E.
- Restriccion: no existe fallback silencioso a mock ante errores reales.
- Salida: evidencia de habilitacion y trazabilidad por empresa antes de declarar produccion.

## Fase C: Culminacion del motor fiscal

- Prioridad: P0/P1.
- Tareas: TASK-310, TASK-311, TASK-312, TASK-313, TASK-314 y TASK-315.
- Objetivo: perfiles temporales, calculo por linea, acumulaciones, ReteICA territorial, conciliacion contable-fiscal, reversos, certificados, observabilidad y E2E.
- Dependencia: Fase A completada; la parte DIAN puede avanzar en paralelo cuando no comparta contratos.
- Salida: resultados explicables, inmutables, conciliables y bloqueados ante configuracion insuficiente.

## Fase D: Contadores y acceso inicial

- Prioridad: P1.
- Tareas: TASK-290, TASK-291 y TASK-294.
- Objetivo: asociacion de un contador activo por empresa, portal multiempresa limitado y cambio obligatorio de credencial temporal.
- Dependencia: permisos y aislamiento existentes; reportes reutilizados sin duplicar datos.
- Salida: contador sin privilegios ROOT y acceso solo a empresas asociadas.

## Fase E: Notificaciones

- Prioridad: P1.
- Tarea: TASK-295.
- Objetivo: puerto de correo con adaptadores local/productivo para credenciales, inventario bajo y reportes listos.
- Dependencias: TASK-294 para credenciales y contratos de eventos de inventario/reporting.
- Salida: reintentos auditables sin bloquear ni corromper la transaccion principal.

## Compuertas por fase

1. Discovery y evidencia oficial/Context7 aplicable.
2. Requisitos y criterios de aceptacion aprobados.
3. Diseno, contratos, datos, seguridad, observabilidad y pruebas documentados.
4. Confirmacion MUT-001 antes de implementar.
5. Implementacion trazable tarea -> criterio -> prueba.
6. Validacion, README, estado SDD y reporte final actualizados.
7. Confirmacion separada para commit, push o despliegue.

## Trabajo no planificado

- Los requisitos sin tarea activa no autorizan implementacion por si solos.
- Los identificadores historicos omitidos no se reutilizan.
- Toda nueva necesidad se agrega al final de la numeracion vigente y antes de `Context7 evidence` en `tasks.md`.
