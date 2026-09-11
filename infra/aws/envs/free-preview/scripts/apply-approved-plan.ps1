[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [ValidateSet('883425315805')]
    [string]$ConfirmAccount,
    [Parameter(Mandatory)]
    [switch]$ConfirmApply
)

$ErrorActionPreference = 'Stop'
if (-not $ConfirmApply) { throw 'Explicit -ConfirmApply is required.' }
$environmentRoot = Split-Path -Parent $PSScriptRoot
$planPath = Join-Path $environmentRoot 'free-preview.tfplan'
if (-not (Test-Path -LiteralPath $planPath)) { throw 'Generate and validate free-preview.tfplan first.' }

& (Join-Path $PSScriptRoot 'preflight.ps1') -ExpectedAccountId $ConfirmAccount
if ($LASTEXITCODE -ne 0) { throw 'AWS Free Plan preflight failed.' }
Push-Location $environmentRoot
try {
    terraform apply -input=false $planPath
    if ($LASTEXITCODE -ne 0) { throw 'terraform apply failed.' }
}
finally { Pop-Location }

