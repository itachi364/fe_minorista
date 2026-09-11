[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [uri]$BaseUrl
)

$ErrorActionPreference = 'Stop'
$health = Invoke-WebRequest -UseBasicParsing -Uri ([uri]::new($BaseUrl, '/healthz')) -TimeoutSec 20
if ($health.StatusCode -ne 200 -or $health.Content.Trim() -ne 'ok') {
    throw 'Edge health check failed.'
}

$sessionStatus = 0
try {
    $session = Invoke-WebRequest -UseBasicParsing -Uri ([uri]::new($BaseUrl, '/api/v1/auth/session')) -TimeoutSec 30
    $sessionStatus = [int]$session.StatusCode
}
catch [Net.WebException] {
    if ($null -eq $_.Exception.Response) { throw }
    $sessionStatus = [int]$_.Exception.Response.StatusCode
}
if ($sessionStatus -ge 500 -or $sessionStatus -eq 0) {
    throw "BFF smoke test returned HTTP $sessionStatus."
}
Write-Host "Smoke test PASS: edge=200, BFF=$sessionStatus"
