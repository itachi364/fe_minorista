locals {
  public_service_enabled = contains(keys(var.services), var.public_service_name)
  name_prefix_short      = substr(var.name_prefix, 0, 20)
  public_tg_name         = substr("${local.name_prefix_short}-${var.public_service_name}", 0, 32)
}

resource "aws_ecs_cluster" "this" {
  name = "${var.name_prefix}-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-cluster"
  })
}

resource "aws_ecs_cluster_capacity_providers" "this" {
  cluster_name       = aws_ecs_cluster.this.name
  capacity_providers = ["FARGATE"]

  default_capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 100
  }
}

resource "aws_service_discovery_private_dns_namespace" "this" {
  name        = "${var.name_prefix}.local"
  description = "Private namespace for ECS service discovery"
  vpc         = var.vpc_id

  tags = merge(var.tags, {
    Name = "${var.name_prefix}.local"
  })
}

resource "aws_service_discovery_service" "service" {
  for_each = var.services

  name = each.key

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.this.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-${each.key}-discovery"
  })
}

resource "aws_ecr_repository" "service" {
  for_each = var.services

  name                 = "${var.name_prefix}/${each.key}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-${each.key}"
  })
}

resource "aws_cloudwatch_log_group" "service" {
  for_each = var.services

  name              = "/ecs/${var.name_prefix}/${each.key}"
  retention_in_days = 30

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-${each.key}-logs"
  })
}

data "aws_iam_policy_document" "ecs_tasks_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "execution" {
  name               = "${var.name_prefix}-ecs-execution"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_role.json

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "execution" {
  role       = aws_iam_role.execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

data "aws_iam_policy_document" "execution_secrets" {
  count = length(var.secret_arns) > 0 ? 1 : 0

  statement {
    actions   = ["secretsmanager:GetSecretValue"]
    resources = var.secret_arns
  }
}

resource "aws_iam_role_policy" "execution_secrets" {
  count  = length(var.secret_arns) > 0 ? 1 : 0
  name   = "${var.name_prefix}-ecs-secrets"
  role   = aws_iam_role.execution.id
  policy = data.aws_iam_policy_document.execution_secrets[0].json
}

resource "aws_iam_role" "task" {
  name               = "${var.name_prefix}-ecs-task"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_role.json

  tags = var.tags
}

resource "aws_security_group" "alb" {
  count       = local.public_service_enabled ? 1 : 0
  name        = "${var.name_prefix}-internal-alb-sg"
  description = "Internal ALB security group"
  vpc_id      = var.vpc_id

  ingress {
    description = "HTTP from inside VPC and API Gateway VPC Link ENIs"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-internal-alb-sg"
  })
}

resource "aws_lb" "internal" {
  count              = local.public_service_enabled ? 1 : 0
  name               = "${var.name_prefix}-internal"
  internal           = true
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb[0].id]
  subnets            = var.subnet_ids

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-internal"
  })
}

resource "aws_lb_target_group" "public_service" {
  count       = local.public_service_enabled ? 1 : 0
  name        = local.public_tg_name
  port        = var.services[var.public_service_name].container_port
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = var.vpc_id

  health_check {
    path                = "/actuator/health"
    matcher             = "200"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-${var.public_service_name}-tg"
  })
}

resource "aws_lb_listener" "http" {
  count             = local.public_service_enabled ? 1 : 0
  load_balancer_arn = aws_lb.internal[0].arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.public_service[0].arn
  }
}

resource "aws_security_group_rule" "app_from_alb" {
  count                    = local.public_service_enabled ? 1 : 0
  type                     = "ingress"
  from_port                = var.services[var.public_service_name].container_port
  to_port                  = var.services[var.public_service_name].container_port
  protocol                 = "tcp"
  security_group_id        = var.app_security_group_id
  source_security_group_id = aws_security_group.alb[0].id
  description              = "Allow internal ALB to reach the public BFF service"
}

resource "aws_ecs_task_definition" "service" {
  for_each = var.services

  family                   = "${var.name_prefix}-${each.key}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = each.value.cpu
  memory                   = each.value.memory
  execution_role_arn       = aws_iam_role.execution.arn
  task_role_arn            = aws_iam_role.task.arn

  container_definitions = jsonencode([
    {
      name      = each.key
      image     = coalesce(each.value.container_image, "${aws_ecr_repository.service[each.key].repository_url}:${each.value.image_tag}")
      essential = true
      portMappings = [
        {
          containerPort = each.value.container_port
          hostPort      = each.value.container_port
          protocol      = "tcp"
        }
      ]
      environment = [
        for name, value in each.value.environment : {
          name  = name
          value = value
        }
      ]
      secrets = concat(
        [
          for name, value_from in var.common_secrets : {
            name      = name
            valueFrom = value_from
          }
        ],
        [
          for name, value_from in each.value.secrets : {
            name      = name
            valueFrom = value_from
          }
        ]
      )
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.service[each.key].name
          awslogs-region        = data.aws_region.current.region
          awslogs-stream-prefix = each.key
        }
      }
    }
  ])

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-${each.key}"
  })
}

data "aws_region" "current" {}

resource "aws_ecs_service" "service" {
  for_each = var.services

  name            = each.key
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.service[each.key].arn
  desired_count   = each.value.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = var.subnet_ids
    security_groups  = [var.app_security_group_id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.service[each.key].arn
  }

  dynamic "load_balancer" {
    for_each = each.key == var.public_service_name && local.public_service_enabled ? [1] : []
    content {
      target_group_arn = aws_lb_target_group.public_service[0].arn
      container_name   = each.key
      container_port   = each.value.container_port
    }
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-${each.key}"
  })

  depends_on = [aws_ecs_cluster_capacity_providers.this]
}