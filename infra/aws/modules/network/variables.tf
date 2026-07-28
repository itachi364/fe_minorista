variable "name_prefix" {
  type        = string
  description = "Prefix used for AWS resource names."
}

variable "vpc_cidr" {
  type        = string
  description = "CIDR block for the VPC."
}

variable "availability_zones" {
  type        = list(string)
  description = "Availability zones used by public and private subnets."
}

variable "public_subnet_cidrs" {
  type        = list(string)
  description = "CIDR blocks for public subnets."
}

variable "private_subnet_cidrs" {
  type        = list(string)
  description = "CIDR blocks for private subnets."
}

variable "enable_nat_gateway" {
  type        = bool
  description = "Create a single NAT gateway for private subnet egress."
  default     = false
}

variable "tags" {
  type        = map(string)
  description = "Tags applied to resources."
  default     = {}
}