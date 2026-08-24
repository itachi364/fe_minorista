resource "aws_kms_key" "this" {
  description             = "KMS key for ${var.name_prefix} Secrets Manager values"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-secrets-kms"
  })
}

resource "aws_kms_alias" "this" {
  name          = "alias/${var.name_prefix}-secrets"
  target_key_id = aws_kms_key.this.key_id
}

resource "aws_secretsmanager_secret" "this" {
  for_each = var.secret_names

  name        = "${var.name_prefix}/${each.key}"
  description = "Managed secret placeholder for ${each.key}. Secret value must be loaded outside Terraform state."
  kms_key_id  = aws_kms_key.this.arn

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-${each.key}"
  })
}
