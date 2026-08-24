variable "name_prefix" {
  type        = string
  description = "Prefix used for AWS resource names."
}

variable "vpc_id" {
  type        = string
  description = "VPC ID."
}

variable "vpc_cidr" {
  type        = string
  description = "VPC CIDR used for internal ALB access."
}

variable "subnet_ids" {
  type        = list(string)
  description = "Private subnet IDs used by ECS services and internal ALB."
}

variable "app_security_group_id" {
  type        = string
  description = "Base application security group ID."
}

variable "public_service_name" {
  type        = string
  description = "Service exposed to API Gateway through the internal ALB."
  default     = "bff-service"
}

variable "secret_arns" {
  type        = list(string)
  description = "Secrets Manager ARNs that ECS tasks may read during startup."
  default     = []
}

variable "runtime_secret_arn_patterns" {
  type        = list(string)
  description = "Secrets Manager ARN patterns that ECS task code may create or update at runtime."
  default     = []
}

variable "kms_key_arns" {
  type        = list(string)
  description = "KMS key ARNs that ECS task code may use for runtime secret encryption."
  default     = []
}

variable "common_secrets" {
  type        = map(string)
  description = "Secrets injected into every ECS container."
  default     = {}
}

variable "services" {
  type = map(object({
    container_port  = number
    cpu             = number
    memory          = number
    desired_count   = number
    image_tag       = optional(string, "latest")
    container_image = optional(string)
    environment     = optional(map(string), {})
    secrets         = optional(map(string), {})
  }))
  description = "ECS service definitions."
}

variable "tags" {
  type        = map(string)
  description = "Tags applied to resources."
  default     = {}
}
