resource "aws_db_subnet_group" "this" {
  name       = "${var.name_prefix}-db-subnets"
  subnet_ids = var.subnet_ids

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-db-subnets"
  })
}

resource "aws_db_instance" "this" {
  identifier                          = "${var.name_prefix}-postgres"
  engine                              = "postgres"
  engine_version                      = "16"
  instance_class                      = var.instance_class
  allocated_storage                   = var.allocated_storage
  db_name                             = var.db_name
  username                            = var.username
  manage_master_user_password         = true
  db_subnet_group_name                = aws_db_subnet_group.this.name
  vpc_security_group_ids              = [var.security_group_id]
  publicly_accessible                 = false
  storage_encrypted                   = true
  backup_retention_period             = var.backup_retention_period
  deletion_protection                 = var.deletion_protection
  skip_final_snapshot                 = var.skip_final_snapshot
  iam_database_authentication_enabled = true
  auto_minor_version_upgrade          = true
  copy_tags_to_snapshot               = true

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-postgres"
  })
}