param(
    [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,
    [string[]]$PackDirectory = @(),
    [switch]$Apply
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if (-not $Apply) { throw 'Checksum 갱신은 maintenance 작업입니다. 명시적으로 -Apply를 지정하십시오.' }

function Get-MigrationVersion([string]$Name) {
    if ($Name -notmatch '^V(\d+)__.+\.sql$') { throw "invalid migration: $Name" }
    return [int]$Matches[1]
}

function Get-HashLine([IO.FileInfo]$File) {
    $hash = (Get-FileHash -LiteralPath $File.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    return "$hash *$($File.Name)"
}

function Rebuild-Pack([string]$Dir) {
    if (-not (Test-Path -LiteralPath $Dir -PathType Container)) { throw "migration directory missing: $Dir" }
    $files = @(Get-ChildItem -LiteralPath $Dir -Filter 'V*.sql' -File | Sort-Object { Get-MigrationVersion $_.Name })
    if ($files.Count -eq 0) { throw "migration SQL missing: $Dir" }
    $manifest = Join-Path $Dir 'checksums.sha256'
    if (-not (Test-Path -LiteralPath $manifest -PathType Leaf)) {
        $initial = @($files | ForEach-Object { Get-HashLine $_ })
        [IO.File]::WriteAllLines($manifest, $initial, [Text.UTF8Encoding]::new($false))
        Write-Host "[CREATED] $manifest"
        return
    }

    # Published checksum manifests are append-only evidence.  Re-sorting or
    # normalizing old lines changes historical bytes even when no SQL changed.
    $existingText = [IO.File]::ReadAllText($manifest, [Text.Encoding]::UTF8)
    $existingLines = @($existingText -split '\r?\n' | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    $seenVersions = @{}
    $seenNames = @{}
    foreach ($line in $existingLines) {
        if ($line -notmatch '^([0-9a-fA-F]{64})\s+\*?(V\d+__.+\.sql)$') {
            throw "invalid immutable checksum manifest line: manifest=$manifest line=$line"
        }
        $expectedHash = $Matches[1].ToLowerInvariant()
        $name = $Matches[2]
        $version = Get-MigrationVersion $name
        if ($seenVersions.ContainsKey($version)) { throw "duplicate Flyway version V$version in $manifest" }
        $file = Join-Path $Dir $name
        if (-not (Test-Path -LiteralPath $file -PathType Leaf)) { throw "checksum manifest references missing migration: $file" }
        $actualHash = (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($actualHash -cne $expectedHash) { throw "immutable migration checksum drift: $file" }
        $seenVersions[$version] = $name
        $seenNames[$name] = $true
    }

    $appendLines = [System.Collections.Generic.List[string]]::new()
    $maxPublishedVersion = if ($seenVersions.Count -gt 0) { [int](($seenVersions.Keys | Measure-Object -Maximum).Maximum) } else { 0 }
    foreach ($file in $files) {
        $version = Get-MigrationVersion $file.Name
        if ($seenNames.ContainsKey($file.Name)) { continue }
        if ($seenVersions.ContainsKey($version)) { throw "duplicate Flyway version V$version in $Dir" }
        if ($version -le $maxPublishedVersion) {
            throw "unregistered historical migration cannot be inserted into append-only checksum manifest: $($file.FullName)"
        }
        $seenVersions[$version] = $file.Name
        $seenNames[$file.Name] = $true
        $appendLines.Add((Get-HashLine $file))
    }
    if ($appendLines.Count -eq 0) {
        Write-Host "[UNCHANGED] $manifest"
        return
    }
    $newline = if ($existingText.Contains("`r`n")) { "`r`n" } else { "`n" }
    $prefix = if ($existingText.Length -eq 0 -or $existingText.EndsWith("`n")) { '' } else { $newline }
    [IO.File]::AppendAllText($manifest, $prefix + ($appendLines -join $newline) + $newline, [Text.UTF8Encoding]::new($false))
    Write-Host "[APPENDED] $manifest count=$($appendLines.Count)"
}

if ($PackDirectory.Count -gt 0) {
    $approvedRoots = @(
        'cpf-tools/db/vendor/mariadb/source/migration/flyway',
        'cpf-tools/db/vendor/mariadb/migration/flyway',
        'cpf-tools/db/vendor/postgresql/migration/flyway',
        'cpf-tools/db/vendor/oracle/migration/flyway'
    ) | ForEach-Object { [IO.Path]::GetFullPath((Join-Path $Root $_)) }
    foreach ($requestedDirectory in $PackDirectory) {
        $directory = if ([IO.Path]::IsPathRooted($requestedDirectory)) {
            [IO.Path]::GetFullPath($requestedDirectory)
        } else {
            [IO.Path]::GetFullPath((Join-Path $Root $requestedDirectory))
        }
        $insideApprovedRoot = @($approvedRoots | Where-Object {
                $directory.Equals($_, [StringComparison]::OrdinalIgnoreCase) -or
                $directory.StartsWith($_ + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)
            }).Count -gt 0
        if (-not $insideApprovedRoot) {
            throw "PackDirectory는 중앙 migration root 내부여야 합니다: $requestedDirectory"
        }
        Rebuild-Pack $directory
    }
    return
}

# MariaDB는 historical migration chain을 canonical source/runtime 양쪽에서 관리한다.
$source = Join-Path $Root 'cpf-tools/db/vendor/mariadb/source/migration/flyway'
$runtime = Join-Path $Root 'cpf-tools/db/vendor/mariadb/migration/flyway'
Rebuild-Pack $source
Rebuild-Pack $runtime

# PostgreSQL/Oracle은 물리 DB별 Flyway history가 독립적이다. 현재 production Pack은
# Profile로 요구하고, retired Domain을 포함한 immutable historical pack도 디렉터리에서 발견해 보존한다.
# REFERENCE_FIXTURE는 current snapshot의 비운영 물리 대상이며 immutable refDB lineage를
# 소비하므로 referenceFixture라는 동일 이름의 historical migration pack을 요구하지 않는다.
$profile = Get-Content -LiteralPath (Join-Path $Root 'cpf-tools/db/config/database-install.default.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$canonicalSchema = Get-Content -LiteralPath (Join-Path $Root 'cpf-tools/db/canonical/platform-schema.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$referenceFixtureDatabase = [string]$canonicalSchema.canonicalPolicy.platformDatabaseArchitecture.REFERENCE_FIXTURE.physicalName
if ([string]::IsNullOrWhiteSpace($referenceFixtureDatabase)) {
    throw 'Canonical REFERENCE_FIXTURE physicalName is required for migration-pack ownership.'
}
$requiredProductionDatabases = @(
    $profile.modules.PSObject.Properties |
        Where-Object {
            [bool]$_.Value.enabled -and
            [string]$_.Value.logicalDatabase -cne $referenceFixtureDatabase
        } |
        ForEach-Object { [string]$_.Value.logicalDatabase } |
        Sort-Object -Unique
)
foreach ($vendor in @('postgresql','oracle')) {
    $migrationRoot = Join-Path $Root "cpf-tools/db/vendor/$vendor/migration/flyway"
    $packDirectories = @(
        Get-ChildItem -LiteralPath $migrationRoot -Directory |
            Where-Object {
                @(Get-ChildItem -LiteralPath $_.FullName -File -Filter 'V*.sql').Count -gt 0
            } |
            Sort-Object Name
    )
    foreach ($logicalDatabase in $requiredProductionDatabases) {
        if (@($packDirectories | Where-Object { $_.Name -ceq $logicalDatabase }).Count -ne 1) {
            throw "current production migration pack missing: vendor=$vendor database=$logicalDatabase"
        }
    }
    foreach ($discoveredPackDirectory in $packDirectories) {
        Rebuild-Pack $discoveredPackDirectory.FullName
    }
}
