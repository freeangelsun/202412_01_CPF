param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

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
        if (-not $entries.ContainsKey($file.Name)) { throw "migration missing from checksum manifest: $($file.FullName)" }
        $actual = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($actual -ne $entries[$file.Name]) { throw "migration checksum mismatch: $($file.FullName)" }
    }
    foreach ($entry in $entries.Keys) {
        if (-not (Test-Path -LiteralPath (Join-Path $Dir $entry) -PathType Leaf)) { throw "checksum manifest points to missing migration: $Dir/$entry" }
    }
    Write-Host "[PASS] migration checksum pack: $Dir"
}

$source = Join-Path $Root 'cpf-tools/db/vendor/mariadb/source/migration/flyway'
$runtime = Join-Path $Root 'cpf-tools/db/vendor/mariadb/migration/flyway'
Check-Pack $source
Check-Pack $runtime
foreach ($file in Get-ChildItem -LiteralPath $source -Filter 'V*.sql' -File) {
    $runtimeFile = Join-Path $runtime $file.Name
    if (-not (Test-Path -LiteralPath $runtimeFile -PathType Leaf)) { throw "runtime lifecycle missing canonical migration: $($file.Name)" }
    if ((Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash -ne (Get-FileHash -LiteralPath $runtimeFile -Algorithm SHA256).Hash) {
        throw "source/runtime migration drift: $($file.Name)"
    }
}
Write-Host '[PASS] MariaDB source/runtime migration parity'

$sourceRollback = Join-Path $Root 'cpf-tools/db/vendor/mariadb/source/migration/rollback'
$runtimeRollback = Join-Path $Root 'cpf-tools/db/vendor/mariadb/rollback'
if (Test-Path -LiteralPath $sourceRollback -PathType Container) {
    foreach ($file in Get-ChildItem -LiteralPath $sourceRollback -Filter '*.sql' -File) {
        $runtimeFile = Join-Path $runtimeRollback $file.Name
        if (-not (Test-Path -LiteralPath $runtimeFile -PathType Leaf)) { throw "runtime rollback missing canonical artifact: $($file.Name)" }
        if ((Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash -ne (Get-FileHash -LiteralPath $runtimeFile -Algorithm SHA256).Hash) {
            throw "source/runtime rollback drift: $($file.Name)"
        }
    }
    Write-Host '[PASS] MariaDB source/runtime rollback parity'
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
