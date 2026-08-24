# Database Design

Este documento es la fuente SDD vigente para decisiones de persistencia junto con `specs/data-dictionary.md`. `specs/data-model.md` queda como documento historico/transitorio y matriz de evolucion legacy; ante diferencias, prevalecen `database-design.md` y `data-dictionary.md`.

Este documento consolida decisiones de persistencia para los modulos nuevos y complementa `specs/data-model.md` y `specs/data-dictionary.md`.

## Principios

- PostgreSQL es la fuente de verdad transaccional.
- Cada microservicio es dueno de su schema y sus migraciones Flyway.
- Los datos de negocio deben vivir en base de datos, no en frontend.
- La SPA no debe incluir `initialState` con empresas, terceros, productos, ventas, resoluciones, usuarios empresariales ni catalogos.
- El unico seed funcional permitido para pruebas locales iniciales es el usuario global `ROOT`.
- Los catalogos oficiales y operativos se cargan por migraciones o modulo administrativo, y se consumen via BFF/catalog-service.

## Schemas

- `tenant`: empresas y licencias.
- `bff`: sesiones web productivas e intentos OAuth temporales del BFF, cifrados y gobernados por Flyway.
- `identity`: usuarios, roles, permisos y sesiones.
- `catalog`: catalogos globales, configuracion por empresa, departamentos y municipios.
- `thirdparty`: clientes/proveedores consolidados.
- `inventory`: productos, stock, compras y kardex.
- `billing`: ventas, documentos electronicos y consumidor final parametrizable.
- `dian_provider`: configuracion DIAN por empresa, envios, respuestas normalizadas y trazabilidad tecnica sin secretos.
- `accounting`: PUC, reglas, asientos, cuentas por cobrar/pagar, ingresos, egresos, costos y activos.
- `audit`: eventos de auditoria.
- `payroll`: trabajadores, contratos, pagos diarios, liquidaciones y nomina electronica opcional.

## Catalogos DB-only

Catalogos que deben existir en `catalog.catalog_definition` y `catalog.catalog_item`:

- `THIRD_PARTY_ROLE`
- `PERSON_TYPE`
- `ITEM_TYPE`
- `DIAN_DOCUMENT_TYPE`
- `TAX_RESPONSIBILITY`
- `TAX_REGIME`
- `SALES_TAX`
- `PAYMENT_METHOD`
- `VIRTUAL_WALLET`
- `FISCAL_DOCUMENT_TYPE`
- `FISCAL_ENVIRONMENT`
- `PAYROLL_CONTRACT_TYPE`
- `PAYROLL_WORKER_CLASSIFICATION`
- `PAYROLL_PAYMENT_FREQUENCY`
- `PAYROLL_EARNING_TYPE`
- `PAYROLL_DEDUCTION_TYPE`

DIVIPOLA se mantiene en tablas relacionales:

- `catalog.department`
- `catalog.municipality`

## Nomina

Tablas implementadas:

- `payroll.payroll_settings`
- `payroll.worker`
- `payroll.daily_labor_payment`
- `payroll.electronic_payroll_document`

Tablas objetivo futuras:

- `payroll.contract`
- `payroll.payroll_period`
- `payroll.payroll_settlement`
- `payroll.payroll_settlement_line`

Reglas:

- `payroll_settings.electronic_payroll_enabled=false` bloquea la generacion de documento soporte electronico mock.
- Los pagos diarios verbales exigen `legal_notice_accepted=true`.
- `INDEPENDENT_CONTRACTOR` se contabiliza como egreso/proveedor o gasto operativo.
- Todas las tablas de nomina incluyen `company_id` y auditoria.

## Inventario Y Consumo De Insumos

- Los servicios facturables se almacenan en `inventory.product` con `item_type='SERVICE'`, `sale_enabled=true` y `stock_tracked=false`.
- Los insumos controlados se almacenan en `inventory.product` con `item_type='SUPPLY'` y `stock_tracked=true`.
- La relacion sugerida servicio-insumo usa `inventory.service_supply_reference`; no representa una receta automatica ni descuenta stock por si sola.
- El consumo asistido confirmado por usuario reutiliza `inventory.inventory_movement` con `movement_type='CONSUMPTION_OUT'` y `source_document_type='MANUAL_SUPPLY_CONSUMPTION'`.
- No se requiere tabla nueva para TASK-113; la trazabilidad se conserva con `source_document_id`, `idempotency_key`, `reason`, `created_by` y kardex.

## Contabilidad

El modelo contable debe cubrir:

- Ingresos.
- Egresos.
- Costos de operacion.
- Activos.
- Cuentas por cobrar.
- Cuentas por pagar.
- Asientos de nomina y pagos diarios.

Si una regla PUC no existe, el comando falla con error estructurado y auditoria.

## Datos iniciales

Permitido:

- Usuario `ROOT` dummy para pruebas locales cuando la variable de entorno lo habilite.
- Catalogos regulatorios/operativos mediante migraciones controladas.

No permitido:

- Empresas demo.
- Administradores empresariales demo.
- Terceros demo.
- Productos demo.
- Resoluciones demo.
- Ventas demo.
- Catalogos hardcodeados en frontend.

## Sesion Web Productiva

Tabla implementada:

- `bff.secure_sessions`

Reglas:

- `secure_sessions.session_type='OAUTH_ATTEMPT'` conserva `state`, `nonce` y `code_verifier` dentro de payload cifrado temporal. No guarda passwords.
- `secure_sessions.session_type='USER_SESSION'` conserva usuario, claims minimos, expiracion, token interno y tokens Cognito dentro de payload cifrado.
- `secure_sessions.id` guarda hash SHA-256 del identificador opaco entregado en cookie; la DB no guarda el valor de cookie en claro.
- La cookie del navegador contiene solo el identificador opaco de sesion, no tokens.
- CSRF se modela como token rotado asociado a sesion; no otorga autenticacion por si solo.
- Las sesiones vencidas, revocadas o comprometidas deben invalidarse server-side.
- Los tokens cifrados no deben aparecer en reportes, auditoria, errores ni logs.
- En AWS productivo se puede reemplazar la persistencia PostgreSQL por un store administrado equivalente con TTL, siempre que mantenga cifrado, auditoria y aislamiento.

Estado actual: `bff-service` tiene migracion `V001__create_bff_secure_sessions.sql` y usa `BFF_SESSION_STORE=jdbc` por defecto. `BFF_SESSION_STORE=memory` queda reservado para fallback local/test.

### identity.user_account y Cognito

- `identity.user_account.cognito_subject` guarda el claim `sub` de Cognito cuando el usuario productivo ya fue vinculado.
- El campo es opcional para soportar root/local/E2E y usuarios provisionados antes del primer login Cognito.
- Existe indice unico parcial sobre `cognito_subject` cuando no es nulo, evitando que dos usuarios locales apunten al mismo sujeto Cognito.
- El primer login Cognito solo enlaza `sub` contra un usuario activo previamente creado por correo; no se crean usuarios automaticamente desde claims externos.

## Licenciamiento

Tabla existente:

- `tenant.company_license`

Campos principales:

- `company_id`: empresa contratante.
- `plan_code`: nombre comercial o plantilla base (`BASIC`, `POS`, `FULL`, `CUSTOM`).
- `status`: `ACTIVE`, `SUSPENDED`, `EXPIRED`, `CANCELLED`.
- `valid_from`, `valid_to`: vigencia comercial.
- `max_users`, `max_monthly_documents`: limites opcionales.
- `enabled_modules`: arreglo de codigos tecnicos de modulos contratados.

Reglas:

- `enabled_modules` usa codigos en ingles para contrato tecnico, pero la UI muestra etiquetas en espanol.
- Licencias existentes sin `enabled_modules` deben migrarse a arreglo vacio o valor explicito segun migracion aprobada; una licencia sin modulos no habilita operacion empresarial.
- ROOT no depende de `tenant.company_license`.

## Configuracion DIAN por empresa

Tablas objetivo:

- `dian_provider.dian_company_configuration`
- `dian_provider.provider_submission`
- `dian_provider.provider_response`

Reglas:

- `dian_company_configuration.company_id` es obligatorio y aisla cada configuracion por empresa.
- `operation_mode` debe soportar `MOCK` y `SOFTWARE_PROPIO_CLIENTE`; otros modos requieren aprobacion SDD.
- `environment` debe diferenciar `HABILITACION` y `PRODUCCION`.
- La base de datos no almacena certificados, PIN tecnico, claves, tokens ni credenciales en claro.
- La base de datos solo almacena referencias seguras (`certificate_secret_ref`, `software_pin_secret_ref`, `technical_key_secret_ref`), alias, huella, vencimiento, estado, ultima prueba y metadata no sensible.
- Una configuracion activa en modo real exige certificado vigente, referencias seguras completas, resolucion vigente compatible y prueba exitosa o estado de habilitacion aprobado.
- Cada mutacion de configuracion DIAN registra auditoria y nunca incluye secretos en `audit.detail`.
- El modo `MOCK` conserva pruebas locales/E2E, pero no habilita operacion productiva ni valida cumplimiento tecnico DIAN.

## Productizacion operativa

### Datos requeridos por el E2E

La prueba desde cero debe crear datos en las tablas activas de cada bounded context:

- `tenant.company` y `tenant.company_license`.
- `identity.user_account`, `cognito_subject`, roles, permisos y membresias empresariales.
- `catalog.catalog_definition`, `catalog.catalog_item`, `catalog.department`, `catalog.municipality`.
- `thirdparty.third_party`.
- `inventory.product`, `inventory.inventory_movement` y tablas de compra/entrada cuando existan.
- `billing.sale`, lineas de venta, documento fiscal, submission mock y configuracion de consumidor final.
- `accounting.account`, reglas contables y asientos/comprobantes.
- `audit.audit_event`.

### Compras e inventario

Si el flujo de compra no esta completamente modelado, se debe introducir o completar un modelo transaccional con:

- Cabecera de compra por empresa y proveedor.
- Lineas de compra con producto/insumo, cantidad, costo unitario, impuesto y subtotal.
- Estado de compra (`DRAFT`, `CONFIRMED`, `CANCELLED`).
- Movimiento de inventario idempotente al confirmar.
- Cuenta por pagar o egreso segun medio de pago/configuracion.
- Asiento contable balanceado segun regla PUC empresarial.

### Servicios con insumos

- `inventory.service_supply_reference` conserva referencias sugeridas entre servicio e insumos.
- El consumo real queda en `inventory.inventory_movement` con `CONSUMPTION_OUT`, `source_document_id`, `source_document_type`, `reason`, `idempotency_key` y usuario.
- No se deben crear descuentos automaticos por receta sin confirmacion explicita.

### Reportes y licencias

- Los reportes se calculan desde tablas activas de contabilidad, inventario, billing e identidad.
- El uso de licencia mensual se calcula desde documentos fiscales emitidos en `billing` y usuarios activos vinculados en `identity`.
- Las proyecciones event-driven deben poder reconstruirse desde los datos canonicos persistidos.
- Estado actual: `reporting.reporting_inbox_event` y `reporting.reporting_event_projection` son creadas por `reporting-projection-lambda` si no existen. Antes de produccion se recomienda mover esas estructuras a migraciones Flyway gobernadas por un owner de schema aprobado o documentar formalmente la excepcion operacional.

### Depuracion futura

Ninguna tabla legacy debe eliminarse hasta que:

- Exista flujo equivalente activo.
- La tabla no tenga referencias en JPA, repositorios, SQL, migraciones posteriores o scripts.
- Los datos esten vacios o migrados/respaldados.
- Flyway valide sobre base limpia y base local actual.

### Limpieza operativa de `public.*`

Estado TASK-167: los microservicios activos ejecutan Flyway sobre sus esquemas propios (`tenant`, `identity`, `catalog`, `thirdparty`, `inventory`, `billing`, `accounting`, `audit`, `dian_provider`, `payroll`). Ningun microservicio activo gobierna el esquema `public`, por lo que la depuracion de tablas heredadas `public.*` se trata como operacion controlada de base local/ambiente, no como migracion de un bounded context.

Script operativo:

- `scripts/db/drop-empty-legacy-public-tables.sql`

Reglas:

- El script solo elimina tablas `public.*` existentes con cero filas.
- Si una tabla tiene filas, se conserva y queda pendiente de migracion, respaldo o descarte aprobado.
- No se eliminan ni reescriben migraciones Flyway historicas ya aplicadas, para evitar romper validaciones de historial y checksum.

Tablas `public.*` vacias detectadas el 2026-08-19 y candidatas de limpieza segura local: `auditoria`, `accounting_entry`, `accounting_entry_line`, `compra`, `detalle_compra`, `detalle_factura`, `detalle_gasto`, `factura`, `gastos`, `parametros`, `registro_accesos`, `roles`, `usuarios`.

Tablas `public.*` con filas detectadas el 2026-08-19 y pendientes de decision de datos: `accounting_account`, `accounting_rule`, `accounting_rule_line`, `billing_electronic_document_trace_event`, `billing_electronic_pos_document`, `billing_electronic_pos_document_line`, `billing_fiscal_audit_event`, `billing_issuer_profile`, `billing_numbering_resolution`, `billing_provider_submission`, `categoria`, `cliente`, `impuesto`, `metodo_pago`, `pais`, `producto`, `proveedor`, `tipo_gasto`, `tipodocumento`.

## Branding Empresarial Y Marca NexoFiscal

Tabla objetivo en `tenant-service`:

- `tenant.company_branding`

Campos principales:

- `company_id`: PK/FK hacia `tenant.company`.
- `display_name`: nombre visual de la empresa en UI, si difiere de razon social/nombre comercial.
- `primary_color`, `accent_color`: colores opcionales aprobados para tema visual.
- `main_logo_storage_key`: referencia de almacenamiento del logo principal.
- `header_logo_storage_key`: referencia de almacenamiento del logo superior.
- `login_logo_storage_key`: referencia de almacenamiento del logo de login.
- `favicon_storage_key`: referencia de almacenamiento del favicon.
- `main_logo_content_type`, `header_logo_content_type`, `login_logo_content_type`, `favicon_content_type`: tipo MIME validado.
- `main_logo_hash`, `header_logo_hash`, `login_logo_hash`, `favicon_hash`: huella criptografica para trazabilidad e invalidacion de cache.
- `updated_at`, `updated_by`: auditoria minima de actualizacion.

Reglas:

- La base de datos no almacena binarios de logos salvo aprobacion posterior; almacena referencias y metadata.
- Los archivos deben vivir en almacenamiento seguro: volumen local controlado para desarrollo o S3 privado con KMS en AWS.
- Toda mutacion se refleja en `audit.audit_event` con `resource_type='COMPANY_BRANDING'`.
- Si no existe registro de branding, la SPA usa fallback `NexoFiscal`.

## Reporting Service Objetivo

Esquema objetivo:

- `reporting.report_definition`
- `reporting.report_execution`
- `reporting.report_export`
- `reporting.report_export_download`

Campos principales de `report_definition`:

- `code`: codigo tecnico (`SALES_BY_SELLER`, `PURCHASES_SUMMARY`, etc.).
- `label`: etiqueta visible por defecto.
- `description`: descripcion funcional.
- `required_modules`: modulos de licencia requeridos.
- `required_permissions`: permisos RBAC requeridos.
- `date_range_required`: indica si `from/to` son obligatorios.
- `allowed_chart_types`: `TABLE`, `BAR`, `LINE`, `PIE`, `KPI`.
- `export_formats`: `CSV`, `XLSX`, `PDF` cuando aplique.
- `active`: estado operacional.

Campos principales de `report_execution`:

- `id`, `company_id`, `report_code`.
- `requested_by`, `requested_at`.
- `from_date`, `to_date`.
- `filters_json`: filtros normalizados sin datos sensibles.
- `chart_type`.
- `status`: `SUCCESS`, `FAILED`, `VALIDATION_ERROR`.
- `correlation_id`.

Campos principales de `report_export`:

- `id`, `company_id`, `report_code`, `format`.
- `status`: `PROCESSING`, `READY`, `FAILED`, `EXPIRED`.
- `storage_key`, `content_type`, `file_name`, `content_hash`.
- `requested_by`, `requested_at`, `ready_at`, `expires_at`.
- `error_code`, `error_message` sanitizado.

Reglas:

- Las proyecciones event-driven (`reporting.reporting_inbox_event`, `reporting.reporting_event_projection`) son reconstruibles desde eventos canonicos y no reemplazan datos fuente.
- Los reportes por vendedor resuelven elegibilidad de usuarios desde `identity-service` por rol/permiso de ventas.
- Los reportes deben aislar siempre por `company_id`.
- Las exportaciones pesadas pueden procesarse asincronamente con SQS/EventBridge/Lambda o worker del `reporting-service`, segun la tarea aprobada.

## Artefactos POS, Historico E Impresion

Tablas objetivo en `billing-service`:

- `billing.fiscal_document_artifact`
- `billing.pos_print_job`

Campos principales de `fiscal_document_artifact`:

- `id`, `company_id`, `document_id`.
- `artifact_type`: `PRINTABLE_HTML`, `XML`, `JSON_METADATA`, `QR`, `PDF`.
- `storage_key` o `storage_uri`.
- `content_type`, `file_name`, `content_hash`.
- `generated_at`, `generated_by`.
- `active`.

Campos principales de `pos_print_job`:

- `id`, `company_id`, `document_id`.
- `paper_width_mm`: 58 u 80 inicialmente.
- `strategy`: `WEB_PRINT` inicialmente.
- `status`: `REQUESTED`, `OPENED`, `PRINTED`, `FAILED`, `CANCELLED`.
- `requested_by`, `requested_at`, `printed_at`.
- `error_message` sanitizado.
- `correlation_id`.

Reglas:

- El historico de ventas debe consultar datos canonicos de `billing.sale`, lineas, documento fiscal y artefactos.
- Las reimpresiones no recrean documentos fiscales; solo registran nuevo `pos_print_job`.
- Cada descarga e impresion/reimpresion queda auditada.
- Los conectores directos de impresora no se modelan como activos hasta completar tarea de hardware y seguridad.

## Reportes Asincronos Avanzados

Estado: modelo objetivo para Fase 24; no implementado hasta completar TASK-145 a TASK-163.

Tablas objetivo en `reporting-service`:

- `reporting.report_export_job`
- `reporting.report_export_download_token`
- `reporting.report_export_download_attempt`
- `reporting.report_export_notification`

Campos principales de `report_export_job`:

- `id`: UUID del job.
- `company_id`: empresa propietaria.
- `requested_by`: usuario solicitante.
- `report_code`: codigo tecnico del reporte.
- `format`: `CSV`, `XLSX`, `PDF` cuando aplique.
- `chart_type`: tipo de visualizacion solicitada si aplica.
- `filters_json`: filtros normalizados sin secretos ni payloads excesivos.
- `status`: `PENDING`, `PROCESSING`, `READY`, `FAILED`, `EXPIRED`, `REVOKED`.
- `storage_bucket_reference`: alias/referencia de bucket, no URL publica.
- `storage_key`: key privada del objeto S3 o referencia cifrada equivalente.
- `content_type`, `file_name`, `content_hash`, `size_bytes`.
- `requested_at`, `processing_started_at`, `ready_at`, `expires_at`, `revoked_at`.
- `error_code`, `error_message`: error sanitizado para UI/soporte.
- `correlation_id`.

Campos principales de `report_export_download_token`:

- `id`: UUID interno.
- `job_id`, `company_id`, `user_id`.
- `token_hash`: hash del token enviado al usuario; el token en claro no se almacena.
- `status`: `ACTIVE`, `USED`, `EXPIRED`, `REVOKED`.
- `expires_at`: TTL del enlace intermediado configurado por `REPORT_LINK_TOKEN_TTL_HOURS`.
- `created_at`, `used_at`, `revoked_at`.
- `last_correlation_id`.

Campos principales de `report_export_download_attempt`:

- `id`, `job_id`, `company_id`, `token_id`.
- `requested_by` cuando se pueda resolver.
- `result`: `SUCCESS`, `DENIED`, `EXPIRED`, `REVOKED`, `FAILED`.
- `requested_at`, `ip_hash`, `user_agent_hash`, `correlation_id`.
- `failure_code`: motivo funcional sanitizado.

Campos principales de `report_export_notification`:

- `id`, `job_id`, `company_id`, `recipient_user_id`, `recipient_email_hash`.
- `channel`: `EMAIL`.
- `status`: `PENDING`, `SENT`, `FAILED`.
- `provider_message_id`: referencia tecnica no sensible.
- `sent_at`, `error_code`, `correlation_id`.

Reglas:

- El enlace publico se construye con `APP_PUBLIC_BASE_URL`, no se persiste como URL canonica obligatoria.
- La URL prefirmada S3 se genera solo al hacer clic y no se guarda como dato permanente.
- El TTL inicial de la URL prefirmada es `REPORT_DOWNLOAD_PRESIGNED_TTL_SECONDS=5`.
- Los archivos quedan en S3 privado con KMS y politica de retencion configurable.
- Las tablas deben indexarse por `company_id`, `requested_by`, `status`, `report_code`, `requested_at` y `expires_at`.
- Los jobs y tokens deben respetar aislamiento multiempresa y RBAC.
