data "aws_region" "current" {}

resource "aws_cognito_user_pool" "this" {
  name = "${var.name_prefix}-users"

  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]
  mfa_configuration        = "OPTIONAL"

  account_recovery_setting {
    recovery_mechanism {
      name     = "verified_email"
      priority = 1
    }
  }

  software_token_mfa_configuration {
    enabled = true
  }

  password_policy {
    minimum_length                   = 12
    require_lowercase                = true
    require_numbers                  = true
    require_symbols                  = true
    require_uppercase                = true
    temporary_password_validity_days = 7
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-users"
  })
}

resource "aws_cognito_user_pool_client" "web" {
  name         = "${var.name_prefix}-web"
  user_pool_id = aws_cognito_user_pool.this.id

  generate_secret                      = false
  callback_urls                        = var.callback_urls
  logout_urls                          = var.logout_urls
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_scopes                 = ["openid", "email", "profile"]
  supported_identity_providers         = ["COGNITO"]
  prevent_user_existence_errors        = "ENABLED"

  access_token_validity  = 15
  id_token_validity      = 15
  refresh_token_validity = 8

  token_validity_units {
    access_token  = "minutes"
    id_token      = "minutes"
    refresh_token = "hours"
  }
}

resource "aws_cognito_user_pool_domain" "this" {
  domain       = replace("${var.name_prefix}-auth", "_", "-")
  user_pool_id = aws_cognito_user_pool.this.id
}

resource "aws_cognito_user_group" "root" {
  name         = "ROOT"
  user_pool_id = aws_cognito_user_pool.this.id
  description  = "Global platform administration role. Requires MFA policy enforcement."
  precedence   = 1
}

resource "aws_cognito_user_group" "company_admin" {
  name         = "COMPANY_ADMIN"
  user_pool_id = aws_cognito_user_pool.this.id
  description  = "Company administrator baseline role. Requires MFA policy enforcement."
  precedence   = 10
}

resource "aws_cognito_user_group" "seller" {
  name         = "SELLER"
  user_pool_id = aws_cognito_user_pool.this.id
  description  = "Point-of-sale user role."
  precedence   = 50
}

resource "aws_cognito_user_group" "accountant" {
  name         = "ACCOUNTANT"
  user_pool_id = aws_cognito_user_pool.this.id
  description  = "Accounting and reporting user role."
  precedence   = 60
}
