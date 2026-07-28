# AWS Terraform IaC

Infraestructura objetivo para la plataforma de facturacion electronica en AWS.

## Alcance inicial

- Frontend SPA en S3 privado + CloudFront.
- Entrada publica HTTP API Gateway hacia BFF privado.
- BFF y microservicios Spring Boot en ECS Fargate.
- RDS PostgreSQL administrado.
- EventBridge/SQS + DLQ para mensajeria administrada.
- Secrets Manager para secretos de servicios.
- ECR por artefacto desplegable.

## Estructura

```text
infra/aws
  envs/dev              # composicion del ambiente dev
  modules/api           # API Gateway HTTP + VPC Link
  modules/database      # RDS PostgreSQL
  modules/ecs           # ECS Fargate, ECR, ALB interno y servicios
  modules/frontend      # S3 + CloudFront
  modules/messaging     # EventBridge, SQS y DLQ
  modules/network       # VPC, subnets y security groups base
  modules/secrets       # Secrets Manager
```

## Uso esperado

Terraform no debe guardar secretos reales en archivos versionados. Copia `terraform.tfvars.example` como `terraform.tfvars` solo en tu entorno local o usa variables del pipeline.

```powershell
cd infra/aws/envs/dev
terraform init
terraform fmt -recursive -check ..\..
terraform validate
terraform plan -out dev.tfplan
```

No ejecutar `terraform apply` sin aprobacion explicita y sin revisar el plan.

## Imagenes

Los servicios ECS quedan con `desired_count = 0` por defecto hasta que existan imagenes publicadas en ECR o se definan `container_image` externos. Esto permite crear la base cloud sin intentar arrancar tareas sin imagen lista.