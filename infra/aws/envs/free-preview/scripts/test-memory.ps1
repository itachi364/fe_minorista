[CmdletBinding()]
param(
    [int]$StabilitySeconds = 180,
    [int]$StartupTimeoutSeconds = 600,
    [int]$MaximumTotalMemoryMiB = 6500
)

$ErrorActionPreference = 'Stop'
$environmentRoot = Split-Path -Parent $PSScriptRoot
$deploymentRoot = (Resolve-Path (Join-Path $environmentRoot '../../free-preview')).Path
$templatePath = Join-Path $environmentRoot 'runtime.env.example'
$temporaryEnv = Join-Path $environmentRoot '.artifacts/runtime-memory-test.env'
$composePath = Join-Path $deploymentRoot 'compose.preview.yml'

function New-RandomBase64 {
    param([int]$ByteCount)
    $bytes = New-Object byte[] $ByteCount
    $generator = [Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $generator.GetBytes($bytes)
        return [Convert]::ToBase64String($bytes)
    }
    finally {
        $generator.Dispose()
    }
}

New-Item -ItemType Directory -Force -Path (Split-Path -Parent $temporaryEnv) | Out-Null
$secretA = New-RandomBase64 -ByteCount 32
$secretB = New-RandomBase64 -ByteCount 32
$content = Get-Content -Raw -LiteralPath $templatePath
$content = $content -replace 'POSTGRES_PASSWORD=change_me', "POSTGRES_PASSWORD=$secretA"
$content = $content -replace 'DB_PASSWORD=change_me', "DB_PASSWORD=$secretA"
$content = $content -replace 'BFF_SESSION_ENCRYPTION_KEY=change_me', "BFF_SESSION_ENCRYPTION_KEY=$secretB"
$content = $content -replace 'IDENTITY_ROOT_USER_PASSWORD=change_me', 'IDENTITY_ROOT_USER_PASSWORD=MemoryTest#2026!'
$content = $content -replace 'TENANT_FILES_DOWNLOAD_TOKEN_SECRET=change_me', "TENANT_FILES_DOWNLOAD_TOKEN_SECRET=$secretB"
$content = $content -replace 'PREVIEW_HTTP_PORT=80', 'PREVIEW_HTTP_PORT=18080'
[IO.File]::WriteAllText($temporaryEnv, $content, (New-Object Text.UTF8Encoding $false))
$env:PREVIEW_RUNTIME_ENV_FILE = $temporaryEnv

function Convert-ToMiB {
    param([string]$Value)
    if ($Value -notmatch '^([0-9.]+)([KMG]iB)$') { throw "Unsupported Docker memory value: $Value" }
    $number = [double]$Matches[1]
    switch ($Matches[2]) {
        'KiB' { return $number / 1024 }
        'MiB' { return $number }
        'GiB' { return $number * 1024 }
    }
}

try {
    docker compose --env-file $temporaryEnv -f $composePath up -d
    if ($LASTEXITCODE -ne 0) { throw 'Preview Compose startup failed.' }

    $startupDeadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
    do {
        Start-Sleep -Seconds 10
        $rows = @(docker compose --env-file $temporaryEnv -f $composePath ps --format json | ForEach-Object { $_ | ConvertFrom-Json })
        $unhealthy = @($rows | Where-Object { $_.Health -eq 'unhealthy' -or $_.State -ne 'running' })
        if ($unhealthy.Count -gt 0) {
            throw "A preview container failed: $(($unhealthy.Name) -join ', ')"
        }
        $notReady = @($rows | Where-Object { $_.Health -and $_.Health -ne 'healthy' })
        if ((Get-Date) -ge $startupDeadline -and $notReady.Count -gt 0) {
            throw "Preview containers did not become healthy within $StartupTimeoutSeconds seconds: $(($notReady.Name) -join ', ')"
        }
    } while ($notReady.Count -gt 0)

    Start-Sleep -Seconds $StabilitySeconds
    $rows = @(docker compose --env-file $temporaryEnv -f $composePath ps --format json | ForEach-Object { $_ | ConvertFrom-Json })
    $failed = @($rows | Where-Object { $_.Health -eq 'unhealthy' -or $_.State -ne 'running' })
    if ($failed.Count -gt 0) {
        throw "A preview container failed during the stability window: $(($failed.Name) -join ', ')"
    }

    $usageRows = @(docker stats --no-stream --format '{{json .}}' | ForEach-Object { $_ | ConvertFrom-Json })
    $previewNames = @($rows.Name)
    $totalMiB = 0.0
    foreach ($usage in $usageRows | Where-Object { $_.Name -in $previewNames }) {
        $used = ($usage.MemUsage -split ' / ')[0].Trim()
        $totalMiB += Convert-ToMiB $used
    }
    if ($totalMiB -gt $MaximumTotalMemoryMiB) {
        throw "Memory gate failed: $([math]::Round($totalMiB, 2)) MiB exceeds $MaximumTotalMemoryMiB MiB."
    }

    $response = Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:18080/healthz' -TimeoutSec 10
    if ($response.StatusCode -ne 200) { throw 'Frontend health endpoint failed.' }
    Write-Host "Memory gate PASS: $([math]::Round($totalMiB, 2)) MiB across $($previewNames.Count) containers."
}
catch {
    Write-Warning 'Preview validation failed. Capturing container state and recent logs before cleanup.'
    docker compose --env-file $temporaryEnv -f $composePath ps --all
    docker compose --env-file $temporaryEnv -f $composePath logs --no-color --tail 80
    throw
}
finally {
    docker compose --env-file $temporaryEnv -f $composePath down --volumes
    Remove-Item -Force -LiteralPath $temporaryEnv -ErrorAction SilentlyContinue
    Remove-Item Env:PREVIEW_RUNTIME_ENV_FILE -ErrorAction SilentlyContinue
}
