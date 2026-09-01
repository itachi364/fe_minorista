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

### Configuracion contable empresarial

- El modulo `Configuracion contable` no introduce tablas nuevas en esta fase; consulta y opera las tablas existentes del schema `accounting`.
- `accounting.account` almacena el plan de cuentas por empresa, con codigos PUC o parametrizaciones aprobadas.
- `accounting.accounting_rule` y `accounting.accounting_rule_line` almacenan reglas por evento de negocio y lineas de partida doble.
- Para cerrar una venta fiscal, debe existir una regla activa `SALE_CONFIRMED` por `company_id`; de lo contrario `billing-service` debe bloquear la operacion antes de numeracion fiscal, DIAN/mock, inventario y asiento.
- La inicializacion `POST /api/v1/accounting-setup/basic` crea/reactiva cuentas y reglas minimas de prueba, sin reemplazar una parametrizacion contable profesional.
- TASK-223 mantiene las mismas tablas y agrega comportamiento transaccional batch implementado sobre `accounting.account`, `accounting.accounting_rule` y `accounting.accounting_rule_line`.
- En UI, `accounting.accounting_rule_line` debe presentarse como `movimiento contable`; en base de datos puede conservar el nombre tecnico `line`.
- Una cuenta o regla con uso historico en `accounting.accounting_entry_line` no debe eliminarse fisicamente; debe inactivarse o versionarse para conservar trazabilidad.
- Las creaciones batch deben persistirse como una unidad atomica por empresa. Si una cuenta, regla o movimiento contable falla validacion, ninguna fila del lote debe quedar guardada.
- TASK-224 agrega `accounting.accounting_entry.accounting_rule_id` nullable para que los nuevos asientos rastreen la regla exacta usada.
- El uso de cuentas se calcula desde `accounting.accounting_entry_line.account_id`.
- El uso de reglas se calcula desde `accounting.accounting_entry.accounting_rule_id`.
- Las reglas historicas previas a TASK-224 pueden tener asientos sin `accounting_rule_id`; ese historial se conserva como no trazado y no debe inferirse automaticamente.
- Cuentas y reglas con `usageCount > 0` no se actualizan estructuralmente ni se inactivan desde el modulo de configuracion.

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

Estado TASK-178: los microservicios activos ejecutan Flyway sobre sus esquemas propios (`tenant`, `identity`, `catalog`, `thirdparty`, `inventory`, `billing`, `accounting`, `audit`, `dian_provider`, `payroll`). Ningun microservicio activo gobierna el esquema `public`, por lo que la depuracion de tablas heredadas `public.*` se trata como operacion controlada de base local/ambiente, no como migracion de un bounded context.

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

## DIAN Real Parametrizable por Empresa

Estado: implementado en Fase 20 TASK-153 a TASK-163 mediante Flyway V003 de `dian-provider-service`, con alcance local seguro y destino productivo equivalente S3/KMS.

Evolucion de tablas existentes:

- `dian_provider.provider_submission` debe ampliar su alcance para soportar modo `MOCK` y `REAL`, ambiente, tipo documental, clave de idempotencia, estado tecnico, tracking DIAN, ultimo error sanitizado, contador de reintentos y timestamps de ciclo.
- `billing.electronic_document` conserva el estado fiscal de negocio visible para ventas/historico; no almacena secretos ni payload completo DIAN.
- `billing.electronic_document_artifact` conserva artefactos visibles de negocio, descargas y representaciones imprimibles; los artefactos tecnicos de transporte pueden vivir en `dian_provider`.

Tablas objetivo en `dian-provider-service`:

- `dian_provider.dian_submission_event`
- `dian_provider.dian_submission_artifact`
- `dian_provider.dian_technical_validation_result`

Campos principales de `dian_submission_event`:

- `id`, `company_id`, `submission_id`, `document_id`.
- `event_type`: `XML_BUILT`, `IDENTIFIERS_CALCULATED`, `SIGNED`, `VALIDATED`, `TRANSMITTED`, `ACCEPTED`, `REJECTED`, `RETRY_SCHEDULED`, `FAILED`.
- `status`: `SUCCESS`, `FAILURE`, `PENDING`.
- `dian_code`, `dian_message`: codigos/mensajes sanitizados.
- `correlation_id`, `created_at`.

Campos principales de `dian_submission_artifact`:

- `id`, `company_id`, `submission_id`, `document_id`.
- `artifact_type`: `UNSIGNED_XML`, `SIGNED_XML`, `ATTACHED_DOCUMENT`, `ZIP`, `QR`, `GRAPHIC_REPRESENTATION`, `APPLICATION_RESPONSE`, `DIAN_RESPONSE`.
- `storage_bucket_reference`, `storage_key`: referencias privadas, no URL publica.
- `content_type`, `file_name`, `content_hash`, `size_bytes`.
- `created_at`, `created_by`.

Campos principales de `dian_technical_validation_result`:

- `id`, `company_id`, `submission_id`, `document_id`.
- `validation_type`: `XSD`, `SCHEMATRON`, `CODE_LIST`, `SIGNATURE`.
- `result`: `PASSED`, `FAILED`, `SKIPPED`.
- `rule_code`, `message`: detalle tecnico sanitizado.
- `source_version`, `validated_at`.

Reglas:

- `company_id` es obligatorio en toda tabla DIAN real.
- No se persisten certificado, PIN, claves tecnicas ni credenciales.
- El storage de artefactos debe ser privado y cifrado.
- `idempotency_key` debe evitar duplicados por documento fiscal/intento logico.
- Reintentos no pueden duplicar movimientos de inventario ni asientos contables.

## Reportes Asincronos Avanzados

Estado: implementacion inicial local de Fase 24 completada en `reporting-service`. La evolucion productiva conserva S3/KMS/SES/SQS como adaptadores externos sin cambiar contratos de dominio.

Tablas implementadas en `reporting-service`:

- `reporting.report_export_job`
- `reporting.report_export_download_attempt`

Campos principales de `report_export_job`:

- `id`: UUID del job.
- `company_id`: empresa propietaria.
- `requested_by_user_id`: usuario solicitante cuando el BFF lo provee.
- `report_code`: codigo tecnico del reporte.
- `format`: `CSV`, `XLS`.
- `chart_type`: tipo de visualizacion solicitada si aplica.
- `filters_json`: filtros normalizados sin secretos ni payloads excesivos.
- `status`: `PENDING`, `PROCESSING`, `READY`, `FAILED`, `EXPIRED`, `REVOKED`.
- `storage_key`: key privada del objeto exportado o referencia equivalente.
- `filename`, `content_type`, `file_size`.
- `requested_at`, `started_at`, `completed_at`, `expires_at`.
- `token_hash`: hash del token enviado al usuario; el token en claro no se almacena.
- `token_expires_at`: TTL del enlace intermediado configurado por `REPORT_LINK_TOKEN_TTL_HOURS`.
- `notification_status`, `notification_message`: estado funcional de notificacion.
- `failure_message`: error sanitizado para UI/soporte.
- `download_attempts`, `last_downloaded_at`.
- `created_at`, `updated_at`.

Campos principales de `report_export_download_attempt`:

- `id`, `job_id`, `company_id`.
- `attempted_at`.
- `result`: `SUCCESS`, `DENIED`, `FAILED`.
- `detail`: motivo funcional sanitizado.

Reglas:

- El enlace publico se construye con `APP_PUBLIC_BASE_URL`, no se persiste como URL canonica obligatoria.
- La URL prefirmada S3 se genera solo al hacer clic y no se guarda como dato permanente.
- El TTL inicial de la URL prefirmada es `REPORT_DOWNLOAD_PRESIGNED_TTL_SECONDS=5`.
- Los archivos quedan en volumen privado local para Docker; en AWS quedan en S3 privado con KMS y politica de retencion configurable.
- Las tablas deben indexarse por `company_id`, `requested_by`, `status`, `report_code`, `requested_at` y `expires_at`.
- Los jobs y tokens deben respetar aislamiento multiempresa y RBAC.

## Ajustes RBAC Operativos

El rol empresarial `OWNER` se persiste en las tablas existentes de `identity-service`:

- `identity.company_role`
- `identity.company_role_permission`
- `identity.company_user_role_assignment`

Reglas de persistencia:

- Debe existir a lo sumo un rol activo/visible con `company_id` y nombre tecnico `OWNER` por empresa.
- `system_seed=true` identifica que el rol fue creado por el sistema al provisionar el administrador inicial.
- `company_role_permission.permission_code` solo puede contener permisos `COMPANY`; la restriccion actual que evita `GLOBAL_%` se mantiene.
- `company_user_role_assignment` debe asignar el rol `OWNER` materializado al administrador inicial de forma idempotente.
- La membresia legacy `identity.company_membership.roles=["OWNER"]` se conserva temporalmente por compatibilidad hasta que se retire la ruta legacy completa.

## Configuracion Fiscal Activa

- `billing.issuer_profile.active` permite conservar historico de emisores fiscales por empresa.
- Regla funcional: solo un `issuer_profile.active=true` por `company_id`. El caso de uso desactiva emisores hermanos antes de guardar o activar el nuevo emisor.
- `billing.numbering_resolution.active` permite conservar historico de resoluciones y consecutivos usados.
- Regla funcional: solo una resolucion activa por `company_id`, `document_type` y `environment`. El caso de uso desactiva resoluciones hermanas antes de guardar o activar la nueva resolucion.
- No se eliminan registros al inactivar, porque soportan trazabilidad fiscal, historico de ventas y auditoria.

## Politica Fiscal, PIN Operacional y Override

Estado: implementacion inicial de Fase 27.

Tablas implementadas o reutilizadas:

- `billing.company_fiscal_policy`
- `billing.sale_document_type_override`
- `identity.operational_pin`
- `billing.fiscal_note`

Campos principales de `company_fiscal_policy`:

- `company_id`: UUID, PK logica por empresa.
- `default_pos_document_type`: `ELECTRONIC_INVOICE` por defecto recomendado.
- `allow_sale_document_type_override`: boolean.
- `requires_pin_for_override`: boolean.
- `active`: boolean.
- `created_at`, `updated_at`, `updated_by`, `correlation_id`.

Campos principales de `sale_document_type_override`:

- `id`, `company_id`, `sale_id`.
- `from_document_type`, `to_document_type`.
- `seller_user_id`, `authorizer_user_id`.
- `reason`: texto funcional obligatorio, sanitizado.
- `authorized_at`, `correlation_id`.
- `result`: `APPROVED`, `REJECTED`, `FAILED`.

Campos principales de `operational_pin`:

- `user_id`, `company_id`.
- `pin_hash`: hash fuerte; nunca PIN plano.
- `status`: `ACTIVE`, `LOCKED`, `CHANGE_REQUIRED`, `DISABLED`.
- `failed_attempts`: contador consecutivo maximo 3.
- `created_at`, `changed_at`, `locked_at`, `unlocked_at`, `last_used_at`.
- `unlocked_by`, `correlation_id`.

Campos principales de `fiscal_note`:

- `id`, `company_id`, `source_document_id`, `document_type`.
- `resolution_id`, `prefix`, `document_number`.
- `status`, `provider_status`, `cufe_cude`, `qr_content`, `tracking_id`.
- `reason_code`, `reason`, `subtotal`, `tax_total`, `total`.
- `created_by`, `created_at`, `submitted_at`, `validated_at`, `correlation_id`.

Reglas:

- `billing.numbering_resolution` conserva la regla de una activa por `company_id`, `document_type` y `environment`; no es una resolucion global.
- `company_fiscal_policy.default_pos_document_type` debe tener resolucion activa compatible antes de confirmar ventas.
- `operational_pin.failed_attempts >= 3` bloquea el PIN y exige desbloqueo administrativo.
- Desbloquear PIN no lo deja usable: debe quedar `CHANGE_REQUIRED` hasta que el titular lo cambie.
- Notas credito, debito y ajuste POS deben conservar numeracion propia y no reutilizar consecutivos.

## Reportes normalizados fase 30

Estado: diseno pendiente, sin nuevas tablas obligatorias en primera iteracion.

Reglas:

- La normalizacion de TASK-234 usa datos transaccionales existentes y produce datasets en memoria dentro de `reporting-service`.
- No se crean tablas nuevas para `SALES_BY_PRODUCT` ni `SALES_BY_SELLER` hasta que volumen, latencia o costo justifiquen proyecciones materializadas.
- Si se implementan proyecciones futuras, deben quedar en schema `reporting`, con `company_id`, `report_code`, periodo, metrica, dimensiones, auditoria y fecha de refresco.
- Las exportaciones pesadas siguen usando `reporting.report_export_job` y `reporting.report_export_download_attempt`.
- Los datasets normalizados no deben persistir campos tecnicos innecesarios para UI como `idempotency_key`, `created_by` o rutas anidadas de documentos fiscales.

## Bugs Fiscales Y Finanzas Operativas

Estado: diseno pendiente para TASK-238 a TASK-245.

### Resoluciones fiscales

Tablas reutilizadas:

- `billing.numbering_resolution`
- `billing.electronic_document`
- `billing.fiscal_note`
- tablas de auditoria existentes.

Reglas de persistencia:

- Una resolucion sin documentos asociados puede eliminarse fisicamente si la accion queda auditada.
- Una resolucion con documentos, notas fiscales o trazas asociadas no se elimina; solo cambia a `active=false`.
- La condicion `used` se calcula desde documentos fiscales que referencian la resolucion o, si el modelo actual aun no tiene FK directa, desde numero/prefijo/tipo de documento asociados al historico.
- El cierre de venta solo consulta resoluciones `active=true`, con rango vigente, ambiente compatible y `document_type` compatible con la politica fiscal de la venta.

### Compras, reabastecimiento, activos y gastos

Modelo objetivo:

- `inventory.purchase`: encabezado de compra operativa.
  - `classification`: `INVENTORY_REPLENISHMENT`, `ASSET_PURCHASE`, `OPERATING_EXPENSE`.
  - `supplier_id`, `purchase_date`, `payment_method_code`, `status`, `subtotal`, `tax_total`, `total`, `correlation_id`.
- `inventory.purchase_line`: detalle de compra.
  - `purchase_id`, `product_id` opcional, `description`, `quantity`, `unit_cost`, `tax_code`, `tax_rate`, `line_total`.
- `accounting.accounting_expense`: egresos que no incrementan inventario.
  - `expense_type`: `OPERATING_EXPENSE` o `ASSET_PURCHASE`.
  - `supplier_id`, `expense_date`, `concept`, `payment_condition`, `due_date`, `evidence_url`, `subtotal`, `tax_total`, `total`, `status`.
- Activos del negocio en etapa actual: se contabilizan con `expense_type=ASSET_PURCHASE` y cuenta PUC `1520`; una tabla de maestro/depreciacion de activos queda como evolucion futura si se requiere control patrimonial avanzado.

Reglas:

- Reabastecimiento incrementa stock y genera asiento por `INVENTORY_REPLENISHMENT_CONFIRMED`.
- Compra de activo no incrementa stock vendible y genera asiento por `ASSET_PURCHASE_CONFIRMED`.
- Gasto operativo no crea movimiento de inventario y genera asiento por `OPERATING_EXPENSE_CONFIRMED`.
- El boton `Consultar compras` solo debe leer datos persistidos de compras actuales; nunca catalogos locales ni estado inicial de frontend.

### Pagos diarios de empleados

Tablas reutilizadas:

- `payroll.worker`
- `payroll.employment_contract`
- `payroll.payroll_payment` o equivalente vigente del microservicio.
- `accounting.accounting_entry`
- `accounting.accounting_entry_line`
- `accounting.accounting_rule`

Reglas:

- Todo pago diario/verbal confirmado se contabiliza con `DAILY_PAYROLL_PAID`.
- Si la regla no existe o esta incompleta, el pago no queda confirmado.
- Las lineas contables deben permitir clasificar el pago como gasto operacional/egreso para reportes diarios.

### Deudores y cuentas por cobrar

Modelo objetivo:

- `accounting.account_receivable`
  - `id`, `company_id`, `debtor_third_party_id`, `source_type`, `source_id`, `issue_date`, `due_date`, `concept`, `original_amount`, `balance`, `status`, `created_at`, `updated_at`.
- `accounting.account_receivable_payment`
  - `id`, `company_id`, `account_receivable_id`, `payment_date`, `payment_method_code`, `amount`, `accounting_entry_id`, `created_at`.

Reglas:

- Estados: `PENDING`, `PARTIALLY_PAID`, `PAID`, `OVERDUE`.
- Los abonos no pueden superar el saldo pendiente.
- Cada registro y abono crea auditoria y asiento contable.

### Reporte diario de ganancias y gastos

Persistencia:

- Primera iteracion sin tabla materializada; `reporting-service` consulta ventas confirmadas, movimientos de costo y asientos contables por periodo.
- Si el volumen lo exige, se creara proyeccion en `reporting.daily_financial_summary` con `company_id`, `business_date`, metricas monetarias y fecha de refresco.

Metricas minimas:

- Ingresos por ventas.
- Costo de ventas.
- Gastos operativos.
- Pagos diarios de empleados.
- Otros egresos.
- Utilidad/perdida neta.
