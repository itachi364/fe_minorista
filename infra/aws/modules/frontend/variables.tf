variable "name_prefix" {
  type        = string
  description = "Prefix used for AWS resource names."
}

variable "price_class" {
  type        = string
  description = "CloudFront price class."
  default     = "PriceClass_100"
}

variable "tags" {
  type        = map(string)
  description = "Tags applied to resources."
  default     = {}
}