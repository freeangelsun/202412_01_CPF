[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path,
    [string] $ProfilePath = "",
    [ValidateSet("fresh-install", "upgrade", "rollback", "reapply", "verify", "runtime-query")]
    [string] $Action = "verify",
    [ValidateSet("baseline", "core", "coreAndBatch")]
    [string] $FromState = "baseline",
    [ValidateSet("baseline", "core", "coreAndBatch")]
    [string] $TargetState = "coreAndBatch",
    [string] $ResultPath = "",
    [switch] $RequireRun,
    [switch] $ConfirmApply,
    [switch] $ConfirmApplicationsStopped,
    [switch] $ConfirmRollbackReady,
    [string] $ExpectedPlanSha256 = "",
    [string[]] $BackupManifestPath = @(),
    [string] $Operator = "",
    [string] $Reason = "",
    [string] $ApprovalReference = ""
)

if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw "CPF Reference DB lifecycle tool은 pwsh 7 이상이 필요합니다."
}

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path

. (Join-Path $PSScriptRoot "database-profile-common.ps1")

function Get-CpfFileSha256 {
    param([Parameter(Mandatory = $true)][string] $Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Get-CpfTextSha256 {
    param([Parameter(Mandatory = $true)][string] $Text)
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try {
        return ([BitConverter]::ToString($algorithm.ComputeHash($Utf8NoBom.GetBytes($Text)))).
            Replace("-", "").ToLowerInvariant()
    } finally {
        $algorithm.Dispose()
    }
}

function Get-CpfRelativePath {
    param([Parameter(Mandatory = $true)][string] $Path)
    return ([IO.Path]::GetRelativePath($Root, [IO.Path]::GetFullPath($Path))).Replace("\", "/")
}

function New-CpfReferenceMigrationProfile {
    param(
        [Parameter(Mandatory = $true)] $SourceProfile,
        [Parameter(Mandatory = $true)][string] $ModuleKey,
        [Parameter(Mandatory = $true)][string] $HistoricalLogicalDatabase
    )
    Assert-CpfIdentifier $HistoricalLogicalDatabase "historicalLogicalDatabase"
    $projected = $SourceProfile | ConvertTo-Json -Depth 60 | ConvertFrom-Json -Depth 60
    $module = $projected.modules.$ModuleKey
    if ($null -eq $module) { throw "Reference migration profile module이 없습니다: $ModuleKey" }
    $module.logicalDatabase = $HistoricalLogicalDatabase
    $path = Join-Path ([IO.Path]::GetTempPath()) ("cpf-reference-migration-profile-" + [guid]::NewGuid().ToString("N") + ".json")
    [IO.File]::WriteAllText($path, ($projected | ConvertTo-Json -Depth 60) + "`n", $Utf8NoBom)
    return $path
}

function Assert-CpfIdentifier {
    param([Parameter(Mandatory = $true)][string] $Value, [string] $Name = "identifier")
    if ($Value -notmatch '^[A-Za-z][A-Za-z0-9_$#]{0,127}$') {
        throw "$Name 값이 안전한 SQL identifier가 아닙니다."
    }
}

function Protect-CpfOutput {
    param([string] $Text, [string[]] $Secrets)
    $safe = if ($null -eq $Text) { "" } else { $Text }
    foreach ($secret in $Secrets) {
        if (-not [string]::IsNullOrWhiteSpace($secret)) {
            $safe = $safe.Replace($secret, "****")
        }
    }
    return $safe
}

function Invoke-CpfReferenceSql {
    param(
        [Parameter(Mandatory = $true)][string] $Vendor,
        [Parameter(Mandatory = $true)] $Target,
        [Parameter(Mandatory = $true)][string] $Sql,
        [Parameter(Mandatory = $true)][string] $Purpose
    )

    $client = if (-not [string]::IsNullOrWhiteSpace([string]$Target.clientPath)) {
        [string]$Target.clientPath
    } else {
        @{ mariadb = "mariadb"; postgresql = "psql"; oracle = "sqlplus" }[$Vendor]
    }
    if (-not [IO.Path]::IsPathRooted($client)) {
        $command = Get-Command $client -ErrorAction SilentlyContinue
        if ($null -eq $command) { throw "DB client를 찾을 수 없습니다: vendor=$Vendor" }
        $client = $command.Source
    } elseif (-not (Test-Path -LiteralPath $client -PathType Leaf)) {
        throw "DB client를 찾을 수 없습니다: vendor=$Vendor"
    }

    $psi = [Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = $client
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow = $true
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.StandardInputEncoding = [Text.Encoding]::UTF8
    $psi.StandardOutputEncoding = [Text.Encoding]::UTF8
    $psi.StandardErrorEncoding = [Text.Encoding]::UTF8

    $inputSql = $Sql
    if ($Vendor -eq "mariadb") {
        foreach ($argument in @(
                "--host=$($Target.host)",
                "--port=$($Target.port)",
                "--user=$($Target.migrationUsername)",
                "--default-character-set=utf8mb4",
                "--binary-mode",
                "--database=$($Target.databaseName)"
            )) {
            [void]$psi.ArgumentList.Add($argument)
        }
        $psi.Environment["MYSQL_PWD"] = [string]$Target.migrationPassword
    } elseif ($Vendor -eq "postgresql") {
        foreach ($argument in @(
                "-X", "-q", "-A", "-t", "-F", "|",
                "--set=ON_ERROR_STOP=1",
                "--host=$($Target.host)",
                "--port=$($Target.port)",
                "--username=$($Target.migrationUsername)",
                "--dbname=$($Target.databaseName)"
            )) {
            [void]$psi.ArgumentList.Add($argument)
        }
        $psi.Environment["PGPASSWORD"] = [string]$Target.migrationPassword
        Assert-CpfIdentifier ([string]$Target.schemaName) "schemaName"
        $schema = ([string]$Target.schemaName).Replace('"', '""')
        $inputSql = 'SET search_path TO "' + $schema + '";' + "`n" + $Sql
    } else {
        foreach ($argument in @("-L", "-S", "/nolog")) {
            [void]$psi.ArgumentList.Add($argument)
        }
        Assert-CpfIdentifier ([string]$Target.schemaName) "schemaName"
        Assert-CpfIdentifier ([string]$Target.migrationUsername) "migrationUsername"
        $escapedPassword = ([string]$Target.migrationPassword).Replace('"', '""')
        $connect = 'CONNECT ' + $Target.migrationUsername + '/"' + $escapedPassword +
            '"@//' + $Target.host + ':' + $Target.port + '/' + $Target.databaseName
        $inputSql = @"
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET HEADING OFF
SET FEEDBACK OFF
SET PAGESIZE 0
SET TRIMSPOOL ON
SET COLSEP |
$connect
ALTER SESSION SET CURRENT_SCHEMA = $($Target.schemaName);
$Sql
EXIT
"@
    }

    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $psi
    try {
        if (-not $process.Start()) {
            throw "DB client process를 시작할 수 없습니다: vendor=$Vendor"
        }
        $process.StandardInput.Write($inputSql)
        $process.StandardInput.Close()
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $stdout = $stdoutTask.GetAwaiter().GetResult()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) {
            $safeError = Protect-CpfOutput (($stderr + "`n" + $stdout).Trim()) @(
                [string]$Target.migrationPassword,
                [string]$Target.runtimePassword
            )
            throw "Reference DB SQL 실행 실패: vendor=$Vendor purpose=$Purpose exit=$($process.ExitCode) output=$safeError"
        }
        return [string]$stdout
    } finally {
        $process.Dispose()
    }
}

function Get-CpfStatePacks {
    param([Parameter(Mandatory = $true)][string] $State)
    $metadata = $contract.expectedSchemaStates.$State
    if ($null -eq $metadata) { throw "알 수 없는 Reference schema state입니다: $State" }
    return @($metadata.enabledPacks | ForEach-Object { [string]$_ })
}

function Get-CpfPackMetadata {
    param([Parameter(Mandatory = $true)][string] $PackName)
    $metadata = $contract.featurePacks.$PackName
    if ($null -eq $metadata) { throw "알 수 없는 Reference feature pack입니다: $PackName" }
    return $metadata
}

function Get-CpfReferenceArtifactPath {
    param(
        [Parameter(Mandatory = $true)][string] $PackName,
        [Parameter(Mandatory = $true)][string] $Role
    )
    $metadata = Get-CpfPackMetadata $PackName
    $relative = [string]$metadata.artifacts.$Role
    if ([string]::IsNullOrWhiteSpace($relative)) {
        throw "Reference lifecycle artifact metadata가 없습니다: pack=$PackName role=$Role"
    }
    $vendorRoot = [IO.Path]::GetFullPath((Join-Path $Root "cpf-tools/db/vendor/$vendor"))
    $path = [IO.Path]::GetFullPath((Join-Path $vendorRoot $relative))
    if (-not ($path.StartsWith($vendorRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase))) {
        throw "Reference lifecycle artifact가 vendor pack 밖을 가리킵니다: $relative"
    }
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Reference lifecycle artifact가 없습니다: $(Get-CpfRelativePath $path)"
    }
    return $path
}

function ConvertTo-CpfCanonicalBindName {
    param([Parameter(Mandatory = $true)][string] $Name)
    return ([regex]::Replace($Name, '(?<=[a-z0-9])(?=[A-Z])', '_')).ToLowerInvariant()
}

function ConvertTo-CpfRuntimeProbeSql {
    param(
        [Parameter(Mandatory = $true)][string] $PackName,
        [Parameter(Mandatory = $true)][string] $Sql
    )
    $metadata = Get-CpfPackMetadata $PackName
    $expected = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($binding in @($metadata.runtimeBindings)) { [void]$expected.Add([string]$binding) }
    $found = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($match in [regex]::Matches($Sql, '(?<!:):([A-Za-z_][A-Za-z0-9_]*)')) {
        [void]$found.Add((ConvertTo-CpfCanonicalBindName $match.Groups[1].Value))
    }
    if (($expected.Count -ne $found.Count) -or @($expected | Where-Object { -not $found.Contains($_) }).Count -gt 0) {
        throw "Runtime query binding이 canonical contract와 다릅니다: pack=$PackName"
    }
    return [regex]::Replace(
        $Sql,
        '(?<!:):([A-Za-z_][A-Za-z0-9_]*)',
        [Text.RegularExpressions.MatchEvaluator]{
            param($match)
            $name = ConvertTo-CpfCanonicalBindName $match.Groups[1].Value
            if (-not $expected.Contains($name)) { throw "허용되지 않은 Runtime bind marker입니다: $name" }
            if ($name -eq "business_key") { return "'*'" }
            return "'__cpf_ref_probe__'"
        })
}

function Get-CpfStateVerifySql {
    param([Parameter(Mandatory = $true)][string] $State)
    $statePacks = Get-CpfStatePacks $State
    $expectedTables = @(
        $statePacks |
            ForEach-Object { @(Get-CpfPackMetadata $_).tables } |
            ForEach-Object { [string]$_ } |
            Sort-Object -Unique
    )
    $sqlList = if ($expectedTables.Count -gt 0) {
        @($expectedTables | ForEach-Object { "'" + $_.Replace("'", "''") + "'" }) -join ","
    } else {
        "''"
    }
    $sentinel = "reference_schema_state_" + $State.ToLowerInvariant()
    if ($vendor -eq "mariadb") {
        return @"
SET @cpf_ref_state_expected_count := (
  SELECT COUNT(*) FROM information_schema.tables
   WHERE table_schema = DATABASE() AND UPPER(table_name) IN ($sqlList)
);
SET @cpf_ref_state_unexpected_count := (
  SELECT COUNT(*) FROM information_schema.tables
   WHERE table_schema = DATABASE()
     AND UPPER(table_name) REGEXP '^CPF_(EDU_|REF_BAT_)'
     AND UPPER(table_name) NOT IN ($sqlList)
);
SET @cpf_ref_state_assert_sql := IF(
  @cpf_ref_state_expected_count = $($expectedTables.Count) AND @cpf_ref_state_unexpected_count = 0,
  'SELECT 1',
  'SIGNAL SQLSTATE ''45000'' SET MESSAGE_TEXT = ''CPF reference schema state mismatch'''
);
PREPARE cpf_ref_state_statement FROM @cpf_ref_state_assert_sql;
EXECUTE cpf_ref_state_statement;
DEALLOCATE PREPARE cpf_ref_state_statement;
SELECT '$sentinel' AS check_name, 1 AS passed;
"@
    }
    if ($vendor -eq "postgresql") {
        return @"
DO `$`$
DECLARE v_expected_count INTEGER; v_unexpected_count INTEGER;
BEGIN
  SELECT COUNT(*) INTO v_expected_count FROM information_schema.tables
   WHERE table_schema = current_schema() AND UPPER(table_name) IN ($sqlList);
  SELECT COUNT(*) INTO v_unexpected_count FROM information_schema.tables
   WHERE table_schema = current_schema()
     AND UPPER(table_name) ~ '^CPF_(EDU_|REF_BAT_)'
     AND UPPER(table_name) NOT IN ($sqlList);
  IF v_expected_count <> $($expectedTables.Count) OR v_unexpected_count <> 0 THEN
    RAISE EXCEPTION 'CPF reference schema state mismatch: expected=% unexpected=%', v_expected_count, v_unexpected_count;
  END IF;
END `$`$;
SELECT '$sentinel' AS check_name, 1 AS passed;
"@
    }
    return @"
DECLARE
  V_EXPECTED_COUNT NUMBER;
  V_UNEXPECTED_COUNT NUMBER;
BEGIN
  SELECT COUNT(*) INTO V_EXPECTED_COUNT FROM USER_TABLES WHERE TABLE_NAME IN ($sqlList);
  SELECT COUNT(*) INTO V_UNEXPECTED_COUNT FROM USER_TABLES
   WHERE REGEXP_LIKE(TABLE_NAME, '^CPF_(EDU_|REF_BAT_)') AND TABLE_NAME NOT IN ($sqlList);
  IF V_EXPECTED_COUNT <> $($expectedTables.Count) OR V_UNEXPECTED_COUNT <> 0 THEN
    RAISE_APPLICATION_ERROR(-20995, 'CPF reference schema state mismatch');
  END IF;
END;
/
SELECT '$sentinel' AS check_name, 1 AS passed FROM DUAL;
"@
}

function Get-CpfRuntimeGrantSql {
    param([string[]] $PackNames)
    $tables = @(
        $PackNames |
            ForEach-Object { @(Get-CpfPackMetadata $_).tables } |
            ForEach-Object { [string]$_ } |
            Sort-Object -Unique
    )
    if ($tables.Count -eq 0 -or $vendor -eq "mariadb") { return "" }
    Assert-CpfIdentifier ([string]$referenceTarget.runtimeUsername) "runtimeUsername"
    foreach ($table in $tables) { Assert-CpfIdentifier $table "table" }
    if ($vendor -eq "postgresql") {
        $runtimeRole = ([string]$referenceTarget.runtimeUsername).Replace('"', '""')
        return "GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE $($tables -join ', ') TO `"$runtimeRole`";"
    }
    $runtimeUser = ([string]$referenceTarget.runtimeUsername).ToUpperInvariant()
    return (@($tables | ForEach-Object { "GRANT SELECT, INSERT, UPDATE, DELETE ON $_ TO $runtimeUser;" }) -join "`n")
}

function Assert-CpfSentinel {
    param([Parameter(Mandatory = $true)][string] $Output, [Parameter(Mandatory = $true)][string] $Sentinel)
    if ($Output.IndexOf($Sentinel, [StringComparison]::OrdinalIgnoreCase) -lt 0) {
        throw "Reference verify sentinel output이 없습니다: $Sentinel"
    }
}

if ([string]::IsNullOrWhiteSpace($ProfilePath)) {
    $ProfilePath = Join-Path $Root "cpf-tools/db/config/database-install.default.json"
} elseif (-not [IO.Path]::IsPathRooted($ProfilePath)) {
    $ProfilePath = Join-Path $Root $ProfilePath
}
$ProfilePath = [IO.Path]::GetFullPath($ProfilePath)
if ([string]::IsNullOrWhiteSpace($ResultPath)) {
    $ResultPath = Join-Path $Root "build/db-lifecycle/reference-database-lifecycle-result.sanitized.json"
} elseif (-not [IO.Path]::IsPathRooted($ResultPath)) {
    $ResultPath = Join-Path $Root $ResultPath
}
$profile = Get-CpfDatabaseProfile $ProfilePath
$contractPath = Join-Path $Root "cpf-tools/generator/contracts/education-reference-fixture-contract.json"
$manifestPath = Join-Path $Root "cpf-tools/db/vendor-pack-manifest.json"
$contract = Get-Content -LiteralPath $contractPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
$vendorManifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
$historicalLogicalDatabases = @(
    $contract.featurePacks.PSObject.Properties |
        ForEach-Object {
            $migrationArtifact = [string]$_.Value.artifacts.migration
            if ($migrationArtifact -notmatch '(?i)(?:^|/)migration/flyway/(?<logicalDatabase>[A-Za-z][A-Za-z0-9_$#]{0,127})/V[0-9]+__') {
                throw "Reference migration artifact에서 historical logical DB를 결정할 수 없습니다: pack=$($_.Name) path=$migrationArtifact"
            }
            [string]$Matches.logicalDatabase
        } |
        Sort-Object -Unique
)
if ($historicalLogicalDatabases.Count -ne 1) {
    throw "Reference feature pack의 historical logical DB가 하나로 수렴하지 않습니다: $($historicalLogicalDatabases -join ', ')"
}
$historicalLogicalDatabase = [string]$historicalLogicalDatabases[0]

$referenceTargets = @(
    $profile.modules.PSObject.Properties |
        ForEach-Object { ConvertTo-CpfModuleProfile $profile ([string]$_.Name) -SkipSecretResolution } |
        Where-Object { $_.enabled -and $_.logicalDatabase -eq [string]$contract.logicalDatabase }
)
if ($referenceTargets.Count -ne 1) {
    throw "Profile에는 enabled REFERENCE_FIXTURE owner가 정확히 하나 있어야 합니다: count=$($referenceTargets.Count)"
}
$referenceTarget = $referenceTargets[0]
$vendor = ([string]$referenceTarget.vendor).ToLowerInvariant()
if ($vendor -notin @($vendorManifest.officialVendors) -or $vendor -notin @($contract.requiredVendors)) {
    throw "Reference lifecycle의 공식 Vendor가 아닙니다: $vendor"
}

$fromPacks = @(Get-CpfStatePacks $FromState)
$targetPacks = @(Get-CpfStatePacks $TargetState)
$addedPacks = @($targetPacks | Where-Object { $_ -notin $fromPacks })
$removedPacks = @($fromPacks | Where-Object { $_ -notin $targetPacks })
if ($Action -in @("upgrade", "reapply") -and ($removedPacks.Count -gt 0 -or $addedPacks.Count -eq 0)) {
    throw "$Action state 방향이 올바르지 않습니다: $FromState -> $TargetState"
}
if ($Action -eq "rollback" -and ($addedPacks.Count -gt 0 -or $removedPacks.Count -eq 0)) {
    throw "rollback state 방향이 올바르지 않습니다: $FromState -> $TargetState"
}

$internalOperations = [Collections.Generic.List[object]]::new()
$order = 0
function Add-CpfFileOperation {
    param([string] $Role, [string] $PackName, [string] $Path, [string] $Executor = "none")
    $script:order++
    $sql = if ($Path.ToLowerInvariant().EndsWith(".sql")) {
        Get-Content -LiteralPath $Path -Raw -Encoding UTF8
    } else { "" }
    $script:internalOperations.Add([pscustomobject]@{
            order = $script:order
            role = $Role
            pack = $PackName
            path = $Path
            fileSha256 = Get-CpfFileSha256 $Path
            renderedSha256 = if ($sql) { Get-CpfTextSha256 $sql } else { "" }
            sql = $sql
            executor = $Executor
            sentinel = ""
        })
}
function Add-CpfRenderedOperation {
    param([string] $Role, [string] $PackName, [string] $Sql, [string] $Sentinel = "")
    if ([string]::IsNullOrWhiteSpace($Sql)) { return }
    $script:order++
    $script:internalOperations.Add([pscustomobject]@{
            order = $script:order
            role = $Role
            pack = $PackName
            path = ""
            fileSha256 = ""
            renderedSha256 = Get-CpfTextSha256 $Sql
            sql = $Sql
            executor = "sql"
            sentinel = $Sentinel
        })
}

$migrationDryRun = $null
$migrationResultPath = ""
if ($Action -in @("upgrade", "rollback", "reapply")) {
    $direction = if ($Action -eq "rollback") { "rollback" } else { "upgrade" }
    $changedPacks = if ($direction -eq "rollback") { $removedPacks } else { $addedPacks }
    $versions = @(
        $changedPacks |
            ForEach-Object { [int](Get-CpfPackMetadata $_).migrationVersion } |
            Sort-Object -Descending:($direction -eq "rollback")
    )
    $migrationResultPath = Join-Path ([IO.Path]::GetTempPath()) ("cpf-ref-migration-" + [guid]::NewGuid().ToString("N") + ".json")
    $migrationProfilePath = New-CpfReferenceMigrationProfile $profile ([string]$referenceTarget.moduleKey) $historicalLogicalDatabase
    try {
        $migrationParameters = @{
            Root = $Root
            ProfilePath = $migrationProfilePath
            Direction = $direction
            MigrationVersion = [int[]]$versions
            Modules = @([string]$referenceTarget.moduleKey)
            ResultPath = $migrationResultPath
            DryRun = $true
        }
        & (Join-Path $PSScriptRoot "invoke-platform-database-migration.ps1") @migrationParameters
        $migrationDryRun = Get-Content -LiteralPath $migrationResultPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
        if ($migrationDryRun.status -eq "실패") { throw "Reference migration dry-run 생성 실패" }
    } finally {
        Remove-Item -LiteralPath $migrationProfilePath -Force -ErrorAction SilentlyContinue
    }
    $grantSql = if ($direction -eq "upgrade") { Get-CpfRuntimeGrantSql $addedPacks } else { "" }
    Add-CpfRenderedOperation "runtime-grant" "state" $grantSql
} elseif ($Action -eq "fresh-install") {
    if ($FromState -ne "baseline") { throw "fresh-install FromState는 baseline이어야 합니다." }
    $initializer = Join-Path $PSScriptRoot "initialize-cpf-database.ps1"
    Add-CpfFileOperation "baseline-initializer" "baseline" $initializer
    $vendorEntry = $vendorManifest.vendors.$vendor
    foreach ($key in @("provision", "emptyInstall", "productSeed", "verify")) {
        $path = Join-Path $Root ([string]$vendorEntry.lifecycle.$key)
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Baseline lifecycle artifact가 없습니다: $path" }
        Add-CpfFileOperation ("baseline-" + $key) "baseline" $path
    }
    foreach ($packName in $targetPacks) {
        Add-CpfFileOperation "overlay-install" $packName (Get-CpfReferenceArtifactPath $packName "install") "sql"
    }
    Add-CpfRenderedOperation "runtime-grant" "state" (Get-CpfRuntimeGrantSql $targetPacks)
}

if ($Action -in @("fresh-install", "upgrade", "rollback", "reapply", "verify")) {
    foreach ($packName in $targetPacks) {
        $verifyPath = Get-CpfReferenceArtifactPath $packName "verify"
        Add-CpfFileOperation "overlay-verify" $packName $verifyPath "sql"
        $internalOperations[$internalOperations.Count - 1].sentinel = "reference_${packName}_schema"
    }
    $stateSentinel = "reference_schema_state_" + $TargetState.ToLowerInvariant()
    Add-CpfRenderedOperation "schema-state-verify" "state" (Get-CpfStateVerifySql $TargetState) $stateSentinel
}
if ($Action -eq "runtime-query") {
    if ($targetPacks.Count -eq 0) { throw "baseline state에는 실행할 Reference runtime query가 없습니다." }
    foreach ($packName in $targetPacks) {
        $runtimePath = Get-CpfReferenceArtifactPath $packName "runtimeQuery"
        $runtimeSql = Get-Content -LiteralPath $runtimePath -Raw -Encoding UTF8
        $renderedRuntime = ConvertTo-CpfRuntimeProbeSql $packName $runtimeSql
        Add-CpfRenderedOperation "runtime-query" $packName $renderedRuntime
        $internalOperations[$internalOperations.Count - 1].path = $runtimePath
        $internalOperations[$internalOperations.Count - 1].fileSha256 = Get-CpfFileSha256 $runtimePath
    }
}

$sanitizedOperations = @(
    $internalOperations | ForEach-Object {
        [ordered]@{
            order = $_.order
            role = $_.role
            pack = $_.pack
            path = if ($_.path) { Get-CpfRelativePath $_.path } else { "canonical-contract#$($_.role)" }
            fileSha256 = $_.fileSha256
            renderedSha256 = $_.renderedSha256
        }
    }
)
$plan = [ordered]@{
    schemaVersion = 1
    tool = "invoke-reference-database-lifecycle.ps1"
    action = $Action
    vendor = $vendor
    fromState = $FromState
    targetState = $TargetState
    moduleKey = [string]$referenceTarget.moduleKey
    logicalDatabase = [string]$referenceTarget.logicalDatabase
    historicalLogicalDatabase = $historicalLogicalDatabase
    physicalDatabase = [string]$referenceTarget.databaseName
    physicalSchema = [string]$referenceTarget.schemaName
    contractSha256 = Get-CpfFileSha256 $contractPath
    migrationPlan = if ($null -ne $migrationDryRun) { $migrationDryRun.plan } else { $null }
    migrationPlanSha256 = if ($null -ne $migrationDryRun) { [string]$migrationDryRun.planSha256 } else { "" }
    operations = $sanitizedOperations
}
$planJson = $plan | ConvertTo-Json -Depth 50 -Compress
$planSha256 = Get-CpfTextSha256 $planJson
$result = [ordered]@{
    schemaVersion = 1
    tool = "invoke-reference-database-lifecycle.ps1"
    mode = if ($RequireRun) { "RUN" } else { "DRY_RUN" }
    status = "미검증"
    generatedAt = (Get-Date).ToString("o")
    profile = [IO.Path]::GetFileName($ProfilePath)
    plan = $plan
    planSha256 = $planSha256
    operations = @($sanitizedOperations | ForEach-Object { [ordered]@{ order = $_.order; role = $_.role; pack = $_.pack; status = "미검증"; outputSha256 = "" } })
    error = ""
}

try {
    if ($RequireRun) {
        if ($ExpectedPlanSha256 -notmatch '^[0-9a-fA-F]{64}$' -or
            $ExpectedPlanSha256.ToLowerInvariant() -ne $planSha256) {
            throw "Dry-run에서 검토한 -ExpectedPlanSha256와 현재 plan이 일치해야 합니다. current=$planSha256"
        }
        if ($Action -in @("fresh-install", "upgrade", "rollback", "reapply")) {
            if (-not $ConfirmApply) { throw "DB 변경 Action에는 -ConfirmApply가 필요합니다." }
            if (-not $ConfirmApplicationsStopped) { throw "DB 변경 Action에는 -ConfirmApplicationsStopped가 필요합니다." }
            if (-not $ConfirmRollbackReady) { throw "DB 변경 Action에는 -ConfirmRollbackReady가 필요합니다." }
        }
        $referenceTarget = ConvertTo-CpfModuleProfile $profile ([string]$referenceTarget.moduleKey)

        if ($Action -eq "fresh-install") {
            $baselineResultDir = Join-Path (Split-Path -Parent $ResultPath) "baseline-install"
            $initializerParameters = @{
                Root = $Root
                ProfilePath = $ProfilePath
                ResultDir = $baselineResultDir
                All = $true
                SeedMode = "product"
                RequireRun = $true
            }
            & (Join-Path $PSScriptRoot "initialize-cpf-database.ps1") @initializerParameters
        } elseif ($Action -in @("upgrade", "rollback", "reapply")) {
            $direction = if ($Action -eq "rollback") { "rollback" } else { "upgrade" }
            $changedPacks = if ($direction -eq "rollback") { $removedPacks } else { $addedPacks }
            $versions = @(
                $changedPacks |
                    ForEach-Object { [int](Get-CpfPackMetadata $_).migrationVersion } |
                    Sort-Object -Descending:($direction -eq "rollback")
            )
            $migrationProfilePath = New-CpfReferenceMigrationProfile $profile ([string]$referenceTarget.moduleKey) $historicalLogicalDatabase
            try {
                $applyParameters = @{
                    Root = $Root
                    ProfilePath = $migrationProfilePath
                    Direction = $direction
                    MigrationVersion = [int[]]$versions
                    Modules = @([string]$referenceTarget.moduleKey)
                    ResultPath = $migrationResultPath
                    Apply = $true
                    ConfirmApply = $true
                    ConfirmApplicationsStopped = $true
                    ConfirmRollbackReady = $true
                    ExpectedPlanSha256 = [string]$migrationDryRun.planSha256
                    BackupManifestPath = $BackupManifestPath
                    Operator = $Operator
                    Reason = $Reason
                    ApprovalReference = $ApprovalReference
                }
                & (Join-Path $PSScriptRoot "invoke-platform-database-migration.ps1") @applyParameters
            } finally {
                Remove-Item -LiteralPath $migrationProfilePath -Force -ErrorAction SilentlyContinue
            }
        }

        for ($index = 0; $index -lt $internalOperations.Count; $index++) {
            $operation = $internalOperations[$index]
            if ($operation.executor -ne "sql") {
                $result.operations[$index].status = if ($operation.role.StartsWith("baseline-")) { "완료" } else { "미검증" }
                continue
            }
            $output = Invoke-CpfReferenceSql $vendor $referenceTarget $operation.sql $operation.role
            if (-not [string]::IsNullOrWhiteSpace([string]$operation.sentinel)) {
                Assert-CpfSentinel $output $operation.sentinel
            }
            $result.operations[$index].status = "완료"
            $result.operations[$index].outputSha256 = Get-CpfTextSha256 $output
        }
        $result.status = "완료"
    }
} catch {
    $result.status = "실패"
    $result.error = Protect-CpfOutput $_.Exception.Message @(
        [string]$referenceTarget.migrationPassword,
        [string]$referenceTarget.runtimePassword,
        [string]$referenceTarget.adminPassword
    )
    throw
} finally {
    if (-not [string]::IsNullOrWhiteSpace($migrationResultPath)) {
        Remove-Item -LiteralPath $migrationResultPath -Force -ErrorAction SilentlyContinue
    }
    $resultDirectory = Split-Path -Parent $ResultPath
    if (-not [string]::IsNullOrWhiteSpace($resultDirectory)) {
        [IO.Directory]::CreateDirectory($resultDirectory) | Out-Null
    }
    [IO.File]::WriteAllText($ResultPath, ($result | ConvertTo-Json -Depth 60) + "`n", $Utf8NoBom)
    Write-Host "Sanitized Reference DB lifecycle result: $ResultPath"
}

if ($RequireRun) {
    Write-Host "CPF Reference DB lifecycle run PASS. action=$Action state=$TargetState planSha256=$planSha256"
} else {
    Write-Host "CPF Reference DB lifecycle dry-run PASS. action=$Action state=$TargetState planSha256=$planSha256"
    Write-Host "실제 DB는 변경하거나 조회하지 않았습니다."
}
