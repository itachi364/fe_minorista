[CmdletBinding()]
param(
    [string]$ExpectedAccountId = '883425315805',
    [string]$Region = 'us-east-1',
    [decimal]$MinimumCreditsUsd = 25,
    [int]$MinimumRemainingDays = 30
)

$ErrorActionPreference = 'Stop'

function Invoke-AwsJson {
    param([string[]]$Arguments)

    $raw = & aws @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "AWS CLI failed: $($raw -join [Environment]::NewLine)"
    }
    return ($raw -join [Environment]::NewLine) | ConvertFrom-Json
}

if (-not (Get-Command aws -ErrorAction SilentlyContinue)) {
    throw 'AWS CLI is required.'
}

$identity = Invoke-AwsJson @('sts', 'get-caller-identity', '--output', 'json')
if ($identity.Account -ne $ExpectedAccountId) {
    throw "Blocked: authenticated account $($identity.Account) is not $ExpectedAccountId."
}

$plan = Invoke-AwsJson @('freetier', 'get-account-plan-state', '--region', $Region, '--output', 'json')
if ($plan.accountId -ne $ExpectedAccountId) {
    throw "Blocked: Free Tier API returned account $($plan.accountId)."
}
if ($plan.accountPlanType -ne 'FREE' -or $plan.accountPlanStatus -ne 'ACTIVE') {
    throw "Blocked: account plan is $($plan.accountPlanType)/$($plan.accountPlanStatus), expected FREE/ACTIVE."
}

$remainingCredits = [decimal]$plan.accountPlanRemainingCredits.amount
if ($remainingCredits -lt $MinimumCreditsUsd) {
    throw "Blocked: USD $remainingCredits remaining is below the USD $MinimumCreditsUsd safety threshold."
}

$expiration = [DateTimeOffset]::Parse($plan.accountPlanExpirationDate)
$remainingDays = [math]::Floor(($expiration - [DateTimeOffset]::UtcNow).TotalDays)
if ($remainingDays -lt $MinimumRemainingDays) {
    throw "Blocked: only $remainingDays days remain before Free Plan expiration."
}

$null = Invoke-AwsJson @('freetier', 'get-free-tier-usage', '--region', $Region, '--max-results', '100', '--output', 'json')

[pscustomobject]@{
    accountId     = $identity.Account
    principalArn  = $identity.Arn
    planType      = $plan.accountPlanType
    planStatus    = $plan.accountPlanStatus
    remainingUsd  = $remainingCredits
    expirationUtc = $expiration.ToString('o')
    remainingDays = $remainingDays
    result         = 'PASS'
} | ConvertTo-Json

