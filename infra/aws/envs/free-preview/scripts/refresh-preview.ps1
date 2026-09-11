[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [string]$InstanceId,
    [string]$Region = 'us-east-1',
    [string]$ExpectedAccountId = '883425315805'
)

$ErrorActionPreference = 'Stop'
$account = aws sts get-caller-identity --query Account --output text
if ($LASTEXITCODE -ne 0 -or $account.Trim() -ne $ExpectedAccountId) {
    throw "Blocked: expected AWS account $ExpectedAccountId."
}

$commandId = aws ssm send-command `
    --instance-ids $InstanceId `
    --document-name AWS-RunShellScript `
    --parameters 'commands=["sudo /usr/local/sbin/nexofiscal-refresh"]' `
    --comment 'Load the approved NexoFiscal free preview bundle' `
    --region $Region `
    --query 'Command.CommandId' `
    --output text
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($commandId)) {
    throw 'Could not submit the SSM refresh command.'
}

aws ssm wait command-executed --command-id $commandId --instance-id $InstanceId --region $Region
if ($LASTEXITCODE -ne 0) {
    throw "SSM command $commandId did not finish successfully."
}

$status = aws ssm get-command-invocation `
    --command-id $commandId `
    --instance-id $InstanceId `
    --region $Region `
    --query 'Status' `
    --output text
if ($LASTEXITCODE -ne 0 -or $status.Trim() -ne 'Success') {
    throw "SSM refresh finished with status $status."
}
Write-Host "Preview refresh completed through SSM command $commandId."
