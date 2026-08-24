variable "name_prefix" {
  type        = string
  description = "Prefix used for AWS resource names."
}

variable "secret_names" {
  type        = set(string)
  description = "Logical secret names to create without secret values."
}

variable "runtime_secret_environment" {
  type        = string
  description = "Environment segment used by runtime-created company secrets."
}

variable "tags" {
  type        = map(string)
  description = "Tags applied to resources."
  default     = {}
}
