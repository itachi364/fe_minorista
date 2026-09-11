# AWS Free Preview

Entorno temporal y no productivo para la cuenta AWS `883425315805`. Consume creditos del plan `FREE`; no garantiza costo cero permanente y no debe reutilizar `envs/dev`.

## Barreras

- Provider bloqueado a la cuenta exacta y `us-east-1`.
- Preflight `FREE/ACTIVE`, minimo USD 25 y al menos 30 dias restantes.
- Una sola `t3.large`, creditos CPU `standard`, EBS `gp3` de maximo 30 GiB.
- Sin NAT, Fargate, RDS, balanceadores, Route 53, WAF, Secrets Manager, KMS propio ni observabilidad administrada.
- Puerto 80 aceptado solamente desde la prefix list administrada de CloudFront; no existe SSH.
- Apagado local cuatro horas despues del arranque, respaldo EventBridge Scheduler cada cuatro horas y AWS Budgets detiene EC2 automaticamente al 80% del limite mensual.
- Artefactos privados S3 con expiracion a siete dias y configuracion sensible en Parameter Store Standard.

## Secuencia controlada

1. Construir el bundle y ejecutar la prueba local de memoria.

```powershell
.\scripts\build-bundle.ps1
.\scripts\test-memory.ps1
```

2. Generar un plan. Este comando no crea recursos.

```powershell
.\scripts\plan.ps1 -BudgetAlertEmail alerts@example.com
```

3. Revisar `free-preview.tfplan` y `.artifacts/free-preview-plan.json`. El `apply` requiere confirmacion separada.

```powershell
.\scripts\apply-approved-plan.ps1 -ConfirmAccount 883425315805 -ConfirmApply
```

4. Copiar `runtime.env.example` a `runtime.env`, reemplazar todos los placeholders y cargarlo sin imprimir secretos. La actualizacion SSM requiere confirmacion separada.

```powershell
.\scripts\configure-runtime.ps1 -RuntimeEnvPath .\runtime.env
```

5. Publicar el bundle privado y arrancar la aplicacion mediante Systems Manager. Tanto la carga S3 como el comando remoto requieren confirmacion separada.

```powershell
.\scripts\publish-bundle.ps1
.\scripts\refresh-preview.ps1 -InstanceId <instance-id>
```

## Dominio Hostinger

El primer `apply` deja `enable_custom_domain=false`, crea el certificado ACM y entrega `acm_dns_validation_records`. En Hostinger se crea ese CNAME sin repetir `nexofiscal.online`. Cuando ACM reporte `ISSUED`, se genera un nuevo plan con `-EnableCustomDomain`, se aplica con confirmacion y se crea en Hostinger:

```text
Tipo: CNAME
Nombre: app
Destino: <cloudfront_domain_name>
```

El CNAME de validacion ACM debe conservarse para renovaciones. CloudFront funciona con su dominio AWS mientras termina la propagacion.

## Cierre

```powershell
.\scripts\stop-preview.ps1 -InstanceId <instance-id>
.\scripts\inventory.ps1
```

Detener EC2 no elimina EBS ni todos los consumos asociados. Destruir el entorno, borrar datos o respaldar requiere una confirmacion explicita independiente.
