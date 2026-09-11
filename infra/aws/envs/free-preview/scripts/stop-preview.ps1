[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [string]$InstanceId,
    [string]$Region = 'us-east-1'
)

$ErrorActionPreference = 'Stop'
$account = aws sts get-caller-identity --query Account --output text
if ($LASTEXITCODE -ne 0 -or $account.Trim() -ne '883425315805') {
    throw 'Blocked: wrong AWS account.'
}
aws ec2 stop-instances --instance-ids $InstanceId --region $Region | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'EC2 stop failed.' }
aws ec2 wait instance-stopped --instance-ids $InstanceId --region $Region
if ($LASTEXITCODE -ne 0) { throw 'Timed out waiting for the instance to stop.' }
Write-Host "Instance $InstanceId is stopped."

