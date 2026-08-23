param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$publishedInventoryPath = Join-Path $Root 'cpf-docs/deliverables/SHA256SUMS.txt'
if (-not (Test-Path -LiteralPath $publishedInventoryPath -PathType Leaf)) {
    throw "published artifact inventory missing: $publishedInventoryPath"
}
$publishedHashes = @{}
foreach ($line in Get-Content -LiteralPath $publishedInventoryPath -Encoding UTF8) {
    if ([string]::IsNullOrWhiteSpace($line)) { continue }
    if ($line -notmatch '^([0-9a-fA-F]{64})\s+(.+)$') {
        throw "invalid published artifact inventory line: $line"
    }
    $publishedHashes[$Matches[2].Replace('\', '/')] = $Matches[1].ToLowerInvariant()
}

function Check-PublishedSqlDirectory([string]$Dir) {
    $files = @(Get-ChildItem -LiteralPath $Dir -File -Filter '*.sql')
    if ($files.Count -eq 0) { throw "published SQL directory is empty: $Dir" }
    foreach ($file in $files) {
        $relative = [IO.Path]::GetRelativePath($Root, $file.FullName).Replace('\', '/')
        if (-not $publishedHashes.ContainsKey($relative)) {
            throw "published artifact inventory entry missing: $relative"
        }
        $actual = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($actual -cne $publishedHashes[$relative]) {
            throw "published immutable artifact drift: $relative"
        }
    }
    Write-Host "[PASS] published SQL inventory: $Dir files=$($files.Count)"
}

function Check-Pack([string]$Dir) {
    $manifest = Join-Path $Dir 'checksums.sha256'
    if (-not (Test-Path -LiteralPath $manifest -PathType Leaf)) { throw "checksum manifest missing: $manifest" }
    $seen = @{}
    $entries = @{}
    foreach ($line in Get-Content -LiteralPath $manifest -Encoding UTF8) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        if ($line -notmatch '^([0-9a-fA-F]{64})\s+\*?(V(\d+)__.+\.sql)$') {
            throw "invalid checksum manifest entry: $manifest"
        }
        $hash = $Matches[1].ToLowerInvariant()
        $fileName = $Matches[2]
        $version = [int]$Matches[3]
        if ($seen.ContainsKey($version)) { throw "duplicate Flyway version V$version in $manifest" }
        $seen[$version] = $fileName
        $entries[$fileName] = $hash
    }
    $migrationFiles = @(Get-ChildItem -LiteralPath $Dir -Filter 'V*.sql' -File)
    if ($migrationFiles.Count -eq 0) { throw "migration SQL missing: $Dir" }
    foreach ($file in $migrationFiles) {
        if (-not $entries.ContainsKey($file.Name)) { throw "versioned file missing from checksum manifest: $($file.FullName)" }
        $actual = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($actual -ne $entries[$file.Name]) { throw "versioned file checksum mismatch: $($file.FullName)" }
    }
    foreach ($entry in $entries.Keys) {
        if (-not (Test-Path -LiteralPath (Join-Path $Dir $entry) -PathType Leaf)) { throw "checksum manifest points to missing versioned file: $Dir/$entry" }
    }
    Write-Host "[PASS] versioned checksum pack: $Dir"
}

$source = Join-Path $Root 'cpf-tools/db/vendor/mariadb/source/migration/flyway'
$runtime = Join-Path $Root 'cpf-tools/db/vendor/mariadb/migration/flyway'
Check-Pack $source
Check-Pack $runtime
# Both directories contain already-published immutable histories with their own
# checksum evidence. The central runtime directory is the executable lifecycle
# authority; source/migration is a retained compatibility archive. Requiring
# cross-pack byte equality would force a prohibited rewrite of one published
# history. Independent manifest integrity is the fail-closed contract.
Write-Host '[PASS] MariaDB independent source-archive/runtime migration integrity'

$sourceRollback = Join-Path $Root 'cpf-tools/db/vendor/mariadb/source/migration/rollback'
$runtimeRollback = Join-Path $Root 'cpf-tools/db/vendor/mariadb/rollback'
if (Test-Path -LiteralPath $sourceRollback -PathType Container) {
    # The legacy V-named rollback archive has its own immutable manifest.
    Check-Pack $sourceRollback
    # Both complete rollback directories are separately published immutable
    # histories in the existing repository-wide SHA256 inventory. Validate
    # every SQL byte against that evidence; never force cross-history equality.
    Check-PublishedSqlDirectory $sourceRollback
    Check-PublishedSqlDirectory $runtimeRollback
    Write-Host '[PASS] MariaDB independent source-archive/runtime rollback integrity'
}

$expectedLogicalPacks = $null
foreach ($vendor in @('postgresql','oracle')) {
    $migrationRoot = Join-Path $Root "cpf-tools/db/vendor/$vendor/migration/flyway"
    $packDirectories = @(
        Get-ChildItem -LiteralPath $migrationRoot -Directory |
            Where-Object {
                @(Get-ChildItem -LiteralPath $_.FullName -File -Filter 'V*.sql').Count -gt 0
            } |
            Sort-Object Name
    )
    $logicalPacks = @($packDirectories.Name)
    if ($null -eq $expectedLogicalPacks) {
        $expectedLogicalPacks = @($logicalPacks)
    } elseif (($logicalPacks -join ',') -cne ($expectedLogicalPacks -join ',')) {
        throw "official vendor logical migration pack drift: vendor=$vendor expected=$($expectedLogicalPacks -join ',') actual=$($logicalPacks -join ',')"
    }
    foreach ($packDirectory in $packDirectories) {
        Check-Pack $packDirectory.FullName
    }
}
Write-Host '[PASS] Official DB migration checksum integrity'
