param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

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

    foreach ($required in @(
        '00_all_install.sql',
        '00_all_install_and_smoke.sql',
        'migration/flyway/V1__cpf_baseline_install.sql'
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
