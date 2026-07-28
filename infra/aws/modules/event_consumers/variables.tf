variable "name_prefix" {
  type        = string
  description = "Prefix used for AWS resource names."
}

variable "subnet_ids" {
  type        = list(string)
  description = "Private subnet IDs used by Lambda functions that need database access."
}

variable "security_group_ids" {
  type        = list(string)
  description = "Security groups attached to Lambda VPC configuration."
}

variable "accounting_effect_queue_arn" {
  type        = string
  description = "SQS queue ARN for accounting effect messages."
}

variable "provider_retry_queue_arn" {
  type        = string
  description = "SQS queue ARN for provider retry messages."
}

variable "provider_submission_retry_s3_bucket" {
  type        = string
  description = "S3 bucket containing the provider-submission-retry-lambda shaded jar. Empty disables the function."
  default     = ""
}

variable "provider_submission_retry_s3_key" {
  type        = string
  description = "S3 key for the provider-submission-retry-lambda shaded jar."
  default     = "lambdas/provider-submission-retry-lambda.jar"
}

variable "provider_retry_provider_base_url" {
  type        = string
  description = "Base URL for the DIAN provider service used by provider retry Lambda. Empty disables the function."
  default     = ""
}

variable "inventory_effect_queue_arn" {
  type        = string
  description = "SQS queue ARN for inventory effect messages."
}

variable "audit_event_queue_arn" {
  type        = string
  description = "SQS queue ARN for AuditEventRequested messages."
}

variable "accounting_sale_entry_s3_bucket" {
  type        = string
  description = "S3 bucket containing the accounting-sale-entry-lambda shaded jar. Empty disables the function."
  default     = ""
}

variable "accounting_sale_entry_s3_key" {
  type        = string
  description = "S3 key for the accounting-sale-entry-lambda shaded jar."
  default     = "lambdas/accounting-sale-entry-lambda.jar"
}
variable "inventory_sale_effect_s3_bucket" {
  type        = string
  description = "S3 bucket containing the inventory-sale-effect-lambda shaded jar. Empty disables the function."
  default     = ""
}

variable "inventory_sale_effect_s3_key" {
  type        = string
  description = "S3 key for the inventory-sale-effect-lambda shaded jar."
  default     = "lambdas/inventory-sale-effect-lambda.jar"
}

variable "audit_event_writer_s3_bucket" {
  type        = string
  description = "S3 bucket containing the audit-event-writer-lambda shaded jar. Empty disables the function."
  default     = ""
}

variable "audit_event_writer_s3_key" {
  type        = string
  description = "S3 key for the audit-event-writer-lambda shaded jar."
  default     = "lambdas/audit-event-writer-lambda.jar"
}

variable "db_url" {
  type        = string
  description = "JDBC URL for the platform PostgreSQL database."
}

variable "db_username" {
  type        = string
  description = "Database username used by the audit event writer."
}

variable "db_password_secret_arn" {
  type        = string
  description = "Secrets Manager ARN containing the database password JSON field."
}

variable "tags" {
  type        = map(string)
  description = "Tags applied to resources."
  default     = {}
}
