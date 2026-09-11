[CmdletBinding()]
param(
    [string]$BundlePath = '',
    [string]$BucketName = 'nexofiscal-free-preview-883425315805',
    [string]$ObjectKey = 'releases/current/nexofiscal-preview.tar.gz',
    [string]$Region = 'us-east-1'
)

$ErrorActionPreference = 'Stop'
$environmentRoot = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($BundlePath)) {
    $BundlePath = Join-Path $environmentRoot '.artifacts/nexofiscal-preview.tar.gz'
}
$resolved = (Resolve-Path -LiteralPath $BundlePath).Path
$account = aws sts get-caller-identity --query Account --output text
if ($LASTEXITCODE -ne 0 -or $account.Trim() -ne '883425315805') {
    throw 'Blocked: wrong AWS account.'
}

aws s3 cp $resolved "s3://$BucketName/$ObjectKey" --sse AES256 --region $Region
if ($LASTEXITCODE -ne 0) { throw 'Artifact upload failed.' }
Write-Host "Published private artifact s3://$BucketName/$ObjectKey"

