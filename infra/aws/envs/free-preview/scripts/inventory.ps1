[CmdletBinding()]
param([string]$Region = 'us-east-1')

$ErrorActionPreference = 'Stop'
$tag = 'Name=tag:Project,Values=NexoFiscal'
$instance = aws ec2 describe-instances --filters $tag 'Name=tag:Environment,Values=free-preview' --region $Region --output json | ConvertFrom-Json
$buckets = aws s3api list-buckets --query "Buckets[?starts_with(Name, 'nexofiscal-free-preview-')].Name" --output json | ConvertFrom-Json
$distributions = aws cloudfront list-distributions --query "DistributionList.Items[?Comment=='NexoFiscal temporary Free Preview'].{Id:Id,Domain:DomainName,Enabled:Enabled}" --output json | ConvertFrom-Json

[pscustomobject]@{
    account       = (aws sts get-caller-identity --query Account --output text).Trim()
    instances     = @($instance.Reservations.Instances | Select-Object InstanceId, InstanceType, State, PublicIpAddress)
    buckets       = @($buckets)
    distributions = @($distributions)
} | ConvertTo-Json -Depth 8

