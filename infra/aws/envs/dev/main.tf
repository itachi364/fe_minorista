provider "aws" {
  region = var.aws_region

  default_tags {
    tags = local.tags
  }
}

data "aws_caller_identity" "current" {}

data "aws_partition" "current" {}

locals {
  name_prefix = "${var.project}-${var.environment}"

  tags = {
    Project     = var.project
    Environment = var.environment
    ManagedBy   = "terraform"
  }

  db_password_secret   = "${module.database.master_user_secret_arn}:password::"
  service_domain       = "${local.name_prefix}.local"
  frontend_base_url    = var.frontend_base_url != "" ? var.frontend_base_url : "https://${module.frontend.cloudfront_domain_name}"
  api_public_base_url  = var.api_public_base_url != "" ? trimsuffix(var.api_public_base_url, "/") : "https://api.${local.name_prefix}.example.com"
  cognito_redirect_uri = "${local.api_public_base_url}/api/v1/auth/callback"

  db_secret_names = [
    "DB_PASSWORD",
    "TENANT_DB_PASSWORD",
    "IDENTITY_DB_PASSWORD",
    "CATALOG_DB_PASSWORD",
    "THIRDPARTY_DB_PASSWORD",
    "INVENTORY_DB_PASSWORD",
    "BILLING_DB_PASSWORD",
    "DIAN_PROVIDER_DB_PASSWORD",
    "ACCOUNTING_DB_PASSWORD",
    "AUDIT_DB_PASSWORD",
    "PAYROLL_DB_PASSWORD"
  ]

  db_common_secrets = {
    for name in local.db_secret_names : name => local.db_password_secret
  }

  base_environment = {
    DB_URL       = module.database.jdbc_url
    DB_USERNAME  = var.db_username
    JPA_SHOW_SQL = "false"
    APP_ENV      = var.environment
  }

  service_definitions = {
    bff-service = {
      container_port = 8080
      cpu            = 256
      memory         = 512
      desired_count  = 0
      environment = merge(local.base_environment, {
        SERVER_PORT               = "8080"
        TENANT_SERVICE_URL        = "http://tenant-service.${local.service_domain}:8084"
        IDENTITY_SERVICE_URL      = "http://identity-service.${local.service_domain}:8092"
        CATALOG_SERVICE_URL       = "http://catalog-service.${local.service_domain}:8085"
        THIRDPARTY_SERVICE_URL    = "http://thirdparty-service.${local.service_domain}:8086"
        INVENTORY_SERVICE_URL     = "http://inventory-service.${local.service_domain}:8087"
        BILLING_SERVICE_URL       = "http://billing-service.${local.service_domain}:8088"
        ACCOUNTING_SERVICE_URL    = "http://accounting-service.${local.service_domain}:8090"
        AUDIT_SERVICE_URL         = "http://audit-service.${local.service_domain}:8091"
        PAYROLL_SERVICE_URL       = "http://payroll-service.${local.service_domain}:8093"
        REPORTING_SERVICE_URL     = "http://reporting-service.${local.service_domain}:8094"
        DIAN_PROVIDER_SERVICE_URL = "http://dian-provider-service.${local.service_domain}:8089"
        AUTH_MODE                 = "cognito"
        COGNITO_BASE_URL          = module.auth.hosted_ui_base_url
        COGNITO_CLIENT_ID         = module.auth.web_client_id
        COGNITO_REDIRECT_URI      = local.cognito_redirect_uri
        COGNITO_LOGOUT_URI        = local.frontend_base_url
        FRONTEND_BASE_URL         = local.frontend_base_url
        BFF_CSRF_ENABLED          = "true"
        BFF_COOKIE_SECURE         = "true"
      })
      secrets = {
        BFF_SESSION_ENCRYPTION_KEY = module.secrets.secret_arns["bff-session-encryption-key"]
      }
    }

    tenant-service = {
      container_port = 8084
      cpu            = 256
      memory         = 512
      desired_count  = 0
      environment = merge(local.base_environment, {
        SERVER_PORT   = "8084"
        TENANT_DB_URL = module.database.jdbc_url
      })
    }

    identity-service = {
      container_port = 8092
      cpu            = 256
      memory         = 512
      desired_count  = 0
      environment = merge(local.base_environment, {
        SERVER_PORT        = "8092"
        IDENTITY_DB_URL    = module.database.jdbc_url
        TENANT_SERVICE_URL = "http://tenant-service.${local.service_domain}:8084"
      })
    }

    catalog-service = {
      container_port = 8085
      cpu            = 256
      memory         = 512
      desired_count  = 0
      environment = merge(local.base_environment, {
        SERVER_PORT    = "8085"
        CATALOG_DB_URL = module.database.jdbc_url
      })
    }

    thirdparty-service = {
      container_port = 8086
      cpu            = 256
      memory         = 512
      desired_count  = 0
      environment = merge(local.base_environment, {
        SERVER_PORT       = "8086"
        THIRDPARTY_DB_URL = module.database.jdbc_url
      })
    }

    inventory-service = {
      container_port = 8087
      cpu            = 512
      memory         = 1024
      desired_count  = 0
      environment = merge(local.base_environment, {
        SERVER_PORT      = "8087"
        INVENTORY_DB_URL = module.database.jdbc_url
      })
    }

    billing-service = {
      container_port = 8088
      cpu            = 512
      memory         = 1024
      desired_count  = 0
      environment = merge(local.base_environment, {
        SERVER_PORT               = "8088"
        BILLING_DB_URL            = module.database.jdbc_url
        INVENTORY_SERVICE_URL     = "http://inventory-service.${local.service_domain}:8087"
        DIAN_PROVIDER_SERVICE_URL = "http://dian-provider-service.${local.service_domain}:8089"
        ACCOUNTING_SERVICE_URL    = "http://accounting-service.${local.service_domain}:8090"
        AUDIT_SERVICE_URL         = "http://audit-service.${local.service_domain}:8091"
        TENANT_SERVICE_URL        = "http://tenant-service.${local.service_domain}:8084"
      })
    }

    dian-provider-service = {
      container_port = 8089
      cpu            = 256
      memory         = 512
      desired_count  = 0
      environment = merge(local.base_environment, {
        SERVER_PORT                       = "8089"
        DIAN_PROVIDER_DB_URL              = module.database.jdbc_url
        DIAN_PROVIDER_MODE                = "mock"
        DIAN_PROVIDER_SECRETS_ENVIRONMENT = var.environment
        DIAN_MOCK_DEFAULT_STATUS          = "ACCEPTED"
      })
    }

    accounting-service = {
      container_port = 8090
      cpu            = 512
      memory         = 1024
      desired_count  = 0
      environment = merge(local.base_environment, {
        SERVER_PORT       = "8090"
        ACCOUNTING_DB_URL = module.database.jdbc_url
      })
    }

    audit-service = {
      container_port = 8091
      cpu            = 256
      memory         = 512
      desired_count  = 0
      environment = merge(local.base_environment, {
        SERVER_PORT  = "8091"
        AUDIT_DB_URL = module.database.jdbc_url
      })
    }

    payroll-service = {
      container_port = 8093
      cpu            = 512
      memory         = 1024
      desired_count  = 0
      environment = merge(local.base_environment, {
        SERVER_PORT            = "8093"
        PAYROLL_DB_URL         = module.database.jdbc_url
        ACCOUNTING_SERVICE_URL = "http://accounting-service.${local.service_domain}:8090"
      })
    }

    reporting-service = {
      container_port = 8094
      cpu            = 512
      memory         = 1024
      desired_count  = 0
      environment = merge(local.base_environment, {
        SERVER_PORT            = "8094"
        IDENTITY_SERVICE_URL   = "http://identity-service.${local.service_domain}:8092"
        INVENTORY_SERVICE_URL  = "http://inventory-service.${local.service_domain}:8087"
        BILLING_SERVICE_URL    = "http://billing-service.${local.service_domain}:8088"
        ACCOUNTING_SERVICE_URL = "http://accounting-service.${local.service_domain}:8090"
        PAYROLL_SERVICE_URL    = "http://payroll-service.${local.service_domain}:8093"
        TENANT_SERVICE_URL     = "http://tenant-service.${local.service_domain}:8084"
      })
    }
  }
}

module "network" {
  source = "../../modules/network"

  name_prefix          = local.name_prefix
  vpc_cidr             = var.vpc_cidr
  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  enable_nat_gateway   = var.enable_nat_gateway
  tags                 = local.tags
}

module "database" {
  source = "../../modules/database"

  name_prefix             = local.name_prefix
  subnet_ids              = module.network.private_subnet_ids
  security_group_id       = module.network.database_security_group_id
  db_name                 = var.db_name
  username                = var.db_username
  instance_class          = var.db_instance_class
  deletion_protection     = true
  skip_final_snapshot     = false
  backup_retention_period = 7
  tags                    = local.tags
}

module "secrets" {
  source = "../../modules/secrets"

  name_prefix                = local.name_prefix
  runtime_secret_environment = var.environment
  secret_names = [
    "dian-provider-api-key",
    "dian-certificate-password",
    "bff-session-encryption-key",
    "jwt-signing-key"
  ]
  tags = local.tags
}

module "ecs" {
  source = "../../modules/ecs"

  name_prefix           = local.name_prefix
  vpc_id                = module.network.vpc_id
  vpc_cidr              = module.network.vpc_cidr
  subnet_ids            = module.network.private_subnet_ids
  app_security_group_id = module.network.app_security_group_id
  public_service_name   = "bff-service"
  secret_arns           = concat([module.database.master_user_secret_arn], values(module.secrets.secret_arns))
  runtime_secret_arn_patterns = [
    "arn:${data.aws_partition.current.partition}:secretsmanager:${var.aws_region}:${data.aws_caller_identity.current.account_id}:secret:/facturaelectronica/${var.environment}/companies/*"
  ]
  kms_key_arns   = [module.secrets.kms_key_arn]
  common_secrets = local.db_common_secrets
  services       = local.service_definitions
  tags           = local.tags
}

module "api" {
  source = "../../modules/api"

  name_prefix        = local.name_prefix
  subnet_ids         = module.network.private_subnet_ids
  security_group_ids = [module.network.app_security_group_id]
  alb_listener_arn   = module.ecs.internal_alb_listener_arn
  tags               = local.tags
}

module "frontend" {
  source = "../../modules/frontend"

  name_prefix = local.name_prefix
  tags        = local.tags
}

module "auth" {
  source = "../../modules/auth"

  name_prefix   = local.name_prefix
  callback_urls = [local.cognito_redirect_uri]
  logout_urls   = [local.frontend_base_url]
  tags          = local.tags
}

module "messaging" {
  source = "../../modules/messaging"

  name_prefix = local.name_prefix
  event_routes = {
    audit-events = {
      detail_types = ["AuditEventRequested"]
    }
    inventory-effects = {
      detail_types = ["SaleConfirmed", "ElectronicDocumentValidated"]
    }
    accounting-effects = {
      detail_types = ["SaleConfirmed", "InventoryMovementRegistered"]
    }
    reporting-projections = {
      detail_types = ["SaleConfirmed", "ElectronicDocumentValidated", "InventoryMovementRegistered", "AccountingEntryPosted"]
    }
    provider-retries = {
      detail_types = ["ProviderSubmissionPending", "ProviderSubmissionFailed"]
    }
  }
  tags = local.tags
}

module "event_consumers" {
  source = "../../modules/event_consumers"

  name_prefix                         = local.name_prefix
  subnet_ids                          = module.network.private_subnet_ids
  security_group_ids                  = [module.network.app_security_group_id]
  audit_event_queue_arn               = module.messaging.queue_arns["audit-events"]
  inventory_effect_queue_arn          = module.messaging.queue_arns["inventory-effects"]
  accounting_effect_queue_arn         = module.messaging.queue_arns["accounting-effects"]
  provider_retry_queue_arn            = module.messaging.queue_arns["provider-retries"]
  reporting_projection_queue_arn      = module.messaging.queue_arns["reporting-projections"]
  audit_event_writer_s3_bucket        = var.lambda_artifact_bucket
  audit_event_writer_s3_key           = var.audit_event_writer_lambda_s3_key
  inventory_sale_effect_s3_bucket     = var.lambda_artifact_bucket
  accounting_sale_entry_s3_bucket     = var.lambda_artifact_bucket
  provider_submission_retry_s3_bucket = var.lambda_artifact_bucket
  reporting_projection_s3_bucket      = var.lambda_artifact_bucket
  inventory_sale_effect_s3_key        = var.inventory_sale_effect_lambda_s3_key
  accounting_sale_entry_s3_key        = var.accounting_sale_entry_lambda_s3_key
  provider_submission_retry_s3_key    = var.provider_submission_retry_lambda_s3_key
  reporting_projection_s3_key         = var.reporting_projection_lambda_s3_key
  provider_retry_provider_base_url    = var.provider_retry_provider_base_url
  db_url                              = module.database.jdbc_url
  db_username                         = var.db_username
  db_password_secret_arn              = module.database.master_user_secret_arn
  tags                                = local.tags
}
