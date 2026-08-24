output "secret_arns" {
  value = { for key, secret in aws_secretsmanager_secret.this : key => secret.arn }
}

output "kms_key_arn" {
  value = aws_kms_key.this.arn
}

output "runtime_company_secret_prefix" {
  value = "/facturaelectronica/${var.runtime_secret_environment}/companies"
}
