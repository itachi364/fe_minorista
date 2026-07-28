variable "name_prefix" {
  type        = string
  description = "Prefix used for AWS resource names."
}

variable "subnet_ids" {
  type        = list(string)
  description = "Private subnet IDs for the DB subnet group."
}

variable "security_group_id" {
  type        = string
  description = "Security group ID attached to the database."
}

variable "db_name" {
  type        = string
  description = "Initial database name."
}

variable "username" {
  type        = string
  description = "Master database username."
}

variable "instance_class" {
  type        = string
  description = "RDS instance class."
  default     = "db.t4g.micro"
}

variable "allocated_storage" {
  type        = number
  description = "Allocated storage in GiB."
  default     = 20
}

variable "backup_retention_period" {
  type        = number
  description = "Backup retention period in days."
  default     = 7
}

variable "deletion_protection" {
  type        = bool
  description = "Enable deletion protection for the database."
  default     = true
}

variable "skip_final_snapshot" {
  type        = bool
  description = "Skip final snapshot on destroy. Use false outside disposable environments."
  default     = false
}

variable "tags" {
  type        = map(string)
  description = "Tags applied to resources."
  default     = {}
}