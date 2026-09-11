data "aws_caller_identity" "current" {}
data "aws_partition" "current" {}

data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }

  filter {
    name   = "architecture"
    values = ["x86_64"]
  }

  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

data "aws_ec2_managed_prefix_list" "cloudfront_origin" {
  name = "com.amazonaws.global.cloudfront.origin-facing"
}

locals {
  name_prefix = "nexofiscal-free-preview"
  tags = {
    Project     = "NexoFiscal"
    Environment = "free-preview"
    ManagedBy   = "terraform"
    CostScope   = "temporary-preview"
  }
  artifact_key = "releases/current/nexofiscal-preview.tar.gz"
}

resource "aws_vpc" "preview" {
  cidr_block           = "10.91.0.0/24"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = { Name = "${local.name_prefix}-vpc" }
}

resource "aws_internet_gateway" "preview" {
  vpc_id = aws_vpc.preview.id
  tags   = { Name = "${local.name_prefix}-igw" }
}

resource "aws_subnet" "preview" {
  vpc_id                  = aws_vpc.preview.id
  cidr_block              = "10.91.0.0/26"
  map_public_ip_on_launch = true
  availability_zone       = "${var.aws_region}a"

  tags = { Name = "${local.name_prefix}-public" }
}

resource "aws_route_table" "preview" {
  vpc_id = aws_vpc.preview.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.preview.id
  }

  tags = { Name = "${local.name_prefix}-public" }
}

resource "aws_route_table_association" "preview" {
  subnet_id      = aws_subnet.preview.id
  route_table_id = aws_route_table.preview.id
}

resource "aws_security_group" "preview" {
  name        = "${local.name_prefix}-origin"
  description = "HTTP only from the AWS-managed CloudFront origin prefix list"
  vpc_id      = aws_vpc.preview.id

  ingress {
    description     = "CloudFront origin-facing HTTP"
    from_port       = 80
    to_port         = 80
    protocol        = "tcp"
    prefix_list_ids = [data.aws_ec2_managed_prefix_list.cloudfront_origin.id]
  }

  egress {
    description = "Bootstrap, SSM, package repositories and AWS public APIs"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${local.name_prefix}-origin" }
}

resource "aws_s3_bucket" "artifacts" {
  bucket        = "nexofiscal-free-preview-${data.aws_caller_identity.current.account_id}"
  force_destroy = true

  tags = { Name = "${local.name_prefix}-artifacts" }
}

resource "aws_s3_bucket_ownership_controls" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id

  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

resource "aws_s3_bucket_public_access_block" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
    bucket_key_enabled = false
  }
}

resource "aws_s3_bucket_versioning" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id

  versioning_configuration {
    status = "Suspended"
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id

  rule {
    id     = "expire-temporary-artifacts"
    status = "Enabled"

    filter { prefix = "releases/" }

    expiration { days = var.artifact_retention_days }

    abort_incomplete_multipart_upload {
      days_after_initiation = 1
    }
  }
}

resource "aws_ssm_parameter" "runtime_env" {
  name        = var.runtime_parameter_name
  description = "NexoFiscal free preview runtime env; replace placeholder out of band"
  type        = "SecureString"
  tier        = "Standard"
  value       = "CONFIGURE_BEFORE_START"

  lifecycle {
    ignore_changes = [value]
  }
}

data "aws_iam_policy_document" "ec2_assume" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "instance" {
  name               = "${local.name_prefix}-instance"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume.json
}

resource "aws_iam_role_policy_attachment" "instance_ssm" {
  role       = aws_iam_role.instance.name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

data "aws_iam_policy_document" "instance_runtime" {
  statement {
    sid       = "ReadRuntimeParameter"
    actions   = ["ssm:GetParameter"]
    resources = [aws_ssm_parameter.runtime_env.arn]
  }

  statement {
    sid       = "ReadCurrentArtifact"
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.artifacts.arn}/${local.artifact_key}"]
  }

  statement {
    sid       = "ListArtifactBucket"
    actions   = ["s3:ListBucket"]
    resources = [aws_s3_bucket.artifacts.arn]

    condition {
      test     = "StringLike"
      variable = "s3:prefix"
      values   = ["releases/current/*"]
    }
  }
}

resource "aws_iam_role_policy" "instance_runtime" {
  name   = "${local.name_prefix}-runtime"
  role   = aws_iam_role.instance.id
  policy = data.aws_iam_policy_document.instance_runtime.json
}

resource "aws_iam_instance_profile" "preview" {
  name = "${local.name_prefix}-instance"
  role = aws_iam_role.instance.name
}

resource "aws_instance" "preview" {
  ami                                  = data.aws_ami.amazon_linux_2023.id
  instance_type                        = var.instance_type
  subnet_id                            = aws_subnet.preview.id
  vpc_security_group_ids               = [aws_security_group.preview.id]
  iam_instance_profile                 = aws_iam_instance_profile.preview.name
  associate_public_ip_address          = true
  monitoring                           = false
  instance_initiated_shutdown_behavior = "stop"

  user_data = templatefile("${path.module}/templates/user-data.sh.tftpl", {
    aws_region             = var.aws_region
    artifact_bucket        = aws_s3_bucket.artifacts.id
    artifact_key           = local.artifact_key
    runtime_parameter_name = aws_ssm_parameter.runtime_env.name
    compose_version        = var.docker_compose_version
  })
  user_data_replace_on_change = false

  root_block_device {
    volume_type           = "gp3"
    volume_size           = var.root_volume_size_gib
    encrypted             = true
    delete_on_termination = true
    iops                  = 3000
    throughput            = 125
  }

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
    instance_metadata_tags      = "disabled"
  }

  credit_specification {
    cpu_credits = "standard"
  }

  lifecycle {
    precondition {
      condition     = var.root_volume_size_gib <= 30
      error_message = "The root volume exceeds the approved 30 GiB limit."
    }
  }

  tags = { Name = local.name_prefix }
}

resource "aws_acm_certificate" "application" {
  domain_name       = var.custom_domain
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }

  tags = { Name = "${local.name_prefix}-certificate" }
}

resource "aws_cloudfront_distribution" "preview" {
  enabled         = true
  is_ipv6_enabled = true
  comment         = "NexoFiscal temporary Free Preview"
  price_class     = "PriceClass_100"
  aliases         = var.enable_custom_domain ? [var.custom_domain] : []

  origin {
    domain_name = aws_instance.preview.public_dns
    origin_id   = "preview-ec2-origin"

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  default_cache_behavior {
    target_origin_id       = "preview-ec2-origin"
    viewer_protocol_policy = "redirect-to-https"
    allowed_methods        = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods         = ["GET", "HEAD"]
    compress               = true
    min_ttl                = 0
    default_ttl            = 0
    max_ttl                = 0

    forwarded_values {
      query_string = true
      headers      = ["*"]

      cookies { forward = "all" }
    }
  }

  restrictions {
    geo_restriction { restriction_type = "none" }
  }

  viewer_certificate {
    cloudfront_default_certificate = !var.enable_custom_domain
    acm_certificate_arn            = var.enable_custom_domain ? aws_acm_certificate.application.arn : null
    ssl_support_method             = var.enable_custom_domain ? "sni-only" : null
    minimum_protocol_version       = var.enable_custom_domain ? "TLSv1.2_2021" : "TLSv1"
  }

  lifecycle {
    precondition {
      condition     = !var.enable_custom_domain || aws_acm_certificate.application.status == "ISSUED"
      error_message = "ACM must report ISSUED before enable_custom_domain can be true. Add the validation CNAME in Hostinger first."
    }
  }

  tags = { Name = "${local.name_prefix}-edge" }
}

data "aws_iam_policy_document" "scheduler_assume" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["scheduler.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "scheduler" {
  name               = "${local.name_prefix}-stop-scheduler"
  assume_role_policy = data.aws_iam_policy_document.scheduler_assume.json
}

data "aws_iam_policy_document" "scheduler_stop" {
  statement {
    actions   = ["ec2:StopInstances"]
    resources = [aws_instance.preview.arn]
  }
}

resource "aws_iam_role_policy" "scheduler_stop" {
  name   = "${local.name_prefix}-stop-instance"
  role   = aws_iam_role.scheduler.id
  policy = data.aws_iam_policy_document.scheduler_stop.json
}

resource "aws_scheduler_schedule" "stop_guard" {
  name                         = "${local.name_prefix}-stop-every-four-hours"
  schedule_expression          = "cron(0 0/4 * * ? *)"
  schedule_expression_timezone = "UTC"
  state                        = "ENABLED"

  flexible_time_window { mode = "OFF" }

  target {
    arn      = "arn:${data.aws_partition.current.partition}:scheduler:::aws-sdk:ec2:stopInstances"
    role_arn = aws_iam_role.scheduler.arn
    input    = jsonencode({ InstanceIds = [aws_instance.preview.id] })

    retry_policy {
      maximum_event_age_in_seconds = 3600
      maximum_retry_attempts       = 3
    }
  }
}

resource "aws_budgets_budget" "preview" {
  name         = "${local.name_prefix}-monthly"
  budget_type  = "COST"
  limit_amount = tostring(var.budget_limit_usd)
  limit_unit   = "USD"
  time_unit    = "MONTHLY"

  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 50
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = [var.budget_alert_email]
  }

  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 80
    threshold_type             = "PERCENTAGE"
    notification_type          = "FORECASTED"
    subscriber_email_addresses = [var.budget_alert_email]
  }
}

data "aws_iam_policy_document" "budget_action_assume" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["budgets.amazonaws.com"]
    }

    condition {
      test     = "ArnLike"
      variable = "aws:SourceArn"
      values   = ["arn:${data.aws_partition.current.partition}:budgets::${data.aws_caller_identity.current.account_id}:budget/*"]
    }

    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [data.aws_caller_identity.current.account_id]
    }
  }
}

resource "aws_iam_role" "budget_action" {
  name               = "${local.name_prefix}-budget-stop"
  assume_role_policy = data.aws_iam_policy_document.budget_action_assume.json
}

resource "aws_iam_role_policy_attachment" "budget_action_stop" {
  role       = aws_iam_role.budget_action.name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AWSBudgetsActions_RolePolicyForResourceAdministrationWithSSM"
}

resource "aws_budgets_budget_action" "stop_preview" {
  budget_name        = aws_budgets_budget.preview.name
  action_type        = "RUN_SSM_DOCUMENTS"
  approval_model     = "AUTOMATIC"
  notification_type  = "ACTUAL"
  execution_role_arn = aws_iam_role.budget_action.arn

  action_threshold {
    action_threshold_type  = "PERCENTAGE"
    action_threshold_value = 80
  }

  definition {
    ssm_action_definition {
      action_sub_type = "STOP_EC2_INSTANCES"
      instance_ids    = [aws_instance.preview.id]
      region          = var.aws_region
    }
  }

  subscriber {
    subscription_type = "EMAIL"
    address           = var.budget_alert_email
  }

  tags = { Name = "${local.name_prefix}-budget-stop" }
}
