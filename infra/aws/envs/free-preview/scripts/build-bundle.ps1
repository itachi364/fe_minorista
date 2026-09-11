[CmdletBinding()]
param(
    [string]$OutputDirectory = '',
    [switch]$SkipTests
)

$ErrorActionPreference = 'Stop'
$environmentRoot = Split-Path -Parent $PSScriptRoot
$repositoryRoot = (Resolve-Path (Join-Path $environmentRoot '../../../..')).Path
$deploymentRoot = (Resolve-Path (Join-Path $environmentRoot '../../free-preview')).Path
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path $environmentRoot '.artifacts'
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker is required.'
}
docker info | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Docker engine is not available.' }

if (-not $SkipTests) {
    Push-Location $repositoryRoot
    try {
        & .\mvnw.cmd clean verify
        if ($LASTEXITCODE -ne 0) { throw 'Maven verification failed.' }
        Push-Location 'apps/facturaelectronica-web'
        try {
            npm test
            if ($LASTEXITCODE -ne 0) { throw 'Frontend tests failed.' }
            npm run build
            if ($LASTEXITCODE -ne 0) { throw 'Frontend build failed.' }
        }
        finally { Pop-Location }
    }
    finally { Pop-Location }
}

Push-Location $repositoryRoot
try {
    docker build -f infra/aws/free-preview/docker/Dockerfile.platform -t nexofiscal/platform:free-preview .
    if ($LASTEXITCODE -ne 0) { throw 'Platform image build failed.' }
    docker build -f infra/aws/free-preview/docker/Dockerfile.frontend -t nexofiscal/frontend:free-preview .
    if ($LASTEXITCODE -ne 0) { throw 'Frontend image build failed.' }
    docker pull postgres:16-alpine
    if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL image pull failed.' }
}
finally { Pop-Location }

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$stage = Join-Path $OutputDirectory 'bundle-stage'
if (Test-Path $stage) { Remove-Item -Recurse -Force -LiteralPath $stage }
New-Item -ItemType Directory -Path $stage | Out-Null

docker save --output (Join-Path $stage 'images.tar') nexofiscal/platform:free-preview nexofiscal/frontend:free-preview postgres:16-alpine
if ($LASTEXITCODE -ne 0) { throw 'docker save failed.' }
Push-Location $stage
try {
    tar -czf images.tar.gz images.tar
    if ($LASTEXITCODE -ne 0) { throw 'Image compression failed.' }
    Remove-Item -LiteralPath images.tar
    Copy-Item -LiteralPath (Join-Path $deploymentRoot 'compose.preview.yml') -Destination .
    tar -czf ../nexofiscal-preview.tar.gz images.tar.gz compose.preview.yml
    if ($LASTEXITCODE -ne 0) { throw 'Bundle creation failed.' }
}
finally { Pop-Location }

Remove-Item -Recurse -Force -LiteralPath $stage
$bundle = Join-Path $OutputDirectory 'nexofiscal-preview.tar.gz'
$hash = Get-FileHash -Algorithm SHA256 -LiteralPath $bundle
Write-Host "Bundle: $bundle"
Write-Host "SHA256: $($hash.Hash)"

