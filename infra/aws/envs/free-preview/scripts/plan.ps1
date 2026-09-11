[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [string]$BudgetAlertEmail,
    [switch]$EnableCustomDomain
)

$ErrorActionPreference = 'Stop'
$environmentRoot = Split-Path -Parent $PSScriptRoot
$planPath = Join-Path $environmentRoot 'free-preview.tfplan'
$planJsonPath = Join-Path $environmentRoot '.artifacts/free-preview-plan.json'

& (Join-Path $PSScriptRoot 'preflight.ps1')
if ($LASTEXITCODE -ne 0) { throw 'AWS Free Plan preflight failed.' }

New-Item -ItemType Directory -Force -Path (Split-Path -Parent $planJsonPath) | Out-Null
Push-Location $environmentRoot
try {
    terraform init -backend=false
    if ($LASTEXITCODE -ne 0) { throw 'terraform init failed.' }
    terraform fmt -check -recursive
    if ($LASTEXITCODE -ne 0) { throw 'terraform fmt check failed.' }
    terraform validate
    if ($LASTEXITCODE -ne 0) { throw 'terraform validate failed.' }

    $customDomainValue = $EnableCustomDomain.IsPresent.ToString().ToLowerInvariant()
    terraform plan -input=false -out $planPath -var "budget_alert_email=$BudgetAlertEmail" -var "enable_custom_domain=$customDomainValue"
    if ($LASTEXITCODE -ne 0) { throw 'terraform plan failed.' }

    terraform show -json $planPath | Set-Content -Encoding utf8 -LiteralPath $planJsonPath
    if ($LASTEXITCODE -ne 0) { throw 'terraform show failed.' }
}
finally {
    Pop-Location
}

& (Join-Path $PSScriptRoot 'validate-plan.ps1') -PlanJsonPath $planJsonPath
if ($LASTEXITCODE -ne 0) { throw 'Terraform plan policy failed.' }

Write-Host "Validated plan: $planPath"
Write-Host 'No AWS resource was created. Apply requires a separate explicit confirmation.'

