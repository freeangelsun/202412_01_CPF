param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/quality-gate"
} elseif (-not [System.IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$failures = [System.Collections.Generic.List[string]]::new()
$result = [ordered]@{
    checkedAt = [DateTimeOffset]::Now.ToString("o")
    status = "실패"
    canonicalRoot = "specs/sql"
    forbiddenModuleVendorResources = @()
    splitTableCount = 0
    generatedTableCount = 0
    centralMariaTableCount = 0
    flywayVersions = @()
    flywayChecksums = @()
    vendorPackStatuses = @()
}

function Add-Failure([string] $Message) {
    $failures.Add($Message) | Out-Null
}

function Read-Utf8([string] $Path) {
    return [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
}

# 제품 Module의 Vendor별 물리 SQL/MyBatis resource는 최종 Architecture에서 금지합니다.
$forbiddenPatterns = @(
    "src/main/resources/sql/vendor",
    "src/main/resources/mybatis/vendor"
)
$forbidden = [System.Collections.Generic.List[string]]::new()
foreach ($module in Get-ChildItem -LiteralPath $Root -Directory -Filter "cpf-*") {
    foreach ($pattern in $forbiddenPatterns) {
        $path = Join-Path $module.FullName $pattern
        if (Test-Path -LiteralPath $path) {
            foreach ($file in Get-ChildItem -LiteralPath $path -Recurse -File -Force) {
                $forbidden.Add(
                    $file.FullName.Substring($Root.Length + 1).Replace('\', '/')) | Out-Null
            }
        }
    }
}
$result.forbiddenModuleVendorResources = @($forbidden)
if ($forbidden.Count -gt 0) {
    Add-Failure "Module-local Vendor SQL/MyBatis resource가 남아 있습니다. Central Pack으로만 연결하세요."
}

$sqlRoot = Join-Path $Root "specs/sql"
$splitFiles = @(
    "10_cpf_schema.sql",
    "20_cmn_schema.sql",
    "30_adm_schema.sql",
    "35_bat_schema.sql",
    "40_business_modules_schema.sql",
    "45_external_schema.sql"
)
$splitText = ""
foreach ($file in $splitFiles) {
    $path = Join-Path $sqlRoot $file
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        Add-Failure "Split SQL이 없습니다: specs/sql/$file"
        continue
    }
    $splitText += "`n" + (Read-Utf8 $path)
}
$result.splitTableCount = [regex]::Matches(
    $splitText,
    '(?im)^\s*CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?').Count

$generatedInstall = Join-Path $sqlRoot "00_empty_install.sql"
$generatedVerify = Join-Path $sqlRoot "00_verify.sql"
$centralMariaRoot = Join-Path $Root "cpf-tools/db/vendor/mariadb"
$centralInstall = Join-Path $centralMariaRoot "install/00_empty_install.sql"
$centralVerify = Join-Path $centralMariaRoot "verify/00_verify.sql"

foreach ($path in @($generatedInstall, $generatedVerify, $centralInstall, $centralVerify)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        Add-Failure "DB canonical file이 없습니다: $path"
    }
}
if (Test-Path -LiteralPath $generatedInstall) {
    $result.generatedTableCount = [regex]::Matches(
        (Read-Utf8 $generatedInstall),
        '(?im)^\s*CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?').Count
}
if (Test-Path -LiteralPath $centralInstall) {
    $result.centralMariaTableCount = [regex]::Matches(
        (Read-Utf8 $centralInstall),
        '(?im)^\s*CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?').Count
}
if ($result.splitTableCount -ne $result.generatedTableCount) {
    Add-Failure "Split/generated Empty Install Table 수가 다릅니다: split=$($result.splitTableCount) generated=$($result.generatedTableCount)"
}
if ($result.generatedTableCount -ne $result.centralMariaTableCount) {
    Add-Failure "generated/central MariaDB Empty Install Table 수가 다릅니다: generated=$($result.generatedTableCount) central=$($result.centralMariaTableCount)"
}
if ((Test-Path $generatedInstall) -and (Test-Path $centralInstall)) {
    if ((Get-FileHash $generatedInstall -Algorithm SHA256).Hash -ne
            (Get-FileHash $centralInstall -Algorithm SHA256).Hash) {
        Add-Failure "specs/sql/00_empty_install.sql과 Central MariaDB install이 byte parity가 아닙니다."
    }
}
if ((Test-Path $generatedVerify) -and (Test-Path $centralVerify)) {
    if ((Get-FileHash $generatedVerify -Algorithm SHA256).Hash -ne
            (Get-FileHash $centralVerify -Algorithm SHA256).Hash) {
        Add-Failure "specs/sql/00_verify.sql과 Central MariaDB verify가 byte parity가 아닙니다."
    }
}

# Vendor Pack은 중앙 위치에만 존재하고 pack.json의 상태를 명시해야 합니다.
$vendorRoot = Join-Path $Root "cpf-tools/db/vendor"
$requiredVendors = @("mariadb", "mysql", "postgresql", "oracle", "sqlserver")
$packStatuses = [System.Collections.Generic.List[object]]::new()
foreach ($vendor in $requiredVendors) {
    $packRoot = Join-Path $vendorRoot $vendor
    $manifest = Join-Path $packRoot "pack.json"
    if (-not (Test-Path -LiteralPath $manifest -PathType Leaf)) {
        Add-Failure "Vendor Pack manifest가 없습니다: cpf-tools/db/vendor/$vendor/pack.json"
        continue
    }
    try {
        $payload = Read-Utf8 $manifest | ConvertFrom-Json
        if ([string]$payload.vendor -ne $vendor) {
            Add-Failure "Vendor Pack manifest vendor 불일치: folder=$vendor manifest=$($payload.vendor)"
        }
        $packStatuses.Add([ordered]@{
            vendor = $vendor
            status = [string] $payload.status
            schemaVersion = [int] $payload.schemaVersion
        }) | Out-Null
    } catch {
        Add-Failure "Vendor Pack manifest parse 실패: $manifest"
    }
}
$result.vendorPackStatuses = @($packStatuses)

# Historical Flyway checksum은 변경해서 통과시키지 않습니다.
$migrationDir = Join-Path $sqlRoot "migration/flyway"
if (-not (Test-Path -LiteralPath $migrationDir -PathType Container)) {
    Add-Failure "Flyway migration directory가 없습니다."
} else {
    $versions = @(
        Get-ChildItem -LiteralPath $migrationDir -File -Filter "V*__*.sql" |
        ForEach-Object {
            if ($_.Name -notmatch '^V(?<version>\d+)__.+\.sql$') {
                Add-Failure "Flyway 파일명이 표준에 맞지 않습니다: $($_.Name)"
                return
            }
            [int] $Matches.version
        } |
        Where-Object { $null -ne $_ } |
        Sort-Object
    )
    $result.flywayVersions = $versions
    if ($versions.Count -eq 0 -or $versions[0] -ne 1) {
        Add-Failure "Flyway migration은 V1부터 존재해야 합니다."
    } elseif ((Compare-Object -ReferenceObject @(1..$versions[-1]) -DifferenceObject $versions).Count -gt 0) {
        Add-Failure "Flyway version이 연속적이지 않습니다: $($versions -join ',')"
    }

    $manifestPath = Join-Path $migrationDir "checksums.sha256"
    if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
        Add-Failure "Flyway checksum manifest가 없습니다."
    } else {
        $checksumRows = [System.Collections.Generic.List[object]]::new()
        foreach ($line in [System.IO.File]::ReadAllLines($manifestPath, [System.Text.Encoding]::UTF8)) {
            if ([string]::IsNullOrWhiteSpace($line) -or $line.TrimStart().StartsWith("#")) {
                continue
            }
            if ($line -notmatch '^(?<sha>[a-f0-9]{64})\s+\*(?<file>V\d+__.+\.sql)$') {
                Add-Failure "Flyway checksum manifest 행 형식 오류: $line"
                continue
            }
            $path = Join-Path $migrationDir $Matches.file
            if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
                Add-Failure "Flyway checksum 대상 파일 누락: $($Matches.file)"
                continue
            }
            $actual = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()
            $matchesManifest = $actual -eq $Matches.sha
            $checksumRows.Add([ordered]@{
                file = $Matches.file
                expected = $Matches.sha
                actual = $actual
                matches = $matchesManifest
            }) | Out-Null
            if (-not $matchesManifest) {
                Add-Failure "Historical Flyway checksum 불일치: $($Matches.file)"
            }
        }
        $result.flywayChecksums = @($checksumRows)
    }
}

$generatorPath = Join-Path $Root "scripts/create-domain.ps1"
if (-not (Test-Path -LiteralPath $generatorPath -PathType Leaf)) {
    Add-Failure "Domain Generator가 없습니다."
} else {
    $generatorText = Read-Utf8 $generatorPath
    foreach ($vendor in $requiredVendors) {
        if ($generatorText -notmatch [regex]::Escape($vendor)) {
            Add-Failure "Domain Generator에서 Vendor 선택이 확인되지 않습니다: $vendor"
        }
    }
}

$result.status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
$result.failures = @($failures)
$resultPath = Join-Path $ResultDir "sql-canonical-result.sanitized.json"
[System.IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 12),
    $Utf8NoBom)

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL $_" }
    exit 1
}
Write-Host "SQL canonical check passed. tables=$($result.splitTableCount) vendors=$($packStatuses.Count)"
