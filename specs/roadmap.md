# Roadmap SDD activo

## Regla de uso

Este archivo ordena exclusivamente trabajo pendiente. No reemplaza requisitos, criterios ni tareas. Una fase requiere nueva aprobacion MUT-001 antes de modificar codigo, datos, infraestructura o sistemas externos.

## Fase B: Integracion DIAN productiva

- Prioridad: P0.
- Tareas: TASK-273, TASK-274, TASK-276 y TASK-264, en ese orden.
- Objetivo: transporte SOAP WCF firmado con certificado propio por empresa, respuestas normalizadas, fixtures sanitizados y validacion real E2E.
- Restriccion: no existe fallback silencioso a mock ante errores reales.
- Salida: evidencia de habilitacion y trazabilidad por empresa antes de declarar produccion.

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
- La fase 39 esta cerrada. Incorporar una nueva tarifa nacional o municipal exige fuente oficial, version, pruebas y publicacion; un concepto `REQUIRES_REVIEW` no es una regla operativa.
- Los identificadores historicos omitidos no se reutilizan.
- Toda nueva necesidad se agrega al final de la numeracion vigente y antes de `Context7 evidence` en `tasks.md`.
