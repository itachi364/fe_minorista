param(
    [string] $SonarHostUrl = "http://localhost:9000",
    [switch] $SkipMavenCoverage,
    [switch] $SkipFrontendCoverage,
    [switch] $SkipAnalysis
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $root

if (-not $env:SONAR_TOKEN -and -not $SkipAnalysis) {
    throw "Define SONAR_TOKEN antes de ejecutar el analisis. Ejemplo: `$env:SONAR_TOKEN='tu_token'"
}

if (-not $SkipMavenCoverage) {
    Write-Host "Generando cobertura Java con Maven/JaCoCo..."
    & .\mvnw.cmd clean verify
}

if (-not $SkipFrontendCoverage) {
    Write-Host "Generando cobertura frontend con Vitest/LCOV..."
    Push-Location .\apps\facturaelectronica-web
    try {
        & npm run coverage
    } finally {
        Pop-Location
    }
}

if (-not $SkipAnalysis) {
    $scanner = Join-Path $root "apps\facturaelectronica-web\node_modules\.bin\sonar-scanner-npm.cmd"
    if (-not (Test-Path $scanner)) {
        throw "No se encontro sonar-scanner-npm. Ejecuta `npm install` en apps/facturaelectronica-web."
    }

    Write-Host "Ejecutando analisis SonarQube en $SonarHostUrl..."
    & $scanner "-Dsonar.host.url=$SonarHostUrl" "-Dsonar.token=$env:SONAR_TOKEN"
}
