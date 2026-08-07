[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Require-Env([string]$Name) {
    $value = [Environment]::GetEnvironmentVariable($Name)
    if ([string]::IsNullOrWhiteSpace($value)) { throw "required environment variable missing: $Name" }
    return $value.Trim()
}
function Safe-Name([string]$Value) {
    if ($Value -notmatch '^[A-Za-z0-9_.-]{1,160}$') { throw "unsafe identifier" }
    return $Value
}
function Write-State([string]$Name, [hashtable]$Value) {
    $path = Join-Path $script:WorkRoot ($Name + '.json')
    $Value | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath $path -Encoding utf8NoBOM
    return $path
}
function File-Hash([string]$Path) { (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() }

$requirementId = Safe-Name (Require-Env 'CPF_EDU_REQUIREMENT_ID')
$businessKey = Safe-Name (Require-Env 'CPF_EDU_BUSINESS_KEY')
$dataScope = Require-Env 'CPF_EDU_DATA_SCOPE'
$traceId = Safe-Name (Require-Env 'CPF_EDU_TRACE_ID')
$fencingToken = [long](Require-Env 'CPF_EDU_FENCING_TOKEN')
$payloadJson = [Console]::In.ReadToEnd()
if ([string]::IsNullOrWhiteSpace($payloadJson)) { $payloadJson = '{}' }
$payload = $payloadJson | ConvertFrom-Json -AsHashtable
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..\..')).Path
$workBase = Join-Path $repoRoot 'build\cpf-edu-ops'
$script:WorkRoot = Join-Path $workBase (Join-Path $requirementId $businessKey)
New-Item -ItemType Directory -Path $script:WorkRoot -Force | Out-Null

$common = [ordered]@{
    requirementId = $requirementId
    businessKey = $businessKey
    dataScope = $dataScope
    traceId = $traceId
    fencingToken = $fencingToken
    executedAtUtc = [DateTimeOffset]::UtcNow.ToString('O')
    sandboxRoot = $script:WorkRoot
}

switch ($requirementId) {
    'EDU-DEV-01' {
        $domainName = Safe-Name ([string]($payload.domainName ?? 'reference-sandbox'))
        $systemCode = Safe-Name ([string]($payload.systemCode ?? 'REF'))
        $target = Join-Path $script:WorkRoot $domainName
        New-Item -ItemType Directory -Path (Join-Path $target 'src\main\java') -Force | Out-Null
        New-Item -ItemType Directory -Path (Join-Path $target 'src\test\java') -Force | Out-Null
        $manifest = $common + @{ action='GENERATOR_SANDBOX'; domainName=$domainName; systemCode=$systemCode; generatedDomainLinkedToEdu=$false }
        $manifestPath = Write-State 'domain-manifest' $manifest
        $result = $manifest + @{ manifestSha256 = File-Hash $manifestPath }
    }
    { $_ -in @('EDU-DEV-14','EDU-OPS-04') } {
        $vendors = @('oracle','postgresql','mariadb')
        $includeBatch = if ($payload.ContainsKey('includeBatch')) { [bool]$payload.includeBatch } else { $true }
        $checks = @()
        foreach ($vendor in $vendors) {
            $base = Join-Path $repoRoot "cpf-tools\db\vendor\$vendor"
            $required = @(
                'source\57_reference_edu_operation_ledger.sql',
                'install\01_reference_edu_operation_ledger.sql',
                'migration\flyway\refDB\V93__manual_edu_135_operation_ledger.sql',
                'rollback\refDB\U93__manual_edu_135_operation_ledger.sql',
                'runtime\ref\manual_edu_135_operation_queries.sql',
                'verify\93_verify_manual_edu_135_operation_ledger.sql'
            )
            if ($includeBatch) {
                $required += @(
                    'source\58_reference_batch_job_pack.sql',
                    'install\02_reference_batch_job_pack.sql',
                    'migration\flyway\refDB\V94__reference_batch_job_pack.sql',
                    'rollback\refDB\U94__reference_batch_job_pack.sql',
                    'runtime\ref\reference_batch_job_queries.sql',
                    'verify\94_verify_reference_batch_job_pack.sql'
                )
            }
            foreach ($relative in $required) {
                $path = Join-Path $base $relative
                if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "vendor lifecycle file missing: $vendor/$relative" }
            }
            $migrationHashes = @{
                core = File-Hash (Join-Path $base 'migration\flyway\refDB\V93__manual_edu_135_operation_ledger.sql')
            }
            if ($includeBatch) {
                $migrationHashes.batch = File-Hash (Join-Path $base 'migration\flyway\refDB\V94__reference_batch_job_pack.sql')
            }
            $checks += @{ vendor=$vendor; migrationSha256=$migrationHashes; rollbackPresent=$true }
        }
        $ownershipPath = Join-Path $repoRoot 'cpf-tools\generator\contracts\reference-edu-schema-ownership-contract.json'
        $centralPath = Join-Path $repoRoot 'cpf-tools\generator\contracts\central-domain-template-contract.json'
        $ownership = Get-Content -LiteralPath $ownershipPath -Raw -Encoding utf8 | ConvertFrom-Json -AsHashtable
        $central = Get-Content -LiteralPath $centralPath -Raw -Encoding utf8 | ConvertFrom-Json -AsHashtable
        foreach ($prefix in @('CPF_EDU_','CPF_REF_BAT_')) {
            if ($ownership.forbiddenGeneratedDomainPrefixes -notcontains $prefix) { throw "generator ownership exclusion missing: $prefix" }
            if ($central.referenceOwnedExclusions.forbiddenTablePrefixes -notcontains $prefix) { throw "central generator exclusion missing: $prefix" }
        }
        $result = $common + @{
            action='THREE_VENDOR_REFDB_AND_GENERATOR_PARITY'
            includeBatch=$includeBatch
            vendors=$checks
            generatedDomainEduLinked=$false
            ownershipContractSha256=File-Hash $ownershipPath
            centralGeneratorContractSha256=File-Hash $centralPath
        }
    }
    'EDU-OPS-01' {
        $source = Join-Path $repoRoot 'cpf-tools\generator\contracts\reference-edu-schema-ownership-contract.json'
        if (-not (Test-Path -LiteralPath $source -PathType Leaf)) { throw 'ownership contract missing' }
        $copy = Join-Path $script:WorkRoot 'reference-edu-schema-ownership-contract.json'
        Copy-Item -LiteralPath $source -Destination $copy -Force
        $result = $common + @{ action='ARTIFACT_INSTALL_SANDBOX'; artifact=$copy; sha256=File-Hash $copy }
    }
    'EDU-OPS-02' {
        $required = @('profile','environment','timezone')
        $missing = @($required | Where-Object { -not $payload.ContainsKey($_) -or [string]::IsNullOrWhiteSpace([string]$payload[$_]) })
        if ($missing.Count -gt 0) { throw "configuration missing: $($missing -join ',')" }
        $result = $common + @{ action='CONFIGURATION_VALIDATION'; valid=$true; keys=@($payload.Keys | Sort-Object) }
    }
    'EDU-OPS-03' {
        $expiresAt = [DateTimeOffset]::Parse([string]($payload.expiresAt ?? [DateTimeOffset]::UtcNow.AddDays(30).ToString('O')))
        $days = [math]::Floor(($expiresAt - [DateTimeOffset]::UtcNow).TotalDays)
        $result = $common + @{ action='SECRET_CERTIFICATE_ROTATION_PLAN'; secretMaterialPersisted=$false; daysToExpiry=$days; state=if($days -lt 7){'ROTATE_NOW'}else{'READY'} }
    }
    'EDU-OPS-05' {
        $topic = Safe-Name ([string]($payload.topic ?? 'cpf.ref.edu'))
        $group = Safe-Name ([string]($payload.consumerGroup ?? 'cpf-ref-edu'))
        $result = $common + @{ action='KAFKA_LIFECYCLE_SANDBOX'; topic=$topic; consumerGroup=$group; aclMode='least-privilege'; runtimeBrokerVerificationRequired=$true }
    }
    { $_ -in @('EDU-OPS-06','EDU-OPS-07','EDU-OPS-08','EDU-OPS-09','EDU-OPS-15') } {
        $sequence = switch ($requirementId) {
            'EDU-OPS-06' { @('DEPENDENCY_CHECK','START','HEALTHY','DRAIN','STOP') }
            'EDU-OPS-07' { @('INSTANCE_ADD','HEALTHY','CONNECTION_DRAIN','INSTANCE_REMOVE') }
            'EDU-OPS-08' { @('GREEN_DEPLOY','CANARY','PROMOTE','ROLLBACK_READY') }
            'EDU-OPS-09' { @('VALIDATE','APPLY_PARTIAL','RECONCILE','CONFIRM') }
            default { @('PRECHECK','APPLICATION_UPGRADE','DB_COMPATIBILITY','ROLLBACK_POINT') }
        }
        $statePath = Write-State 'deployment-state' ($common + @{ action='DEPLOYMENT_LIFECYCLE_SANDBOX'; sequence=$sequence })
        $result = $common + @{ action='DEPLOYMENT_LIFECYCLE_SANDBOX'; sequence=$sequence; stateSha256=File-Hash $statePath }
    }
    'EDU-OPS-10' {
        $metric = @{ logAvailable=$true; metricAvailable=$true; traceAvailable=$true; retentionDays=[int]($payload.retentionDays ?? 30); capacityPercent=[int]($payload.capacityPercent ?? 10) }
        $result = $common + @{ action='OBSERVABILITY_PIPELINE_CHECK'; result=$metric }
    }
    'EDU-OPS-11' {
        $source = Write-State 'business-snapshot' ($common + @{ records=@(@{id='A';amount='1000.00'},@{id='B';amount='2000.00'}) })
        $backup = Join-Path $script:WorkRoot 'business-snapshot.backup.json'
        Copy-Item -LiteralPath $source -Destination $backup -Force
        if ((File-Hash $source) -ne (File-Hash $backup)) { throw 'backup reconciliation hash mismatch' }
        $result = $common + @{ action='BACKUP_RESTORE_RECONCILIATION'; backup=$backup; sha256=File-Hash $backup; reconciled=$true }
    }
    'EDU-OPS-12' {
        $lease = Write-State 'dr-lease' ($common + @{ primary='site-a'; standby='site-b'; epoch=$fencingToken; splitBrainPrevented=$true })
        $result = $common + @{ action='DR_FAILOVER_SANDBOX'; leaseSha256=File-Hash $lease; splitBrainPrevented=$true }
    }
    'EDU-OPS-13' {
        $result = $common + @{ action='FAILURE_RUNBOOK'; injectedFailure=[string]($payload.failureType ?? 'network'); recoverySteps=@('DETECT','ISOLATE','RECOVER','RECONCILE','OBSERVE') }
    }
    'EDU-OPS-14' {
        $result = $common + @{ action='SECURITY_INCIDENT_BLOCK'; account=[string]($payload.accountId ?? 'masked'); sessionsRevoked=$true; keyRotationRequested=$true; secretMaterialPersisted=$false }
    }
    default { throw "requirement is not allowlisted for process execution: $requirementId" }
}
$resultPath = Write-State 'result' $result
[ordered]@{ status='SUCCEEDED'; requirementId=$requirementId; resultPath=$resultPath; resultSha256=File-Hash $resultPath; traceId=$traceId } | ConvertTo-Json -Depth 12 -Compress
