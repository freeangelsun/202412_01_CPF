param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root 'build/quality-gate'
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$resultPath = Join-Path $ResultDir 'sql-canonical-result.sanitized.json'
$result = [ordered]@{
    checkedAt = (Get-Date).ToString('o')
    status = 'FAILED'
    canonicalRoot = 'specs/sql'
    moduleLocalSql = @()
    flywayVersions = @()
    flywayChecksums = @()
    generatorDatabaseVendors = @()
}

try {
    $moduleLocalSql = @(Get-ChildItem -LiteralPath $Root -Directory | ForEach-Object {
        $resourceRoot = Join-Path $_.FullName 'src/main/resources'
        if (Test-Path -LiteralPath $resourceRoot) {
            Get-ChildItem -LiteralPath $resourceRoot -Recurse -File -Filter '*.sql'
        }
    } | ForEach-Object { $_.FullName.Substring($Root.Length + 1).Replace('\', '/') })
    $result.moduleLocalSql = $moduleLocalSql
    if ($moduleLocalSql.Count -gt 0) {
        throw "모듈 resources 아래에 비정본 SQL이 남아 있습니다: $($moduleLocalSql -join ', ')"
    }

    $migrationDir = Join-Path $Root 'specs/sql/migration/flyway'
    $versions = @(Get-ChildItem -LiteralPath $migrationDir -File -Filter 'V*__*.sql' | ForEach-Object {
        if ($_.Name -notmatch '^V(?<version>\d+)__.+\.sql$') {
            throw "Flyway 파일명이 표준에 맞지 않습니다: $($_.Name)"
        }
        [int] $Matches.version
    } | Sort-Object)
    $result.flywayVersions = $versions
    if (($versions | Select-Object -Unique).Count -ne $versions.Count) {
        throw 'Flyway migration 버전이 중복됩니다.'
    }
    if ($versions.Count -eq 0 -or $versions[0] -ne 1) {
        throw 'Flyway migration은 V1부터 시작해야 합니다.'
    }
    $expected = @(1..$versions[-1])
    if ((Compare-Object -ReferenceObject $expected -DifferenceObject $versions).Count -gt 0) {
        throw "Flyway migration 버전이 연속적이지 않습니다: actual=$($versions -join ',')"
    }

    $checksumManifestPath = Join-Path $migrationDir 'checksums.sha256'
    if (-not (Test-Path -LiteralPath $checksumManifestPath -PathType Leaf)) {
        throw 'Flyway checksum manifest가 없습니다: specs/sql/migration/flyway/checksums.sha256'
    }
    $checksumRows = @()
    foreach ($line in [IO.File]::ReadAllLines($checksumManifestPath, [Text.Encoding]::UTF8)) {
        if ([string]::IsNullOrWhiteSpace($line) -or $line.TrimStart().StartsWith('#')) {
            continue
        }
        if ($line -notmatch '^(?<sha>[a-f0-9]{64})\s+\*(?<file>V\d+__.+\.sql)$') {
            throw "Flyway checksum manifest 행이 잘못되었습니다: $line"
        }
        $migrationPath = Join-Path $migrationDir $Matches.file
        if (-not (Test-Path -LiteralPath $migrationPath -PathType Leaf)) {
            throw "Flyway checksum 대상 파일이 없습니다: $($Matches.file)"
        }
        $actualSha = (Get-FileHash -LiteralPath $migrationPath -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($actualSha -ne $Matches.sha) {
            throw "배포 migration checksum이 변경되었습니다: $($Matches.file)"
        }
        $checksumRows += [ordered]@{ file = $Matches.file; sha256 = $actualSha }
    }
    if ($checksumRows.Count -ne $versions.Count) {
        throw "Flyway checksum manifest와 migration 파일 수가 다릅니다: manifest=$($checksumRows.Count), migration=$($versions.Count)"
    }
    $result.flywayChecksums = $checksumRows

    foreach ($required in @(
        '00_all_install.sql',
        '00_all_install_and_smoke.sql',
        'migration/flyway/V1__cpf_baseline_install.sql',
        'migration/flyway/V37__official_ref_external_expansion.sql',
        'migration/rollback/R37__official_ref_external_expansion.sql'
    )) {
        if (-not (Test-Path -LiteralPath (Join-Path (Join-Path $Root 'specs/sql') $required))) {
            throw "SQL 정본 필수 파일이 없습니다: $required"
        }
    }

    $generatorText = [IO.File]::ReadAllText((Join-Path $Root 'scripts/create-domain.ps1'), [Text.Encoding]::UTF8)
    $vendors = @('mariadb', 'postgresql', 'oracle', 'sqlserver')
    foreach ($vendor in $vendors) {
        if ($generatorText -notmatch ('"' + [regex]::Escape($vendor) + '"')) {
            throw "도메인 생성기가 DB 벤더를 지원하지 않습니다: $vendor"
        }
    }
    $result.generatorDatabaseVendors = $vendors
    $result.status = 'DONE'
} finally {
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 10), $Utf8NoBom)
}

Write-Host "SQL 정본 검증 완료: moduleLocalSql=0, flyway=V1..V$($versions[-1]), vendors=$($vendors.Count)"
