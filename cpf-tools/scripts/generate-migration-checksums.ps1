param(
    [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path,
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
    $seen = @{}
    $lines = [System.Collections.Generic.List[string]]::new()
    foreach ($file in Get-ChildItem -LiteralPath $Dir -Filter 'V*.sql' -File | Sort-Object { Get-MigrationVersion $_.Name }) {
        $version = Get-MigrationVersion $file.Name
        if ($seen.ContainsKey($version)) { throw "duplicate Flyway version V$version in $Dir" }
        $seen[$version] = $file.Name
        $lines.Add((Get-HashLine $file))
    }
    if ($lines.Count -eq 0) { throw "migration SQL missing: $Dir" }
    $manifest = Join-Path $Dir 'checksums.sha256'
    [IO.File]::WriteAllLines($manifest, $lines, [Text.UTF8Encoding]::new($false))
    Write-Host "[UPDATED] $manifest"
}

# MariaDB는 historical migration chain을 canonical source/runtime 양쪽에서 관리한다.
$source = Join-Path $Root 'cpf-tools/db/vendor/mariadb/source/migration/flyway'
$runtime = Join-Path $Root 'cpf-tools/db/vendor/mariadb/migration/flyway'
Rebuild-Pack $source
Rebuild-Pack $runtime

# PostgreSQL/Oracle은 물리 DB별 Flyway history가 독립적이므로 logicalDatabase별 checksum 정본을 둔다.
$logicalDatabases = @('cpfDB','cmnDB','admDB','bzaDB','batDB','mbrDB','accDB','refDB')
foreach ($vendor in @('postgresql','oracle')) {
    foreach ($logicalDatabase in $logicalDatabases) {
        Rebuild-Pack (Join-Path $Root "cpf-tools/db/vendor/$vendor/migration/flyway/$logicalDatabase")
    }
}
