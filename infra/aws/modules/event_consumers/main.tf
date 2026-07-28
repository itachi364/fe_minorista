locals {
  audit_event_writer_enabled = var.audit_event_writer_s3_bucket != ""
  inventory_effect_enabled   = var.inventory_sale_effect_s3_bucket != ""
  accounting_effect_enabled  = var.accounting_sale_entry_s3_bucket != ""
  provider_retry_enabled     = var.provider_submission_retry_s3_bucket != "" && var.provider_retry_provider_base_url != ""
}

data "aws_iam_policy_document" "lambda_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "audit_event_writer" {
  count = local.audit_event_writer_enabled ? 1 : 0

  name               = "${var.name_prefix}-audit-event-writer-lambda"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "audit_event_writer_basic" {
  count = local.audit_event_writer_enabled ? 1 : 0

  role       = aws_iam_role.audit_event_writer[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "audit_event_writer_vpc" {
  count = local.audit_event_writer_enabled ? 1 : 0

  role       = aws_iam_role.audit_event_writer[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

data "aws_iam_policy_document" "audit_event_writer" {
  statement {
    effect = "Allow"
    actions = [
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:ChangeMessageVisibility"
    ]
    resources = [var.audit_event_queue_arn]
  }

  statement {
    effect    = "Allow"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [var.db_password_secret_arn]
  }
}

resource "aws_iam_role_policy" "audit_event_writer" {
  count = local.audit_event_writer_enabled ? 1 : 0

  name   = "${var.name_prefix}-audit-event-writer"
  role   = aws_iam_role.audit_event_writer[0].id
  policy = data.aws_iam_policy_document.audit_event_writer.json
}

resource "aws_cloudwatch_log_group" "audit_event_writer" {
  count = local.audit_event_writer_enabled ? 1 : 0

  name              = "/aws/lambda/${var.name_prefix}-audit-event-writer"
  retention_in_days = 30

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-audit-event-writer-logs"
  })
}

resource "aws_lambda_function" "audit_event_writer" {
  count = local.audit_event_writer_enabled ? 1 : 0

  function_name = "${var.name_prefix}-audit-event-writer"
  role          = aws_iam_role.audit_event_writer[0].arn
  runtime       = "java17"
  handler       = "com.msvanegasg.facturaelectronica.auditlambda.AuditEventWriterHandler::handleRequest"
  s3_bucket     = var.audit_event_writer_s3_bucket
  s3_key        = var.audit_event_writer_s3_key
  memory_size   = 512
  timeout       = 30
  publish       = true

  environment {
    variables = {
      AUDIT_DB_URL                      = var.db_url
      AUDIT_DB_USERNAME                 = var.db_username
      AUDIT_DB_PASSWORD_SECRET_ARN      = var.db_password_secret_arn
      AUDIT_DB_PASSWORD_SECRET_JSON_KEY = "password"
    }
  }

  vpc_config {
    subnet_ids         = var.subnet_ids
    security_group_ids = var.security_group_ids
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-audit-event-writer"
  })

  depends_on = [
    aws_cloudwatch_log_group.audit_event_writer,
    aws_iam_role_policy_attachment.audit_event_writer_basic,
    aws_iam_role_policy_attachment.audit_event_writer_vpc,
    aws_iam_role_policy.audit_event_writer
  ]
}

resource "aws_lambda_event_source_mapping" "audit_event_writer" {
  count = local.audit_event_writer_enabled ? 1 : 0

  event_source_arn                   = var.audit_event_queue_arn
  function_name                      = aws_lambda_function.audit_event_writer[0].arn
  batch_size                         = 10
  maximum_batching_window_in_seconds = 5
  function_response_types            = ["ReportBatchItemFailures"]
}

resource "aws_iam_role" "inventory_sale_effect" {
  count = local.inventory_effect_enabled ? 1 : 0

  name               = "${var.name_prefix}-inventory-sale-effect-lambda"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "inventory_sale_effect_basic" {
  count = local.inventory_effect_enabled ? 1 : 0

  role       = aws_iam_role.inventory_sale_effect[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "inventory_sale_effect_vpc" {
  count = local.inventory_effect_enabled ? 1 : 0

  role       = aws_iam_role.inventory_sale_effect[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

data "aws_iam_policy_document" "inventory_sale_effect" {
  statement {
    effect = "Allow"
    actions = [
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:ChangeMessageVisibility"
    ]
    resources = [var.inventory_effect_queue_arn]
  }

  statement {
    effect    = "Allow"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [var.db_password_secret_arn]
  }
}

resource "aws_iam_role_policy" "inventory_sale_effect" {
  count = local.inventory_effect_enabled ? 1 : 0

  name   = "${var.name_prefix}-inventory-sale-effect"
  role   = aws_iam_role.inventory_sale_effect[0].id
  policy = data.aws_iam_policy_document.inventory_sale_effect.json
}

resource "aws_cloudwatch_log_group" "inventory_sale_effect" {
  count = local.inventory_effect_enabled ? 1 : 0

  name              = "/aws/lambda/${var.name_prefix}-inventory-sale-effect"
  retention_in_days = 30

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-inventory-sale-effect-logs"
  })
}

resource "aws_lambda_function" "inventory_sale_effect" {
  count = local.inventory_effect_enabled ? 1 : 0

  function_name = "${var.name_prefix}-inventory-sale-effect"
  role          = aws_iam_role.inventory_sale_effect[0].arn
  runtime       = "java17"
  handler       = "com.msvanegasg.facturaelectronica.inventorylambda.InventorySaleEffectHandler::handleRequest"
  s3_bucket     = var.inventory_sale_effect_s3_bucket
  s3_key        = var.inventory_sale_effect_s3_key
  memory_size   = 512
  timeout       = 30
  publish       = true

  environment {
    variables = {
      INVENTORY_DB_URL                      = var.db_url
      INVENTORY_DB_USERNAME                 = var.db_username
      INVENTORY_DB_PASSWORD_SECRET_ARN      = var.db_password_secret_arn
      INVENTORY_DB_PASSWORD_SECRET_JSON_KEY = "password"
    }
  }

  vpc_config {
    subnet_ids         = var.subnet_ids
    security_group_ids = var.security_group_ids
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-inventory-sale-effect"
  })

  depends_on = [
    aws_cloudwatch_log_group.inventory_sale_effect,
    aws_iam_role_policy_attachment.inventory_sale_effect_basic,
    aws_iam_role_policy_attachment.inventory_sale_effect_vpc,
    aws_iam_role_policy.inventory_sale_effect
  ]
}

resource "aws_lambda_event_source_mapping" "inventory_sale_effect" {
  count = local.inventory_effect_enabled ? 1 : 0

  event_source_arn                   = var.inventory_effect_queue_arn
  function_name                      = aws_lambda_function.inventory_sale_effect[0].arn
  batch_size                         = 10
  maximum_batching_window_in_seconds = 5
  function_response_types            = ["ReportBatchItemFailures"]
}
resource "aws_iam_role" "accounting_sale_entry" {
  count = local.accounting_effect_enabled ? 1 : 0

  name               = "${var.name_prefix}-accounting-sale-entry-lambda"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "accounting_sale_entry_basic" {
  count = local.accounting_effect_enabled ? 1 : 0

  role       = aws_iam_role.accounting_sale_entry[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "accounting_sale_entry_vpc" {
  count = local.accounting_effect_enabled ? 1 : 0

  role       = aws_iam_role.accounting_sale_entry[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

data "aws_iam_policy_document" "accounting_sale_entry" {
  statement {
    effect = "Allow"
    actions = [
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:ChangeMessageVisibility"
    ]
    resources = [var.accounting_effect_queue_arn]
  }

  statement {
    effect    = "Allow"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [var.db_password_secret_arn]
  }
}

resource "aws_iam_role_policy" "accounting_sale_entry" {
  count = local.accounting_effect_enabled ? 1 : 0

  name   = "${var.name_prefix}-accounting-sale-entry"
  role   = aws_iam_role.accounting_sale_entry[0].id
  policy = data.aws_iam_policy_document.accounting_sale_entry.json
}

resource "aws_cloudwatch_log_group" "accounting_sale_entry" {
  count = local.accounting_effect_enabled ? 1 : 0

  name              = "/aws/lambda/${var.name_prefix}-accounting-sale-entry"
  retention_in_days = 30

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-accounting-sale-entry-logs"
  })
}

resource "aws_lambda_function" "accounting_sale_entry" {
  count = local.accounting_effect_enabled ? 1 : 0

  function_name = "${var.name_prefix}-accounting-sale-entry"
  role          = aws_iam_role.accounting_sale_entry[0].arn
  runtime       = "java17"
  handler       = "com.msvanegasg.facturaelectronica.accountinglambda.AccountingSaleEntryHandler::handleRequest"
  s3_bucket     = var.accounting_sale_entry_s3_bucket
  s3_key        = var.accounting_sale_entry_s3_key
  memory_size   = 512
  timeout       = 30
  publish       = true

  environment {
    variables = {
      ACCOUNTING_DB_URL                      = var.db_url
      ACCOUNTING_DB_USERNAME                 = var.db_username
      ACCOUNTING_DB_PASSWORD_SECRET_ARN      = var.db_password_secret_arn
      ACCOUNTING_DB_PASSWORD_SECRET_JSON_KEY = "password"
    }
  }

  vpc_config {
    subnet_ids         = var.subnet_ids
    security_group_ids = var.security_group_ids
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-accounting-sale-entry"
  })

  depends_on = [
    aws_cloudwatch_log_group.accounting_sale_entry,
    aws_iam_role_policy_attachment.accounting_sale_entry_basic,
    aws_iam_role_policy_attachment.accounting_sale_entry_vpc,
    aws_iam_role_policy.accounting_sale_entry
  ]
}

resource "aws_lambda_event_source_mapping" "accounting_sale_entry" {
  count = local.accounting_effect_enabled ? 1 : 0

  event_source_arn                   = var.accounting_effect_queue_arn
  function_name                      = aws_lambda_function.accounting_sale_entry[0].arn
  batch_size                         = 10
  maximum_batching_window_in_seconds = 5
  function_response_types            = ["ReportBatchItemFailures"]
}
resource "aws_iam_role" "provider_submission_retry" {
  count = local.provider_retry_enabled ? 1 : 0

  name               = "${var.name_prefix}-provider-submission-retry-lambda"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "provider_submission_retry_basic" {
  count = local.provider_retry_enabled ? 1 : 0

  role       = aws_iam_role.provider_submission_retry[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "provider_submission_retry_vpc" {
  count = local.provider_retry_enabled ? 1 : 0

  role       = aws_iam_role.provider_submission_retry[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

data "aws_iam_policy_document" "provider_submission_retry" {
  statement {
    effect = "Allow"
    actions = [
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:ChangeMessageVisibility"
    ]
    resources = [var.provider_retry_queue_arn]
  }

  statement {
    effect    = "Allow"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [var.db_password_secret_arn]
  }
}

resource "aws_iam_role_policy" "provider_submission_retry" {
  count = local.provider_retry_enabled ? 1 : 0

  name   = "${var.name_prefix}-provider-submission-retry"
  role   = aws_iam_role.provider_submission_retry[0].id
  policy = data.aws_iam_policy_document.provider_submission_retry.json
}

resource "aws_cloudwatch_log_group" "provider_submission_retry" {
  count = local.provider_retry_enabled ? 1 : 0

  name              = "/aws/lambda/${var.name_prefix}-provider-submission-retry"
  retention_in_days = 30

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-provider-submission-retry-logs"
  })
}

resource "aws_lambda_function" "provider_submission_retry" {
  count = local.provider_retry_enabled ? 1 : 0

  function_name = "${var.name_prefix}-provider-submission-retry"
  role          = aws_iam_role.provider_submission_retry[0].arn
  runtime       = "java17"
  handler       = "com.msvanegasg.facturaelectronica.providerretry.ProviderSubmissionRetryHandler::handleRequest"
  s3_bucket     = var.provider_submission_retry_s3_bucket
  s3_key        = var.provider_submission_retry_s3_key
  memory_size   = 512
  timeout       = 30
  publish       = true

  environment {
    variables = {
      PROVIDER_RETRY_DB_URL                      = var.db_url
      PROVIDER_RETRY_DB_USERNAME                 = var.db_username
      PROVIDER_RETRY_DB_PASSWORD_SECRET_ARN      = var.db_password_secret_arn
      PROVIDER_RETRY_DB_PASSWORD_SECRET_JSON_KEY = "password"
      PROVIDER_RETRY_PROVIDER_BASE_URL           = var.provider_retry_provider_base_url
    }
  }

  vpc_config {
    subnet_ids         = var.subnet_ids
    security_group_ids = var.security_group_ids
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-provider-submission-retry"
  })

  depends_on = [
    aws_cloudwatch_log_group.provider_submission_retry,
    aws_iam_role_policy_attachment.provider_submission_retry_basic,
    aws_iam_role_policy_attachment.provider_submission_retry_vpc,
    aws_iam_role_policy.provider_submission_retry
  ]
}

resource "aws_lambda_event_source_mapping" "provider_submission_retry" {
  count = local.provider_retry_enabled ? 1 : 0

  event_source_arn                   = var.provider_retry_queue_arn
  function_name                      = aws_lambda_function.provider_submission_retry[0].arn
  batch_size                         = 10
  maximum_batching_window_in_seconds = 5
  function_response_types            = ["ReportBatchItemFailures"]
}