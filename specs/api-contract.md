# API Contract: Microservicios

## Objetivo

Definir contratos iniciales entre microservicios para una plataforma multiempresa de facturacion electronica, POS electronico, inventario y contabilidad.

Estos contratos son la base para futuros archivos OpenAPI por servicio.

## Estado de contratos y OpenAPI

Los servicios incluyen `springdoc-openapi-starter-webmvc-ui`, por lo que pueden exponer documentacion runtime en `/v3/api-docs` y `/swagger-ui.html` cuando el servicio esta levantado. Eso no reemplaza los contratos SDD versionados de este archivo ni constituye todavia un artefacto OpenAPI formal publicado en el repositorio.

Pendiente aprobado: generar, versionar y validar OpenAPI por servicio/BFF cuando los DTO publicos terminen de estabilizarse. Hasta entonces, `specs/api-contract.md` es la fuente contractual principal.

## Convenciones generales

- Versionado HTTP: `/api/v1`.
- Formato: JSON UTF-8.
- Fechas: ISO-8601.
- Montos: decimal string o numero JSON con precision controlada por backend.
- IDs: `uuid` recomendado para nuevas tablas y contratos.
- Autenticacion externa: `Authorization: Bearer <token>`.
- Aislamiento multiempresa: `X-Company-Id` obligatorio en APIs de negocio.
- Trazabilidad: `X-Correlation-Id` obligatorio o generado por gateway.
- Idempotencia: `Idempotency-Key` obligatorio en emision fiscal, movimientos de inventario y contabilizacion.
- Unidad de despliegue: un artefacto y contenedor por microservicio/bounded context, no por endpoint individual.
- Comunicacion vigente: REST sincrono para comandos/consultas inmediatas y eventos Outbox/Inbox hacia EventBridge/SQS + Lambda para efectos posteriores, auditoria, reportes y reintentos productivos.

## Microservicios fisicos objetivo

| Microservicio | Responsabilidad principal | Artefacto objetivo |
|---|---|---|
| `tenant-service` | Empresas y estado del tenant | `services/tenant-service` |
| `identity-service` | Usuarios, roles y permisos | `services/identity-service` |
| `catalog-service` | Catalogos oficiales y configurables | `services/catalog-service` |
| `thirdparty-service` | Clientes y proveedores | `services/thirdparty-service` |
| `inventory-service` | Productos, costos, compras, stock y kardex | `services/inventory-service` |
| `billing-service` | Ventas, POS, factura electronica, notas y numeracion | `services/billing-service` |
| `dian-provider-service` | Mock DIAN y conexion real DIAN parametrizable por empresa | `services/dian-provider-service` |
| `accounting-service` | PUC, reglas, asientos, libro diario y mayor | `services/accounting-service` |
| `audit-service` | Auditoria fiscal y tecnica | `services/audit-service` |
| `payroll-service` | Empleados, contratos, pagos diarios, liquidaciones y nomina electronica opcional | `services/payroll-service` |

## Headers obligatorios

| Header | Requerido | Aplica | Descripcion |
|---|---:|---|---|
| `Authorization` | Si | APIs expuestas | Token de usuario o servicio. |
| `X-Company-Id` | Si | Datos de negocio | Tenant/empresa propietaria de la operacion. |
| `X-User-Id` | Si | Acciones autenticadas | Usuario autenticado que ejecuta la accion, derivado del login/BFF. |
| `X-Correlation-Id` | Si | Todas | Trazabilidad entre servicios. |
| `Idempotency-Key` | Si | Comandos criticos | Evita duplicados por reintentos. |

Nota productiva TASK-164/TASK-171:

- El navegador no debe construir ni enviar `Authorization` en JavaScript. La autenticacion publica usa cookie opaca `HttpOnly` emitida por el BFF.
- El BFF traduce la sesion publica a headers internos seguros hacia microservicios: `Authorization` de servicio cuando aplique, `X-User-Id`, `X-Company-Id`, `X-Correlation-Id` e `Idempotency-Key`.
- Las mutaciones autenticadas por cookie requieren token CSRF en header aprobado, por ejemplo `X-CSRF-Token`.
- Las mutaciones criticas de configuracion plataforma/empresa con sesion Cognito requieren evidencia MFA (`mfaAuthenticated=true`) en la sesion server-side del BFF. Ventas POS se mantienen operativas sin reto MFA por transaccion.

## Error estandar

```json
{
  "timestamp": "2026-05-11T22:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "La solicitud no cumple las reglas de validacion.",
  "correlationId": "7b6f1c88-7bb9-40a0-93c0-7df2e50975ef",
  "details": [
    {
      "field": "lines[0].quantity",
      "message": "La cantidad debe ser mayor a cero."
    }
  ]
}
```

Codigos base:

- `VALIDATION_ERROR`
- `BUSINESS_RULE_VIOLATION`
- `RESOURCE_NOT_FOUND`
- `DUPLICATE_RESOURCE`
- `UNAUTHORIZED`
- `FORBIDDEN`
- `TENANT_ACCESS_DENIED`
- `EXTERNAL_PROVIDER_ERROR`
- `IDEMPOTENCY_CONFLICT`
- `INTERNAL_ERROR`

## bff-service

Responsabilidad: frontera publica consumida por la SPA y expuesta por API Gateway en produccion.

Estado TASK-063:

- Microservicio fisico inicial implementado en `services/bff-service`.
- No tiene persistencia ni reglas fiscales propias.
- Expone `/api/v1/**` solo para rutas aprobadas y enruta hacia el microservicio dueno del contrato.
- Propaga `Authorization`, `X-Company-Id`, `X-User-Id`, `X-Correlation-Id`, `Idempotency-Key`, `Content-Type` y `Accept`.
- Estado TASK-090: registra auditoria best-effort para `POST`, `PUT`, `PATCH` y `DELETE` que pasen por `/api/v1/**`, sin persistir payloads ni secretos.
- Para `POST /api/v1/companies` sin `X-Company-Id`, el BFF toma el `id` de la empresa creada en la respuesta y registra auditoria contra esa empresa.
- Filtra cabeceras de respuesta a `Content-Type` y `X-Correlation-Id`.
- Rechaza rutas internas no publicas, por ejemplo `/api/v1/provider/**`.

Reglas:

- El frontend debe consumir siempre el BFF.
- Los microservicios internos no deben exponerse al navegador.
- El BFF no debe implementar reglas de negocio que pertenezcan a billing, inventory, accounting, tenant, identity, catalog, thirdparty o audit.
- Un fallo de servicio interno debe responder como error publico estructurado sin stack trace ni detalles de infraestructura.
- La falla del registro de auditoria no debe tumbar la accion principal; debe quedar como integracion best-effort hasta completar eventing asincrono.
- Estado TASK-065: `/api/v1/auth/**` y `/api/v1/me/**` se enrutan a `identity-service`; `/api/v1/companies/{companyId}/license/**` se enruta a `tenant-service`; `/api/v1/companies/{companyId}/memberships`, `/api/v1/companies/{companyId}/users/{userId}/roles` y `/api/v1/companies/{companyId}/permissions` se enrutan a `identity-service`.
- Estado TASK-122: `billing-service` queda protegido por permisos efectivos en BFF. `SALES_CREATE` autoriza registrar y confirmar venta POS con emision electronica asociada; `FISCAL_DOCUMENTS_ISSUE` autoriza configuracion fiscal, resoluciones, notas, ajustes y operaciones fiscales avanzadas.

### Auth productivo Cognito + BFF session

Endpoints publicos objetivo:

- `GET /api/v1/auth/login-url`
- `GET /api/v1/auth/callback?code=&state=`
- `GET /api/v1/auth/session`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/refresh` opcional si se requiere renovacion explicita server-side.

`GET /api/v1/auth/login-url`:

Respuesta en `AUTH_MODE=cognito`:

```json
{
  "authMode": "cognito",
  "url": "https://<cognito-domain>/oauth2/authorize?...",
  "state": "opaque-login-state"
}
```

## Gobierno documental

TASK-249 no introduce endpoints, payloads ni eventos nuevos. Su alcance es documental: README como guia operativa del repositorio y `specs/` como fuente de trazabilidad funcional, tecnica, contractual, de persistencia e infraestructura.

Reglas:

- El BFF genera `state`, `nonce`, PKCE `code_verifier` y `code_challenge`.
- El `code_verifier` se conserva de forma temporal en cookie opaca `NF_OAUTH_ATTEMPT`, `HttpOnly`, y no se retorna a la SPA.
- La URL usa Authorization Code Grant con PKCE y scopes minimos `openid`, `email`, `profile` y los aprobados por arquitectura.
- En `AUTH_MODE=local`, el endpoint retorna `{ "authMode": "local" }` para desarrollo/E2E y no crea cookie OAuth.

`GET /api/v1/auth/callback`:

Reglas:

- Valida `state` y expiracion del intento de login.
- Intercambia `code` por tokens en Cognito desde el BFF.
- Solicita a `identity-service` una sesion interna para el usuario local activo asociado al `sub` Cognito; si no existe vinculo, `identity-service` puede enlazarlo una unica vez contra un usuario activo previamente provisionado por correo.
- Crea sesion server-side con tokens Cognito cifrados y token interno cifrado.
- Emite cookie opaca `HttpOnly; Secure; SameSite=Lax|Strict`.
- Redirige a la SPA sin incluir tokens en query string, fragment, storage ni response body.
- Estado actual: el BFF implementa intercambio contra `/oauth2/token`, consulta `/oauth2/userInfo`, emite sesion interna contra `identity-service`, guarda intentos OAuth/sesiones cifradas en `bff.secure_sessions` cuando `BFF_SESSION_STORE=jdbc` y emite `NF_SESSION`. La base guarda hash del identificador opaco, no el valor de cookie.

`GET /api/v1/auth/session`:

Respuesta:

```json
{
  "userId": "uuid",
  "email": "usuario@example.com",
  "fullName": "Usuario Empresa",
  "globalRoles": [],
  "companies": [
    {
      "companyId": "uuid",
      "companyName": "Empresa SAS",
      "roles": ["OWNER"],
      "permissions": ["SALES_CREATE"]
    }
  ],
  "csrfToken": "opaque-non-secret-token",
  "expiresAt": "2026-08-19T11:00:00Z"
}
```

Reglas:

- No retorna `accessToken`, `refreshToken`, `idToken`, bearer token interno, cookie ni secretos.
- `csrfToken` no es secreto de autenticacion; solo protege mutaciones contra CSRF.
- El BFF puede renovar tokens server-side sin exponerlos al navegador.
- Estado actual transitorio: el endpoint retorna `authenticated`, `authMode`, `csrfToken`, `userId`, `email`, `fullName`, `groups` y `expiresAt` cuando existe `NF_SESSION` valida. La SPA hidrata empresas/licencia mediante requests normales al BFF sin header `Authorization` en modo Cognito.
- Tambien retorna `mfaAuthenticated` para que la UI pueda anticipar acciones criticas; la autorizacion efectiva sigue siendo server-side.

`POST /api/v1/auth/logout`:

Reglas:

- Requiere cookie de sesion y CSRF valido.
- Revoca sesion server-side.
- Invoca `identity-service` para revocar el token interno y auditar `LOGOUT`.
- Limpia cookie con expiracion inmediata.
- Revoca tokens Cognito cuando aplique.
- Registra auditoria segura.

`POST /api/v1/auth/logout` en `identity-service`:

- Contrato interno/local para revocar la sesion interna emitida por `identity-service`.
- Requiere `Authorization: Bearer <internal-token>`.
- Responde `204 No Content` cuando procesa la solicitud de forma idempotente.
- Revoca `identity.user_session.revoked_at` cuando existe.
- Registra `LOGOUT` en `identity.identity_access_audit`.
- Si el token no existe, registra intento fallido sin exponer el token.

`POST /api/v1/auth/login`:

- Se mantiene solo como contrato local/transitorio para desarrollo y E2E.
- En produccion debe estar deshabilitado, no expuesto por API Gateway o responder `404/403` seguro.

`POST /api/v1/internal/auth/cognito/session`:

Contrato interno BFF -> `identity-service`; no debe exponerse como endpoint publico de la SPA.

Request:

```json
{
  "subject": "cognito-sub",
  "email": "usuario@empresa.com",
  "fullName": "Usuario Empresa",
  "groups": ["COMPANY_ADMIN"]
}
```

Response:

```json
{
  "userId": "uuid",
  "email": "usuario@empresa.com",
  "fullName": "Usuario Empresa",
  "tokenType": "Bearer",
  "accessToken": "internal-opaque-token",
  "expiresAt": "2026-08-24T19:00:00Z",
  "globalRoles": []
}
```

Reglas:

- El BFF llama este contrato solo despues de validar `code` y `state` con Cognito.
- El token interno se guarda cifrado en la sesion server-side del BFF y nunca se retorna a la SPA.
- Estado actual: resolucion primaria por `cognitoSubject` persistente. El email solo se usa para el primer enlace contra usuarios activos previamente provisionados; no hay autocreacion durante login.
## tenant-service

Responsabilidad: empresas, configuracion multiempresa y estado del tenant.

### Endpoints

- `POST /api/v1/companies`
- `GET /api/v1/companies/{companyId}`
- `PUT /api/v1/companies/{companyId}`
- `PUT /api/v1/companies/{companyId}/activate`
- `PUT /api/v1/companies/{companyId}/suspend`

### CompanyResponse

### CompanyRequest

```json
{
  "legalName": "Mi Empresa SAS",
  "tradeName": "Mi Tienda",
  "identificationTypeCode": 31,
  "identificationNumber": "900123456",
  "verificationDigit": "7",
  "email": "admin@example.com"
}
```

### CompanyResponse

```json
{
  "id": "uuid",
  "legalName": "Mi Empresa SAS",
  "tradeName": "Mi Tienda",
  "identificationTypeCode": 31,
  "identificationNumber": "900123456",
  "verificationDigit": "7",
  "email": "admin@example.com",
  "status": "ACTIVE"
}
```

## identity-service

Responsabilidad: usuarios, autenticacion, sesiones, permisos efectivos, roles globales de plataforma, roles por empresa y auditoria de acceso.

Estado TASK-056:

- Microservicio fisico implementado en `services/identity-service`.
- Persistencia propia bajo schema `identity`.
- Login con token opaco Bearer, token persistido como hash y expiracion configurable.
- Passwords persistidos con hash PBKDF2, nunca en texto plano.
- Modelo actual con roles fijos por empresa: `OWNER`, `ADMIN`, `CASHIER`, `ACCOUNTANT`, `AUDITOR`.
- Estado TASK-072: `POST /api/v1/auth/login` incluye `globalRoles` en la respuesta. Cuando contiene `ROOT`, el cliente puede iniciar flujo global sin `company_id`, membresia empresarial ni licencia empresarial.
- Estado TASK-073: `ROOT` puede crear usuario con `POST /api/v1/users` y asignar `OWNER` como administrador inicial mediante `POST /api/v1/companies/{companyId}/memberships`, sin membresia previa ni licencia empresarial.

Objetivo TASK-068/TASK-069:

- `ROOT` es global, no tiene `company_id`, no depende de licencia empresarial y administra empresas contratantes, licencias, usuarios root y administradores iniciales.
- Todo rol distinto de `ROOT` pertenece a una empresa y se aisla por `company_id`.
- Los nombres de roles empresariales son configurables por cada empresa.
- Los permisos son modulares y persistidos; el sistema no debe depender de nombres de roles hardcodeados para autorizar acciones.
- Permisos `GLOBAL_*` son exclusivos de `ROOT`.
- Un actor solo puede crear, editar o asignar roles con permisos estrictamente menores que sus permisos efectivos.
- Se registra auditoria interna para login, creacion de usuario, creacion/edicion de rol, asignacion de permisos y asignacion de roles.

### Endpoints actuales

- `POST /api/v1/auth/login`
- `POST /api/v1/users`
- `GET /api/v1/me`
- `GET /api/v1/me/companies`
- `POST /api/v1/companies/{companyId}/memberships`
- `POST /api/v1/companies/{companyId}/users/{userId}/roles`
- `PUT /api/v1/companies/{companyId}/memberships/{membershipId}/roles`
- `GET /api/v1/companies/{companyId}/permissions?userId=`

### Endpoints objetivo RBAC modular

Estado TASK-069: implementados en `identity-service` los endpoints de catalogo de permisos, roles empresariales, asignaciones y permisos efectivos. Los endpoints globales de creacion de empresas siguen delegados al flujo existente `tenant-service` + `identity-service` mientras se completa la UI administrativa.

Root global:

- `POST /api/v1/platform/root-users`
- `POST /api/v1/platform/companies`
- `POST /api/v1/platform/companies/{companyId}/admin`
- `GET /api/v1/platform/permissions` root-only; retorna catalogo completo de permisos globales y empresariales, y responde `403` para usuarios no ROOT.
- `GET /api/v1/platform/audit-events?from=&to=&userId=`

Roles y usuarios por empresa:

- `GET /api/v1/companies/{companyId}/roles`
- `POST /api/v1/companies/{companyId}/roles`
- `GET /api/v1/companies/{companyId}/roles/{roleId}`
- `PUT /api/v1/companies/{companyId}/roles/{roleId}`
- `PUT /api/v1/companies/{companyId}/roles/{roleId}/activate`
- `PUT /api/v1/companies/{companyId}/roles/{roleId}/deactivate`
- `GET /api/v1/companies/{companyId}/permissions/catalog` retorna solo permisos empresariales delegables.
- `POST /api/v1/companies/{companyId}/users`
- `GET /api/v1/companies/{companyId}/users?email=`
- `PUT /api/v1/companies/{companyId}/users/{userId}`
- `PUT /api/v1/companies/{companyId}/users/{userId}/activate`
- `PUT /api/v1/companies/{companyId}/users/{userId}/deactivate`
- `POST /api/v1/companies/{companyId}/users/{userId}/role-assignments`
- `DELETE /api/v1/companies/{companyId}/users/{userId}/role-assignments/{roleId}`
- `GET /api/v1/companies/{companyId}/users/{userId}/effective-permissions`

### Cognito integration objective

`identity-service` mantiene usuarios, roles, empresas y permisos como fuente de autorizacion de negocio. Cognito queda como proveedor de autenticacion productiva.

Campos objetivo para vincular usuarios:

- `cognitoSubject`: claim `sub` de Cognito persistido como `identity.user_account.cognito_subject`.
- `emailVerified`: estado derivado de Cognito.
- `mfaRequired`: politica de negocio para ROOT/admin.
- `lastLoginAt`: auditoria funcional.

Reglas:

- El backend crea usuarios Cognito para administradores iniciales y usuarios empresariales cuando el ambiente productivo use Cognito.
- La contrasena inicial no se retorna despues de crear usuario; se usa flujo temporal/force-change-password o invitacion Cognito segun politica aprobada.
- ROOT/admin requieren MFA en produccion.
- La autorizacion por permisos efectivos sigue en `identity-service`; Cognito no reemplaza RBAC empresarial.

Ejemplo `POST /api/v1/companies/{companyId}/roles`:

```json
{
  "name": "Vendedor POS",
  "description": "Puede vender y emitir POS electronico",
  "permissionCodes": ["SALES_CREATE", "FISCAL_DOCUMENTS_ISSUE", "REPORTS_VIEW"]
}
```

Reglas:

- Un usuario puede pertenecer a varias empresas, pero sus roles empresariales se calculan por `companyId`.
- Todo token permite determinar usuario autenticado, alcance global cuando aplique, empresas autorizadas y permisos efectivos por empresa.
- `ROOT` puede crear la empresa contratante y el administrador inicial.
- El administrador empresarial puede crear roles y usuarios dentro de su empresa si tiene `COMPANY_ROLES_MANAGE` y/o `COMPANY_USERS_MANAGE`.
- El backend rechaza cualquier rol empresarial que contenga permisos `GLOBAL_*`.
- El backend rechaza delegar permisos iguales, superiores o no poseidos por el actor.
- La licencia empresarial se valida al crear usuarios o roles empresariales cuando la politica comercial lo requiera; `ROOT` no consume licencia para entrar al panel global.

## Frontend navegacion operativa

- Pantalla inicial autenticada: `Ventas`.
- Menus principales:
  - `Ventas`.
  - `Reportes`.
  - `Contabilidad`: agrupa `Terceros`, `Inventario`, `Fiscal`, `Documentos fiscales`, `Configuracion contable` y `Nomina`.
  - `Configuracion`: agrupa `Empresa`, `Licencias`, `Catalogos`, `Logs`, `Usuarios` y `Roles`.
- Los submenus conservan validacion por licencia y permisos efectivos. El frontend puede ocultar opciones, pero la autorizacion real permanece en BFF/backend.

## catalog-service

Responsabilidad: catalogos globales y catalogos configurables por empresa.

Estado TASK-033:

- Microservicio fisico implementado en `services/catalog-service`.
- Contratos legacy de catalogo retirados en TASK-088: `/api/categorias`, `/api/paises`, `/api/tipos-documento`, `/api/metodos-pago`, `/api/parametros`, `/api/tipos-gasto`, `/api/impuestos` y `/api/productos`.
- Los contratos `/api/v1/catalogs/*` y `/api/v1/company-catalogs/*` son el contrato estable activo.

### Catalogos versionados v1

- `GET /api/v1/catalog-definitions`
- `GET /api/v1/catalogs/{catalogCode}/items`
- `GET /api/v1/catalogs/{catalogCode}/items?includeInactive=true`
- `POST /api/v1/catalogs/{catalogCode}/items`
- `PUT /api/v1/catalogs/{catalogCode}/items/{itemCode}`
- `PUT /api/v1/catalogs/{catalogCode}/items/{itemCode}/activation`
- `GET /api/v1/company-catalogs/{catalogCode}/items`
- `POST /api/v1/company-catalogs/{catalogCode}/items`
- `PUT /api/v1/company-catalogs/{catalogCode}/items/{itemCode}/activation`

Catalogos iniciales:

- `DIAN_DOCUMENT_TYPE`
- `TAX_RESPONSIBILITY`
- `TAX_REGIME`
- `SALES_TAX`
- `PAYMENT_METHOD`
- `VIRTUAL_WALLET`
- `FISCAL_DOCUMENT_TYPE`
- `FISCAL_ENVIRONMENT`

`CatalogDefinitionResponse`:

```json
{
  "code": "PAYMENT_METHOD",
  "label": "Metodos de pago",
  "description": "Opciones de pago disponibles para ventas",
  "regulatory": false,
  "companyConfigurable": true,
  "globalEditableByRoot": true,
  "active": true
}
```

`CatalogItemResponse`:

```json
{
  "catalogCode": "PAYMENT_METHOD",
  "code": "CASH",
  "label": "Efectivo",
  "description": "Pago en efectivo",
  "active": true,
  "enabledForCompany": true,
  "regulatory": false,
  "source": "APP",
  "sourceVersion": "2026-08"
}
```

Para `SALES_TAX`, el codigo tecnico viaja en `code` y la UI muestra `label` en espanol. Los items iniciales deben conservar fuente DIAN/Gobierno, version/corte y tarifa fiscal en el contrato de producto que los consume.

`CatalogItemRequest`:

```json
{
  "code": "CASH",
  "label": "Efectivo",
  "description": "Pago en efectivo",
  "regulatory": false,
  "source": "APP",
  "sourceVersion": "2026-08",
  "validFrom": "2026-08-01",
  "validTo": null,
  "sortOrder": 10
}
```

`CatalogItemActivationRequest`:

```json
{
  "active": true
}
```

### DIVIPOLA v1

- `GET /api/v1/catalogs/departments`
- `GET /api/v1/catalogs/departments/{departmentCode}/municipalities`
- `GET /api/v1/catalogs/municipalities/{municipalityCode}`

`DepartmentResponse`:

```json
{
  "code": "11",
  "name": "Bogota, D.C.",
  "active": true,
  "source": "DANE DIVIPOLA",
  "sourceVersion": "2025"
}
```

`MunicipalityResponse`:

```json
{
  "code": "11001",
  "departmentCode": "11",
  "name": "Bogota, D.C.",
  "active": true,
  "source": "DANE DIVIPOLA",
  "sourceVersion": "2025"
}
```

Regla:

- Los catalogos oficiales pueden omitirse de `X-Company-Id`.
- Los catalogos configurables por empresa requieren `X-Company-Id`.
- Los codigos regulatorios no son editables por empresa.
- `ROOT` puede crear, actualizar o inactivar catalogos globales permitidos.
- Las mutaciones globales y empresariales de catalogos registran auditoria `CATALOG_ADMINISTRATION` con resultado `SUCCESS` o `FAILURE`.
- Un administrador empresarial solo puede operar `company-catalogs` y extensiones empresariales permitidas para su `company_id`.
- La UI debe mostrar `CatalogDefinitionResponse.label` y `CatalogItemResponse.label` en espanol, aunque `code`, `catalogCode` e `itemCode` se conserven en ingles en contratos y base de datos.
- Los catalogos regulatorios oficiales deben preservar `source`, `sourceVersion`, `validFrom` y `validTo`.
- `company-catalogs` solo permite activar/inactivar opciones para una empresa mediante `X-Company-Id`.
- La activacion empresarial no puede habilitar un item global inactivo.
- `department.code` y `municipality.code` corresponden a codigos DANE/DIVIPOLA; un municipio siempre referencia un departamento existente.
- La UI debe consultar municipios por departamento, no cargar todo DIVIPOLA como catalogo generico.

## thirdparty-service

Responsabilidad: clientes y proveedores aislados por empresa.

Estado TASK-033:

- Microservicio fisico implementado en `services/thirdparty-service`.
- TASK-059 lote 2 retira contratos legacy `/api/clientes` y `/api/proveedores`; el contrato canonico queda en `/api/v1`.
- `thirdparty-service` ya no consulta `catalog-service` en runtime para crear terceros; recibe `identificationTypeCode` en el contrato v1 y calcula DV NIT en dominio.
- TASK-047 agrega el modelo fiscal unificado `/api/v1/third-parties`, con roles cliente/proveedor y DV NIT automatico.
- Los contratos `/api/v1/customers` y `/api/v1/suppliers` existen como vistas por rol sobre el modelo fiscal unificado.

### Terceros fiscales

- `POST /api/v1/third-parties`
- `GET /api/v1/third-parties/{thirdPartyId}`
- `GET /api/v1/third-parties/by-document?identificationTypeCode=&identificationNumber=`
- `PUT /api/v1/third-parties/{thirdPartyId}`
- `PUT /api/v1/third-parties/{thirdPartyId}/activate`
- `PUT /api/v1/third-parties/{thirdPartyId}/deactivate`

### Clientes v1

- `POST /api/v1/customers`
- `GET /api/v1/customers?active=&identificationNumberPrefix=`

### Proveedores v1

- `POST /api/v1/suppliers`
- `GET /api/v1/suppliers?active=`

### Contratos retirados de clientes/proveedores

Retirados en TASK-059 lote 2. Los consumidores deben usar `/api/v1/customers`, `/api/v1/suppliers` o `/api/v1/third-parties`.

### ThirdPartyRequest

```json
{
  "personType": "JURIDICA",
  "identificationTypeCode": 31,
  "identificationNumber": "900123456",
  "fullName": null,
  "businessName": "Cliente Prueba SAS",
  "tradeName": "Cliente Prueba",
  "email": "cliente@example.com",
  "phone": "3000000000",
  "address": "Calle 1 # 2-3",
  "municipalityCode": "11001",
  "taxResponsibilities": ["O-13"],
  "taxRegime": "RESPONSABLE_IVA",
  "roles": ["CUSTOMER"]
}
```

Reglas:

- `identificationTypeCode + identificationNumber` es unico por empresa.
- `identificationTypeCode=31` calcula `verificationDigit` automaticamente; el request no debe tratar el DV como dato libre.
- Para tipos de documento distintos a NIT, `verificationDigit` debe quedar nulo o vacio.
- `identificationNumberPrefix` permite buscar clientes activos por prefijo de numero de documento para Venta POS; siempre se filtra por `X-Company-Id` y rol `CUSTOMER`.
- Para NIT, el numero de documento corresponde al NIT base sin DV, solo con digitos; el DV viaja separado y es calculado por backend.
- `personType` debe ser `NATURAL` o `JURIDICA`.
- Un tercero puede tener rol `CUSTOMER`, `SUPPLIER` o `BOTH`.
- `taxResponsibilities` acepta `O-13`, `O-15`, `O-23`, `O-47` o `R-99-PN`; `R-99-PN` es excluyente.
- `taxRegime` acepta `ORDINARIO`, `SIMPLE`, `RESPONSABLE_IVA` o `NO_RESPONSABLE_IVA`.
- Si el tercero es solo `CUSTOMER` y `personType=NATURAL`, el backend exige perfil automatico: `identificationTypeCode` distinto de `31`, `verificationDigit=null`, `businessName=null`, `tradeName=null`, `taxResponsibilities=["R-99-PN"]` y `taxRegime=NO_RESPONSABLE_IVA`.
- Ninguna consulta puede retornar terceros de otra empresa.

## inventory-service

Responsabilidad: productos, stock simple, compras documentales, movimientos y kardex.

Estado TASK-034:

- Microservicio fisico implementado en `services/inventory-service`.
- Endpoints `/api/v1` implementados para productos, disponibilidad, kardex, movimientos, compras y confirmacion de compras.
- Las operaciones implementadas requieren `X-Company-Id`.
- `Idempotency-Key` es obligatorio en movimientos y compras; en creacion de producto se usa cuando se registra stock inicial.
- Las rutas de busqueda/listado, actualizacion y desactivacion quedan marcadas como contrato objetivo posterior si no aparecen implementadas en el controlador activo del microservicio.

Estado TASK-048:

- `POST /api/v1/products` acepta `itemType`, `saleEnabled`, `purchaseEnabled` y `stockTracked`.
- Si `itemType` se omite, se conserva compatibilidad y se crea `PHYSICAL_GOOD` con venta, compra y stock habilitados.
- `SERVICE` es facturable, pero no puede tener stock automatico ni stock inicial.
- `POST /api/v1/service-supply-references` y `GET /api/v1/products/{serviceProductId}/supply-references` permiten registrar insumos sugeridos para servicios sin generar movimientos de kardex.
- `GET /api/v1/products/{serviceProductId}/supply-consumption-suggestions` permite cargar insumos sugeridos, stock actual y costo para que el usuario confirme consumo real despues de vender el servicio.
- `POST /api/v1/service-supply-consumptions` registra consumos reales confirmados como movimientos `CONSUMPTION_OUT` con origen `MANUAL_SUPPLY_CONSUMPTION`.
- Los movimientos de inventario solo afectan items con `stockTracked=true`; las compras documentales no modifican stock.

### Productos

- `POST /api/v1/products`
- `GET /api/v1/products/{productId}`
- `GET /api/v1/products/by-barcode/{barcode}`
- `GET /api/v1/products/by-barcode/{barcode}?includeInactive=true`
- `PUT /api/v1/products/{productId}`
- `PUT /api/v1/products/{productId}/deactivate`
- `GET /api/v1/products/{productId}/availability?quantity=`
- `GET /api/v1/products/{productId}/kardex`
- `GET /api/v1/products/{serviceProductId}/supply-references`
- `GET /api/v1/products/{serviceProductId}/supply-consumption-suggestions`

Reglas TASK-250/TASK-251:

- `POST /api/v1/products` crea productos nuevos; si SKU o codigo de barras ya existen para la empresa, responde `409 DUPLICATE_RESOURCE`.
- `PUT /api/v1/products/{productId}` actualiza datos maestros vigentes, no stock historico ni snapshots de ventas/documentos ya emitidos.
- `PUT /api/v1/products/{productId}/deactivate` marca `active=false`; no elimina producto ni movimientos.
- La busqueda POS usa productos activos. La busqueda de mantenimiento puede usar `includeInactive=true` para permitir actualizacion o reactivacion futura.
- Las respuestas de reportes deben basarse en snapshots historicos o joins tolerantes a inactivos; un producto inactivo no puede romper reportes.

### Compras

- `POST /api/v1/purchases`
- `POST /api/v1/purchases/{purchaseId}/confirm`

### Movimientos

- `POST /api/v1/inventory-movements`
- Kardex por producto: `GET /api/v1/products/{productId}/kardex`

### Referencias servicio-insumo

- `POST /api/v1/service-supply-references`
- `GET /api/v1/products/{serviceProductId}/supply-references`
- `POST /api/v1/service-supply-consumptions`

### ProductRequest

```json
{
  "sku": "SKU-001",
  "barcode": "7701234567890",
  "name": "Cafe 500g",
  "description": "Bolsa de cafe",
  "itemType": "PHYSICAL_GOOD",
  "saleEnabled": true,
  "purchaseEnabled": true,
  "stockTracked": true,
  "salePrice": 12605.04,
  "cost": 9000,
  "initialStock": 10,
  "taxCategoryCode": "IVA",
  "taxCode": "IVA_19",
  "taxLabel": "IVA 19%",
  "taxRate": 19
}
```

### ProductResponse

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "sku": "SKU-001",
  "barcode": "7701234567890",
  "name": "Cafe 500g",
  "description": "Bolsa de cafe",
  "itemType": "PHYSICAL_GOOD",
  "saleEnabled": true,
  "purchaseEnabled": true,
  "stockTracked": true,
  "salePrice": 12605.04,
  "cost": 9000,
  "taxCategoryCode": "IVA",
  "taxCode": "IVA_19",
  "taxLabel": "IVA 19%",
  "taxRate": 19,
  "active": true,
  "currentStock": 10,
  "createdAt": "2026-05-20T10:00:00Z",
  "updatedAt": "2026-05-20T10:00:00Z"
}
```

### ProductAvailabilityResponse

```json
{
  "companyId": "uuid",
  "productId": "uuid",
  "requestedQuantity": 2,
  "availableQuantity": 10,
  "available": true
}
```

### InventoryMovementRequest

```json
{
  "productId": "uuid",
  "movementType": "CONSUMPTION_OUT",
  "quantity": 4,
  "unitCost": 9000,
  "sourceDocumentType": "MANUAL_SUPPLY_CONSUMPTION",
  "sourceDocumentId": "uuid",
  "reason": "Consumo operativo de insumo usado en servicios"
}
```

### ServiceSupplyReferenceRequest

```json
{
  "serviceProductId": "uuid",
  "supplyProductId": "uuid",
  "notes": "Insumo sugerido para manicura; el consumo real se registra manualmente."
}
```

### ServiceSupplyReferenceResponse

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "serviceProductId": "uuid",
  "supplyProductId": "uuid",
  "notes": "Insumo sugerido para manicura; el consumo real se registra manualmente.",
  "active": true,
  "createdAt": "2026-05-20T10:00:00Z"
}
```

### ProductUpdateRequest

Igual a `ProductRequest`, excepto:

- `initialStock` no modifica stock existente en actualizacion.
- `sku` y `barcode` deben mantenerse unicos por `companyId`.
- Los cambios de nombre, descripcion, precio, costo, impuesto y flags operativos aplican solo hacia operaciones futuras.

### SuggestedSupplyConsumptionResponse

```json
[
  {
    "serviceProductId": "uuid",
    "supplyProductId": "uuid",
    "supplySku": "SUP-ESMALTE-ROJO",
    "supplyName": "Esmalte rojo",
    "currentStock": 10,
    "unitCost": 1500,
    "notes": "Insumo sugerido para manicura"
  }
]
```

### ConfirmServiceSupplyConsumptionRequest

Requiere `X-Company-Id`, `X-User-Id` cuando exista sesion y `Idempotency-Key`.

```json
{
  "serviceProductId": "uuid",
  "sourceDocumentId": "uuid",
  "reason": "Consumo real de insumos por manicura facturada",
  "lines": [
    {
      "supplyProductId": "uuid",
      "quantity": 0.25
    }
  ]
}
```

Reglas:

- `sourceDocumentId` debe ser la venta, factura o documento operativo que origina el consumo.
- Cada `supplyProductId` debe estar asociado previamente al servicio mediante `service_supply_reference`.
- El backend registra un movimiento `CONSUMPTION_OUT` por insumo, con idempotencia derivada de `Idempotency-Key + supplyProductId`.
- La confirmacion no calcula receta automatica; el usuario decide cantidades reales.

### PurchaseRequest

```json
{
  "supplierId": "uuid",
  "subtotal": 90000,
  "taxTotal": 17100,
  "total": 107100,
  "paymentCondition": "CREDIT",
  "dueDate": "2026-06-20",
  "evidenceUrl": "https://example.local/evidence.pdf",
  "lines": [
    {
      "description": "Factura proveedor cafe",
      "quantity": 10,
      "unitCost": 9000,
      "subtotal": 90000,
      "tax": 17100,
      "total": 107100
    }
  ]
}
```

Reglas:

- Una venta facturada descuenta stock.
- Una compra confirmada no aumenta stock ni genera movimientos `PURCHASE_IN`; solo registra control financiero/documental y contabilidad.
- Una compra a credito requiere `dueDate`.
- Si `ACCOUNTING_SERVICE_URL` esta configurado, `inventory-service` intenta contabilizar la compra confirmada con evento `PURCHASE_CONFIRMED` y crear CxP en `accounting-service` cuando sea credito.
- Stock negativo no esta permitido en la fase inicial.
- Todo movimiento debe referenciar documento origen.
- Los tipos de item objetivo son `PHYSICAL_GOOD`, `SERVICE` y `SUPPLY`.
- Un `SERVICE` puede facturarse, pero no descuenta insumos automaticamente.
- Las referencias servicio-insumo son informativas y no generan kardex.
- `initialStock` solo se acepta cuando `stockTracked=true`.
- La disponibilidad de un `SERVICE` sin stock se considera disponible para venta; la venta final se ajusta en `billing-service`.
- Los movimientos manuales de insumos incluyen `CONSUMPTION_OUT` y `WASTE_OUT`.
- `CONSUMPTION_OUT` debe usar `sourceDocumentType=MANUAL_SUPPLY_CONSUMPTION`.
- `WASTE_OUT` debe usar `sourceDocumentType=MANUAL_SUPPLY_WASTE`.
- `reason` es obligatorio para `CONSUMPTION_OUT` y `WASTE_OUT`.
- Los gastos sin inventario no deben crear movimientos de stock.

## billing-service

Responsabilidad: ventas POS, facturacion electronica, documento equivalente POS, notas, numeracion y estados fiscales.

Estado TASK-041:

- Microservicio fisico inicial implementado en `services/billing-service`.
- Endpoints implementados: `POST /api/v1/issuers`, `GET /api/v1/issuers/current`, `POST /api/v1/numbering-resolutions`, `GET /api/v1/numbering-resolutions`, `POST /api/v1/sales`, `POST /api/v1/sales/{saleId}/confirm` y `GET /api/v1/sales/{saleId}`.
- La creacion de venta valida disponibilidad contra `inventory-service` usando `GET /api/v1/products/{productId}/availability`.
- La confirmacion exige emisor activo y resolucion vigente para `ELECTRONIC_POS` en ambiente `TEST`.
- La confirmacion asigna prefijo y consecutivo desde `billing.numbering_resolution`, valida configuracion DIAN empresarial, genera documento electronico POS y consume `dian-provider-service` por HTTP.
- Si la conexion DIAN acepta, aplica automaticamente `SALE_OUT` y asiento contable idempotente.

Estado TASK-049:

- La venta soporta lineas mixtas de bienes fisicos y servicios.
- Cada linea guarda snapshot de `productSku`, `productName`, `itemType` y `stockTracked` obtenido desde `inventory-service`.
- La disponibilidad de inventario se valida solo cuando `stockTracked=true`.
- La confirmacion aplica `SALE_OUT` solo para lineas con `stockTracked=true`.
- Las lineas tipo `SERVICE` no consumen insumos automaticamente.

### Emisor

- `POST /api/v1/issuers`
- `GET /api/v1/issuers`
- `GET /api/v1/issuers/current`
- `PUT /api/v1/issuers/{issuerId}/activate`
- `PUT /api/v1/issuers/{issuerId}/deactivate`
- `PUT /api/v1/issuers/{issuerId}` contrato objetivo para actualizacion auditada del emisor fiscal.

Reglas:

- `POST /api/v1/issuers` crea un emisor activo y desactiva otros emisores activos de la misma empresa.
- `PUT /api/v1/issuers/{issuerId}/activate` activa el emisor indicado y desactiva otros emisores activos de la misma empresa.
- `PUT /api/v1/issuers/{issuerId}/deactivate` inactiva el emisor indicado sin eliminar historial.

### Resoluciones

- `POST /api/v1/numbering-resolutions`
- `GET /api/v1/numbering-resolutions?documentType=&active=`
- `GET /api/v1/numbering-resolutions/{resolutionId}` contrato objetivo para consulta por identificador.
- `PUT /api/v1/numbering-resolutions/{resolutionId}/activate`
- `PUT /api/v1/numbering-resolutions/{resolutionId}/deactivate`

Reglas:

- `POST /api/v1/numbering-resolutions` crea una resolucion activa y desactiva otras resoluciones activas de la misma empresa, tipo documental y ambiente.
- `PUT /api/v1/numbering-resolutions/{resolutionId}/activate` activa la resolucion indicada y desactiva otras resoluciones activas del mismo alcance.
- `PUT /api/v1/numbering-resolutions/{resolutionId}/deactivate` inactiva la resolucion indicada sin eliminar historial ni reutilizar consecutivos.

### Ventas POS

- `POST /api/v1/sales`
- `POST /api/v1/sales/{saleId}/confirm`
- `GET /api/v1/sales/{saleId}`
- `GET /api/v1/sales?status=&from=&to=`

### Documento equivalente electronico POS

- `POST /api/v1/electronic-pos`
- `POST /api/v1/electronic-pos/{documentId}/submit`
- `GET /api/v1/electronic-pos/{documentId}`
- `POST /api/v1/electronic-pos/{documentId}/adjustment-notes`

### Factura electronica

- `POST /api/v1/electronic-invoices`
- `POST /api/v1/electronic-invoices/{documentId}/issue`
- `GET /api/v1/electronic-invoices/{documentId}`
- `GET /api/v1/electronic-invoices?status=&customerId=&from=&to=&prefix=&number=`
- `GET /api/v1/electronic-invoices/{documentId}/artifacts`
- `GET /api/v1/electronic-invoices/{documentId}/events`

### Notas

- `POST /api/v1/credit-notes`
- `POST /api/v1/debit-notes`
- `GET /api/v1/credit-notes/{noteId}`
- `GET /api/v1/debit-notes/{noteId}`

### SaleRequest

```json
{
  "buyerIdentificationMode": "IDENTIFIED_CUSTOMER",
  "customerId": "uuid",
  "paymentMethodCode": "VIRTUAL_WALLET",
  "virtualWalletCode": "NEQUI",
  "items": [
    {
      "productId": "uuid",
      "quantity": 2,
      "discountAmount": 0
    }
  ]
}
```

Para consumidor final:

```json
{
  "buyerIdentificationMode": "FINAL_CONSUMER",
  "customerId": null,
  "paymentMethodCode": "CASH",
  "virtualWalletCode": null,
  "items": [
    {
      "productId": "uuid",
      "quantity": 1,
      "discountAmount": 0
    }
  ]
}
```

Reglas:

- `buyerIdentificationMode` acepta `IDENTIFIED_CUSTOMER` o `FINAL_CONSUMER`.
- Si `buyerIdentificationMode=IDENTIFIED_CUSTOMER`, `customerId` es obligatorio y debe pertenecer a la empresa.
- Si `buyerIdentificationMode=FINAL_CONSUMER`, `customerId` debe ser nulo; billing resuelve el perfil fiscal desde configuracion persistida.
- La SPA no envia `saleChannel`; el flujo `Venta POS` usa canal interno `POS`.
- El canal `POS` no define por si solo el tipo fiscal. El tipo fiscal se resuelve por politica empresarial: por defecto `ELECTRONIC_INVOICE`, con `ELECTRONIC_POS` como opcion parametrizable o override autorizado.
- La SPA no envia `unitPrice`, `taxCode` ni `taxRate` como fuente fiscal; billing toma precio e impuesto desde `inventory-service`.

### Politica fiscal y override operacional

- `GET /api/v1/fiscal-policy`
- `PUT /api/v1/fiscal-policy`
- `POST /api/v1/sales/{saleId}/document-type-override`
- `GET /api/v1/companies/{companyId}/operational-pin`
- `PUT /api/v1/companies/{companyId}/operational-pin`
- `POST /api/v1/companies/{companyId}/operational-pin/verify`
- `PUT /api/v1/companies/{companyId}/operational-pin/unlock`

`FiscalPolicyResponse`:

```json
{
  "companyId": "uuid",
  "defaultSaleDocumentType": "ELECTRONIC_INVOICE",
  "allowDocumentTypeOverride": true,
  "requirePinForOverride": true,
  "active": true
}
```

`SaleDocumentTypeOverrideRequest`:

```json
{
  "documentType": "ELECTRONIC_POS",
  "authorizedBy": "uuid-opcional",
  "pin": "123456",
  "reason": "Cliente solicita documento equivalente POS para esta venta"
}
```

Reglas:

- `documentType` solo acepta tipos documentales habilitados por politica y licencia.
- Si `authorizedBy` no viaja en el payload, el backend usa el usuario autenticado enviado por contexto de seguridad.
- El autorizador debe pertenecer a la misma empresa, estar activo, tener permiso `SALES_DOCUMENT_TYPE_OVERRIDE` y PIN operacional `ACTIVE`.
- El PIN tiene exactamente 6 digitos numericos y viaja solo en el request de autorizacion; backend nunca lo retorna.
- Tras 3 fallos consecutivos el PIN queda bloqueado.
- El override aplica solo a la venta indicada y debe registrarse con auditoria.
- Si la SPA solicita override antes de cerrar la venta, primero debe crear una venta `DRAFT`, enviar el override sobre ese `saleId` y luego confirmar ese mismo borrador.
- `OperationalPinResponse` solo expone `configured`, `locked`, `mustChange`, `remainingAttempts` y `updatedAt`; nunca retorna PIN ni hash.

### Modulos fiscales independientes

- `POST /api/v1/credit-notes`
- `GET /api/v1/credit-notes?status=&from=&to=&customerId=`
- `GET /api/v1/credit-notes/{noteId}`
- `POST /api/v1/debit-notes`
- `GET /api/v1/debit-notes?status=&from=&to=&customerId=`
- `GET /api/v1/debit-notes/{noteId}`
- `POST /api/v1/pos-adjustment-notes`
- `GET /api/v1/pos-adjustment-notes?status=&from=&to=&customerId=`
- `GET /api/v1/pos-adjustment-notes/{noteId}`

Reglas:

- Nota credito usa resolucion `CREDIT_NOTE`.
- Nota debito usa resolucion `DEBIT_NOTE`.
- Nota de ajuste POS usa resolucion `POS_ADJUSTMENT_NOTE`.
- Estos endpoints no pertenecen al modulo `Ventas`; requieren permisos fiscales especificos y auditoria propia.

### IssuerProfileRequest

```json
{
  "legalName": "ACME SAS",
  "nit": "900123456",
  "verificationDigit": "7",
  "taxResponsibilities": ["O-13"],
  "municipalityCode": "11001",
  "address": "Calle 1 # 2-3"
}
```

### SaleLineResponse

```json
{
  "id": "uuid",
  "productId": "uuid",
  "productSku": "SERV-1",
  "productName": "Manicura",
  "itemType": "SERVICE",
  "stockTracked": false,
  "quantity": 1,
  "unitPrice": 35000,
  "discountAmount": 0,
  "taxCode": "IVA_19",
  "taxRate": 19,
  "subtotal": 35000,
  "taxAmount": 6650,
  "total": 41650
}
```

### NumberingResolutionRequest

```json
{
  "documentType": "ELECTRONIC_POS",
  "resolutionNumber": "18760000001",
  "prefix": "POS",
  "fromNumber": 100,
  "toNumber": 200,
  "validFrom": "2026-01-01",
  "validTo": "2026-12-31",
  "environment": "TEST"
}
```

### ElectronicDocumentResponse

```json
{
  "id": "uuid",
  "documentType": "ELECTRONIC_POS",
  "prefix": "POS",
  "number": 123,
  "cufeCude": "abc123",
  "qrContent": "https://...",
  "subtotal": 30000,
  "taxTotal": 5700,
  "total": 35700,
  "status": "VALIDATED",
  "providerStatus": "ACCEPTED",
  "inventoryAppliedAt": "2026-05-19T10:01:00Z",
  "accountingAppliedAt": "2026-05-19T10:01:01Z"
}
```

### ElectronicPosRequest

```json
{
  "saleId": "uuid",
  "buyerName": "Consumidor Final",
  "buyerIdentificationTypeCode": 13,
  "buyerDocumentNumber": "123456789",
  "documentDate": "2026-05-15",
  "environment": "TEST",
  "lines": [
    {
      "productId": "uuid",
      "quantity": 2,
      "unitPrice": 15000,
      "discountAmount": 0,
      "taxCode": "IVA_19",
      "taxRate": 19
    }
  ]
}
```

### SubmitElectronicDocumentResponse

```json
{
  "documentId": "uuid",
  "providerSubmissionId": "mock-submission-id",
  "providerStatus": "ACCEPTED",
  "documentStatus": "VALIDATED",
  "cufeCude": "mock-cude",
  "qrContent": "mock-qr-content"
}
```

Reglas:

- `X-Company-Id` define la empresa emisora.
- La numeracion fiscal debe ser idempotente y no reutilizable.
- Confirmar una venta fiscal exige emisor activo y resolucion vigente con numeros disponibles.
- El POS genera documento electronico a partir de la venta.
- La afectacion de inventario ocurre cuando la venta/documento llegue al estado aprobado por la politica transaccional.
- Si la conexion DIAN devuelve `ACCEPTED`, `billing-service` aplica `SALE_OUT` en `inventory-service` y genera asiento `SALE_CONFIRMED` en `accounting-service`.
- `SALE_OUT` solo se aplica a lineas con `stockTracked=true`; los servicios no generan consumo automatico de insumos.
- El snapshot de linea fiscal/operativa se toma desde `inventory-service` al crear la venta.
- Los campos `inventoryAppliedAt` y `accountingAppliedAt` evidencian la aplicacion idempotente de efectos posteriores.
- Reintentar `POST /api/v1/sales/{saleId}/confirm` no debe duplicar documento, movimientos de inventario ni asiento contable.
- En ambiente local, `POST /api/v1/electronic-pos/{documentId}/submit` usa conector DIAN mock sin llamadas externas.

## dian-provider-service

Responsabilidad: encapsular el mock local y la conexion real DIAN parametrizable por empresa. Este servicio no representa una oferta de proveedor tecnologico DIAN de la plataforma.

Estado TASK-036:

- Microservicio fisico implementado en `services/dian-provider-service`.
- Modo local soportado: `DIAN_PROVIDER_MODE=mock`.
- Persistencia de envios mock en `dian_provider.provider_submission`.
- `billing-service` consume `POST /api/v1/provider/electronic-pos` por HTTP usando `DIAN_PROVIDER_SERVICE_URL`.
- `GET /api/v1/provider/submissions/{trackingId}` permite consultar el resultado mock persistido y requiere `X-Company-Id`.
- Estado objetivo TASK-145/TASK-163: cada request real debe resolver configuracion DIAN activa por `companyId`, validar secretos/referencias, generar XML UBL, calcular CUFE/CUDE/QR, firmar, validar tecnicamente, transmitir a DIAN, persistir respuesta/artefactos y rechazar emision si la configuracion esta incompleta, vencida, inactiva o no probada.

### Endpoints internos

- `POST /api/v1/provider/electronic-invoices`
- `POST /api/v1/provider/electronic-pos`
- `POST /api/v1/provider/credit-notes`
- `POST /api/v1/provider/debit-notes`
- `GET /api/v1/provider/submissions/{trackingId}`

### Contrato objetivo DIAN real

Estado actualizado 2026-09-03: la Fase 20 tiene implementado un pipeline configurable `stub/http` de referencia, pero el transporte DIAN WCF SOAP real no esta implementado. La conexion real de habilitacion debe consumir `https://vpfe-hab.dian.gov.co/WcfDianCustomerServices.svc?wsdl` o `?singleWsdl`, construir envelope SOAP y ejecutar operaciones oficiales DIAN antes de considerar cerrada la emision real.

- `POST /api/v1/provider/real-submissions`
- `POST /api/v1/provider/real-submissions/{submissionId}/validate`
- `GET /api/v1/provider/real-submissions/{submissionId}`
- `GET /api/v1/provider/real-submissions/{submissionId}/artifacts`

Headers obligatorios:

- `X-Company-Id`: empresa propietaria de la configuracion DIAN.
- `X-User-Id`: usuario que origina la operacion.
- `X-Correlation-Id`: correlacion transversal.
- `Idempotency-Key`: clave estable por documento fiscal/intento logico.

`RealDianSubmissionRequest`:

```json
{
  "companyId": "uuid",
  "documentId": "uuid",
  "documentType": "ELECTRONIC_POS",
  "environment": "HABILITATION",
  "fiscalSnapshot": {
    "issuer": {},
    "buyer": {},
    "lines": [],
    "totals": {},
    "taxes": []
  }
}
```

`RealDianSubmissionResponse`:

```json
{
  "submissionId": "uuid",
  "documentId": "uuid",
  "status": "ACCEPTED",
  "dianTrackingId": "string",
  "cufeCude": "string",
  "qrContent": "string",
  "validationSummary": {
    "xsd": "PASSED",
    "schematron": "PASSED",
    "codeLists": "PASSED"
  },
  "artifacts": [
    {
      "artifactType": "SIGNED_XML",
      "contentHash": "sha256:...",
      "downloadAvailable": true
    }
  ],
  "processedAt": "2026-08-24T10:00:00Z"
}
```

Errores funcionales:

- `DIAN_CONFIGURATION_INCOMPLETE`
- `DIAN_CONFIGURATION_NOT_ACTIVE`
- `DIAN_CERTIFICATE_EXPIRED`
- `DIAN_ARTIFACT_VALIDATION_FAILED`
- `DIAN_SIGNATURE_FAILED`
- `DIAN_TECHNICAL_VALIDATION_FAILED`
- `DIAN_TRANSPORT_FAILED`
- `DIAN_REJECTED`
- `DIAN_RESPONSE_UNAVAILABLE`
- `DIAN_REAL_MODE_NOT_AVAILABLE`

### Configuracion DIAN por empresa

Endpoints publicos via BFF, visibles para ROOT y administradores empresariales con permiso fiscal/configuracion:

- `GET /api/v1/dian-configuration/companies/{companyId}`
- `PUT /api/v1/dian-configuration/companies/{companyId}`
- `POST /api/v1/dian-configuration/companies/{companyId}/test`
- `POST /api/v1/dian-configuration/companies/{companyId}/activate`
- `POST /api/v1/dian-configuration/companies/{companyId}/deactivate`

`DianConfigurationRequest`:

Estado legacy actual: el contrato JSON acepta `certificatePayload` como texto/base64 de solo entrada. Este campo queda deprecado para la implementacion comercial y debe reemplazarse por carga multipart `.p12/.pfx`.

```json
{
  "mode": "REAL",
  "environment": "TEST",
  "softwareId": "uuid-o-identificador-dian",
  "softwarePin": "valor-sensible-solo-entrada",
  "technicalKey": "valor-sensible-solo-entrada",
  "certificatePayload": "base64-o-pem-solo-entrada",
  "certificatePassword": "valor-sensible-solo-entrada",
  "certificateAlias": "certificado empresa",
  "certificateFingerprint": "sha256:...",
  "certificateExpiresAt": "2027-08-19T00:00:00Z",
  "serviceBaseUrl": "https://catalogo-vpfe-hab.dian.gov.co/...",
  "testSetId": "set-pruebas",
  "acceptedResponsibility": true
}
```

Contrato objetivo para certificado:

```http
PUT /api/v1/dian-configuration/companies/{companyId}/certificate
Content-Type: multipart/form-data
X-Company-Id: {companyId}
X-User-Id: {userId}
X-Correlation-Id: {correlationId}
```

Partes multipart:

- `certificateFile`: archivo unico `.p12` o `.pfx`.
- `certificatePassword`: password de solo entrada.
- `certificateAlias`: alias funcional opcional.

Respuesta:

```json
{
  "companyId": "uuid",
  "certificateConfigured": true,
  "certificateAlias": "certificado empresa",
  "certificateFingerprint": "sha256:...",
  "certificateExpiresAt": "2027-08-19T00:00:00Z",
  "status": "READY_FOR_TEST"
}
```

`DianConfigurationResponse`:

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "mode": "REAL",
  "environment": "TEST",
  "softwareId": "uuid-o-identificador-dian",
  "softwarePinConfigured": true,
  "technicalKeyConfigured": true,
  "certificateConfigured": true,
  "certificateAlias": "certificado empresa",
  "certificateFingerprint": "sha256:...",
  "certificateExpiresAt": "2027-08-19T00:00:00Z",
  "serviceBaseUrl": "https://catalogo-vpfe-hab.dian.gov.co/...",
  "testSetId": "set-pruebas",
  "acceptedResponsibility": true,
  "status": "READY_FOR_TEST",
  "lastTestStatus": "NOT_TESTED",
  "lastTestAt": null,
  "lastTestMessage": null
}
```

`DianConfigurationTestResponse`:

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "status": "TESTED",
  "lastTestStatus": "SUCCESS",
  "lastTestAt": "2026-08-19T10:00:00Z",
  "lastTestMessage": "Configuracion real lista para pruebas controladas DIAN."
}
```

### ProviderSubmissionRequest

```json
{
  "companyId": "uuid",
  "documentId": "uuid",
  "documentType": "ELECTRONIC_INVOICE",
  "payload": {},
  "idempotencyKey": "uuid"
}
```

### ProviderSubmissionResponse

```json
{
  "trackingId": "provider-tracking-id",
  "status": "ACCEPTED",
  "cufeCude": "abc123",
  "qrContent": "https://...",
  "artifacts": [
    {
      "type": "XML",
      "storageUri": "s3://bucket/path.xml",
      "contentHash": "sha256"
    }
  ],
  "rawResponse": {}
}
```

Reglas:

- No guardar secretos en payloads ni logs.
- Todo error externo debe mapearse a `EXTERNAL_PROVIDER_ERROR` con detalle seguro.
- La integracion real de envio queda implementada parcialmente como pipeline configurable `stub/http` en Fase 20 TASK-153 a TASK-163, con XML UBL base, firma/validacion de referencia, CUFE/CUDE, QR, transporte, respuestas, artefactos e idempotencia. Esta base no equivale a conexion DIAN real aceptada porque falta SOAP WCF, WS-Security/firma certificada y mapeo completo de respuestas DIAN.
- En desarrollo local se usa el microservicio mock sin llamadas externas y solo sirve para probar el flujo interno.
- Variables locales del mock:
  - `DIAN_PROVIDER_MODE=mock`.
  - `DIAN_MOCK_DEFAULT_STATUS=ACCEPTED|REJECTED|FAILED`.
  - `DIAN_MOCK_ERROR_CODE`.
  - `DIAN_MOCK_ERROR_MESSAGE`.
- El servicio cuenta con compuerta tecnica para modo real: verifica artefactos DIAN locales/configurados (`DIAN_TECHNICAL_ARTIFACTS_ROOT`, XSD UBL 2.1, Schematron DIAN, XSL compilado y lista de codigos) antes de aprobar una prueba de configuracion real.
- El envio de documentos en modo real no debe degradar silenciosamente a mock; si falla configuracion, artefactos tecnicos o transporte, retorna error funcional sanitizado.
- El request de configuracion DIAN no puede incluir secretos en claro; solo referencias seguras o datos no sensibles. Si una UI necesita cargar un certificado, debe enviarlo a un endpoint seguro que lo almacene en Secrets Manager/almacen equivalente y retorne referencia no sensible.
- Las respuestas API nunca retornan certificado, PIN, claves, tokens ni credenciales; solo banderas `*Configured`, alias, huella, vencimiento y estado.
- Activar modo real exige `responsibilityAccepted=true` para confirmar que la empresa entiende que opera su propia habilitacion/certificacion DIAN.

### Transporte SOAP WCF DIAN objetivo

Endpoint habilitacion:

```text
https://vpfe-hab.dian.gov.co/WcfDianCustomerServices.svc
WSDL: https://vpfe-hab.dian.gov.co/WcfDianCustomerServices.svc?wsdl
Single WSDL: https://vpfe-hab.dian.gov.co/WcfDianCustomerServices.svc?singleWsdl
```

Operaciones iniciales:

- `SendTestSetAsync(fileName, contentFile, testSetId)`: envio de set de pruebas de habilitacion.
- `GetStatusZip(trackId)`: consulta de estado del ZIP/ZipKey retornado.
- `GetStatus(trackId)`: consulta de estado por CUFE/CUDE cuando aplique.
- `SendBillSync(fileName, contentFile)`: envio sincronico productivo o pruebas controladas si la configuracion DIAN lo exige.
- `SendBillAsync(fileName, contentFile)`: envio asincrono cuando el flujo aprobado lo permita.
- `GetNumberingRange(accountCode, accountCodeT, softwareCode)`: consulta de rangos cuando se apruebe sincronizacion de resoluciones.

Reglas SOAP:

- Cada operacion define `SOAPAction` segun WSDL DIAN.
- `contentFile` debe corresponder al ZIP/base64 del XML UBL firmado cuando aplique.
- El envelope debe incluir headers requeridos por DIAN/WCF y seguridad WS-Security/X.509 segun operacion/anexo.
- El cliente SOAP debe resolver endpoint por empresa/ambiente y no desde configuracion global unica.
- Respuestas SOAP se normalizan a `ProviderSubmissionStatus`, codigo DIAN, mensaje, tracking/zipKey, `ApplicationResponse`, CUFE/CUDE, QR y artefactos privados.

## accounting-service

Responsabilidad: PUC, cuentas por empresa, asientos, libro diario y libro mayor.

Estado TASK-037:

- Microservicio fisico implementado en `services/accounting-service`.
- Persistencia propia bajo schema `accounting`.
- `POST /api/v1/accounting-entries` es idempotente por `companyId`, `sourceType` y `sourceId`; si el asiento ya existe, retorna el asiento existente.

### Configuracion base contable

- `POST /api/v1/accounting-setup/basic`

Crea o reactiva una plantilla contable minima para pruebas operativas de pequenas empresas en Colombia. La plantilla no sustituye el PUC oficial completo ni una parametrizacion contable profesional; deja cuentas y reglas editables por empresa.

Respuesta:

```json
{
  "companyId": "uuid",
  "templateName": "BASIC_COLOMBIA_SMALL_BUSINESS",
  "accounts": [
    {"code": "1105", "name": "Caja", "active": true}
  ],
  "rules": [
    {"eventType": "SALE_CONFIRMED", "sourceType": "SALE", "active": true}
  ]
}
```

Reglas:

- Crea las cuentas base `1105`, `1110`, `1305`, `1435`, `1520`, `2205`, `2408`, `4135`, `5105` y `5135` si no existen para la empresa.
- Si una cuenta base existe inactiva, la reactiva sin duplicar codigo.
- Crea reglas faltantes de venta, compras documentales, reabastecimiento, gastos, activos, deudores, pago de cuenta por pagar, recaudo de cuenta por cobrar y pago diario de nomina.
- Si una regla activa ya existe para el evento contable, la conserva sin reemplazarla aunque haya sido usada por procesos reales.
- Los errores por regla faltante se reportan como `400 BUSINESS_RULE_VIOLATION` con mensaje funcional segun la operacion afectada.

### Cuentas

- `POST /api/v1/accounts`
- `POST /api/v1/accounts/batch`
- `GET /api/v1/accounts?code=`
- `GET /api/v1/accounts?active=`

### AccountsBatchRequest

Permite crear una o varias cuentas en una sola operacion. La operacion debe ser transaccional.

```json
{
  "accounts": [
    {
      "code": "1105",
      "name": "Caja",
      "description": "Efectivo y caja general",
      "category": "ASSET",
      "active": true
    },
    {
      "code": "4135",
      "name": "Comercio al por mayor y al por menor",
      "description": "Ingresos operacionales",
      "category": "INCOME",
      "active": true
    }
  ]
}
```

Reglas:

- Si una cuenta del lote es invalida, duplicada o viola el PUC/configuracion empresarial, no se crea ninguna cuenta del lote.
- El backend debe devolver detalles por indice/fila para que la SPA resalte el campo correspondiente.
- Cuentas usadas por asientos no se eliminan fisicamente; solo pueden inactivarse conservando historial.

### Reglas contables

- `POST /api/v1/accounting-rules`
- `POST /api/v1/accounting-rules/batch`
- `PUT /api/v1/accounting-rules/active`
- `POST /api/v1/accounting-rules/{eventType}/deactivate`
- `GET /api/v1/accounting-rules?eventType=&active=`

Uso obligatorio en cierre de venta:

- `billing-service` debe consultar `GET /api/v1/accounting-rules?eventType=SALE_CONFIRMED&active=true` antes de asignar numeracion fiscal o enviar a DIAN/mock.
- Si la respuesta esta vacia, el cierre debe bloquearse con `400 BUSINESS_RULE_VIOLATION` y mensaje funcional de configuracion contable requerida.
- El frontend usa el mismo contrato para mostrar estado en `Configuracion contable`.

### Asientos

- `POST /api/v1/accounting-entries`

### Reportes

- `GET /api/v1/reports/journal?from=&to=`
- `GET /api/v1/reports/ledger?from=&to=&accountCode=`

### AccountingRuleRequest

```json
{
  "eventType": "SALE_CONFIRMED",
  "sourceType": "SALE",
  "name": "Venta POS",
  "lines": [
    {
      "accountCode": "1105",
      "side": "DEBIT",
      "amountType": "TOTAL",
      "description": "Caja"
    },
    {
      "accountCode": "4135",
      "side": "CREDIT",
      "amountType": "SUBTOTAL",
      "description": "Ingresos"
    }
  ]
}
```

### AccountingRulesBatchRequest

Permite crear una o varias reglas contables en una sola operacion. Cada `line` se presenta en la SPA como `movimiento contable`.

```json
{
  "rules": [
    {
      "eventType": "SALE_CONFIRMED",
      "sourceType": "SALE",
      "name": "Venta facturada",
      "activate": true,
      "lines": [
        {
          "accountCode": "1105",
          "side": "DEBIT",
          "amountType": "TOTAL",
          "description": "Ingreso a caja"
        },
        {
          "accountCode": "4135",
          "side": "CREDIT",
          "amountType": "SUBTOTAL",
          "description": "Ingreso por venta"
        },
        {
          "accountCode": "2408",
          "side": "CREDIT",
          "amountType": "TAX",
          "description": "IVA generado"
        }
      ]
    }
  ]
}
```

Reglas:

- La operacion es transaccional: si falla cualquier regla o movimiento contable, no se crea ninguna regla del lote.
- Todas las cuentas referenciadas deben existir activas para la empresa o venir en el mismo flujo de configuracion aprobado.
- Si `activate=true`, el backend debe inactivar/versionar la regla activa previa del mismo evento sin borrar historial.
- Los errores deben incluir indice de regla e indice de movimiento para soportar validacion visual por fila.
- La creacion batch queda reservada para parametrizacion manual. La accion `Completar plantilla basica` debe usar `POST /api/v1/accounting-setup/basic`, no este endpoint, para evitar duplicados cuando ya existen cuentas o reglas.

### AccountingEntryRequest

```json
{
  "eventType": "SALE_CONFIRMED",
  "sourceType": "SALE",
  "sourceId": "uuid",
  "entryDate": "2026-05-11",
  "description": "Venta POS 123",
  "thirdpartyId": "uuid",
  "subtotal": 30000,
  "taxTotal": 5700,
  "total": 35700
}
```

Reglas:

- Todo asiento posteado debe cumplir partida doble.
- En la implementacion local actual, `POST /api/v1/accounting-entries` genera un asiento `POSTED` inmediatamente desde reglas contables activas por empresa; el flujo draft/post-by-id queda pendiente hasta que se apruebe un modelo de borradores contables.
- Reintentar la contabilizacion de una misma venta no debe crear un segundo asiento.
- Las cuentas deben derivar del PUC colombiano o de una configuracion aprobada por empresa.
- TASK-053 agrega configuracion base contable editable, consulta de cuentas por empresa/filtro activo, consulta de reglas por empresa/evento/estado, reemplazo de regla activa y desactivacion explicita de la regla activa.
- TASK-220 agrega prevalidacion de regla activa `SALE_CONFIRMED` desde `billing-service` para evitar ventas parcialmente confirmadas cuando una empresa no ha inicializado contabilidad.
- TASK-222 expone `Configuracion contable` en la SPA usando `POST /api/v1/accounting-setup/basic`, `GET /api/v1/accounts` y `GET /api/v1/accounting-rules`.
- TASK-223 redefine la configuracion contable como asistente editable y agrega contratos batch `POST /api/v1/accounts/batch`, `POST /api/v1/accounting-rules/batch` y `POST /api/v1/accounting-configuration/batch` para crear una o varias cuentas/reglas en una sola accion transaccional.
- `PUT /api/v1/accounting-rules/active` desactiva la regla activa previa para el evento y crea una nueva regla activa; `POST /api/v1/accounting-rules` conserva la validacion de no duplicar regla activa.
- Un documento fiscal validado debe poder rastrearse hasta su asiento contable.

### AccountingConfigurationRequest

`POST /api/v1/accounting-configuration/batch`

```json
{
  "accounts": [
    {
      "code": "1105",
      "name": "Caja",
      "parentAccountId": null
    }
  ],
  "rules": [
    {
      "eventType": "SALE_CONFIRMED",
      "sourceType": "SALE",
      "name": "Venta facturada",
      "lines": [
        {
          "accountCode": "1105",
          "side": "DEBIT",
          "amountType": "TOTAL",
          "description": "Ingreso a caja"
        },
        {
          "accountCode": "4135",
          "side": "CREDIT",
          "amountType": "SUBTOTAL",
          "description": "Ingreso por venta"
        }
      ]
    }
  ]
}
```

Respuesta `201 Created`: `AccountingSetupResponse` con `companyId`, `templateName`, `accounts` y `rules` creadas.

Reglas:

- Requiere `X-Company-Id`.
- Debe enviarse al menos una cuenta o una regla.
- Las cuentas del lote no pueden repetir codigo PUC ni existir previamente para la empresa.
- Las reglas del lote no pueden repetir `eventType` por empresa.
- Cada movimiento contable debe referenciar una cuenta existente o una cuenta incluida en el mismo lote.
- Cada regla debe tener al menos un movimiento debito y uno credito.
- La operacion es atomica: si falla una cuenta, regla o movimiento, no se persiste ningun registro del lote.
- TASK-224 agrega `used` y `usageCount` a `AccountResponse` y `AccountingRuleResponse`.
- `PUT /api/v1/accounts/{accountId}` permite actualizar cuenta solo cuando `usageCount = 0`.
- `POST /api/v1/accounts/{accountId}/deactivate` inactiva cuenta solo cuando `usageCount = 0`.
- `PUT /api/v1/accounting-rules/{ruleId}` permite actualizar regla solo cuando `usageCount = 0`.
- `POST /api/v1/accounting-rules/{ruleId}/deactivate` inactiva regla solo cuando `usageCount = 0`.
- Si el recurso ya fue usado, el backend debe responder `400 BUSINESS_RULE_VIOLATION` o equivalente funcional sin modificar datos.

### Cuentas por cobrar

- `POST /api/v1/accounts-receivable`
- `GET /api/v1/accounts-receivable?status=&customerId=&from=&to=`
- `POST /api/v1/accounts-receivable/{receivableId}/payments`
- `GET /api/v1/reports/accounts-receivable?status=&customerId=&from=&to=`

### AccountsReceivableRequest

```json
{
  "customerId": "uuid",
  "sourceType": "SALE",
  "sourceId": "uuid",
  "issueDate": "2026-05-11",
  "dueDate": "2026-06-10",
  "totalAmount": 35700,
  "idempotencyKey": "sale-credit-001"
}
```

### ReceivablePaymentRequest

```json
{
  "paymentDate": "2026-05-20",
  "amount": 10000,
  "paymentMethod": "BANK_TRANSFER",
  "reference": "TRX-001"
}
```

Reglas:

- Todos los endpoints requieren `X-Company-Id`.
- `sourceType` debe identificar el documento origen aprobado: `SALE`, `ELECTRONIC_INVOICE`, `ELECTRONIC_POS` u `OPENING_BALANCE`.
- `totalAmount` debe ser mayor que cero y `dueDate` no debe ser anterior a `issueDate`.
- Un pago no puede exceder el saldo pendiente.
- El reporte de cuentas por cobrar debe consultar el modelo nuevo `accounting_accounts_receivable`, no tablas legacy ni solo saldos contables agregados.

## audit-service

Responsabilidad: auditoria tecnica y fiscal.

Estado TASK-042:

- Microservicio fisico implementado en `services/audit-service`.
- Persistencia propia bajo schema `audit`.
- Registra eventos de auditoria fiscal/tecnica por empresa mediante `X-Company-Id`.
- Permite consultar eventos por recurso, rango de fechas y usuario.
- TASK-043 conecta `billing-service` como primer productor automatico para eventos de confirmacion de venta y documento electronico.
- TASK-090 conecta el BFF como auditor transversal best-effort para mutaciones publicas y `catalog-service` como productor especifico de cambios de catalogos.
- La integracion asincrona productiva con inventario y contabilidad queda cubierta por Outbox/Inbox, EventBridge/SQS y Lambdas. En modo local/transitorio pueden existir efectos sincronicos o dispatchers deshabilitados segun configuracion.

### Endpoints

- `POST /api/v1/audit-events`
- `GET /api/v1/audit-events?resourceType=&resourceId=&from=&to=&userId=`

### AuditEventRequest

```json
{
  "userId": "uuid",
  "eventType": "ELECTRONIC_DOCUMENT",
  "resourceType": "SALE",
  "resourceId": "sale-1",
  "action": "CONFIRM_SALE",
  "result": "SUCCESS",
  "detail": "{\"documentStatus\":\"VALIDATED\",\"providerStatus\":\"ACCEPTED\"}"
}
```

### AuditEventResponse

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "userId": "uuid",
  "eventType": "ELECTRONIC_DOCUMENT",
  "resourceType": "SALE",
  "resourceId": "sale-1",
  "action": "CONFIRM_SALE",
  "result": "SUCCESS",
  "detail": "{\"documentStatus\":\"VALIDATED\",\"providerStatus\":\"ACCEPTED\"}",
  "occurredAt": "2026-05-20T10:00:00Z"
}
```

Reglas:

- No registrar secretos ni datos sensibles innecesarios.
- Operaciones fiscales, cambios de resolucion, emision, anulacion, ajustes e integraciones externas son auditables.
- Toda accion mutable expuesta por la SPA debe registrar un evento de auditoria si llega con `X-Company-Id`; si el registro falla, la accion principal no se revierte.
- `detail` debe contener detalle seguro, sin certificados, API keys, tokens, credenciales, payloads completos del proveedor ni datos sensibles innecesarios.
- Ninguna consulta debe retornar eventos de otra empresa.
- La SPA expone el modulo `Logs` para `ROOT`, `OWNER`/`ADMIN` o usuarios con `AUDIT_VIEW`; consulta por empresa activa y filtra por recurso, identificador y rango de fechas.
- `billing-service` registra eventos canonicos en Outbox despues de confirmar una venta nueva. El resultado funcional es `SUCCESS` cuando el documento queda `VALIDATED`; de lo contrario es `FAILURE`.
- La entrega productiva hacia auditoria, inventario, contabilidad, reintentos DIAN y reportes usa Outbox/Inbox, EventBridge/SQS, DLQ e idempotencia. En local puede mantenerse deshabilitada con `EVENTING_EVENTBRIDGE_ENABLED=false`.

## Contratos objetivo vigentes para flujo operativo

Estos contratos documentan el flujo operativo activo y el backlog funcional aun no expuesto completamente. Cualquier endpoint marcado como contrato objetivo debe implementarse con tarea y criterio de aceptacion antes de usarse desde la SPA productiva.

### thirdparty-service: ThirdPartyResponse

Implementado en TASK-047 para `/api/v1/third-parties`, `/api/v1/customers` y `/api/v1/suppliers`.

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "roles": ["CUSTOMER", "SUPPLIER"],
  "personType": "JURIDICA",
  "identificationTypeCode": 31,
  "identificationNumber": "900123456",
  "verificationDigit": "7",
  "fullName": null,
  "businessName": "Cliente Proveedor SAS",
  "tradeName": "Cliente Proveedor",
  "email": "contacto@example.com",
  "phone": "3000000000",
  "address": "Calle 1 # 2-3",
  "municipalityCode": "11001",
  "taxResponsibilities": ["O-13"],
  "active": true
}
```

### inventory-service: items de servicio e insumos

- `POST /api/v1/products` debe aceptar `itemType`, `saleEnabled`, `purchaseEnabled` y `stockTracked`.
- `POST /api/v1/service-supply-references` crea una referencia informativa entre un servicio y un insumo.
- `GET /api/v1/products/{serviceId}/supply-references` lista insumos sugeridos.
- `POST /api/v1/inventory-movements` debe aceptar `CONSUMPTION_OUT` y `WASTE_OUT`.

Regla: crear o confirmar una venta de servicio no invoca automaticamente `CONSUMPTION_OUT`.

### inventory-service/accounting-service: compras, gastos y cuentas por pagar

- `POST /api/v1/purchases`
- `POST /api/v1/purchases/{purchaseId}/confirm`
- `POST /api/v1/expenses`
- `POST /api/v1/expenses/{expenseId}/confirm`
- `POST /api/v1/accounts-payable`
- `GET /api/v1/accounts-payable?status=&supplierId=&from=&to=`
- `POST /api/v1/accounts-payable/{payableId}/payments`

### ExpenseRequest

```json
{
  "supplierId": "uuid",
  "expenseDate": "2026-05-20",
  "concept": "Servicio publico energia",
  "subtotal": 100000,
  "taxTotal": 19000,
  "total": 119000,
  "paymentCondition": "CREDIT",
  "dueDate": "2026-06-20",
  "evidenceUrl": "https://example.local/evidence.pdf"
}
```

### AccountsPayableRequest

```json
{
  "supplierId": "uuid",
  "sourceType": "PURCHASE",
  "sourceId": "uuid",
  "issueDate": "2026-05-20",
  "dueDate": "2026-06-20",
  "totalAmount": 107100
}
```

### PayablePaymentRequest

```json
{
  "paymentDate": "2026-05-25",
  "amount": 40000,
  "paymentMethod": "BANK_TRANSFER",
  "reference": "TRX-1"
}
```

Reglas:

- Un gasto confirmado no afecta stock.
- Un gasto o compra a credito crea una cuenta por pagar asociada a `sourceType` y `sourceId`.
- Un pago parcial reduce `balance` y deja estado `PARTIALLY_PAID`; un pago total deja estado `PAID`.
- La contabilizacion depende de reglas PUC parametrizadas por empresa; si la regla no existe, el comando contable falla de forma explicita.

### identity-service: roles y permisos

Contrato local/transitorio para desarrollo y E2E. En produccion, autenticacion publica pasa por Cognito Hosted UI + BFF session; `POST /api/v1/auth/login` no se expone al navegador.

- `POST /api/v1/users`
- `POST /api/v1/auth/login`
- `GET /api/v1/me`
- `GET /api/v1/me/companies`
- `POST /api/v1/companies/{companyId}/memberships`
- `POST /api/v1/companies/{companyId}/users/{userId}/roles`
- `PUT /api/v1/companies/{companyId}/memberships/{membershipId}/roles`
- `GET /api/v1/companies/{companyId}/permissions?userId=`

Roles minimos: `OWNER`, `ADMIN`, `CASHIER`, `ACCOUNTANT`, `AUDITOR`.

Permisos iniciales: `USERS_MANAGE`, `ROLES_MANAGE`, `SALES_CREATE`, `FISCAL_DOCUMENTS_ISSUE`, `INVENTORY_MANAGE`, `PURCHASES_MANAGE`, `ACCOUNTING_MANAGE`, `REPORTS_VIEW`, `AUDIT_VIEW`, `LICENSE_MANAGE`.

#### CreateUserRequest

```json
{
  "email": "owner@example.com",
  "fullName": "Owner User",
  "password": "secret123"
}
```

#### LoginResponse

Respuesta local/transitoria. No aplica para produccion.

```json
{
  "userId": "uuid",
  "email": "owner@example.com",
  "fullName": "Owner User",
  "tokenType": "Bearer",
  "accessToken": "opaque-token",
  "expiresAt": "2026-07-16T22:00:00Z"
}
```

#### MembershipRequest

```json
{
  "userId": "uuid",
  "roles": ["OWNER"]
}
```

#### CompanyAccessResponse

```json
{
  "companyId": "uuid",
  "roles": ["OWNER"],
  "permissions": ["ROLES_MANAGE", "REPORTS_VIEW"]
}
```
### bff-service

Responsabilidad: frontera publica consumida por la SPA y expuesta por API Gateway en produccion.

Estado TASK-063:

- Microservicio fisico inicial implementado en `services/bff-service`.
- No tiene persistencia ni reglas fiscales propias.
- Expone `/api/v1/**` solo para rutas aprobadas y enruta hacia el microservicio dueno del contrato.
- Propaga `Authorization`, `X-Company-Id`, `X-Correlation-Id`, `Idempotency-Key`, `Content-Type` y `Accept`.
- Filtra cabeceras de respuesta a `Content-Type` y `X-Correlation-Id`.
- Rechaza rutas internas no publicas, por ejemplo `/api/v1/provider/**`.

Reglas:

- El frontend debe consumir siempre el BFF.
- Los microservicios internos no deben exponerse al navegador.
- El BFF no debe implementar reglas de negocio que pertenezcan a billing, inventory, accounting, tenant, identity, catalog, thirdparty o audit.
- Un fallo de servicio interno debe responder como error publico estructurado sin stack trace ni detalles de infraestructura.
- Estado TASK-065: `/api/v1/auth/**` y `/api/v1/me/**` se enrutan a `identity-service`; `/api/v1/companies/{companyId}/license/**` se enruta a `tenant-service`; `/api/v1/companies/{companyId}/memberships`, `/api/v1/companies/{companyId}/users/{userId}/roles` y `/api/v1/companies/{companyId}/permissions` se enrutan a `identity-service`.
- Estado objetivo TASK-164/TASK-174: en produccion, el BFF no recibe `Authorization` desde la SPA; resuelve cookie segura server-side y genera headers internos.

### Reglas RBAC billing via BFF

- `POST /api/v1/sales` y `POST /api/v1/sales/{saleId}/confirm`: requiere `SALES_CREATE`.
- `POST /api/v1/electronic-pos?saleId=`: requiere `SALES_CREATE` o `FISCAL_DOCUMENTS_ISSUE`.
- `GET /api/v1/sales/**`, `GET /api/v1/electronic-pos/**`, `GET /api/v1/electronic-invoices/**`: permite `SALES_CREATE`, `REPORTS_VIEW` o `FISCAL_DOCUMENTS_ISSUE` segun consulta operativa.
- `POST /api/v1/issuers`, `POST /api/v1/numbering-resolutions`, `POST /api/v1/credit-notes`, `POST /api/v1/debit-notes` y `POST /api/v1/electronic-pos/{documentId}/adjustment-notes`: requiere `FISCAL_DOCUMENTS_ISSUE` o permiso administrativo indicado para configuracion fiscal.
## tenant-service: licenciamiento

- `POST /api/v1/companies/{companyId}/license`
- `GET /api/v1/companies/{companyId}/license`
- `PUT /api/v1/companies/{companyId}/license/suspend`
- `PUT /api/v1/companies/{companyId}/license/activate`
- `GET /api/v1/companies/{companyId}/license/validation?action=ISSUE_FISCAL_DOCUMENT`

Acciones iniciales de validacion: `CREATE_TRANSACTION`, `ISSUE_FISCAL_DOCUMENT`, `CREATE_USER`.
Los servicios consumidores deben evaluar este contrato antes de comandos de negocio que creen usuarios, transacciones o documentos fiscales.
Si `allowed=false`, el servicio consumidor debe bloquear el comando con error estructurado usando `reasonCode` y `message`.
Las consultas, exportaciones y acciones administrativas de recuperacion no requieren validacion de licencia en esta fase.
Estado TASK-058 licencia consumidores:

- `billing-service` valida licencia contra `tenant-service` antes de crear una venta nueva (`CREATE_TRANSACTION`) y antes de confirmar/emision fiscal (`ISSUE_FISCAL_DOCUMENT`).
- `identity-service` valida licencia contra `tenant-service` antes de crear o actualizar una membresia/roles por empresa mediante accion `CREATE_USER`.
- La creacion global de usuario en `identity-service` no consume licencia porque todavia no pertenece a una empresa; el control aplica al asociarlo a una empresa.
- Los servicios consumidores usan `TENANT_SERVICE_URL` como configuracion externa y no dependen de otros contenedores para arrancar.

### CompanyLicenseRequest

```json
{
  "planCode": "SMALL_BUSINESS",
  "validFrom": "2026-05-01",
  "validTo": "2027-05-01",
  "maxUsers": 5,
  "maxMonthlyDocuments": 1000
}
```

### CompanyLicenseResponse

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "planCode": "SMALL_BUSINESS",
  "status": "ACTIVE",
  "validFrom": "2026-05-01",
  "validTo": "2027-05-01",
  "maxUsers": 5,
  "maxMonthlyDocuments": 1000,
  "createdAt": "2026-05-19T10:00:00Z",
  "updatedAt": "2026-05-19T10:00:00Z"
}
```

### CompanyLicenseValidationResponse

```json
{
  "companyId": "uuid",
  "action": "ISSUE_FISCAL_DOCUMENT",
  "allowed": false,
  "status": "SUSPENDED",
  "reasonCode": "LICENSE_SUSPENDED",
  "message": "La licencia de la empresa esta suspendida."
}
```
### Reportes minimos por servicio dueno del dato

TASK-054 implementa los reportes minimos como endpoints de lectura en los servicios duenos del dato.
Desde TASK-185 existe `reporting-service` como microservicio fisico para catalogo/opciones/query de reportes avanzados. Los reportes minimos se conservan en `billing-service`, `inventory-service` y `accounting-service` como fuentes canonicas; las proyecciones event-driven viven en `reporting-projection-lambda`.

#### billing-service

- `GET /api/v1/reports/sales?status=&from=&to=&sellerId=&customerId=&productId=&paymentMethodCode=&documentStatus=`
- `GET /api/v1/reports/electronic-documents?documentType=&status=&customerId=&from=&to=&prefix=&number=&cufeCude=`

Reglas:

- `sellerId`, `customerId`, `productId`, `paymentMethodCode` y `documentStatus` son filtros opcionales.
- `productId` filtra ventas que tengan al menos una linea asociada al producto/servicio indicado.
- Los filtros opcionales se construyen como predicados dinamicos en backend; no se deben implementar con JPQL que dependa de parametros nulos.
- `reporting-service` usa este contrato como fuente canonica para `SALES_BY_SELLER` y `SALES_BY_PRODUCT`.

#### inventory-service

- `GET /api/v1/reports/inventory-stock?active=`
- `GET /api/v1/reports/kardex?productId=&from=&to=`
- `GET /api/v1/reports/purchases?status=&supplierId=&from=&to=`

#### accounting-service

- `GET /api/v1/reports/expenses?status=&supplierId=&from=&to=`
- `GET /api/v1/accounts-payable?status=&supplierId=&from=&to=`
- `GET /api/v1/reports/journal?from=&to=`
- `GET /api/v1/reports/ledger?from=&to=&accountCode=`
- `GET /api/v1/reports/trial-balance?from=&to=&accountCode=`
- `GET /api/v1/reports/accounts-receivable?status=&customerId=&from=&to=`

#### Estado TASK-055

- `GET /api/v1/reports/accounts-receivable?status=&customerId=&from=&to=` consulta el agregado de cartera por cobrar persistido en `accounting-service`.

## Eventos internos propuestos

Los eventos pueden implementarse inicialmente como llamadas sincrones y luego migrar a mensajeria.

### `SaleConfirmed`

Publicado por: `billing-service`.

Consumidores:

- `inventory-service`
- `accounting-service`

```json
{
  "eventId": "uuid",
  "companyId": "uuid",
  "saleId": "uuid",
  "documentId": "uuid",
  "occurredAt": "2026-05-11T22:00:00Z",
  "items": [
    {
      "productId": "uuid",
      "quantity": 2
    }
  ],
  "total": 35700
}
```

### `ElectronicDocumentValidated`

Publicado por: `billing-service`.

Consumidores:

- `inventory-service`
- `accounting-service`
- `audit-service`

### `InventoryMovementRegistered`

Publicado por: `inventory-service`.

Consumidores:

- `accounting-service`
- `audit-service`

### `AccountingEntryPosted`

Publicado por: `accounting-service`.

Consumidores:

- `reporting-service` para catalogo, opciones y consultas de reportes avanzados; `reporting-projection-lambda` queda reservado para proyecciones asincronas reconstruibles.
- `audit-service`

## Contract tests futuros

Cuando los servicios existan fisicamente, cada contrato debe tener:

- Pruebas de consumidor para requests.
- Pruebas de proveedor para responses.
- Validacion de headers obligatorios.
- Validacion de errores estandar.
- Validacion de aislamiento por `X-Company-Id`.

## Pendientes

- Reemplazar firma/validacion de referencia DIAN por XMLDSig/XAdES certificado y fixtures oficiales de habilitacion por empresa antes de operacion comercial real.
- Formalizar OpenAPI por servicio despues de estabilizar DTOs publicos y contratos BFF; Springdoc ya permite generar documentacion runtime, pero falta versionarla como artefacto controlado.
- Completar endpoints objetivo de actualizacion/inactivacion donde hoy existe solo contrato documentado.

### Estado TASK-052: documentos fiscales, consultas y notas

Implementacion local agregada:

- `GET /api/v1/sales?status=&from=&to=` lista ventas por empresa, estado y rango de fechas.
- `POST /api/v1/electronic-pos?saleId=` confirma la venta indicada y retorna la venta con documento POS.
- `POST /api/v1/electronic-pos/{documentId}/submit` retorna el estado persistido del documento POS; el envio real ya ocurre al confirmar la venta.
- `GET /api/v1/electronic-pos/{documentId}` consulta documento POS por empresa.
- `POST /api/v1/electronic-invoices?saleId=` confirma la venta indicada y retorna la venta con factura electronica.
- `POST /api/v1/electronic-invoices/{documentId}/issue` retorna el estado persistido de la factura; el envio real ya ocurre al confirmar la venta.
- `GET /api/v1/electronic-invoices/{documentId}` consulta factura por empresa.
- `GET /api/v1/electronic-invoices?status=&customerId=&from=&to=&prefix=&number=&cufeCude=` consulta facturas por filtros fiscales.
- `GET /api/v1/electronic-invoices/{documentId}/artifacts` retorna artefactos mock `XML`, `PDF` y `QR` para pruebas funcionales.
- `GET /api/v1/electronic-invoices/{documentId}/events` retorna evento fiscal mock de envio/estado del proveedor.
- `POST /api/v1/credit-notes` crea y envia una nota credito con numeracion propia.
- `GET /api/v1/credit-notes/{noteId}` consulta nota credito.
- `POST /api/v1/debit-notes` crea y envia una nota debito con numeracion propia.
- `GET /api/v1/debit-notes/{noteId}` consulta nota debito.
- `POST /api/v1/electronic-pos/{documentId}/adjustment-notes` crea y envia una nota de ajuste POS con numeracion propia.
- `dian-provider-service` agrega `POST /api/v1/provider/credit-notes`, `POST /api/v1/provider/debit-notes` y `POST /api/v1/provider/pos-adjustment-notes`.

### FiscalNoteRequest

```json
{
  "originalDocumentId": "uuid",
  "adjustmentKind": "CANCELLATION",
  "reason": "Correccion de valor facturado",
  "subtotal": 10000,
  "taxTotal": 1900,
  "total": 11900
}
```

Reglas:

- `adjustmentKind` solo aplica para `POS_ADJUSTMENT_NOTE`; si se omite en nota de ajuste POS, se toma `CORRECTION`.
- Nota credito y nota debito requieren una factura electronica original `VALIDATED` de la misma empresa.
- Nota de ajuste POS requiere un documento `ELECTRONIC_POS` original `VALIDATED` de la misma empresa.
- Cada nota usa resolucion y consecutivo propio segun `documentType`: `CREDIT_NOTE`, `DEBIT_NOTE` o `POS_ADJUSTMENT_NOTE`.
- La respuesta mock aceptada deja `status=VALIDATED`, `providerStatus=ACCEPTED`, CUFE/CUDE y QR simulados.
- La idempotencia se conserva por `companyId` e `Idempotency-Key`.


## BFF public API boundary

El frontend objetivo consume solo el `bff-service` expuesto por API Gateway. Los microservicios internos no se publican directamente al navegador. El BFF debe:

- Propagar `Authorization`, `X-Company-Id`, `X-Correlation-Id` e `Idempotency-Key`.
- Propagar y validar `X-User-Id` contra `/api/v1/me` para acciones empresariales protegidas.
- Validar permisos efectivos en `identity-service` antes de enrutar catálogos administrables, contabilidad, nomina y logs. `ROOT` conserva acceso global por `/api/v1/platform/permissions`.
- Agregar respuestas de varios servicios cuando el flujo de pantalla lo requiera.
- Normalizar errores publicos sin exponer detalles internos.
- Mantener contratos publicos versionados independientes de contratos internos.

## Event envelope target

Los eventos productivos se publicaran hacia EventBridge/SQS usando un envelope comun versionado. TASK-062 lote 1 ya persiste este envelope en Outbox local de productores:

```json
{
  "eventId": "uuid",
  "eventType": "SaleConfirmed",
  "eventVersion": 1,
  "occurredAt": "2026-07-21T00:00:00Z",
  "companyId": "uuid",
  "aggregateType": "SALE",
  "aggregateId": "uuid",
  "producer": "billing-service",
  "correlationId": "string",
  "idempotencyKey": "string",
  "payload": {}
}
```

Reglas:

- `taxCode` y `taxRate` son obligatorios para items vendibles (`saleEnabled=true`) y se toman del catalogo `SALES_TAX`.
- `GET /api/v1/products/by-barcode/{barcode}` filtra por `X-Company-Id`, retorna productos activos y permite al POS agregar productos escaneados automaticamente.
- En UI, el usuario captura `precio final` con IVA incluido; la SPA calcula y envia `salePrice` como precio unitario sin IVA/base gravable. El backend no recibe `finalSalePrice` en el contrato estable.
- En ventas POS, la SPA no envia `unitPrice`, `taxCode` ni `taxRate`; `billing-service` toma precio e impuesto desde inventario y retorna por linea `subtotal`, `taxAmount` y `total`.
- La respuesta de producto es la fuente para el snapshot fiscal de linea que usa `billing-service`.

Reglas de pago:

- `paymentMethodCode` acepta `CASH`, `DEBIT_CARD`, `CREDIT_CARD`, `BREB_KEY`, `BANK_TRANSFER` o `VIRTUAL_WALLET`.
- `virtualWalletCode` solo aplica cuando `paymentMethodCode = VIRTUAL_WALLET`.
- `virtualWalletCode` acepta `NEQUI`, `DAVIPLATA`, `MOVII`, `DALE`, `RAPPIPAY`, `POWWI`, `CFA_EXPRESS`, `AV_VILLAS_DIGITAL_DEPOSIT`, `MOSI` o `BBVA_DINERO_MOVIL`.

Eventos canonicos iniciales:

- `SaleConfirmed`: productor `billing-service`, agregado `SALE`; payload con `saleId`, `documentId`, `documentType`, `status`, `total`, `issuedAt`, items e indicadores de efectos de inventario/contabilidad.
- `ElectronicDocumentValidated`: productor `billing-service`, agregado `ELECTRONIC_DOCUMENT`; payload con documento, tipo, prefijo, numero, CUFE/CUDE, estado de proveedor y total.
- `AuditEventRequested`: productor del servicio que origina la accion; payload con `eventType`, `resourceType`, `resourceId`, `action`, `result`, `userId` opcional y `detail` seguro.
- `ProviderSubmissionFailed`: productor `billing-service`, agregado `SALE`; payload con `saleId`, `documentId`, `documentType`, `documentIdempotencyKey`, `providerStatus=FAILED`, totales, lineas y datos de error tecnico. Solo `FAILED` entra a retry automatico; `REJECTED` requiere correccion manual.
- `InventoryMovementRegistered`: productor `inventory-service`, agregado `INVENTORY_MOVEMENT`; payload con movimiento, producto, referencia, cantidad, costo unitario y stock resultante.
- `AccountingEntryPosted`: productor `accounting-service`, agregado `ACCOUNTING_ENTRY`; payload con asiento, origen, fecha, descripcion, total debito y total credito.

Consumidores Lambda deben persistir Inbox o estado equivalente antes de materializar efectos no idempotentes. Los eventos deben conservar `companyId`, `correlationId` e `idempotencyKey`; `payload` no debe contener secretos ni datos de autenticacion.

Consumidores iniciales TASK-062:

- `audit-event-writer-lambda`: `AuditEventRequested` -> `audit.audit_event`.
- `inventory-sale-effect-lambda`: `SaleConfirmed` -> `inventory.stock_balance` y `inventory.inventory_movement`.
- `accounting-sale-entry-lambda`: `SaleConfirmed` -> `accounting.accounting_entry` y `accounting.accounting_outbox_event`.
- `provider-submission-retry-lambda`: `ProviderSubmissionFailed`/`ProviderSubmissionPending` -> reintento tecnico de documento en `billing.electronic_document`.
- `reporting-projection-lambda`: `SaleConfirmed`, `ElectronicDocumentValidated`, `InventoryMovementRegistered`, `AccountingEntryPosted` -> `reporting.reporting_event_projection`.

## EventBridge delivery mapping

El dispatcher Outbox publica cada evento canonico a EventBridge con:

- `eventBusName`: `EVENTING_EVENT_BUS_NAME`.
- `source`: `producer` del envelope, por ejemplo `billing-service`.
- `detailType`: `eventType`, por ejemplo `SaleConfirmed`.
- `detail`: envelope canonico JSON con `payload` como objeto JSON.

Regla de error: si AWS SDK retorna `failedEntryCount > 0`, el evento queda `FAILED` en Outbox con `publishAttempts` incrementado y `lastError` seguro. No se deben registrar secretos ni credenciales en `lastError`.

## Frontend data contract

Reglas:

- La SPA no debe enviar payloads construidos desde `initialState` demo.
- La SPA solo puede usar datos capturados por el usuario, datos derivados de sesion autenticada o datos cargados desde BFF/API.
- Las opciones de negocio seleccionables deben venir de `catalog-service` por BFF.
- Si un catalogo requerido no esta disponible, el frontend debe bloquear el submit y mostrar error controlado.
- El unico seed funcional permitido para pruebas locales iniciales es el usuario `ROOT`; las pruebas E2E crean el resto por API.

Catalogos obligatorios por API:

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

## audit-service v2

### Endpoints

- `GET /api/v1/audit-events?from=&to=&resourceType=`
- `GET /api/v1/audit-events/resource-types?from=&to=`

Reglas:

- Si `from` y `to` se omiten, el backend retorna eventos del dia actual segun zona horaria operativa configurada.
- `resourceType` es opcional y debe pertenecer a los tipos existentes o catalogados.
- La UI no debe exponer filtro manual por `resourceId`.
- ROOT puede consultar auditoria de la empresa activa o auditoria global cuando exista endpoint aprobado; administradores empresariales solo consultan su empresa.

## payroll-service

Responsabilidad: nomina interna, pagos diarios verbales, costos de personal y nomina electronica opcional por empresa.

### Endpoints implementados

- `GET /api/v1/payroll/settings`
- `PUT /api/v1/payroll/settings`
- `POST /api/v1/payroll/workers`
- `GET /api/v1/payroll/workers`
- `POST /api/v1/payroll/daily-payments`
- `GET /api/v1/payroll/daily-payments?from=&to=`
- `POST /api/v1/payroll/electronic-documents`
- `GET /api/v1/payroll/electronic-documents`

### PayrollSettingsRequest

```json
{
  "electronicPayrollEnabled": false,
  "providerMode": "MOCK"
}
```

### PayrollWorkerRequest

```json
{
  "fullName": "Persona Trabajo Diario",
  "identificationTypeCode": 13,
  "identificationNumber": "1234567890",
  "verificationDigit": null,
  "workerClassification": "DAILY_VERBAL_PAYMENT",
  "active": true
}
```

### DailyLaborPaymentRequest

```json
{
  "workerId": "uuid",
  "workDate": "2026-08-11",
  "activityDescription": "Apoyo en punto de venta",
  "agreedAmount": 80000,
  "paidAmount": 80000,
  "paymentMethodCode": "CASH",
  "legalNoticeAccepted": true,
  "notes": "Pago acordado verbalmente al final de la jornada"
}
```

### ElectronicPayrollDocumentRequest

```json
{
  "dailyLaborPaymentId": "uuid"
}
```

Reglas:

- `electronicPayrollEnabled=false` impide generar soporte electronico mock.
- Todo pago diario verbal debe registrar aceptacion de advertencia legal configurable.
- `classification=INDEPENDENT_CONTRACTOR` se contabiliza como egreso/proveedor o gasto operativo, no como empleado formal.
- Todo comando requiere `X-Company-Id`, `X-User-Id`, `X-Correlation-Id` e idempotencia cuando cree pagos, periodos o documentos.
- `POST /api/v1/payroll/daily-payments` intenta crear automaticamente un asiento en `accounting-service` con evento `PAYROLL_DAILY_PAYMENT_REGISTERED`, `sourceType=PAYROLL_DAILY_PAYMENT`, `sourceId=dailyLaborPaymentId`, `subtotal=0`, `taxTotal=0` y `total=paidAmount`.
- La integracion contable de pagos diarios es best-effort en la fase REST local: si `accounting-service` no esta disponible o rechaza temporalmente la solicitud, el pago de nomina ya persistido no se revierte.

## Ajustes TASK-076: UX colombiana y RBAC operativo

### tenant-service

- `GET /api/v1/companies`

Uso: selector global de empresas para `ROOT`.

Respuesta:

```json
[
  {
    "id": "uuid",
    "legalName": "Mi Empresa SAS",
    "tradeName": "Mi Tienda",
    "identificationTypeCode": 31,
    "identificationNumber": "900123456",
    "verificationDigit": "7",
    "email": "admin@example.com",
    "status": "ACTIVE"
  }
]
```

Regla: en esta fase local el endpoint se consume desde el panel ROOT; la autorizacion fuerte queda en la capa identity/BFF al endurecer seguridad gateway.

### identity-service

- `GET /api/v1/companies/{companyId}/users?email=`

Uso: modal de asignacion de roles. Permite buscar usuarios asociados a la empresa por correo o listar usuarios conocidos de la empresa.

Respuesta:

```json
[
  {
    "id": "uuid",
    "email": "vendedor@example.com",
    "fullName": "Usuario Vendedor",
    "status": "ACTIVE",
    "createdAt": "2026-07-16T10:00:00Z",
    "updatedAt": "2026-07-16T10:00:00Z"
  }
]
```

Reglas:

- `ROOT` puede listar usuarios de cualquier empresa.
- Un usuario empresarial solo puede listar usuarios de su empresa si tiene permisos de administracion de usuarios o roles.
- La asignacion final conserva `POST /api/v1/companies/{companyId}/users/{userId}/role-assignments` con `roleIds`.

### Equivalencias UI espanol / contrato tecnico

| Pantalla | Etiqueta UI | Valor tecnico enviado |
|---|---|---|
| Terceros | Cliente | `CUSTOMER` dentro de `roles` |
| Terceros | Proveedor | `SUPPLIER` dentro de `roles` |
| Terceros | Cliente y proveedor | `CUSTOMER`, `SUPPLIER` dentro de `roles` |
| Persona | Natural | `NATURAL` |
| Persona | Juridica | `JURIDICA` |
| Inventario | Bien fisico | `PHYSICAL_GOOD` |
| Inventario | Servicio/intangible | `SERVICE` |
| Inventario | Insumo | `SUPPLY` |
| Resolucion fiscal | POS electronico | `ELECTRONIC_POS` |
| Resolucion fiscal | Factura electronica | `ELECTRONIC_INVOICE` |
| Resolucion fiscal | Nota credito | `CREDIT_NOTE` |
| Resolucion fiscal | Nota debito | `DEBIT_NOTE` |
| Resolucion fiscal | Nota de ajuste POS | `POS_ADJUSTMENT_NOTE` |
| Ambiente | Pruebas | `TEST` |
| Ambiente | Produccion | `PRODUCTION` |

### Municipios

La UI debe mostrar departamento y municipio por nombre usando codigos DANE/DIVIPOLA. El backend recibe `municipalityCode` como string de 5 digitos. Ejemplo: `11001` para Bogota D.C.

## tenant-service licencias parametrizables

### Endpoints

- `POST /api/v1/companies/{companyId}/license`
- `GET /api/v1/companies/{companyId}/license`
- `PUT /api/v1/companies/{companyId}/license/activate`
- `PUT /api/v1/companies/{companyId}/license/suspend`
- `GET /api/v1/companies/{companyId}/license/validation?action=&module=`

### CompanyLicenseRequest

```json
{
  "planCode": "CUSTOM",
  "validFrom": "2026-08-11",
  "validTo": "2027-08-11",
  "maxUsers": 10,
  "maxMonthlyDocuments": 1000,
  "enabledModules": [
    "COMPANY",
    "THIRDPARTY",
    "INVENTORY",
    "BILLING",
    "ACCOUNTING",
    "REPORTS",
    "USERS"
  ]
}
```

### CompanyLicenseResponse

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "planCode": "CUSTOM",
  "status": "ACTIVE",
  "validFrom": "2026-08-11",
  "validTo": "2027-08-11",
  "maxUsers": 10,
  "maxMonthlyDocuments": 1000,
  "enabledModules": [
    "COMPANY",
    "THIRDPARTY",
    "INVENTORY",
    "BILLING"
  ],
  "createdAt": "2026-08-11T20:00:00Z",
  "updatedAt": "2026-08-11T20:00:00Z"
}
```

### CompanyLicenseValidationResponse

```json
{
  "companyId": "uuid",
  "action": "CREATE_TRANSACTION",
  "module": "BILLING",
  "allowed": true,
  "status": "ACTIVE",
  "maxUsers": 10,
  "maxMonthlyDocuments": 1000,
  "reasonCode": "LICENSE_ACTIVE",
  "message": "La licencia permite ejecutar la accion solicitada."
}
```

Reglas:

- `module` es opcional para compatibilidad; si se envia, debe estar dentro de `enabledModules`.
- Si la empresa no tiene licencia, el backend responde `404 RESOURCE_NOT_FOUND` con mensaje funcional y la SPA lo traduce a `Licencia no configurada`.
- ROOT puede crear o actualizar licencias. Usuarios empresariales no administran la licencia comercial de su empresa en esta fase.
- La licencia define modulos contratados; RBAC define permisos por usuario dentro de esos modulos.
- `maxUsers` y `maxMonthlyDocuments` pueden ser `null`; `null` significa sin limite comercial para esa cuota.
- Si `identity-service` detecta que una nueva asignacion empresarial supera `maxUsers`, debe responder `400 BUSINESS_RULE_VIOLATION`.
- Si `billing-service` detecta que una nueva emision supera `maxMonthlyDocuments`, debe responder `400 BUSINESS_RULE_VIOLATION`.

## tenant-service administracion de empresas

### Endpoints

- `POST /api/v1/companies`: crea empresa contratante. Uso reservado para ROOT desde la SPA/BFF.
- `PUT /api/v1/companies/{companyId}`: actualiza datos basicos de la empresa existente.
- `PUT /api/v1/companies/{companyId}/activate`: activa empresa. Uso reservado para ROOT.
- `PUT /api/v1/companies/{companyId}/suspend`: inactiva/suspende empresa. Uso reservado para ROOT.
- `GET /api/v1/companies/{companyId}`: consulta datos de empresa para mostrar nombre, identificacion y estado.

### CompanyRequest para actualizacion

```json
{
  "legalName": "Empresa Demo SAS",
  "tradeName": "Tienda Demo",
  "identificationTypeCode": 31,
  "identificationNumber": "900123456",
  "verificationDigit": "7",
  "email": "admin@example.com"
}
```

Reglas UI/BFF:

- ROOT ve selector de empresa y acciones de crear/actualizar/activar/inactivar.
- OWNER/ADMIN empresarial ve la empresa activa como campo informativo por nombre, no como lista desplegable.
- OWNER/ADMIN empresarial usa `PUT /api/v1/companies/{activeCompanyId}` para actualizar su empresa.
- La UI no debe mostrar UUID como etiqueta principal de empresa; si el backend solo entrega `companyId` en `/me/companies`, la SPA consulta `GET /api/v1/companies/{companyId}` antes de renderizar el encabezado empresarial.

## frontend etiquetas RBAC

- Los codigos de permisos viajan y se guardan en ingles (`ACCOUNTING_VIEW`, `AUDIT_VIEW`, etc.).
- La SPA traduce grupos, permisos y descripciones a espanol antes de renderizar el selector de permisos.
- La traduccion visual no altera el payload `permissionCodes` enviado al backend.

## Contratos fase productizacion operativa

### Flujo E2E desde cero

El script/prueba E2E local debe operar solo mediante API publicas del BFF o microservicios expuestos localmente. En produccion, el primer paso se reemplaza por Cognito Hosted UI + BFF session:

- `POST /api/v1/auth/login`: autentica ROOT y administrador empresarial.
- `POST /api/v1/companies`: crea empresa contratante.
- `POST /api/v1/companies/{companyId}/license`: crea licencia parametrizable.
- `POST /api/v1/platform/companies/{companyId}/owner`: crea administrador inicial OWNER cuando aplique.
- `GET /api/v1/catalogs/{catalogCode}/items`: obtiene catalogos persistidos requeridos.
- `POST /api/v1/third-parties`: crea cliente/proveedor.
- `POST /api/v1/products`: crea producto, servicio o insumo.
- `POST /api/v1/inventory/movements` o flujo de compra equivalente: registra entrada de stock.
- `POST /api/v1/sales`: crea venta POS.
- `POST /api/v1/sales/{saleId}/confirm-pos`: confirma POS y emite documento electronico mock.
- `GET /api/v1/audit/events`: consulta auditoria autorizada.

### Listados operativos

Los modulos nuevos deben exponer consultas consistentes:

```http
GET /api/v1/{resource}?page=0&size=20&query=texto&status=ACTIVE
```

Respuesta minima esperada:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

Reglas:

- `companyId` se obtiene del contexto autenticado o del selector ROOT autorizado; no debe ser texto libre para usuarios empresariales.
- Los filtros de `status` y `query` son opcionales.
- Las respuestas deben incluir identificador tecnico, etiqueta visible, estado y fechas de creacion/actualizacion cuando aplique.

### Uso de licencia

```http
GET /api/v1/platform/licenses/usage?companyId={companyId}
```

```json
{
  "companyId": "uuid",
  "companyName": "Empresa Demo SAS",
  "licenseStatus": "ACTIVE",
  "validFrom": "2026-08-18",
  "validTo": "2027-08-18",
  "enabledModules": ["BILLING", "INVENTORY", "ACCOUNTING"],
  "activeUsers": 3,
  "maxUsers": 10,
  "monthlyDocuments": 25,
  "maxMonthlyDocuments": 1000
}
```

Reglas:
- Uso reservado a `ROOT`.
- `companyId` es obligatorio y debe venir de una empresa seleccionada explicitamente por `ROOT`.
- `monthlyDocuments` se calcula con documentos electronicos emitidos del mes actual en `billing-service`.
- La consulta de documentos electronicos debe aceptar filtros opcionales nulos; con solo `from`, `to` y empresa debe responder `200` con lista vacia o documentos reales.

### Pruebas de contrato BFF

- El BFF debe reenviar `Authorization`, `X-Company-Id`, `X-Correlation-Id` e `Idempotency-Key` cuando aplique.
- El BFF debe preservar codigos funcionales de error (`LICENSE_NOT_CONFIGURED`, `BUSINESS_RULE_VIOLATION`, `RESOURCE_NOT_FOUND`, `UNAUTHORIZED`) para que la SPA muestre mensajes especificos.
- Los endpoints criticos deben tener prueba que valide ruta, metodo, payload y mapeo de respuesta/error.

### Reportes financieros minimos

```http
GET /api/v1/reports/income-statement?from=2026-08-01&to=2026-08-31
GET /api/v1/reports/balance-sheet?from=2026-08-01&to=2026-08-31
X-Company-Id: {companyId}
```

Respuesta:

```json
{
  "companyId": "uuid",
  "fromDate": "2026-08-01",
  "toDate": "2026-08-31",
  "statementType": "INCOME_STATEMENT",
  "groups": [
    {"code": "4", "label": "Ingresos operacionales", "total": 1000000.00},
    {"code": "6", "label": "Costos de venta", "total": 400000.00},
    {"code": "5", "label": "Gastos operacionales", "total": 250000.00},
    {"code": "7", "label": "Costos de produccion o prestacion de servicios", "total": 0.00}
  ],
  "total": 350000.00
}
```

Reglas:

- `income-statement` agrupa PUC por prefijos `4`, `6`, `5` y `7`.
- `balance-sheet` agrupa PUC por prefijos `1`, `2` y `3`.
- El BFF enruta ambos endpoints a `accounting-service` y exige permisos de consulta contable/reportes.

## Contratos fase marca, branding, reportes avanzados e impresion POS

### Branding empresarial

Los endpoints publicos deben exponerse por BFF y enrutar a `tenant-service`.

```http
GET /api/v1/companies/{companyId}/branding
X-Company-Id: {companyId}
```

Respuesta:

```json
{
  "companyId": "uuid",
  "displayName": "Empresa Demo SAS",
  "mainLogoUrl": "https://...",
  "headerLogoUrl": "https://...",
  "loginLogoUrl": "https://...",
  "faviconUrl": "https://...",
  "updatedAt": "2026-08-19T10:00:00Z"
}
```

```http
PUT /api/v1/companies/{companyId}/branding
Content-Type: application/json
```

```json
{
  "displayName": "Tienda Demo",
  "primaryColor": "#0F766E",
  "accentColor": "#1D4ED8"
}
```

```http
POST /api/v1/companies/{companyId}/branding/assets
Content-Type: multipart/form-data
```

Campos:

- `purpose`: `MAIN_LOGO`, `HEADER_LOGO`, `LOGIN_LOGO` o `FAVICON`.
- `file`: archivo PNG, JPEG, WebP o ICO dentro de limites configurados.

Reglas:

- ROOT puede operar sobre cualquier empresa seleccionada.
- OWNER/ADMIN empresarial solo puede operar sobre su empresa activa.
- El backend retorna URL/referencia de lectura, nunca ruta interna sensible.
- Toda mutacion genera auditoria `COMPANY_BRANDING/*`.

### Catalogo de reportes

```http
GET /api/v1/report-definitions
X-Company-Id: {companyId}
```

Respuesta:

```json
[
  {
    "code": "SALES_BY_SELLER",
    "label": "Ventas por vendedor",
    "description": "Agrupa ventas confirmadas por vendedor.",
    "category": "Ventas",
    "filters": [
      {"code": "from", "label": "Desde", "type": "DATE", "required": true},
      {"code": "to", "label": "Hasta", "type": "DATE", "required": true},
      {"code": "sellerId", "label": "Vendedor", "type": "SELECT", "required": false, "optionSource": "SELLERS"}
    ],
    "chartTypes": ["TABLE", "BAR", "LINE"]
  }
]
```

Reportes objetivo iniciales:

- `SALES_BY_SELLER`: ventas por usuarios con rol/permiso de ventas.
- `SALES_BY_PRODUCT`: ventas por producto/servicio.
- `PURCHASES`: compras realizadas.
- `INVENTORY_STOCK`: stock actual.
- `PROFITABILITY`: ingresos, costos y margen basico.
- `ACCOUNTS_RECEIVABLE`: cuentas por cobrar.
- `PAYROLL_DAILY_PAYMENTS`: pagos diarios/nomina interna.
- `LICENSE_USAGE`: uso de licencia por empresa.

```http
GET /api/v1/reports/{reportCode}/options?from=2026-08-01&to=2026-08-31
X-Company-Id: {companyId}
```

Respuesta:

```json
{
  "reportCode": "SALES_BY_SELLER",
  "options": {
    "sellerId": [
      {"value": "uuid", "label": "Ana Rojas"}
    ]
  }
}
```

Regla: para `SALES_BY_SELLER`, `sellerId` solo lista usuarios activos con rol/permiso efectivo de ventas.

```http
POST /api/v1/reports/query
X-Company-Id: {companyId}
Content-Type: application/json
```

```json
{
  "reportCode": "SALES_BY_SELLER",
  "from": "2026-08-01",
  "to": "2026-08-31",
  "chartType": "BAR",
  "filters": {
    "sellerId": "uuid"
  }
}
```

Respuesta:

```json
{
  "companyId": "uuid",
  "reportCode": "SALES_BY_SELLER",
  "chartType": "BAR",
  "appliedFilters": {
    "from": "2026-08-01",
    "to": "2026-08-31",
    "sellerId": "uuid"
  },
  "data": [],
  "generatedAt": "2026-08-24T15:00:00Z"
}
```

### Exportacion de reportes

```http
POST /api/v1/reports/export?format=CSV
X-Company-Id: {companyId}
Content-Type: application/json
```

```json
{
  "reportCode": "SALES_BY_SELLER",
  "from": "2026-08-01",
  "to": "2026-08-31",
  "chartType": "TABLE",
  "filters": {}
}
```

Formatos implementados:

- `CSV`: `text/csv; charset=UTF-8`, extension `.csv`.
- `XLS`: `application/vnd.ms-excel; charset=UTF-8`, extension `.xls` compatible con Excel mediante SpreadsheetML XML.

Respuesta:

```http
200 OK
Content-Disposition: attachment; filename="nexofiscal-sales_by_seller-2026-08-24.csv"
Content-Type: text/csv; charset=UTF-8
```

Regla: la exportacion reutiliza las mismas validaciones y filtros de `/api/v1/reports/query`. El BFF debe reenviar `Content-Disposition` y auditar la descarga como accion `POST`. Exportaciones pesadas, `exportId`, expiracion, descargas diferidas y storage privado quedan como evolucion asincrona posterior sobre S3/EventBridge/SQS.

### Reportes asincronos avanzados

Estado: implementacion inicial disponible en `reporting-service` y expuesta por BFF.

Crear job de exportacion pesado:

```http
POST /api/v1/reports/export-jobs
X-Company-Id: {companyId}
Idempotency-Key: {key}
Content-Type: application/json
```

```json
{
  "reportCode": "SALES_BY_SELLER",
  "format": "XLS",
  "chartType": "TABLE",
  "filters": {
    "from": "2026-08-01",
    "to": "2026-08-31",
    "sellerId": "uuid"
  },
  "notifyByEmail": true
}
```

Respuesta:

```json
{
  "jobId": "uuid",
  "status": "PENDING",
  "reportCode": "SALES_BY_SELLER",
  "format": "XLS",
  "requestedAt": "2026-08-24T15:00:00Z",
  "downloadAvailable": false
}
```

Consultar jobs autorizados:

```http
GET /api/v1/reports/export-jobs?status=READY&from=2026-08-01&to=2026-08-31
X-Company-Id: {companyId}
```

Consultar detalle:

```http
GET /api/v1/reports/export-jobs/{jobId}
X-Company-Id: {companyId}
```

Generar enlace intermediado para correo o UI:

```http
POST /api/v1/reports/export-jobs/{jobId}/download-link
X-Company-Id: {companyId}
```

Respuesta:

```json
{
  "jobId": "uuid",
  "downloadLink": "{APP_PUBLIC_BASE_URL}/reportes/descarga/{token}",
  "expiresAt": "2026-08-27T15:00:00Z",
  "presignedTtlSeconds": 5
}
```

Resolver descarga desde link intermediado:

```http
GET /reportes/descarga/{token}
```

Reglas:

- El dominio no se hardcodea; se construye con `APP_PUBLIC_BASE_URL`.
- El correo nunca incluye URL directa de S3.
- Crear/listar jobs requiere sesion, empresa activa, licencia y permiso `REPORTS_VIEW`.
- El endpoint de descarga por token no requiere sesion activa; el token temporal intermediado es la credencial de descarga y se almacena solo como hash.
- Si el job esta `READY`, la implementacion local entrega streaming controlado; produccion puede generar una URL prefirmada de S3 con TTL `REPORT_DOWNLOAD_PRESIGNED_TTL_SECONDS`, inicialmente `5`.
- `REPORT_LINK_TOKEN_TTL_HOURS` gobierna la vida del token intermediado; no gobierna la URL prefirmada de S3.
- Cada clic exitoso o fallido genera auditoria.

### Historico de ventas y documentos

```http
GET /api/v1/sales/history?from=2026-08-01&to=2026-08-31&sellerId={sellerId}&customerId={customerId}&paymentMethodCode=CASH&documentStatus=VALIDATED
X-Company-Id: {companyId}
```

Respuesta fase 1:

```json
[
  {
    "id": "uuid",
    "companyId": "uuid",
    "customerId": "uuid",
    "paymentMethodCode": "CASH",
    "saleChannel": "POS",
    "status": "CONFIRMED",
    "subtotal": 100000.00,
    "taxTotal": 19000.00,
    "total": 119000.00,
    "createdAt": "2026-08-19T10:30:00Z",
    "electronicDocument": {
      "status": "VALIDATED",
      "prefix": "POS",
      "documentNumber": "100"
    }
  }
]
```

Regla: en la fase inicial la respuesta es una lista de `SaleResponse` filtrada por empresa. Paginacion, nombres enriquecidos de vendedor/cliente, descarga historica de artefactos y ordenamiento avanzado quedan como evolucion posterior cuando exista volumen real.

### Reportes normalizados para UI y exportacion

Consulta normalizada:

```http
POST /api/v1/reports/query
X-Company-Id: {companyId}
Authorization: Bearer {token}
Content-Type: application/json
```

Payload:

```json
{
  "reportCode": "SALES_BY_PRODUCT",
  "from": "2026-08-01",
  "to": "2026-08-28",
  "filters": {
    "productId": "uuid-opcional"
  },
  "chartType": "BAR"
}
```

Respuesta objetivo:

```json
{
  "companyId": "uuid",
  "reportCode": "SALES_BY_PRODUCT",
  "chartType": "BAR",
  "columns": [
    { "key": "productName", "label": "Producto", "type": "TEXT" },
    { "key": "quantitySold", "label": "Cantidad vendida", "type": "NUMBER" },
    { "key": "subtotal", "label": "Subtotal", "type": "MONEY" },
    { "key": "taxTotal", "label": "IVA", "type": "MONEY" },
    { "key": "total", "label": "Total", "type": "MONEY" },
    { "key": "salesCount", "label": "Ventas", "type": "NUMBER" }
  ],
  "rows": [
    {
      "productName": "Cafe 500g",
      "quantitySold": 4,
      "subtotal": 20168.08,
      "taxTotal": 3831.92,
      "total": 24000.00,
      "salesCount": 2
    }
  ],
  "series": [
    {
      "label": "Cafe 500g",
      "value": 24000.00,
      "secondaryValue": 4
    }
  ],
  "generatedAt": "2026-08-28T16:30:00Z"
}
```

Reglas:

- `reporting-service` debe normalizar datasets por `reportCode` antes de responder a la SPA o exportar.
- `SALES_BY_PRODUCT` agrupa por producto/servicio usando lineas de venta confirmadas.
- `SALES_BY_SELLER` agrupa por vendedor usando ventas confirmadas y usuarios con rol/permiso de venta.
- `columns` define solo columnas aprobadas para usuario final, con labels en espanol y tipos de presentacion.
- `rows` contiene datos tabulares ya listos para UI/exportacion.
- `series` contiene los puntos que usaran graficas `BAR`, `LINE`, `PIE` o `KPI` segun aplique.
- La SPA no debe inferir columnas finales a partir de JSON transaccional cuando exista `columns`/`rows`.
- Campos tecnicos como `companyId`, `idempotencyKey`, rutas anidadas de `electronicDocument` o IDs internos solo pueden exponerse si el reporte los declara explicitamente en `columns`.
- Exportaciones CSV/Excel deben usar `columns` y `rows` normalizados, no el payload crudo del microservicio fuente.

### Artefactos e impresion POS

```http
POST /api/v1/sales/{saleId}/receipt?widthMm=80
X-Company-Id: {companyId}
```

Respuesta fase 1:

```http
200 OK
Content-Type: text/html; charset=UTF-8
Content-Disposition: inline; filename="nexofiscal-pos-POS100.html"
```

Reglas:

- El HTML incluye CSS `@page` para 58/80 mm y ejecuta `window.print()` al cargar.
- La solicitud es `POST` para que el BFF audite intento de impresion/reimpresion sin reemitir documento fiscal.
- El comprobante fase 1 es reproducible desde venta confirmada y documento electronico asociado; storage binario dedicado queda para evolucion posterior.
- `ESC_POS`, `WEB_USB`, `WEB_SERIAL` o `LOCAL_AGENT` quedan reservados para tareas futuras con aprobacion de hardware y seguridad.
- Cada solicitud de impresion/reimpresion debe auditarse.

### Ajustes RBAC y configuracion fiscal POS

Crear administrador inicial:

```http
POST /api/v1/companies/{companyId}/memberships
X-Company-Id: {companyId}
Authorization: Bearer {rootToken}
```

Payload:

```json
{
  "userId": "uuid",
  "roles": ["OWNER"]
}
```

Reglas:

- Si el actor es `ROOT` y el rol solicitado contiene `OWNER`, `identity-service` debe crear o reutilizar el rol empresarial `OWNER` de la empresa.
- El rol materializado debe tener permisos `COMPANY` y no debe exponer permisos `GLOBAL_*`.
- La respuesta de roles empresariales debe incluir el `OWNER` materializado para que el administrador lo vea en el modulo Roles.

Confirmar POS con configuracion fiscal faltante:

```http
POST /api/v1/sales/{saleId}/confirm
X-Company-Id: {companyId}
Idempotency-Key: {key}
```

Cerrar venta en un solo paso:

```http
POST /api/v1/sales/close
X-Company-Id: {companyId}
X-User-Id: {userId}
Idempotency-Key: {key}
Content-Type: application/json
```

Payload: igual a `POST /api/v1/sales`.

Respuesta:

```http
200 OK
Content-Type: application/json
```

```json
{
  "id": "uuid",
  "status": "CONFIRMED",
  "paymentMethodCode": "CASH",
  "electronicDocument": {
    "documentType": "ELECTRONIC_INVOICE",
    "status": "VALIDATED",
    "cufeCude": "string"
  }
}
```

Reglas:

- La operacion crea la venta en borrador y la confirma fiscalmente en el mismo caso de uso.
- La misma `Idempotency-Key` identifica el cierre completo y evita duplicar venta/documento en reintentos.
- El vendedor ve la accion como `Cerrar venta`; no debe ejecutar manualmente `crear` y luego `confirmar`.
- Si falta emisor fiscal o resolucion activa, retorna `400 BUSINESS_RULE_VIOLATION` con mensaje funcional.
- Si falla el conector DIAN, retorna `502 EXTERNAL_PROVIDER_ERROR` con mensaje funcional y correlation ID.

Errores funcionales:

```json
{
  "status": 400,
  "code": "BUSINESS_RULE_VIOLATION",
  "message": "Debes configurar un emisor fiscal activo antes de confirmar ventas POS.",
  "correlationId": "uuid",
  "details": []
}
```

```json
{
  "status": 400,
  "code": "BUSINESS_RULE_VIOLATION",
  "message": "Debes configurar una resolucion de numeracion activa para POS electronico antes de confirmar ventas.",
  "correlationId": "uuid",
  "details": []
}
```

Regla: estos errores son de configuracion fiscal, no de permisos. La SPA debe mostrarlos como accion requerida en el modulo Fiscal.

### Administracion ROOT de empresas desde tabla

Reglas de UI/contrato:

- La tabla de empresas consume `GET /api/v1/companies` y cada accion usa el identificador de la fila seleccionada.
- Crear empresa usa `POST /api/v1/companies` con formulario vacio/controlado; no debe reutilizar datos de la empresa activa.
- Actualizar empresa usa `PUT /api/v1/companies/{companyId}` donde `{companyId}` proviene de la fila `Actualizar`.
- Activar/inactivar usa `PUT /api/v1/companies/{companyId}/activate` o `PUT /api/v1/companies/{companyId}/suspend`.
- Crear administrador usa `POST /api/v1/users` y luego `POST /api/v1/companies/{companyId}/memberships`; el `{companyId}` proviene de la fila `Crear administrador`.
- Marca empresarial usa `PUT /api/v1/companies/{companyId}/branding` y `POST /api/v1/companies/{companyId}/branding/assets`; el `{companyId}` proviene de la fila `Crear marca empresarial`.
- El campo `Empresa` en modales es informativo y bloqueado; no forma parte editable del payload.

### Gestion segura de resoluciones fiscales con error

```http
DELETE /api/v1/numbering-resolutions/{resolutionId}
X-Company-Id: {companyId}
Authorization: Bearer {token}
```

Reglas:

- Elimina fisicamente solo resoluciones sin documentos asociados ni consecutivos usados.
- Si la resolucion ya fue usada, retorna `409 BUSINESS_RULE_VIOLATION` indicando que debe inactivarse.
- La operacion requiere permiso de configuracion fiscal y auditoria.

```http
PUT /api/v1/numbering-resolutions/{resolutionId}/deactivate
X-Company-Id: {companyId}
Authorization: Bearer {token}
```

Reglas:

- Inactiva resoluciones usadas o no usadas.
- Una resolucion inactiva no puede ser seleccionada por `POST /api/v1/sales/close` ni `POST /api/v1/sales/{saleId}/confirm`.
- La UI debe ofrecer `Eliminar` solo si `used=false`; de lo contrario solo `Inactivar`.

### Compras documentales

```http
GET /api/v1/purchases?status={status}&supplierId={supplierId}&from={yyyy-MM-dd}&to={yyyy-MM-dd}
X-Company-Id: {companyId}
```

Reglas:

- El listado de compras consume este contrato y muestra facturas de proveedor reales del backend.
- `status`, `supplierId`, `from` y `to` son opcionales.
- La compra documental no modifica inventario; el stock se aumenta o ajusta desde `POST /api/v1/inventory-movements`.

```http
POST /api/v1/purchases
X-Company-Id: {companyId}
Idempotency-Key: {key}
Content-Type: application/json
```

Payload objetivo:

```json
{
  "supplierId": "uuid",
  "purchaseDate": "2026-09-01",
  "concept": "Factura proveedor cafe",
  "paymentCondition": "CASH",
  "dueDate": null,
  "total": 59500,
  "evidence": {
    "type": "URL",
    "url": "https://example.local/factura-proveedor.pdf",
    "fileAssetId": null
  },
  "lines": [
    {
      "description": "Factura proveedor cafe",
      "total": 59500
    }
  ]
}
```

Reglas TASK-252:

- `quantity`, `unitCost`, `subtotal` y `tax` no son campos de captura para compras documentales.
- El total de compra es no discriminado desde la perspectiva del negocio usuario.
- Si el modelo interno conserva campos historicos, debe mapear `subtotal=total`, `taxTotal=0`, `quantity=1` y `unitCost=total` solo como compatibilidad tecnica, sin exponerlo en UI.
- La evidencia es opcional. `type=NONE` no requiere soporte, `type=URL` requiere URL `http/https`, `type=PDF` requiere `fileAssetId` de un PDF previamente validado.

### Gastos operativos

```http
POST /api/v1/expenses
X-Company-Id: {companyId}
Idempotency-Key: {key}
Content-Type: application/json
```

Payload objetivo:

```json
{
  "supplierId": "uuid",
  "expenseType": "OPERATING_EXPENSE",
  "expenseDate": "2026-08-31",
  "concept": "Energia local comercial",
  "paymentCondition": "CASH",
  "dueDate": null,
  "total": 119000,
  "evidence": {
    "type": "PDF",
    "fileAssetId": "uuid",
    "url": null
  }
}
```

Reglas:

- No crea movimientos de inventario.
- `expenseType=OPERATING_EXPENSE` genera asiento mediante `OPERATING_EXPENSE_CONFIRMED`.
- `expenseType=ASSET_PURCHASE` genera asiento mediante `ASSET_PURCHASE_CONFIRMED`.
- Si `paymentCondition=CREDIT`, crea cuenta por pagar.
- `subtotal` y `taxTotal` no son campos de captura; si existen en persistencia por compatibilidad, se calculan como `subtotal=total` y `taxTotal=0`.
- La evidencia opcional comparte el contrato `NONE|PDF|URL` definido para compras.

### Archivos empresariales y evidencias

```http
POST /api/v1/companies/{companyId}/files
X-Company-Id: {companyId}
Content-Type: multipart/form-data
```

Campos multipart:

- `category`: `INVOICE`, `LOGO`, `BACKGROUND`, `PURCHASE_EVIDENCE`, `EXPENSE_EVIDENCE` u `OTHER`.
- `file`: archivo unico; para `INVOICE`, `PURCHASE_EVIDENCE` y `EXPENSE_EVIDENCE` solo `application/pdf`.

Respuesta:

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "category": "PURCHASE_EVIDENCE",
  "originalFilename": "factura-proveedor.pdf",
  "contentType": "application/pdf",
  "fileSize": 120000,
  "contentHash": "hash-sha256",
  "url": "/api/v1/companies/{companyId}/files/{assetId}?hash={contentHash}",
  "uploadedBy": "uuid",
  "uploadedAt": "2026-09-01T10:00:00Z"
}
```

Reglas:

- El navegador no recibe bucket, key interna, credenciales ni URL publica permanente.
- En desarrollo el adaptador puede escribir en volumen/contenedor local; en produccion usa S3 privado/KMS.
- Los prefijos actuales se construyen por empresa y categoria funcional: `{companyId}/{folderName}/{assetId}-{safeFileName}`.
- Las descargas futuras deben pasar por BFF/RBAC y auditoria.

### Deudores y cuentas por cobrar

```http
POST /api/v1/accounts-receivable
X-Company-Id: {companyId}
Idempotency-Key: {key}
Content-Type: application/json
```

Payload objetivo:

```json
{
  "debtorThirdPartyId": "uuid",
  "sourceType": "MANUAL",
  "sourceId": null,
  "issueDate": "2026-08-31",
  "dueDate": "2026-09-30",
  "concept": "Prestamo temporal al negocio",
  "amount": 500000
}
```

```http
POST /api/v1/accounts-receivable/{accountReceivableId}/payments
X-Company-Id: {companyId}
Idempotency-Key: {key}
Content-Type: application/json
```

Payload objetivo:

```json
{
  "paymentDate": "2026-09-05",
  "paymentMethodCode": "TRANSFER",
  "amount": 100000
}
```

Reglas:

- La cuenta por cobrar conserva saldo, estado `PENDING`, `PARTIALLY_PAID`, `PAID` u `OVERDUE`.
- Todo abono genera asiento contable y auditoria.
- Un deudor manual puede usar `sourceType=MANUAL` y `sourceId` generado por backend cuando no exista documento origen.
- Falta de tercero, monto invalido, regla contable faltante o configuracion incompleta debe responder `400/409` funcional, no `500`.

### QR parametrizable en comprobante POS

Reglas TASK-256:

- La representacion imprimible incluye QR grafico embebido o referenciado de forma segura.
- En modo `MOCK`, el contenido QR se construye desde `APP_PUBLIC_BASE_URL` o parametro equivalente hacia una ruta controlada de consulta de comprobante.
- En modo DIAN real, el contenido QR viene de la respuesta DIAN/proveedor y prevalece sobre el valor mock.
- Si el servicio no puede construir un QR valido para el modo activo, responde error funcional de configuracion antes de entregar comprobante incompleto.

### Marca empresarial con color picker

Reglas TASK-257:

- `primaryColor` y `accentColor` siguen siendo valores hexadecimales validados por backend.
- La SPA debe capturarlos con control `input type=color` y mostrar descripcion breve de impacto visual.
- Color principal: botones primarios, resaltado de menu activo y acciones principales.
- Color acento: badges, detalles secundarios y estados de apoyo.

### Reporte diario de ganancias y gastos

```http
POST /api/v1/reports/query
X-Company-Id: {companyId}
Content-Type: application/json
```

Payload:

```json
{
  "reportCode": "DAILY_PROFIT_AND_LOSS",
  "chartType": "TABLE",
  "from": "2026-08-31",
  "to": "2026-08-31",
  "filters": {}
}
```

Respuesta normalizada:

```json
{
  "reportCode": "DAILY_PROFIT_AND_LOSS",
  "columns": [
    { "key": "metric", "label": "Concepto", "type": "text" },
    { "key": "amount", "label": "Valor", "type": "money" }
  ],
  "rows": [
    { "metric": "Ingresos por ventas", "amount": 800000 },
    { "metric": "Costos de venta", "amount": 350000 },
    { "metric": "Gastos operativos", "amount": 120000 },
    { "metric": "Pagos diarios", "amount": 90000 },
    { "metric": "Utilidad / perdida neta", "amount": 240000 }
  ],
  "series": [
    { "label": "Ingresos por ventas", "value": 800000 },
    { "label": "Costos de venta", "value": 350000 },
    { "label": "Gastos operativos", "value": 120000 },
    { "label": "Pagos diarios", "value": 90000 },
    { "label": "Utilidad / perdida neta", "value": 240000 }
  ]
}
```

## Contratos objetivo fase 35 - mejoras priorizadas

Estado: documentado; pendiente de implementacion.

### Readiness empresarial

```http
GET /api/v1/company-readiness
X-Company-Id: {companyId}
Accept: application/json
```

Respuesta objetivo:

```json
{
  "companyId": "uuid",
  "overallStatus": "BLOCKED",
  "checks": [
    {
      "code": "ACTIVE_LICENSE",
      "label": "Licencia activa",
      "status": "READY",
      "module": "Configuracion",
      "blocking": true,
      "actionCode": null,
      "message": "La licencia esta vigente."
    },
    {
      "code": "ACTIVE_NUMBERING_RESOLUTION",
      "label": "Resolucion fiscal activa",
      "status": "BLOCKED",
      "module": "Fiscal",
      "blocking": true,
      "actionCode": "OPEN_FISCAL_RESOLUTION",
      "message": "Configura una resolucion activa para el tipo de documento predeterminado."
    }
  ]
}
```

Reglas:

- El BFF compone readiness desde servicios internos; no debe confiar en la SPA para decidir bloqueo real.
- Los estados permitidos son `READY`, `WARNING` y `BLOCKED`.
- Cada bloqueo debe indicar accion sugerida y modulo responsable.

### Readiness contable

```http
GET /api/v1/accounting-readiness
X-Company-Id: {companyId}
Accept: application/json
```

Respuesta objetivo:

```json
{
  "companyId": "uuid",
  "missingRules": [
    {
      "eventType": "ACCOUNT_RECEIVABLE_REGISTERED",
      "label": "Registro de cuenta por cobrar",
      "requiredBy": ["Deudores"],
      "severity": "BLOCKING"
    }
  ],
  "missingAccounts": [
    {
      "suggestedCode": "1305",
      "label": "Clientes nacionales",
      "requiredBy": ["Deudores"],
      "severity": "BLOCKING"
    }
  ]
}
```

Reglas:

- El diagnostico no debe crear cuentas ni reglas por si solo.
- Las acciones de reparacion deben ser explicitas y auditadas.

### Auditoria operativa

```http
GET /api/v1/audit/operations?companyId={companyId}&module={module}&result={result}&from={date}&to={date}&correlationId={id}
Accept: application/json
```

Reglas:

- ROOT puede filtrar por cualquier empresa.
- Administradores empresariales solo consultan eventos de su empresa.
- Payloads sensibles, certificados, PIN, passwords, tokens, URLs privadas y contenido binario nunca se exponen.

### Salud de negocio y observabilidad

```http
GET /api/v1/observability/business-health
X-Company-Id: {companyId}
Accept: application/json
```

Respuesta objetivo:

```json
{
  "companyId": "uuid",
  "salesStatus": "READY",
  "dianStatus": "WARNING",
  "storageStatus": "READY",
  "reportingStatus": "READY",
  "lastCriticalErrors": []
}
```

Reglas:

- Este contrato complementa Actuator tecnico; no reemplaza `/actuator/health`.
- Debe servir para soporte y administracion funcional, no para revelar detalles internos.

### Impresion termica POS

```http
POST /api/v1/pos-print-jobs
X-Company-Id: {companyId}
Idempotency-Key: {key}
Content-Type: application/json
```

Payload objetivo:

```json
{
  "saleId": "uuid",
  "documentId": "uuid",
  "printerMode": "BROWSER",
  "paperWidthMm": 80,
  "copyReason": "ORIGINAL"
}
```

Reglas:

- El endpoint registra la intencion/auditoria de impresion; el acceso fisico a impresora queda del lado navegador, WebUSB/WebSerial o agente local aprobado.
- Reimpresiones deben indicar motivo y usuario.

### Reportes pesados

- Los reportes pesados de fase 35 deben reutilizar los contratos de jobs definidos para Fase 24.
- La descarga mantiene link intermediado y URL prefirmada de corta vida generada al momento del clic.
- La UI debe consumir datasets normalizados, no JSON crudo de microservicios.
