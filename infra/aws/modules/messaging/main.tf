resource "aws_cloudwatch_event_bus" "this" {
  name = "${var.name_prefix}-events"

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-events"
  })
}

resource "aws_sqs_queue" "dlq" {
  for_each = var.event_routes

  name                      = "${var.name_prefix}-${each.key}-dlq"
  message_retention_seconds = var.dlq_message_retention_seconds

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-${each.key}-dlq"
  })
}

resource "aws_sqs_queue" "queue" {
  for_each = var.event_routes

  name                      = "${var.name_prefix}-${each.key}"
  message_retention_seconds = var.message_retention_seconds
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq[each.key].arn
    maxReceiveCount     = var.max_receive_count
  })

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-${each.key}"
  })
}

resource "aws_cloudwatch_event_rule" "route" {
  for_each = var.event_routes

  name           = "${var.name_prefix}-${each.key}-route"
  event_bus_name = aws_cloudwatch_event_bus.this.name
  event_pattern = jsonencode({
    "detail-type" = each.value.detail_types
  })

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-${each.key}-route"
  })
}

resource "aws_cloudwatch_event_target" "queue" {
  for_each = var.event_routes

  rule           = aws_cloudwatch_event_rule.route[each.key].name
  event_bus_name = aws_cloudwatch_event_bus.this.name
  target_id      = "${each.key}-queue"
  arn            = aws_sqs_queue.queue[each.key].arn
}

data "aws_iam_policy_document" "queue" {
  for_each = var.event_routes

  statement {
    effect  = "Allow"
    actions = ["sqs:SendMessage"]

    principals {
      type        = "Service"
      identifiers = ["events.amazonaws.com"]
    }

    resources = [aws_sqs_queue.queue[each.key].arn]

    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"
      values   = [aws_cloudwatch_event_rule.route[each.key].arn]
    }
  }
}

resource "aws_sqs_queue_policy" "queue" {
  for_each = var.event_routes

  queue_url = aws_sqs_queue.queue[each.key].id
  policy    = data.aws_iam_policy_document.queue[each.key].json
}