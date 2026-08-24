output "user_pool_id" {
  value = aws_cognito_user_pool.this.id
}

output "web_client_id" {
  value = aws_cognito_user_pool_client.web.id
}

output "hosted_ui_base_url" {
  value = "https://${aws_cognito_user_pool_domain.this.domain}.auth.${data.aws_region.current.region}.amazoncognito.com"
}
