# ADR-003: AWS Free Preview con compuertas de costo

## Estado

Propuesto el 2026-09-11. No autoriza creacion de recursos.

## Contexto

La cuenta objetivo `883425315805` fue consultada mediante APIs AWS de solo lectura el 2026-09-11. STS confirmo la cuenta y Free Tier informo plan `FREE`, estado `ACTIVE`, USD 140,01 restantes y expiracion `2026-12-13T15:52:07.517Z`.

El target existente en `infra/aws/envs/dev` usa ECS Fargate, RDS, ALB/VPC Link, Secrets Manager y una clave KMS propia. Aunque los servicios ECS tienen `desired_count = 0`, otros recursos tienen consumo fijo o medido. Ese entorno no cumple el objetivo de preservar creditos y no debe reutilizarse para una demostracion gratuita.

La ejecucion Docker local observada supera 8 GiB en modo desarrollo para la plataforma completa. Una instancia micro no puede alojarla. Antes de elegir una EC2 de 8 GiB, deben construirse artefactos de produccion, limitarse las JVM y probarse el conjunto bajo restriccion de memoria; el fallo bloquea el despliegue en vez de escalar capacidad.

## Decision

Se creara un ambiente Terraform independiente `free-preview`, exclusivamente temporal y no productivo:

- cuenta bloqueada con `allowed_account_ids = ["883425315805"]` y preflight STS/Free Tier;
- una instancia EC2 de 8 GiB como maximo inicial, sin auto scaling ni inicio programado;
- un volumen EBS `gp3` de hasta 30 GB para sistema, contenedores y PostgreSQL;
- frontend, proxy, BFF, servicios y PostgreSQL en Docker, con limites de memoria y redes internas;
- CloudFront como entrada HTTPS hacia un unico proxy; servicios y base de datos no se publican;
- administracion por Systems Manager Session Manager, sin puerto SSH;
- Parameter Store Standard `SecureString` y claves administradas por AWS, sin secretos en Terraform/Git/user data;
- bucket privado temporal para artefactos, cifrado administrado y lifecycle corto;
- parada local obligatoria a las cuatro horas y automatizacion independiente de respaldo;
- presupuesto/alertas y accion de detencion como defensa adicional, no como limite instantaneo;
- etiquetas `Project=NexoFiscal`, `Environment=free-preview`, `ManagedBy=terraform` en todo recurso compatible;
- ningun recurso previo se importa, adopta, modifica o elimina.

Quedan prohibidos en este ambiente NAT Gateway, ECS Fargate, RDS/Aurora, RDS Proxy, ALB/NLB, Route 53, WAF, Secrets Manager, claves KMS propias, Managed Grafana/Prometheus, Marketplace, Savings Plans, Reserved Instances, Organizations y Control Tower.

## Operacion aprobada

1. Consultar STS y Free Tier sin mutaciones.
2. Construir y probar artefactos localmente.
3. Ejecutar `terraform fmt`, `validate`, politica del plan y `terraform plan`.
4. Presentar recursos, estimacion y riesgos al usuario.
5. Solicitar confirmacion separada para `apply`.
6. Publicar artefactos y arrancar solo con confirmacion separada.
7. Ejecutar smoke tests y verificar presupuesto/apagado.
8. Detener al terminar; respaldar y destruir solamente con confirmacion explicita.

## Consecuencias

- El ambiente consume creditos promocionales aunque la cuenta Free Plan no cobre externamente mientras conserve ese estado.
- No existe alta disponibilidad, escalado, recuperacion administrada ni aptitud productiva.
- Al vencer el plan o agotarse los creditos, AWS puede cerrar la cuenta; los datos requeridos deben exportarse antes.
- Una `t3.large` de referencia en `us-east-1` cuesta aproximadamente USD 0,0832/h. Con 120 horas mensuales consume cerca de USD 9,98 de creditos antes de almacenamiento y otros consumos.
- Detener EC2 elimina el consumo de computo, pero EBS y una direccion IPv4/Elastic IP conservada pueden seguir consumiendo beneficios o creditos; el runbook debe verificar y eliminar residuos.

## Alternativas rechazadas

- Target ECS/Fargate actual: demasiadas tareas y componentes medidos para USD 140,15.
- Una EC2 micro: memoria insuficiente para doce JVM, PostgreSQL y proxy.
- Reescritura Lambda/DynamoDB: podria aprovechar cuotas Always Free, pero cambia contratos, persistencia y arquitectura funcional; no es un despliegue de la aplicacion existente.
- RDS Free Plan: disponible mediante creditos, pero agrega consumo persistente y duplica infraestructura que puede residir temporalmente en la unica EC2.

## Evidencia

- AWS Free Plan: https://docs.aws.amazon.com/awsaccountbilling/latest/aboutv2/free-tier-plans.html
- Seguimiento Free Tier: https://docs.aws.amazon.com/awsaccountbilling/latest/aboutv2/tracking-free-tier-usage.html
- Precios Fargate: https://aws.amazon.com/fargate/pricing/
- Precios VPC/NAT/IPv4: https://aws.amazon.com/vpc/pricing/
- Parameter Store: https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-parameter-store.html
- Precios EBS: https://aws.amazon.com/ebs/pricing/
- CloudFront Free Tier: https://aws.amazon.com/cloudfront/faqs/

## Context7 evidence

- Library/tool: Terraform AWS Provider `/hashicorp/terraform-provider-aws/v6.33.0`.
- Topic consulted: restriccion de cuenta y AWS Budgets.
- Relevant finding: `allowed_account_ids` valida la identidad obtenida mediante STS y bloquea una cuenta no permitida; `aws_budgets_budget` permite presupuesto y notificaciones.
- Decision impact: todo provider del ambiente bloquea la cuenta exacta y el presupuesto se administra como IaC, sin atribuirle garantia de corte inmediato.
