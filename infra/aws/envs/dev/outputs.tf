output "frontend_cloudfront_domain_name" {
  value = module.frontend.cloudfront_domain_name
}

output "api_endpoint" {
  value = module.api.api_endpoint
}

output "ecs_cluster_name" {
  value = module.ecs.cluster_name
}

output "ecr_repository_urls" {
  value = module.ecs.repository_urls
}

output "database_address" {
  value = module.database.address
}

output "event_bus_name" {
  value = module.messaging.event_bus_name
}

output "event_queue_arns" {
  value = module.messaging.queue_arns
}
output "audit_event_writer_lambda_arn" {
  value = module.event_consumers.audit_event_writer_lambda_arn
}
output "inventory_sale_effect_lambda_arn" {
  value = module.event_consumers.inventory_sale_effect_lambda_arn
}
output "accounting_sale_entry_lambda_arn" {
  value = module.event_consumers.accounting_sale_entry_lambda_arn
}
output "provider_submission_retry_lambda_arn" {
  value = module.event_consumers.provider_submission_retry_lambda_arn
}