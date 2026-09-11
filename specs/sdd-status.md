# Estado SDD de NexoFiscal

## Corte

- Fecha de actualizacion: 2026-09-11.
- Commit base de la auditoria integral: `a4654d0`; cambios de fase 39 pendientes de commit.
- Estado observado: fase 39 implementada, validada y desplegada localmente; los servicios intervenidos quedaron saludables y el catalogo nacional respondio con 16 conceptos.
- Proposito: fuente canonica corta del estado real. Los detalles historicos permanecen en `tasks.md` y las decisiones objetivo en los documentos de diseno.

## Vocabulario

- `IMPLEMENTED`: existe en codigo, tiene persistencia/contrato cuando aplica y cuenta con evidencia de prueba registrada.
- `PARTIAL`: existe una vertical util, pero faltan capacidades necesarias para declarar completo el alcance.
- `TARGET`: especificado, aun no implementado.
- `HISTORICAL`: decision o resultado conservado para trazabilidad; no describe necesariamente el estado vigente.
- `RETIRED`: comportamiento retirado que no debe recuperarse sin nueva especificacion.

## Arquitectura fisica actual

| Capa | Estado | Componentes |
|---|---|---|
| SPA | IMPLEMENTED | React 19 + Vite, acceso por BFF |
| Borde | IMPLEMENTED | `bff-service` en `8083`, sesiones seguras, autorizacion, proxy y normalizacion de errores |
| Servicios | IMPLEMENTED | tenant, catalog, thirdparty, inventory, billing, dian-provider, accounting, audit, identity, payroll y reporting |
| Persistencia | IMPLEMENTED | PostgreSQL 16 local, esquema por servicio y Flyway |
| Eventing | PARTIAL | contratos compartidos y cinco Lambdas Maven; AWS EventBridge/SQS sigue siendo target productivo |
| Observabilidad | IMPLEMENTED local | Actuator, Prometheus y Grafana provisionados; alertamiento productivo externo sigue siendo target |
| Produccion AWS | TARGET | CloudFront/S3, API Gateway, ECS Fargate, RDS/Aurora, SQS/EventBridge, Secrets Manager/KMS y SES |

## Servicios locales

| Servicio | Puerto | Responsabilidad principal |
|---|---:|---|
| frontend | 5173 | SPA NexoFiscal |
| bff-service | 8083 | Borde publico y composicion |
| tenant-service | 8084 | Empresa, licencia, branding, archivos y perfil fiscal |
| catalog-service | 8085 | Catalogos regulatorios y operativos |
| thirdparty-service | 8086 | Clientes, proveedores y actividades CIIU |
| inventory-service | 8087 | Productos, stock, kardex y compras documentales |
| billing-service | 8088 | Ventas, documentos fiscales, numeracion y comprobantes |
| dian-provider-service | 8089 | Configuracion DIAN y transporte mock/referencia |
| accounting-service | 8090 | Plan de cuentas, asientos, gastos, cartera y motor fiscal gobernado |
| audit-service | 8091 | Auditoria sanitizada |
| identity-service | 8092 | Usuarios, roles, permisos, sesiones y PIN operacional |
| payroll-service | 8093 | Nomina y documentos de nomina electronica modelados |
| reporting-service | 8094 | Reportes normalizados y exportaciones asincronas |
| Prometheus | 9090 | Recoleccion local de metricas |
| Grafana | 3001 | Datasource y dashboard local |
| PostgreSQL | 15432 | Persistencia local |

## Capacidades

| Capacidad | Estado | Evidencia o limitacion principal |
|---|---|---|
| Multiempresa, seleccion activa y aislamiento | IMPLEMENTED | tenant, BFF, RBAC y filtros por empresa |
| Licencias POS, completa y personalizable | IMPLEMENTED | presets y capacidades server-side; portal contador no se activa por licencia |
| Usuarios, roles, permisos y PIN | IMPLEMENTED | incluye `COMPANY_SETTINGS_MANAGE` y `FISCAL_SETTINGS_MANAGE` |
| Contrasena temporal y cambio inicial | TARGET | TASK-294 |
| Clientes/proveedores y CIIU multiactividad | IMPLEMENTED | catalogo controlado y compatibilidad historica |
| Productos, inventario y kardex | IMPLEMENTED | compras documentales no incrementan stock automaticamente |
| Ventas POS y venta interna no fiscal | IMPLEMENTED | la venta no fiscal exige clasificacion vigente `NOT_OBLIGATED_VERIFIED` |
| Facturacion y documento equivalente electronico | PARTIAL | flujos/modelos/mock implementados; DIAN SOAP real y E2E pendientes |
| Certificado empresarial DIAN | IMPLEMENTED | carga privada `.p12`/`.pfx`; no existe certificado ROOT compartido |
| Compras, gastos, cartera y contabilidad | IMPLEMENTED | configuracion POS basica automatica y configuracion avanzada separada |
| Motor de retenciones | IMPLEMENTED | TASK-309 a TASK-315 cerradas; perfiles temporales tipados, calculo por linea, acumulaciones, ReteICA gobernada, atomicidad de compra, conciliacion, reversos, certificados y metricas. Conceptos sin regla oficial verificada bloquean la confirmacion. |
| Nomina | IMPLEMENTED | modulo fisico y persistencia; homologacion productiva DIAN debe validarse por alcance |
| Reportes y exportaciones | IMPLEMENTED | jobs y descargas intermediadas; correo al finalizar pendiente |
| Portal de contador | TARGET | TASK-290 y TASK-291 |
| Notificaciones por correo | TARGET | TASK-295 |

## Estado de migraciones

| Esquema | Ultima version aplicada |
|---|---|
| tenant | `013` local validada |
| identity | `009` |
| catalog | `013` |
| thirdparty | `009` local validada |
| inventory | `008` |
| billing | `013` |
| dian_provider | `003` |
| accounting | `021` local validada |
| audit | `002` |
| payroll | `002` |
| reporting | `001` |
| bff | `001` |

Las versiones tenant V013, thirdparty V009 y accounting V021, junto con sus predecesoras, fueron aplicadas en PostgreSQL local. El despliegue autorizado se completo con `tenant-service`, `thirdparty-service`, `accounting-service`, `inventory-service`, `bff-service` y `frontend` saludables; la verificacion HTTP encontro 16 conceptos nacionales, 5 verificados y 11 bloqueados para revision.

Las tablas de readiness empresarial no existen fisicamente: el BFF compone ese diagnostico desde datos de los servicios. Las tablas de contador y notificaciones aun pertenecen al roadmap. V015-V021 cubren eventos juridicos, paquetes ReteICA, calculo por linea, acumulacion, mapeos fiscales y de presentacion, reversos, cierres, auxiliar 350, confirmacion fiscal atomica y certificados.

## Backlog funcional activo

- DIAN productiva: TASK-264, TASK-273, TASK-274 y TASK-276.
- Contadores, accesos y correo: TASK-290, TASK-291, TASK-294 y TASK-295.
- Culminacion fiscal: TASK-309 a TASK-315 cerradas; la ampliacion de tarifas permanece sometida al gobierno ordinario del catalogo y a fuentes oficiales verificadas.
- Gobierno documental: TASK-316 cerrada el 2026-09-09 con trazabilidad, Compose, backend, frontend y diff validados; el render automatico Mermaid no se ejecuto por no existir CLI en el repositorio.

## Restricciones vigentes

- La aplicacion no debe declararse lista para emision DIAN productiva mientras el transporte SOAP y el E2E real sigan pendientes.
- Ninguna empresa emite con certificados de otra empresa ni con un certificado ROOT/global.
- No se generan CUFE, CUDE o QR DIAN para una venta interna no fiscal.
- El motor fiscal no sustituye validacion profesional ni habilita reglas nacionales/territoriales sin fuente oficial vigente.
- El PUC historico puede servir como plantilla; no se presenta como un plan NIIF universal.

## Fuentes de verdad relacionadas

- Requisitos: `requirements.md`.
- Criterios verificables: `acceptance-criteria.md`.
- Diseno: `design.md` y `architecture.md`.
- Contratos: `api-contract.md`.
- Persistencia: `database-design.md` y `data-dictionary.md`.
- Plan activo: `roadmap.md`.
- Linea normativa: `legal-baseline.md`.
- Historial de ejecucion: `tasks.md`.
