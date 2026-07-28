variable "name_prefix" {
  type        = string
  description = "Prefix used for AWS resource names."
}

variable "event_routes" {
  type = map(object({
    detail_types = list(string)
  }))
  description = "Map of SQS queue keys to EventBridge detail-types."
  default     = {}
}

variable "message_retention_seconds" {
  type        = number
  description = "SQS message retention in seconds."
  default     = 345600
}

variable "dlq_message_retention_seconds" {
  type        = number
  description = "SQS DLQ retention in seconds."
  default     = 1209600
}

variable "max_receive_count" {
  type        = number
  description = "Max receive count before moving a message to DLQ."
  default     = 5
}

variable "tags" {
  type        = map(string)
  description = "Tags applied to resources."
  default     = {}
}