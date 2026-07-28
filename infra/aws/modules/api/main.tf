resource "aws_apigatewayv2_api" "this" {
  name          = "${var.name_prefix}-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_headers = ["authorization", "content-type", "idempotency-key", "x-company-id", "x-correlation-id"]
    allow_methods = ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"]
    allow_origins = ["https://*"]
    max_age       = 300
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-api"
  })
}

resource "aws_apigatewayv2_vpc_link" "this" {
  name               = "${var.name_prefix}-vpc-link"
  subnet_ids         = var.subnet_ids
  security_group_ids = var.security_group_ids

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-vpc-link"
  })
}

resource "aws_apigatewayv2_integration" "bff" {
  api_id             = aws_apigatewayv2_api.this.id
  integration_type   = "HTTP_PROXY"
  integration_method = "ANY"
  integration_uri    = var.alb_listener_arn
  connection_type    = "VPC_LINK"
  connection_id      = aws_apigatewayv2_vpc_link.this.id
}

resource "aws_apigatewayv2_route" "proxy" {
  api_id    = aws_apigatewayv2_api.this.id
  route_key = "ANY /{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.bff.id}"
}

resource "aws_apigatewayv2_route" "root" {
  api_id    = aws_apigatewayv2_api.this.id
  route_key = "ANY /"
  target    = "integrations/${aws_apigatewayv2_integration.bff.id}"
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.this.id
  name        = "$default"
  auto_deploy = true

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-default-stage"
  })
}