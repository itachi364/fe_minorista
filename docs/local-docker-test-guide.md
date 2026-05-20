# Guia local Docker

Esta guia dejo de ser el flujo activo de pruebas porque estaba orientada al monolito transitorio en `localhost:8083`.

El flujo local vigente usa microservicios fisicos independientes, PostgreSQL, proveedor DIAN mock y la prueba automatizada desde cero.

## Servicios activos

```powershell
docker compose up -d
docker compose ps
```

Servicios esperados:

- `postgres`
- `tenant-service`
- `catalog-service`
- `thirdparty-service`
- `inventory-service`
- `billing-service`
- `dian-provider-service`
- `accounting-service`

`legacy-monolith` no se levanta por Docker Compose y no hace parte del reactor Maven por defecto.

## Prueba E2E vigente

Ejecutar:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\e2e-from-zero.ps1
```

La guia detallada del flujo vigente esta en:

```text
docs/e2e-from-zero-test-guide.md
```

## Monolito transitorio

El monolito se conserva solo como referencia temporal mientras se cierran brechas de gastos, auditoria/identity, POS directo/historicos y migraciones de datos legacy.

Para compilarlo o ejecutarlo bajo demanda debe activarse el perfil Maven explicito:

```powershell
.\mvnw.cmd -Plegacy-monolith -pl services/legacy-monolith test
.\mvnw.cmd -Plegacy-monolith -pl services/legacy-monolith spring-boot:run
```

No usar esta guia para validar nuevas funcionalidades. Las nuevas validaciones deben ejecutarse contra los microservicios activos y registrarse en `specs/tasks.md`.
