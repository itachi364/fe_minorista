output "audit_event_writer_lambda_arn" {
  value = local.audit_event_writer_enabled ? aws_lambda_function.audit_event_writer[0].arn : null
}

output "inventory_sale_effect_lambda_arn" {
  value = local.inventory_effect_enabled ? aws_lambda_function.inventory_sale_effect[0].arn : null
}
output "accounting_sale_entry_lambda_arn" {
  value = local.accounting_effect_enabled ? aws_lambda_function.accounting_sale_entry[0].arn : null
}
output "provider_submission_retry_lambda_arn" {
  value = local.provider_retry_enabled ? aws_lambda_function.provider_submission_retry[0].arn : null
}