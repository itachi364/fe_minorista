[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [string]$PlanJsonPath
)

$ErrorActionPreference = 'Stop'
$resolvedPath = (Resolve-Path -LiteralPath $PlanJsonPath).Path
$plan = Get-Content -Raw -LiteralPath $resolvedPath | ConvertFrom-Json

$allowedTypes = @(
    'aws_acm_certificate',
    'aws_budgets_budget',
    'aws_budgets_budget_action',
    'aws_cloudfront_distribution',
    'aws_iam_instance_profile',
    'aws_iam_role',
    'aws_iam_role_policy',
    'aws_iam_role_policy_attachment',
    'aws_instance',
    'aws_internet_gateway',
    'aws_route_table',
    'aws_route_table_association',
    'aws_s3_bucket',
    'aws_s3_bucket_lifecycle_configuration',
    'aws_s3_bucket_ownership_controls',
    'aws_s3_bucket_public_access_block',
    'aws_s3_bucket_server_side_encryption_configuration',
    'aws_s3_bucket_versioning',
    'aws_scheduler_schedule',
    'aws_security_group',
    'aws_ssm_parameter',
    'aws_subnet',
    'aws_vpc'
)

$changes = @($plan.resource_changes | Where-Object { $_.mode -eq 'managed' })
$blocked = @($changes | Where-Object { $_.type -notin $allowedTypes })
if ($blocked.Count -gt 0) {
    throw "Blocked resource types: $(($blocked.type | Sort-Object -Unique) -join ', ')"
}

$destructive = @($changes | Where-Object { 'delete' -in $_.change.actions })
if ($destructive.Count -gt 0) {
    throw "Deployment plan contains delete actions: $(($destructive.address) -join ', ')"
}

$instances = @($changes | Where-Object { $_.type -eq 'aws_instance' })
if ($instances.Count -ne 1) {
    throw "Expected exactly one EC2 instance, found $($instances.Count)."
}

$instance = $instances[0].change.after
if ($instance.instance_type -ne 't3.large') {
    throw "Only t3.large is allowed, found $($instance.instance_type)."
}
if ($instance.monitoring -ne $false) {
    throw 'Detailed EC2 monitoring must remain disabled.'
}
if ($instance.credit_specification[0].cpu_credits -ne 'standard') {
    throw 'T3 CPU credits must use standard mode.'
}
if ($instance.root_block_device[0].volume_type -ne 'gp3' -or [int]$instance.root_block_device[0].volume_size -gt 30) {
    throw 'The EC2 root device must be gp3 and no larger than 30 GiB.'
}

$requiredTypes = @(
    'aws_budgets_budget',
    'aws_budgets_budget_action',
    'aws_cloudfront_distribution',
    'aws_scheduler_schedule',
    'aws_ssm_parameter'
)
foreach ($requiredType in $requiredTypes) {
    if (-not ($changes | Where-Object { $_.type -eq $requiredType })) {
        throw "Required guardrail resource is missing: $requiredType."
    }
}

foreach ($change in $changes) {
    $tags = $change.change.after.tags_all
    if ($null -ne $tags -and $tags.Project -ne 'NexoFiscal') {
        throw "Resource $($change.address) is missing the Project=NexoFiscal tag."
    }
}

[pscustomobject]@{
    result         = 'PASS'
    managedChanges = $changes.Count
    resourceTypes  = @($changes.type | Sort-Object -Unique)
    instanceType   = $instance.instance_type
    rootVolumeGiB  = $instance.root_block_device[0].volume_size
} | ConvertTo-Json -Depth 5
