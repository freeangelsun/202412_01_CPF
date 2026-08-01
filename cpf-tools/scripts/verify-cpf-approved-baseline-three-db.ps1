[CmdletBinding()]
param(
    [string]$Root = (Get-Location).Path,
    [Parameter(Mandatory)][string]$MariaDbProfile,
    [Parameter(Mandatory)][string]$PostgreSqlProfile,
    [Parameter(Mandatory)][string]$OracleProfile,
    [Parameter(Mandatory)][string]$MariaDbUpgradeProfile,
    [Parameter(Mandatory)][string]$PostgreSqlUpgradeProfile,
    [Parameter(Mandatory)][string]$OracleUpgradeProfile,
    [Parameter(Mandatory)][string[]]$BackupManifestPath,
    [string]$EvidenceRoot = '',
    [int[]]$MigrationVersions = @(),
    [switch]$AllowDestructiveRollback
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$rootPath = (Resolve-Path -LiteralPath $Root).Path
$metadataPath = Join-Path $rootPath 'cpf-tools/db/metadata/CPF_BASELINE_MIGRATION_CHECKSUMS_B894157.json'
if (-not (Test-Path -LiteralPath $metadataPath -PathType Leaf)) { throw "Approved baseline metadata missing: $metadataPath" }
if (-not $AllowDestructiveRollback) { throw 'Three-DB lifecycle requires explicit -AllowDestructiveRollback.' }
if ($BackupManifestPath.Count -eq 0) { throw 'BackupManifestPath is required.' }

$sourceSha = (& git -C $rootPath rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $sourceSha -notmatch '^[0-9a-f]{40}$') { throw 'exact source SHA resolution failed' }
$sourceBranch = (& git -C $rootPath branch --show-current).Trim()
$dirtyBefore = @(& git -C $rootPath status --porcelain=v1 --untracked-files=all)
if ($LASTEXITCODE -ne 0) { throw 'git status failed' }
if ($dirtyBefore.Count -gt 0) { throw "Approved baseline DB verification requires clean tree.`n$($dirtyBefore -join [Environment]::NewLine)" }

$metadata = Get-Content -LiteralPath $metadataPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
$baselineSha = ([string]$metadata.baseCommit).Trim().ToLowerInvariant()
if ($baselineSha -notmatch '^[0-9a-f]{40}$') { throw 'Approved baseline exact SHA is invalid.' }
& git -C $rootPath cat-file -e "$baselineSha^{commit}"
if ($LASTEXITCODE -ne 0) { throw "Approved baseline commit is not available locally: $baselineSha" }

if ([string]::IsNullOrWhiteSpace($EvidenceRoot)) {
    $EvidenceRoot = Join-Path ([IO.Path]::GetTempPath()) ("cpf-approved-baseline-db-{0}" -f [guid]::NewGuid().ToString('N'))
}
$out = if ([IO.Path]::IsPathRooted($EvidenceRoot)) { [IO.Path]::GetFullPath($EvidenceRoot) } else { Join-Path $rootPath $EvidenceRoot }
New-Item -ItemType Directory -Path $out -Force | Out-Null
$baselineArchive = Join-Path $out "cpf-baseline-$baselineSha.tar"
$baselineRoot = Join-Path $out 'baseline-root'
New-Item -ItemType Directory -Path $baselineRoot -Force | Out-Null

& git -C $rootPath archive --format=tar --output=$baselineArchive $baselineSha
if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $baselineArchive -PathType Leaf)) { throw 'Approved baseline archive creation failed.' }
& tar -xf $baselineArchive -C $baselineRoot
if ($LASTEXITCODE -ne 0) { throw 'Approved baseline archive extraction failed.' }
$baselineArchiveSha256 = (Get-FileHash -LiteralPath $baselineArchive -Algorithm SHA256).Hash.ToLowerInvariant()
$baselineMetadataSha256 = (Get-FileHash -LiteralPath $metadataPath -Algorithm SHA256).Hash.ToLowerInvariant()

function Normalize-Relative([string]$Path) { return $Path.Replace('\\','/').TrimStart('/') }
function Assert-BaselineMigrationHashes {
    $baselineDbRoot = Join-Path $baselineRoot 'cpf-tools/db'
    if (-not (Test-Path -LiteralPath $baselineDbRoot -PathType Container)) { throw 'Baseline DB source is missing.' }
    $allMigrationFiles = @(Get-ChildItem -LiteralPath $baselineDbRoot -Recurse -File -Filter 'V*.sql')
    $verified = [Collections.Generic.List[object]]::new()
    foreach ($packProperty in $metadata.packs.PSObject.Properties) {
        $packName = [string]$packProperty.Name
        $vendor = $packName.Split('/')[0].ToLowerInvariant()
        foreach ($entry in @($packProperty.Value)) {
            if ([string]$entry -notmatch '^([0-9a-fA-F]{64}) \*(V\d+__.+\.sql)$') { throw "Invalid baseline checksum entry: $entry" }
            $expected = $Matches[1].ToLowerInvariant(); $fileName = $Matches[2]
            $candidates = @($allMigrationFiles | Where-Object {
                $_.Name -eq $fileName -and (Normalize-Relative $_.FullName).ToLowerInvariant().Contains("/vendor/$vendor/")
            })
            if ($candidates.Count -ne 1) {
                throw "Baseline migration resolution must be unique: pack=$packName file=$fileName candidates=$($candidates.Count)"
            }
            $actual = (Get-FileHash -LiteralPath $candidates[0].FullName -Algorithm SHA256).Hash.ToLowerInvariant()
            if ($actual -ne $expected) { throw "Approved baseline migration hash mismatch: pack=$packName file=$fileName" }
            $verified.Add([ordered]@{ pack=$packName; file=$fileName; sha256=$actual }) | Out-Null
        }
    }
    return @($verified)
}
$baselineMigrations = @(Assert-BaselineMigrationHashes)

$profiles = [ordered]@{
    mariadb = (Resolve-Path -LiteralPath $MariaDbProfile).Path
    postgresql = (Resolve-Path -LiteralPath $PostgreSqlProfile).Path
    oracle = (Resolve-Path -LiteralPath $OracleProfile).Path
}
$upgradeProfiles = [ordered]@{
    mariadb = (Resolve-Path -LiteralPath $MariaDbUpgradeProfile).Path
    postgresql = (Resolve-Path -LiteralPath $PostgreSqlUpgradeProfile).Path
    oracle = (Resolve-Path -LiteralPath $OracleUpgradeProfile).Path
}
$backups = @($BackupManifestPath | ForEach-Object { (Resolve-Path -LiteralPath $_).Path })

if ($MigrationVersions.Count -eq 0) {
    $baselineVersions = @($baselineMigrations | ForEach-Object { if ($_.file -match '^V(\d+)__') { [int]$Matches[1] } })
    $baselineMax = if ($baselineVersions.Count -gt 0) { ($baselineVersions | Measure-Object -Maximum).Maximum } else { 0 }
    $currentMigrationRoot = Join-Path $rootPath 'cpf-tools/db/vendor'
    $MigrationVersions = @(
        Get-ChildItem -LiteralPath $currentMigrationRoot -Recurse -File -Filter 'V*.sql' |
            ForEach-Object { if ($_.Name -match '^V(\d+)__') { [int]$Matches[1] } } |
            Where-Object { $_ -gt $baselineMax } |
            Sort-Object -Unique
    )
}
if ($MigrationVersions.Count -eq 0) { throw 'No sequential upgrade versions were resolved after the approved baseline.' }
$MigrationVersions = @($MigrationVersions | Sort-Object -Unique)

$results = [Collections.Generic.List[object]]::new()
$failures = [Collections.Generic.List[string]]::new()
$startedAt = [DateTimeOffset]::UtcNow
function Sanitize([string]$Value) {
    return ($Value -replace '(?i)(password|secret|token|authorization|cookie)\s*[:=]\s*\S+', '$1=***')
}
function Invoke-Step([string]$Vendor, [string]$Name, [scriptblock]$Action) {
    $stepStarted = [DateTimeOffset]::UtcNow; $exitCode = 0; $message = 'PASS'
    try {
        & $Action
        if ($LASTEXITCODE -and $LASTEXITCODE -ne 0) { throw "exit=$LASTEXITCODE" }
    } catch {
        $exitCode = 1; $message = Sanitize $_.Exception.Message
        $script:failures.Add("$Vendor/$Name`: $message") | Out-Null
    } finally {
        $script:results.Add([ordered]@{
            vendor=$Vendor; name=$Name; startedAt=$stepStarted.ToString('o');
            finishedAt=[DateTimeOffset]::UtcNow.ToString('o'); exitCode=$exitCode; result=$message
        }) | Out-Null
    }
}
function Invoke-Migration([string]$Vendor, [string]$Profile, [string]$Direction, [int]$Version) {
    $prefix = "$Vendor-$Direction-v$Version"
    $dry = Join-Path $out "$prefix-dry-run.sanitized.json"
    $apply = Join-Path $out "$prefix-apply.sanitized.json"
    Invoke-Step $Vendor "$Direction-v$Version-dry-run" {
        & (Join-Path $rootPath 'cpf-tools/scripts/invoke-platform-database-migration.ps1') `
            -Root $rootPath -ProfilePath $Profile -Direction $Direction -MigrationVersion $Version -ResultPath $dry
    }
    if (-not (Test-Path -LiteralPath $dry -PathType Leaf)) { return }
    $planSha = [string](Get-Content -LiteralPath $dry -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100).planSha256
    if ($planSha -notmatch '^[0-9a-f]{64}$') { $script:failures.Add("$Vendor/$prefix`: plan SHA missing") | Out-Null; return }
    Invoke-Step $Vendor "$Direction-v$Version-apply" {
        & (Join-Path $rootPath 'cpf-tools/scripts/invoke-platform-database-migration.ps1') `
            -Root $rootPath -ProfilePath $Profile -Direction $Direction -MigrationVersion $Version `
            -ResultPath $apply -Apply -ConfirmApply -ConfirmApplicationsStopped -ConfirmRollbackReady `
            -ExpectedPlanSha256 $planSha -BackupManifestPath $backups
    }
}

Invoke-Step 'all' 'baseline-checksum-metadata' { if ($baselineMigrations.Count -eq 0) { throw 'No approved baseline migrations were verified.' } }
Invoke-Step 'all' 'current-migration-checksums' { & (Join-Path $rootPath 'cpf-tools/scripts/check-migration-checksums.ps1') -Root $rootPath }
Invoke-Step 'all' 'canonical-db-lifecycle-contract' { & (Join-Path $rootPath 'cpf-tools/scripts/check-canonical-db-lifecycle-contract.ps1') -Root $rootPath }
Invoke-Step 'all' 'vendor-static-token-parity' { & python (Join-Path $rootPath 'cpf-tools/scripts/verify-cpf-db-vendor-static-token-parity.py') --root $rootPath --json-report (Join-Path $out 'static-token-parity.json') }

foreach ($vendor in $profiles.Keys) {
    $profile = $profiles[$vendor]; $upgradeProfile = $upgradeProfiles[$vendor]
    $baselineInit = Join-Path $baselineRoot 'cpf-tools/scripts/initialize-cpf-database.ps1'
    Invoke-Step $vendor 'approved-baseline-install' {
        if (-not (Test-Path -LiteralPath $baselineInit -PathType Leaf)) { throw "Baseline initializer missing at $baselineSha" }
        & $baselineInit -Root $baselineRoot -ProfilePath $profile -All -SeedMode product -RequireRun -ResultDir (Join-Path $out "$vendor-baseline-install")
    }
    foreach ($version in $MigrationVersions) { Invoke-Migration $vendor $upgradeProfile 'upgrade' $version }
    Invoke-Step $vendor 'runtime-query-contract' {
        if ($vendor -eq 'mariadb') {
            & (Join-Path $rootPath 'cpf-tools/scripts/smoke-platform-runtime-query-packs-mariadb.ps1') -Root $rootPath -ProfilePath $profile -EvidencePath (Join-Path $out "$vendor-runtime-query.json")
        } else {
            & (Join-Path $rootPath 'cpf-tools/scripts/smoke-platform-runtime-query-packs-official-db.ps1') -Root $rootPath -ProfilePath $profile -Vendor $vendor -EvidencePath (Join-Path $out "$vendor-runtime-query.json")
        }
    }
    Invoke-Step $vendor 'schema-drift' { & (Join-Path $rootPath 'cpf-tools/scripts/check-database-schema-drift.ps1') -Root $rootPath }
    foreach ($version in @($MigrationVersions | Sort-Object -Descending)) { Invoke-Migration $vendor $upgradeProfile 'rollback' $version }
    foreach ($version in $MigrationVersions) { Invoke-Migration $vendor $upgradeProfile 'upgrade' $version }
    Invoke-Step $vendor 'forward-reapply-runtime-query' {
        if ($vendor -eq 'mariadb') {
            & (Join-Path $rootPath 'cpf-tools/scripts/smoke-platform-runtime-query-packs-mariadb.ps1') -Root $rootPath -ProfilePath $profile -EvidencePath (Join-Path $out "$vendor-reapply-runtime-query.json")
        } else {
            & (Join-Path $rootPath 'cpf-tools/scripts/smoke-platform-runtime-query-packs-official-db.ps1') -Root $rootPath -ProfilePath $profile -Vendor $vendor -EvidencePath (Join-Path $out "$vendor-reapply-runtime-query.json")
        }
    }
}

$finalSha = (& git -C $rootPath rev-parse HEAD).Trim()
$dirtyAfter = @(& git -C $rootPath status --porcelain=v1 --untracked-files=all)
if ($finalSha -ne $sourceSha) { $failures.Add("Source SHA changed: $sourceSha -> $finalSha") | Out-Null }
if ($dirtyAfter.Count -gt 0) { $failures.Add("Source tree changed during DB lifecycle: $($dirtyAfter -join ', ')") | Out-Null }
$evidence = [ordered]@{
    schemaVersion=1; evidenceId='CPF-SELF-DEV-029-APPROVED-BASELINE-THREE-DB';
    sourceSha=$sourceSha; resultSha=if($failures.Count -eq 0){$sourceSha}else{$null}; branch=$sourceBranch;
    approvedBaselineSha=$baselineSha; baselineArchiveSha256=$baselineArchiveSha256;
    baselineMetadataPath='cpf-tools/db/metadata/CPF_BASELINE_MIGRATION_CHECKSUMS_B894157.json';
    baselineMetadataSha256=$baselineMetadataSha256; baselineMigrationCount=$baselineMigrations.Count;
    vendors=@($profiles.Keys); migrationVersions=$MigrationVersions; startedAt=$startedAt.ToString('o');
    finishedAt=[DateTimeOffset]::UtcNow.ToString('o'); exitCode=if($failures.Count -eq 0){0}else{1};
    sourceDirtyBefore=$false; sourceDirtyAfter=($dirtyAfter.Count -gt 0); results=$results; failures=$failures;
    sanitized=$true; releaseEligible=($failures.Count -eq 0)
}
$evidencePath = Join-Path $out 'CPF_SELF_DEV_029_APPROVED_BASELINE_THREE_DB.sanitized.json'
[IO.File]::WriteAllText($evidencePath, ($evidence | ConvertTo-Json -Depth 30) + "`n", $Utf8NoBom)
if ($failures.Count -gt 0) { throw "Approved baseline three-DB lifecycle failed: $($failures -join '; ')" }
Write-Host "[CPF][PASS] approved baseline three-DB lifecycle evidence=$evidencePath"
