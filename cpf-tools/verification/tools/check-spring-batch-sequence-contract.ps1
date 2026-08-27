param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path,
    [switch] $RequireOfficialJar
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
$ContractPath = Join-Path $Root "cpf-tools/db/canonical/platform-non-table-objects.json"
$Contract = Get-Content -Raw -Encoding UTF8 -LiteralPath $ContractPath | ConvertFrom-Json -Depth 30
$PlatformSchemaPath = Join-Path $Root "cpf-tools/db/canonical/platform-schema.json"
$PlatformSchema = Get-Content -Raw -Encoding UTF8 -LiteralPath $PlatformSchemaPath | ConvertFrom-Json -Depth 100
$OfficialVendors = @("mariadb", "postgresql", "oracle")
$ExpectedNames = @($Contract.objects.name | ForEach-Object { [string] $_ })
$ExpectedSorted = @($ExpectedNames | Sort-Object -CaseSensitive)
$ExpectedExact = @(
    "BATCH_JOB_EXECUTION_SEQ",
    "BATCH_JOB_INSTANCE_SEQ",
    "BATCH_STEP_EXECUTION_SEQ"
)
$LegacyName = "BATCH_JOB_SEQ"
$Version = [int] $Contract.migration.version
$Description = [string] $Contract.migration.description
$JarVerified = $false
$Failures = [Collections.Generic.List[string]]::new()

function Add-Failure([string] $Message) {
    $script:Failures.Add($Message)
}

function Read-Text([string] $RelativePath) {
    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        Add-Failure "Missing Spring Batch sequence artifact: $RelativePath"
        return ""
    }
    return [IO.File]::ReadAllText($path, [Text.Encoding]::UTF8)
}

function Get-SequenceNames([string] $Sql) {
    return @(
        [regex]::Matches(
            $Sql,
            "(?im)^\s*CREATE\s+SEQUENCE(?:\s+IF\s+NOT\s+EXISTS)?\s+([A-Z][A-Z0-9_]*SEQ)\b"
        ) |
            ForEach-Object { $_.Groups[1].Value.ToUpperInvariant() }
    )
}

function Get-CurrentSequenceNames([object[]] $Objects) {
    $currentLogicalDatabase = [string] $Contract.canonicalPolicy.currentLogicalDatabase
    return @($Objects | ForEach-Object {
        $object = $_
        $matches = @($PlatformSchema.tables | Where-Object {
            [string] $_.currentName -ceq [string] $object.idTable
        })
        if ($matches.Count -ne 1) {
            Add-Failure "Current Spring Batch table mapping must be exact: table=$($object.idTable) count=$($matches.Count)"
            return
        }
        $mapped = $matches[0]
        if ([string] $mapped.logicalDatabase -cne $currentLogicalDatabase) {
            Add-Failure "Current Spring Batch table has wrong logical database: table=$($object.idTable) actual=$($mapped.logicalDatabase)"
            return
        }
        "$([string] $mapped.targetTableName)_SEQ"
    })
}

function Compare-ExactSet([string[]] $Actual, [string[]] $Expected) {
    return (($Actual | Sort-Object -CaseSensitive -Unique) -join "`n") -ceq
        (($Expected | Sort-Object -CaseSensitive -Unique) -join "`n")
}

if ([int] $Contract.schemaVersion -ne 1 -or
        [string] $Contract.contract -cne "CPF_PLATFORM_NON_TABLE_OBJECTS") {
    Add-Failure "Invalid Platform non-table object canonical header."
}
if (-not (Compare-ExactSet @($Contract.canonicalPolicy.officialVendors) $OfficialVendors)) {
    Add-Failure "Official sequence vendors must be exactly mariadb,postgresql,oracle."
}
if (-not (Compare-ExactSet $ExpectedSorted $ExpectedExact)) {
    Add-Failure "Canonical Spring Batch 6.0.4 sequence names are not exact."
}
$ExpectedCurrentNames = @(Get-CurrentSequenceNames @($Contract.objects))
$ExpectedCurrentExact = @(
    "BAT_SB_JOB_EXECUTION_SEQ",
    "BAT_SB_JOB_INSTANCE_SEQ",
    "BAT_SB_STEP_EXECUTION_SEQ"
)
if (-not (Compare-ExactSet $ExpectedCurrentNames $ExpectedCurrentExact) -or
        $ExpectedCurrentNames.Count -ne $ExpectedCurrentExact.Count) {
    Add-Failure "Current Spring Batch sequence names do not match the canonical BAT_SB table mapping."
}
$RetiredCurrentNames = @($ExpectedNames + $LegacyName | Sort-Object -CaseSensitive -Unique)
if ($Version -ne 73 -or $Description -cne "spring_batch_6_sequence_contract") {
    Add-Failure "Spring Batch sequence migration must use the canonical new V73 contract."
}

$syncScript = Join-Path $Root "cpf-tools/verification/tools/sync-platform-non-table-objects.ps1"
& pwsh -NoProfile -File $syncScript -Root $Root -Check
if ($LASTEXITCODE -ne 0) {
    Add-Failure "Canonical non-table object generated artifacts drifted."
}

foreach ($vendor in $OfficialVendors) {
    $source = Read-Text "cpf-tools/db/vendor/$vendor/source/$($Contract.canonicalPolicy.currentSourceFile)"
    $retiredSource = Read-Text "cpf-tools/db/vendor/$vendor/source/35_bat_schema.sql"
    $install = Read-Text "cpf-tools/db/vendor/$vendor/install/00_empty_install.sql"
    $verify = Read-Text "cpf-tools/db/vendor/$vendor/verify/00_verify.sql"
    foreach ($pair in @(@("source", $source), @("install", $install))) {
        $location = [string] $pair[0]
        $sql = [string] $pair[1]
        $names = @(Get-SequenceNames $sql)
        if (-not (Compare-ExactSet $names $ExpectedCurrentNames) -or $names.Count -ne $ExpectedCurrentNames.Count) {
            Add-Failure "Spring Batch sequence name/count drift: vendor=$vendor location=$location actual=$($names -join ',')"
        }
        foreach ($retiredName in $RetiredCurrentNames) {
            if ($sql -match "(?im)^\s*CREATE\s+SEQUENCE(?:\s+IF\s+NOT\s+EXISTS)?\s+$([regex]::Escape($retiredName))\b") {
                Add-Failure "Retired Spring Batch sequence remains in current projection: vendor=$vendor location=$location name=$retiredName"
            }
        }
    }
    $retiredManagedNames = @(Get-SequenceNames $retiredSource | Where-Object {
        $_ -in @($ExpectedCurrentNames + $RetiredCurrentNames)
    })
    if ($retiredManagedNames.Count -gt 0) {
        Add-Failure "Managed Spring Batch sequence remains in retired source: vendor=$vendor actual=$($retiredManagedNames -join ',')"
    }
    if ($verify -notmatch "spring_batch_6_sequence_contract" -or
            @($ExpectedCurrentNames | Where-Object { $verify -notmatch [regex]::Escape($_) }).Count -gt 0 -or
            @($RetiredCurrentNames | Where-Object { $verify -notmatch [regex]::Escape($_) }).Count -gt 0) {
        Add-Failure "Fail-closed Spring Batch sequence verification is incomplete: vendor=$vendor"
    }
    $managedNamespaceFragment = switch ($vendor) {
        "mariadb" { "LEFT(UPPER(table_name), 7) = 'BAT_SB_'" }
        "postgresql" { "LEFT(UPPER(sequence_name), 7) = 'BAT_SB_'" }
        "oracle" { "SUBSTR(sequence_name, 1, 7) = 'BAT_SB_'" }
    }
    if ($verify -notmatch [regex]::Escape($managedNamespaceFragment)) {
        Add-Failure "Spring Batch sequence count is not scoped to its managed namespace: vendor=$vendor"
    }

    $definition = $Contract.vendorDefinition.$vendor
    if ($vendor -ceq "mariadb") {
        $required = @(
            "START WITH $($definition.startWith)",
            "MINVALUE $($definition.minValue)",
            "MAXVALUE $($definition.maxValue)",
            [string] $definition.cache,
            [string] $definition.cycle,
            "ENGINE=$($definition.engine)"
        )
    } elseif ($vendor -ceq "postgresql") {
        $required = @(
            "START WITH $($definition.startWith)",
            "MINVALUE $($definition.minValue)",
            "MAXVALUE $($definition.maxValue)",
            [string] $definition.cycle
        )
    } else {
        $required = @(
            "START WITH $($definition.startWith)",
            "MINVALUE $($definition.minValue)",
            "MAXVALUE $($definition.maxValue)",
            [string] $definition.order,
            [string] $definition.cycle
        )
    }
    foreach ($fragment in $required) {
        if ($source -notmatch [regex]::Escape($fragment)) {
            Add-Failure "Vendor sequence definition drift: vendor=$vendor fragment=$fragment"
        }
    }
}

$mariaSourceMigration = "cpf-tools/db/vendor/mariadb/source/migration/flyway/V${Version}__${Description}.sql"
$mariaRuntimeMigration = "cpf-tools/db/vendor/mariadb/migration/flyway/V${Version}__${Description}.sql"
$mariaSourceRollback = "cpf-tools/db/vendor/mariadb/source/migration/rollback/R${Version}__${Description}.sql"
$mariaRuntimeRollback = "cpf-tools/db/vendor/mariadb/rollback/R${Version}__${Description}.sql"
$mariaMigration = Read-Text $mariaSourceMigration
$mariaRuntime = Read-Text $mariaRuntimeMigration
$mariaRollback = Read-Text $mariaSourceRollback
$mariaRuntimeRollbackText = Read-Text $mariaRuntimeRollback
if ($mariaMigration -cne $mariaRuntime) {
    Add-Failure "MariaDB V73 canonical/runtime migration parity failed."
}
if ($mariaRollback -cne $mariaRuntimeRollbackText) {
    Add-Failure "MariaDB R73 canonical/runtime rollback parity failed."
}
foreach ($fragment in @(
        "NEXT VALUE FOR BATCH_JOB_SEQ",
        "MAX(JOB_INSTANCE_ID)",
        "MAX(JOB_EXECUTION_ID)",
        "MAX(STEP_EXECUTION_ID)",
        "table_type",
        "DROP SEQUENCE BATCH_JOB_SEQ",
        "CREATE SEQUENCE BATCH_JOB_INSTANCE_SEQ"
    )) {
    if ($mariaMigration -notmatch [regex]::Escape($fragment)) {
        Add-Failure "MariaDB safe sequence upgrade fragment missing: $fragment"
    }
}
$exactNextPattern = "(?is)GREATEST\s*\(\s*@cpf_sequence_start\s*,\s*@cpf_observed_next\s*\)"
$migrationCaptureCount = @($Contract.objects).Count +
    @($Contract.objects | ForEach-Object { @($_.legacyNames) }).Count
if ([regex]::Matches($mariaMigration, $exactNextPattern).Count -ne $migrationCaptureCount -or
        $mariaMigration -match "@cpf_observed_next\s*\+") {
    Add-Failure "MariaDB V73 must preserve the exact observed next value without an extra increment."
}
if ([regex]::Matches($mariaRollback, $exactNextPattern).Count -ne 2 -or
        $mariaRollback -match "@cpf_observed_next\s*\+") {
    Add-Failure "MariaDB R73 must preserve the exact observed next value without an extra increment."
}

$pgMigration = Read-Text "cpf-tools/db/vendor/postgresql/migration/flyway/batDB/V${Version}__${Description}.sql"
$pgRollback = Read-Text "cpf-tools/db/vendor/postgresql/rollback/batDB/R${Version}__${Description}.sql"
if ($pgMigration -notmatch "(?i)\bsetval\s*\(" -or
        $pgMigration -notmatch "(?i)\bCREATE\s+SEQUENCE\s+IF\s+NOT\s+EXISTS\b" -or
        $pgMigration -match "(?i)\b(?:ENGINE|NOCACHE|NEXT\s+VALUE\s+FOR)\b") {
    Add-Failure "PostgreSQL V73 sequence migration is not vendor-native/safe."
}
if ($pgRollback -notmatch "(?i)\bDROP\s+SEQUENCE\s+IF\s+EXISTS\b") {
    Add-Failure "PostgreSQL R73 sequence rollback is incomplete."
}

$oracleMigration = Read-Text "cpf-tools/db/vendor/oracle/migration/flyway/batDB/V${Version}__${Description}.sql"
$oracleRollback = Read-Text "cpf-tools/db/vendor/oracle/rollback/batDB/R${Version}__${Description}.sql"
if ($oracleMigration -notmatch "(?i)\buser_sequences\b" -or
        $oracleMigration -notmatch "(?i)\bEXECUTE\s+IMMEDIATE\b" -or
        $oracleMigration -notmatch "(?i)\bORDER\s+NOCYCLE\b" -or
        $oracleMigration -match "(?i)\b(?:ENGINE|IF\s+NOT\s+EXISTS|setval)\b") {
    Add-Failure "Oracle V73 sequence migration is not vendor-native/safe."
}
if ($oracleRollback -notmatch "SQLCODE != -2289") {
    Add-Failure "Oracle R73 sequence rollback does not handle missing sequences safely."
}

$gradleCache = if ([string]::IsNullOrWhiteSpace($env:GRADLE_USER_HOME)) {
    Join-Path $env:USERPROFILE ".gradle"
} else {
    $env:GRADLE_USER_HOME
}
$batchVersion = [string] $Contract.upstreamReference.version
$jarRoot = Join-Path $gradleCache "caches/modules-2/files-2.1/org.springframework.batch/spring-batch-core/$batchVersion"
$jar = if (Test-Path -LiteralPath $jarRoot -PathType Container) {
    Get-ChildItem -LiteralPath $jarRoot -Recurse -File -Filter "spring-batch-core-$batchVersion.jar" |
        Select-Object -First 1
} else {
    $null
}
if ($null -eq $jar) {
    if ($RequireOfficialJar) {
        Add-Failure "Spring Batch $batchVersion official core JAR is not available in the Gradle cache."
    }
} else {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [IO.Compression.ZipFile]::OpenRead($jar.FullName)
    try {
        $sha = [Security.Cryptography.SHA256]::Create()
        try {
            foreach ($vendor in $OfficialVendors) {
                $resourceContract = $Contract.upstreamReference.resources.$vendor
                $resourcePath = [string] $resourceContract.path
                $entry = @($zip.Entries | Where-Object { $_.FullName -ceq $resourcePath })[0]
                if ($null -eq $entry) {
                    Add-Failure "Official Spring Batch schema resource missing from JAR: $resourcePath"
                    continue
                }
                $stream = $entry.Open()
                try {
                    $hash = ([BitConverter]::ToString($sha.ComputeHash($stream))).Replace("-", "").ToLowerInvariant()
                } finally {
                    $stream.Dispose()
                }
                if ($hash -cne [string] $resourceContract.sha256) {
                    Add-Failure "Official Spring Batch schema SHA mismatch: vendor=$vendor"
                }
                $reader = [IO.StreamReader]::new($entry.Open(), [Text.Encoding]::UTF8)
                try {
                    $officialSql = $reader.ReadToEnd()
                } finally {
                    $reader.Dispose()
                }
                if (-not (Compare-ExactSet (Get-SequenceNames $officialSql) $ExpectedNames)) {
                    Add-Failure "Official Spring Batch schema sequence names drifted: vendor=$vendor"
                }
            }
        } finally {
            $sha.Dispose()
        }
    } finally {
        $zip.Dispose()
    }
    $JarVerified = $true
}

if ($Failures.Count -gt 0) {
    $Failures | ForEach-Object { Write-Error $_ }
    throw "Spring Batch sequence contract gate failed: $($Failures.Count) issue(s)."
}

[pscustomobject]@{
    status = "PASS"
    contract = [string] $Contract.contract
    springBatchVersion = $batchVersion
    officialJarVerified = $JarVerified
    sequences = $ExpectedNames
    currentSequences = $ExpectedCurrentNames
    vendors = $OfficialVendors
    migrationVersion = $Version
} | ConvertTo-Json -Depth 5
