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

`legacy-monolith` fue removido del repositorio activo en TASK-059. Docker Compose no lo levanta y el reactor Maven ya no tiene perfil para compilarlo.

## Prueba E2E vigente

Ejecutar:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\e2e-from-zero.ps1
```

La guia detallada del flujo vigente esta en:

```text
docs/e2e-from-zero-test-guide.md
```

## Codigo legacy removido

El monolito transitorio fue eliminado del repositorio en TASK-059. Las validaciones nuevas deben ejecutarse contra los microservicios activos y registrarse en specs/tasks.md. Las tablas public.* legacy existentes no se eliminan desde Docker Compose y deben auditarse con scripts/legacy-data-audit.sql antes de cualquier migracion destructiva.
