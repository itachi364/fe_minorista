variable "aws_region" {
  type        = string
  description = "AWS region for the temporary preview. CloudFront ACM certificates require us-east-1."
  default     = "us-east-1"

  validation {
    condition     = var.aws_region == "us-east-1"
    error_message = "The free preview is intentionally restricted to us-east-1."
  }
}

variable "allowed_account_id" {
  type        = string
  description = "Only AWS account allowed to receive preview resources."
  default     = "883425315805"

  validation {
    condition     = var.allowed_account_id == "883425315805"
    error_message = "This environment is locked to AWS account 883425315805."
  }
}

variable "instance_type" {
  type        = string
  description = "Fixed 8 GiB preview instance selected after the local memory gate."
  default     = "t3.large"

  validation {
    condition     = var.instance_type == "t3.large"
    error_message = "Automatic resizing is forbidden; only t3.large is allowed."
  }
}

variable "root_volume_size_gib" {
  type        = number
  description = "Encrypted gp3 root volume size."
  default     = 30

  validation {
    condition     = var.root_volume_size_gib >= 20 && var.root_volume_size_gib <= 30
    error_message = "The preview root volume must be between 20 and 30 GiB."
  }
}

variable "artifact_retention_days" {
  type        = number
  description = "Retention for temporary deployment bundles."
  default     = 7

  validation {
    condition     = var.artifact_retention_days >= 1 && var.artifact_retention_days <= 14
    error_message = "Preview artifacts must expire in 1 to 14 days."
  }
}

variable "budget_limit_usd" {
  type        = number
  description = "Monthly account budget used as a delayed alerting guardrail."
  default     = 15

  validation {
    condition     = var.budget_limit_usd > 0 && var.budget_limit_usd <= 25
    error_message = "The preview budget must be greater than zero and no more than USD 25."
  }
}

variable "budget_alert_email" {
  type        = string
  description = "Email that must confirm AWS Budget notifications."

  validation {
    condition     = can(regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", var.budget_alert_email))
    error_message = "Provide a valid budget notification email."
  }
}

variable "custom_domain" {
  type        = string
  description = "Hostinger-managed public application subdomain."
  default     = "app.nexofiscal.online"

  validation {
    condition     = var.custom_domain == lower(var.custom_domain) && var.custom_domain == "app.nexofiscal.online"
    error_message = "This preview is restricted to app.nexofiscal.online."
  }
}

variable "enable_custom_domain" {
  type        = bool
  description = "Attach the custom domain only after ACM reports the certificate as ISSUED."
  default     = false
}

variable "runtime_parameter_name" {
  type        = string
  description = "Standard SecureString containing the runtime environment file."
  default     = "/nexofiscal/free-preview/runtime-env"
}

variable "docker_compose_version" {
  type        = string
  description = "Pinned Docker Compose plugin installed during EC2 bootstrap."
  default     = "v2.40.3"

  validation {
    condition     = can(regex("^v[0-9]+\\.[0-9]+\\.[0-9]+$", var.docker_compose_version))
    error_message = "Docker Compose version must use the vMAJOR.MINOR.PATCH format."
  }
}

