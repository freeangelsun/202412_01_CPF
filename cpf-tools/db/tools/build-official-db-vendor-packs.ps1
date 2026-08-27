param(
    [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path
)
$ErrorActionPreference = "Stop"
$official = @("mariadb", "postgresql", "oracle")
$planPath = Join-Path $Root "cpf-tools/db/config/database-source-plan.json"
$plan = Get-Content -LiteralPath $planPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 30
$profilePath = Join-Path $Root "cpf-tools/db/config/database-install.default.json"
$profile = Get-Content -LiteralPath $profilePath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 30
$canonicalSchemaPath = Join-Path $Root 'cpf-tools/db/canonical/platform-schema.json'
$canonicalSchema = Get-Content -LiteralPath $canonicalSchemaPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
$referenceFixtureDatabase = [string]$canonicalSchema.canonicalPolicy.platformDatabaseArchitecture.REFERENCE_FIXTURE.physicalName
if ([string]::IsNullOrWhiteSpace($referenceFixtureDatabase)) {
    throw "Canonical REFERENCE_FIXTURE physicalName is required: $canonicalSchemaPath"
}
$logicalDatabases = @(
    $profile.modules.PSObject.Properties |
        Where-Object { [bool]$_.Value.enabled } |
        ForEach-Object { [string]$_.Value.logicalDatabase } |
        Sort-Object -Unique
)
if ($logicalDatabases.Count -eq 0) { throw "Enabled platform database가 없습니다: $profilePath" }
$historicalLogicalDatabases = @(
    $logicalDatabases | Where-Object { [string]$_ -cne $referenceFixtureDatabase }
)
$qualifiedDatabasePattern = '(?i)\b(?:' +
    (($logicalDatabases | ForEach-Object { [regex]::Escape($_) }) -join '|') +
    ')\.'

# Seed aggregate source/lifecycle files have one canonical writer. This builder
# consumes those files but must never rewrite them with a second formatter.
$seedSync = Join-Path $Root 'cpf-tools/db/tools/sync-canonical-seed-bundles.py'
& python -B $seedSync --root $Root --check
if ($LASTEXITCODE -ne 0) {
    throw 'Canonical Seed bundles are missing or stale. Run sync-database-artifacts.ps1 before assembling vendor packs.'
}

function Read-Source([string]$vendor, [string]$name) {
    $path = Join-Path $Root "cpf-tools/db/vendor/$vendor/source/$name"
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Vendor source missing: $path" }
    return Get-Content -LiteralPath $path -Raw -Encoding UTF8
}
function Write-Utf8([string]$path, [string]$text) {
    $dir = Split-Path -Parent $path
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
    [IO.File]::WriteAllText($path, ($text.TrimEnd()+"`n"), [Text.UTF8Encoding]::new($false))
}
function Build-Bundle([string]$vendor, [object[]]$names, [string]$target) {
    $body = "-- CPF generated lifecycle bundle; vendor=$vendor`n-- Source plan: cpf-tools/db/config/database-source-plan.json`n"
    foreach ($name in $names) {
        $body += "`n-- ===== BEGIN $name =====`n" + (Read-Source $vendor $name) + "`n-- ===== END $name =====`n"
    }
    Write-Utf8 $target $body
}
function Get-LogicalSections([string]$text) {
    $pattern = '(?im)^\s*--\s*CPF_LOGICAL_DATABASE=([A-Za-z][A-Za-z0-9_$#]*)\s*$'
    $matches = [regex]::Matches($text, $pattern)
    $result = @()
    for ($i=0; $i -lt $matches.Count; $i++) {
        $start = $matches[$i].Index
        $end = if ($i + 1 -lt $matches.Count) { $matches[$i+1].Index } else { $text.Length }
        $result += [pscustomobject]@{ logicalDatabase=$matches[$i].Groups[1].Value; text=$text.Substring($start, $end-$start) }
    }
    return $result
}
function Assert-Portable([string]$vendor, [string]$text, [string]$source) {
    $structuralText = [regex]::Replace($text, "'(?:''|[^'])*'", "''")
    $structuralText = [regex]::Replace($structuralText, '(?m)--.*$', '')
    if ($vendor -ne 'mariadb' -and $structuralText -match '(?im)^\s*USE\s+') { throw "$vendor source has MariaDB USE directive: $source" }
    if ($vendor -ne 'mariadb' -and
            ($structuralText -match $qualifiedDatabasePattern -or
             $structuralText -match '(?i)\b[a-z][a-z0-9_]*DB\.')) {
        throw "$vendor source has logical database qualifier: $source"
    }
    if ($vendor -eq 'postgresql' -and $structuralText -match '(?i)\b(AUTO_INCREMENT|ON\s+DUPLICATE\s+KEY|IFNULL\s*\(|DATE_ADD\s*\()') { throw "PostgreSQL source has non-native SQL: $source" }
    if ($vendor -eq 'oracle' -and $structuralText -match '(?i)\b(AUTO_INCREMENT|ON\s+DUPLICATE\s+KEY|IFNULL\s*\(|DATE_ADD\s*\(|LIMIT\s+\d+)') { throw "Oracle source has non-native SQL: $source" }
}

$maria = $plan.mariadb
foreach ($vendor in @('postgresql','oracle')) {
    # PowerShell variable names are case-insensitive.  Using `$root` here
    # overwrote the `$Root` repository parameter and made the second path
    # segment repeat (vendor/postgresql/cpf-tools/db/vendor/postgresql/...).
    $vendorRoot = Join-Path $Root "cpf-tools/db/vendor/$vendor"
    foreach ($name in @($maria.emptyInstallFiles + $maria.productSeedFiles + $maria.optionalSampleSeedFiles + $maria.testSeedFiles)) {
        Assert-Portable $vendor (Read-Source $vendor $name) $name
    }
    Build-Bundle $vendor @('00_provision.sql') (Join-Path $vendorRoot 'provision/00_provision.sql')
    Build-Bundle $vendor $maria.emptyInstallFiles (Join-Path $vendorRoot 'install/00_empty_install.sql')
    Build-Bundle $vendor @('00_verify.sql') (Join-Path $vendorRoot 'verify/00_verify.sql')

    # source/00_*_seed.sql and seed/00_*_seed.sql were already proven against
    # canonical JSON by sync-canonical-seed-bundles.py --check above.
    # source/00_verify.sql is the canonical generated input. It is written by
    # generate-official-db-vendor-source.ps1 and then enriched by the dedicated
    # non-table-object renderer. Never use that file as both Build-Bundle input
    # and output: doing so nests its managed block inside a lifecycle wrapper
    # and breaks the subsequent canonical -Check. The wrapped consumer lives
    # only at verify/00_verify.sql above.

    # V63/R63 are published history. Fresh-install source may evolve, but an
    # artifact rebuild must never rewrite an existing baseline migration.
    $allSchema = ($maria.emptyInstallFiles | ForEach-Object { Read-Source $vendor $_ }) -join "`n"
    $sections = Get-LogicalSections $allSchema
    foreach ($group in ($sections | Group-Object logicalDatabase)) {
        $db = $group.Name
        if ($historicalLogicalDatabases -cnotcontains $db) {
            Write-Host "[SKIPPED] Current-snapshot-only logical database has no independent historical pack: vendor=$vendor logicalDatabase=$db"
            continue
        }
        $body = ($group.Group | ForEach-Object text) -join "`n"
        $migrationDirectory = Join-Path $vendorRoot "migration/flyway/$db"
        $migration = Join-Path $migrationDirectory 'V63__cpf_vendor_baseline.sql'
        $baselineRollback = Join-Path $vendorRoot "rollback/$db/R63__cpf_vendor_baseline.sql"
        $publishedManifest = Join-Path $migrationDirectory 'checksums.sha256'
        $existingVersioned = if (Test-Path -LiteralPath $migrationDirectory -PathType Container) {
            @(Get-ChildItem -LiteralPath $migrationDirectory -File -Filter 'V*.sql')
        } else { @() }
        $isUnpublishedEmptyPack = $existingVersioned.Count -eq 0 -and
            -not (Test-Path -LiteralPath $publishedManifest -PathType Leaf)
        if (-not (Test-Path -LiteralPath $migration -PathType Leaf) -and $isUnpublishedEmptyPack) {
            Write-Utf8 $migration ("-- CPF $vendor initial baseline for $db`n" + $body)
            $tableNames = [regex]::Matches($body, '(?im)^\s*CREATE\s+TABLE\s+([A-Za-z][A-Za-z0-9_$#]*)') | ForEach-Object { $_.Groups[1].Value }
            [array]::Reverse($tableNames)
            $drop = if ($vendor -eq 'postgresql') {
                ($tableNames | ForEach-Object { "DROP TABLE IF EXISTS $_ CASCADE;" }) -join "`n"
            } else {
                ($tableNames | ForEach-Object { "BEGIN EXECUTE IMMEDIATE 'DROP TABLE $_ CASCADE CONSTRAINTS PURGE'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;`n/" }) -join "`n"
            }
            Write-Utf8 $baselineRollback ("-- Exact rollback of CPF $vendor initial baseline for $db`n" + $drop)
        } elseif (-not (Test-Path -LiteralPath $migration -PathType Leaf)) {
            Write-Host "[SKIPPED] Refusing historical V63/R63 backfill into published pack: vendor=$vendor logicalDatabase=$db"
        }
    }
}
Write-Host "Official DB vendor lifecycle bundles generated for PostgreSQL and Oracle."
