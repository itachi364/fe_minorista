output "account_id" {
  description = "AWS account verified by the provider."
  value       = data.aws_caller_identity.current.account_id
}

output "instance_id" {
  description = "Temporary EC2 instance managed by this state."
  value       = aws_instance.preview.id
}

output "artifact_bucket" {
  description = "Private bucket used only for temporary deployment bundles."
  value       = aws_s3_bucket.artifacts.id
}

output "artifact_key" {
  description = "Fixed object key read by the instance bootstrap."
  value       = local.artifact_key
}

output "cloudfront_domain_name" {
  description = "CloudFront URL available before custom DNS is enabled."
  value       = aws_cloudfront_distribution.preview.domain_name
}

output "application_url" {
  description = "Effective public application URL."
  value       = var.enable_custom_domain ? "https://${var.custom_domain}" : "https://${aws_cloudfront_distribution.preview.domain_name}"
}

output "acm_dns_validation_records" {
  description = "Create these CNAME records in Hostinger; keep them for ACM renewal."
  value = {
    for option in aws_acm_certificate.application.domain_validation_options : option.domain_name => {
      name  = option.resource_record_name
      type  = option.resource_record_type
      value = option.resource_record_value
    }
  }
}

output "hostinger_application_cname" {
  description = "After ACM is issued and custom domain enabled, point this Hostinger CNAME to CloudFront."
  value = {
    name  = split(".", var.custom_domain)[0]
    type  = "CNAME"
    value = aws_cloudfront_distribution.preview.domain_name
  }
}

output "runtime_parameter_name" {
  description = "SecureString populated out of band before the application starts."
  value       = aws_ssm_parameter.runtime_env.name
}

