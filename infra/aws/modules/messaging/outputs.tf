output "event_bus_name" {
  value = aws_cloudwatch_event_bus.this.name
}

output "event_bus_arn" {
  value = aws_cloudwatch_event_bus.this.arn
}

output "queue_arns" {
  value = { for key, queue in aws_sqs_queue.queue : key => queue.arn }
}

output "dlq_arns" {
  value = { for key, queue in aws_sqs_queue.dlq : key => queue.arn }
}