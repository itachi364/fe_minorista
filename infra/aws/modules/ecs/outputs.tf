output "cluster_name" {
  value = aws_ecs_cluster.this.name
}

output "cluster_arn" {
  value = aws_ecs_cluster.this.arn
}

output "repository_urls" {
  value = { for key, repo in aws_ecr_repository.service : key => repo.repository_url }
}

output "app_security_group_id" {
  value = var.app_security_group_id
}

output "internal_alb_listener_arn" {
  value = length(aws_lb_listener.http) > 0 ? aws_lb_listener.http[0].arn : null
}

output "internal_alb_dns_name" {
  value = length(aws_lb.internal) > 0 ? aws_lb.internal[0].dns_name : null
}
output "service_discovery_namespace" {
  value = aws_service_discovery_private_dns_namespace.this.name
}
