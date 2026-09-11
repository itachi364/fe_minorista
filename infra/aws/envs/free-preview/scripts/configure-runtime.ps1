[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [string]$RuntimeEnvPath,
    [string]$ParameterName = '/nexofiscal/free-preview/runtime-env',
    [string]$Region = 'us-east-1',
    [string]$ExpectedAccountId = '883425315805'
)

$ErrorActionPreference = 'Stop'
$resolved = (Resolve-Path -LiteralPath $RuntimeEnvPath).Path
$content = Get-Content -Raw -LiteralPath $resolved
if ($content -match 'change_me|CONFIGURE_BEFORE_START') {
    throw 'Runtime env contains a blocked placeholder.'
}
if ([Text.Encoding]::UTF8.GetByteCount($content) -gt 4096) {
    throw 'Runtime env exceeds the 4 KiB Standard Parameter limit.'
}

$account = aws sts get-caller-identity --query Account --output text
if ($LASTEXITCODE -ne 0 -or $account.Trim() -ne $ExpectedAccountId) {
    throw "Blocked: expected AWS account $ExpectedAccountId."
}

aws ssm put-parameter --name $ParameterName --type SecureString --overwrite --value "file://$resolved" --region $Region | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'SSM runtime parameter update failed.' }
Write-Host "Updated SecureString $ParameterName without printing its value."

