resource "aws_secretsmanager_secret" "this" {
  for_each = var.secret_names

  name        = "${var.name_prefix}/${each.key}"
  description = "Managed secret placeholder for ${each.key}. Secret value must be loaded outside Terraform state."

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-${each.key}"
  })
}