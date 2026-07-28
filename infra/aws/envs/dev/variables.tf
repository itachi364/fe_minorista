variable "aws_region" {
  type        = string
  description = "AWS region for the dev environment."
  default     = "us-east-1"
}

variable "project" {
  type        = string
  description = "Project name used in tags and resource names."
  default     = "facturaelectronica"
}

variable "environment" {
  type        = string
  description = "Environment name."
  default     = "dev"
}

variable "availability_zones" {
  type        = list(string)
  description = "Availability zones used by the dev environment."
  default     = ["us-east-1a", "us-east-1b"]
}

variable "vpc_cidr" {
  type        = string
  description = "VPC CIDR block."
  default     = "10.40.0.0/16"
}

variable "public_subnet_cidrs" {
  type        = list(string)
  description = "Public subnet CIDR blocks."
  default     = ["10.40.0.0/24", "10.40.1.0/24"]
}

variable "private_subnet_cidrs" {
  type        = list(string)
  description = "Private subnet CIDR blocks."
  default     = ["10.40.10.0/24", "10.40.11.0/24"]
}

variable "enable_nat_gateway" {
  type        = bool
  description = "Create a NAT gateway for private subnet egress. Disabled by default to avoid surprise costs in dev."
  default     = false
}

variable "db_name" {
  type        = string
  description = "Initial PostgreSQL database name."
  default     = "facturaelectronica"
}

variable "db_username" {
  type        = string
  description = "PostgreSQL master username."
  default     = "factura_admin"
}

variable "db_instance_class" {
  type        = string
  description = "RDS instance class."
  default     = "db.t4g.micro"
}
variable "lambda_artifact_bucket" {
  type        = string
  description = "S3 bucket containing Lambda deployment artifacts. Empty keeps event-driven consumers disabled in dev."
  default     = ""
}

variable "audit_event_writer_lambda_s3_key" {
  type        = string
  description = "S3 key for the audit-event-writer-lambda shaded jar."
  default     = "lambdas/audit-event-writer-lambda.jar"
}
variable "inventory_sale_effect_lambda_s3_key" {
  type        = string
  description = "S3 key for the inventory-sale-effect-lambda shaded jar."
  default     = "lambdas/inventory-sale-effect-lambda.jar"
}
variable "accounting_sale_entry_lambda_s3_key" {
  type        = string
  description = "S3 key for the accounting-sale-entry-lambda shaded jar."
  default     = "lambdas/accounting-sale-entry-lambda.jar"
}
variable "provider_submission_retry_lambda_s3_key" {
  type        = string
  description = "S3 key for the provider-submission-retry-lambda shaded jar."
  default     = "lambdas/provider-submission-retry-lambda.jar"
}

variable "provider_retry_provider_base_url" {
  type        = string
  description = "Base URL for the DIAN provider service used by provider retry Lambda. Empty disables the function."
  default     = ""
}