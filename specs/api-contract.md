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
| `dian-provider-service` | Mock DIAN y futura conexion DIAN parametrizable por empresa | `services/dian-provider-service` |
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

Nota productiva TASK-153/TASK-160:

- El navegador no debe construir ni enviar `Authorization` en JavaScript. La autenticacion publica usa cookie opaca `HttpOnly` emitida por el BFF.
- El BFF traduce la sesion publica a headers internos seguros hacia microservicios: `Authorization` de servicio cuando aplique, `X-User-Id`, `X-Company-Id`, `X-Correlation-Id` e `Idempotency-Key`.
- Las mutaciones autenticadas por cookie requieren token CSRF en header aprobado, por ejemplo `X-CSRF-Token`.

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

Respuesta:

```json
{
  "loginUrl": "https://<cognito-domain>/oauth2/authorize?...",
  "expiresAt": "2026-08-19T10:05:00Z"
}
```

Reglas:

- El BFF genera `state`, `nonce`, PKCE `code_verifier` y `code_challenge`.
- El `code_verifier` se conserva server-side de forma temporal y no se retorna a la SPA.
- La URL usa Authorization Code Grant con PKCE y scopes minimos `openid`, `email`, `profile` y los aprobados por arquitectura.

`GET /api/v1/auth/callback`:

Reglas:

- Valida `state` y expiracion del intento de login.
- Intercambia `code` por tokens en Cognito desde el BFF.
- Crea sesion server-side con tokens cifrados.
- Emite cookie opaca `HttpOnly; Secure; SameSite=Lax|Strict`.
- Redirige a la SPA sin incluir tokens en query string, fragment, storage ni response body.

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

`POST /api/v1/auth/logout`:

Reglas:

- Requiere cookie de sesion y CSRF valido.
- Revoca sesion server-side.
- Limpia cookie con expiracion inmediata.
- Revoca tokens Cognito cuando aplique.
- Registra auditoria segura.

`POST /api/v1/auth/login`:

- Se mantiene solo como contrato local/transitorio para desarrollo y E2E.
- En produccion debe estar deshabilitado, no expuesto por API Gateway o responder `404/403` seguro.
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
- `GET /api/v1/platform/permissions`
- `GET /api/v1/platform/audit-events?from=&to=&userId=`

Roles y usuarios por empresa:

- `GET /api/v1/companies/{companyId}/roles`
- `POST /api/v1/companies/{companyId}/roles`
- `GET /api/v1/companies/{companyId}/roles/{roleId}`
- `PUT /api/v1/companies/{companyId}/roles/{roleId}`
- `PUT /api/v1/companies/{companyId}/roles/{roleId}/activate`
- `PUT /api/v1/companies/{companyId}/roles/{roleId}/deactivate`
- `GET /api/v1/companies/{companyId}/permissions/catalog`
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

- `cognitoSubject`: claim `sub` de Cognito.
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
  - `Contabilidad`: agrupa `Terceros`, `Inventario`, `Fiscal` y `Nomina`.
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

Responsabilidad: productos, stock simple, compras, movimientos y kardex.

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
- Compras y movimientos solo afectan items con `stockTracked=true`.

### Productos

- `POST /api/v1/products`
- `GET /api/v1/products/{productId}`
- `GET /api/v1/products/by-barcode/{barcode}`
- `GET /api/v1/products/{productId}/availability?quantity=`
- `GET /api/v1/products/{productId}/kardex`
- `GET /api/v1/products/{serviceProductId}/supply-references`
- `GET /api/v1/products/{serviceProductId}/supply-consumption-suggestions`

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
  "salePrice": 15000,
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
  "salePrice": 15000,
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
      "productId": "uuid",
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
- Una compra confirmada aumenta stock.
- Una compra a credito requiere `dueDate`.
- Si `ACCOUNTING_SERVICE_URL` esta configurado, `inventory-service` intenta contabilizar la compra confirmada y crear CxP en `accounting-service` de forma best-effort; el fallo de contabilidad no revierte stock ni confirmacion.
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
- `GET /api/v1/issuers/current`
- `PUT /api/v1/issuers/{issuerId}` contrato objetivo para actualizacion auditada del emisor fiscal.

### Resoluciones

- `POST /api/v1/numbering-resolutions`
- `GET /api/v1/numbering-resolutions?documentType=&active=`
- `GET /api/v1/numbering-resolutions/{resolutionId}` contrato objetivo para consulta por identificador.
- `PUT /api/v1/numbering-resolutions/{resolutionId}/activate` contrato objetivo para activacion auditada.
- `PUT /api/v1/numbering-resolutions/{resolutionId}/deactivate` contrato objetivo para inactivacion auditada.

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
- La SPA no envia `saleChannel`; el flujo `Venta POS` usa canal interno `POS` y documento `ELECTRONIC_POS`.
- La SPA no envia `unitPrice`, `taxCode` ni `taxRate` como fuente fiscal; billing toma precio e impuesto desde `inventory-service`.

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

Responsabilidad: encapsular el mock local y la futura conexion DIAN parametrizable por empresa. Este servicio no representa una oferta de proveedor tecnologico DIAN de la plataforma.

Estado TASK-036:

- Microservicio fisico implementado en `services/dian-provider-service`.
- Modo local soportado: `DIAN_PROVIDER_MODE=mock`.
- Persistencia de envios mock en `dian_provider.provider_submission`.
- `billing-service` consume `POST /api/v1/provider/electronic-pos` por HTTP usando `DIAN_PROVIDER_SERVICE_URL`.
- `GET /api/v1/provider/submissions/{trackingId}` permite consultar el resultado mock persistido y requiere `X-Company-Id`.
- Estado objetivo TASK-145/TASK-152: cada request real debe resolver configuracion DIAN activa por `companyId`, validar secretos/referencias y rechazar emision si la configuracion esta incompleta, vencida, inactiva o no probada.

### Endpoints internos

- `POST /api/v1/provider/electronic-invoices`
- `POST /api/v1/provider/electronic-pos`
- `POST /api/v1/provider/credit-notes`
- `POST /api/v1/provider/debit-notes`
- `GET /api/v1/provider/submissions/{trackingId}`

### Configuracion DIAN por empresa

Endpoints publicos via BFF, visibles para ROOT y administradores empresariales con permiso fiscal/configuracion:

- `GET /api/v1/companies/{companyId}/dian-configuration`
- `POST /api/v1/companies/{companyId}/dian-configuration`
- `PUT /api/v1/companies/{companyId}/dian-configuration/{configurationId}`
- `PUT /api/v1/companies/{companyId}/dian-configuration/{configurationId}/activate`
- `PUT /api/v1/companies/{companyId}/dian-configuration/{configurationId}/deactivate`
- `POST /api/v1/companies/{companyId}/dian-configuration/{configurationId}/test`

`DianConfigurationRequest`:

```json
{
  "operationMode": "SOFTWARE_PROPIO_CLIENTE",
  "environment": "HABILITACION",
  "softwareId": "uuid-o-identificador-dian",
  "softwarePinSecretReference": "aws-secretsmanager://tenant/company/software-pin",
  "technicalKeySecretReference": "aws-secretsmanager://tenant/company/technical-key",
  "certificateSecretReference": "aws-secretsmanager://tenant/company/certificate-p12",
  "certificateAlias": "certificado empresa",
  "certificateExpiresAt": "2027-08-19",
  "authorizationUrl": "https://catalogo-vpfe-hab.dian.gov.co/...",
  "productionUrl": "https://catalogo-vpfe.dian.gov.co/...",
  "responsibilityAccepted": true
}
```

`DianConfigurationResponse`:

```json
{
  "id": "uuid",
  "companyId": "uuid",
  "operationMode": "SOFTWARE_PROPIO_CLIENTE",
  "environment": "HABILITACION",
  "softwareIdConfigured": true,
  "softwarePinConfigured": true,
  "technicalKeyConfigured": true,
  "certificateConfigured": true,
  "certificateAlias": "certificado empresa",
  "certificateFingerprint": "sha256:...",
  "certificateExpiresAt": "2027-08-19",
  "status": "PENDING_TEST",
  "lastTestStatus": "NOT_EXECUTED",
  "active": false,
  "disclaimer": "La empresa facturadora es responsable de su habilitacion y certificacion ante DIAN. La plataforma no presta servicio de proveedor tecnologico DIAN."
}
```

`DianConfigurationTestResponse`:

```json
{
  "configurationId": "uuid",
  "status": "SUCCESS",
  "testedAt": "2026-08-19T10:00:00Z",
  "message": "Configuracion validada para ambiente de habilitacion.",
  "correlationId": "uuid"
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
- La integracion real queda pendiente hasta implementar el conector DIAN real configurable por empresa.
- En desarrollo local se usa el microservicio mock sin llamadas externas y solo sirve para probar el flujo interno.
- Variables locales del mock:
  - `DIAN_PROVIDER_MODE=mock`.
  - `DIAN_MOCK_DEFAULT_STATUS=ACCEPTED|REJECTED|FAILED`.
  - `DIAN_MOCK_ERROR_CODE`.
  - `DIAN_MOCK_ERROR_MESSAGE`.
- En esta version, `DIAN_PROVIDER_MODE` solo acepta `mock`; un valor distinto debe fallar explicitamente hasta implementar el adaptador real.
- El request de configuracion DIAN no puede incluir secretos en claro; solo referencias seguras o datos no sensibles. Si una UI necesita cargar un certificado, debe enviarlo a un endpoint seguro que lo almacene en Secrets Manager/almacen equivalente y retorne referencia no sensible.
- Las respuestas API nunca retornan certificado, PIN, claves, tokens ni credenciales; solo banderas `*Configured`, alias, huella, vencimiento y estado.
- Activar modo real exige `responsibilityAccepted=true` para confirmar que la empresa entiende que opera su propia habilitacion/certificacion DIAN.

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

- Crea las cuentas base `1105`, `1110`, `1305`, `1435`, `2205`, `2408`, `4135` y `5135` si no existen para la empresa.
- Si una cuenta base existe inactiva, la reactiva sin duplicar codigo.
- Reemplaza las reglas activas de venta, compra, gasto, pago de cuenta por pagar y recaudo de cuenta por cobrar, dejando la regla previa inactiva para conservar historial.

### Cuentas

- `POST /api/v1/accounts`
- `GET /api/v1/accounts?code=`
- `GET /api/v1/accounts?active=`

### Reglas contables

- `POST /api/v1/accounting-rules`
- `PUT /api/v1/accounting-rules/active`
- `POST /api/v1/accounting-rules/{eventType}/deactivate`
- `GET /api/v1/accounting-rules?eventType=&active=`

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
- `PUT /api/v1/accounting-rules/active` desactiva la regla activa previa para el evento y crea una nueva regla activa; `POST /api/v1/accounting-rules` conserva la validacion de no duplicar regla activa.
- Un documento fiscal validado debe poder rastrearse hasta su asiento contable.

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

Permisos iniciales: `USERS_MANAGE`, `ROLES_MANAGE`, `SALES_CREATE`, `FISCAL_DOCUMENTS_ISSUE`, `INVENTORY_MANAGE`, `ACCOUNTING_MANAGE`, `REPORTS_VIEW`, `AUDIT_VIEW`, `LICENSE_MANAGE`.

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
- Estado objetivo TASK-153/TASK-163: en produccion, el BFF no recibe `Authorization` desde la SPA; resuelve cookie segura server-side y genera headers internos.

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
El `reporting-service` fisico no existe actualmente como artefacto. Los reportes minimos se consultan desde `billing-service`, `inventory-service`, `accounting-service` y agregaciones BFF; las proyecciones event-driven viven en `reporting-projection-lambda`.

#### billing-service

- `GET /api/v1/reports/sales?status=&from=&to=`
- `GET /api/v1/reports/electronic-documents?documentType=&status=&customerId=&from=&to=&prefix=&number=&cufeCude=`

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

- `reporting-projection-lambda` para proyecciones asincronas; `reporting-service` solo si una tarea futura lo materializa como servicio fisico.
- `audit-service`

## Contract tests futuros

Cuando los servicios existan fisicamente, cada contrato debe tener:

- Pruebas de consumidor para requests.
- Pruebas de proveedor para responses.
- Validacion de headers obligatorios.
- Validacion de errores estandar.
- Validacion de aislamiento por `X-Company-Id`.

## Pendientes

- Implementar conector DIAN real parametrizable por empresa con XML UBL, firma, CUFE/CUDE, QR, validaciones tecnicas y respuestas DIAN.
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
GET /api/v1/reports/catalog
X-Company-Id: {companyId}
```

Respuesta:

```json
[
  {
    "code": "SALES_BY_SELLER",
    "label": "Ventas por vendedor",
    "description": "Agrupa ventas confirmadas por vendedor.",
    "requiredPermissions": ["REPORTS_VIEW"],
    "requiredModules": ["REPORTING", "BILLING"],
    "dateRangeRequired": true,
    "allowedChartTypes": ["TABLE", "BAR", "LINE"],
    "exportFormats": ["CSV", "XLSX"]
  }
]
```

Reportes objetivo iniciales:

- `SALES_SUMMARY`: ventas por periodo.
- `SALES_BY_SELLER`: ventas por usuarios con rol/permiso de ventas.
- `SALES_BY_PRODUCT`: ventas por producto/servicio.
- `SALES_BY_PAYMENT_METHOD`: ventas por medio de pago.
- `PURCHASES_SUMMARY`: compras realizadas.
- `INVENTORY_KARDEX`: movimientos y saldos de inventario.
- `BASIC_PROFITABILITY`: ingresos, costos y margen basico.
- `ACCOUNTS_RECEIVABLE`: cuentas por cobrar.
- `ACCOUNTS_PAYABLE`: cuentas por pagar.
- `ACCOUNTING_STATEMENTS`: estado de resultados y balance basico.
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
  "filters": [
    {
      "code": "sellerId",
      "label": "Vendedor",
      "type": "SELECT",
      "required": false,
      "options": [
        {"value": "uuid", "label": "Ana Rojas"}
      ]
    }
  ],
  "allowedChartTypes": ["TABLE", "BAR", "LINE"],
  "exportFormats": ["CSV", "XLSX"]
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
  "reportCode": "SALES_BY_SELLER",
  "from": "2026-08-01",
  "to": "2026-08-31",
  "chartType": "BAR",
  "columns": [
    {"key": "sellerName", "label": "Vendedor", "type": "TEXT"},
    {"key": "totalSales", "label": "Total ventas", "type": "MONEY"}
  ],
  "rows": [
    {"sellerName": "Ana Rojas", "totalSales": 1500000.00}
  ],
  "series": [
    {"label": "Total ventas", "points": [{"x": "Ana Rojas", "y": 1500000.00}]}
  ]
}
```

### Exportacion de reportes

```http
POST /api/v1/reports/export
X-Company-Id: {companyId}
Content-Type: application/json
```

```json
{
  "reportCode": "SALES_BY_SELLER",
  "from": "2026-08-01",
  "to": "2026-08-31",
  "format": "XLSX",
  "filters": {}
}
```

Respuesta sincrona para archivos pequenos:

```json
{
  "exportId": "uuid",
  "status": "READY",
  "downloadUrl": "/api/v1/reports/exports/uuid/download",
  "expiresAt": "2026-08-19T12:00:00Z"
}
```

Respuesta asincrona para archivos pesados:

```json
{
  "exportId": "uuid",
  "status": "PROCESSING"
}
```

```http
GET /api/v1/reports/exports/{exportId}/download
X-Company-Id: {companyId}
```

### Historico de ventas y documentos

```http
GET /api/v1/sales/history?from=2026-08-01&to=2026-08-31&sellerId={sellerId}&customerId={customerId}&paymentMethodCode=CASH&documentStatus=ACCEPTED&page=0&size=20
X-Company-Id: {companyId}
```

Respuesta:

```json
{
  "content": [
    {
      "saleId": "uuid",
      "documentId": "uuid",
      "saleDate": "2026-08-19T10:30:00Z",
      "sellerName": "Ana Rojas",
      "buyerLabel": "Consumidor final",
      "paymentMethodLabel": "Efectivo",
      "grossTotal": 100000.00,
      "taxTotal": 19000.00,
      "netTotal": 119000.00,
      "documentStatus": "MOCK_ACCEPTED",
      "canDownload": true,
      "canReprint": true
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

```http
GET /api/v1/sales/{saleId}/detail
X-Company-Id: {companyId}
```

### Artefactos e impresion POS

```http
GET /api/v1/electronic-pos/{documentId}/artifacts
X-Company-Id: {companyId}
```

Respuesta:

```json
{
  "documentId": "uuid",
  "printableHtmlUrl": "/api/v1/electronic-pos/uuid/artifacts/printable",
  "qrUrl": "/api/v1/electronic-pos/uuid/artifacts/qr",
  "xmlUrl": "/api/v1/electronic-pos/uuid/artifacts/xml",
  "jsonMetadataUrl": "/api/v1/electronic-pos/uuid/artifacts/metadata",
  "hash": "sha256:..."
}
```

```http
POST /api/v1/electronic-pos/{documentId}/print-jobs
X-Company-Id: {companyId}
Content-Type: application/json
```

```json
{
  "paperWidthMm": 80,
  "strategy": "WEB_PRINT"
}
```

Reglas:

- `WEB_PRINT` abre una vista imprimible controlada por la SPA/BFF.
- `ESC_POS`, `WEB_USB`, `WEB_SERIAL` o `LOCAL_AGENT` quedan reservados para tareas futuras con aprobacion de hardware y seguridad.
- Cada solicitud de impresion/reimpresion debe auditarse.
