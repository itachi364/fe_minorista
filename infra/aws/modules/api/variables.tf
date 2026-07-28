variable "name_prefix" {
  type        = string
  description = "Prefix used for AWS resource names."
}

variable "subnet_ids" {
  type        = list(string)
  description = "Private subnet IDs used by API Gateway VPC Link."
}

variable "security_group_ids" {
  type        = list(string)
  description = "Security group IDs attached to API Gateway VPC Link."
}

variable "alb_listener_arn" {
  type        = string
  description = "Internal ALB listener ARN for the BFF integration."
}

variable "tags" {
  type        = map(string)
  description = "Tags applied to resources."
  default     = {}
}