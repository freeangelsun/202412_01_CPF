param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"

# 공식 설치 기준이 되는 현행 스키마 SQL만 검사합니다.
# archive 폴더는 과거 전환용 자료이므로 공식 naming/comment gate 대상에서 제외합니다.
$schemaFiles = @(
    "specs/sql/10_pfw_schema.sql",
    "specs/sql/20_cmn_schema.sql",
    "specs/sql/30_adm_schema.sql",
    "specs/sql/35_bat_schema.sql",
    "specs/sql/40_business_modules_schema.sql"
)
$allowedPrefixes = @("pfw", "cmn", "adm", "bat", "acc", "mbr", "bizadm", "exs")
$commonColumns = @("created_by", "created_at", "updated_by", "updated_at")
$failures = New-Object System.Collections.Generic.List[string]
$schemaTableNames = New-Object System.Collections.Generic.HashSet[string]

foreach ($relativePath in $schemaFiles) {
    $path = Join-Path $Root $relativePath
    if (-not (Test-Path -LiteralPath $path)) {
        $failures.Add("schema file missing: $relativePath")
        continue
    }

    $text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
    $matches = [regex]::Matches(
        $text,
        "CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+([a-z0-9_]+)\s*\((?<body>.*?)\)\s*ENGINE\s*=\s*InnoDB(?<tail>.*?);",
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor [System.Text.RegularExpressions.RegexOptions]::Singleline
    )

    foreach ($match in $matches) {
        $tableName = $match.Groups[1].Value
        $body = $match.Groups["body"].Value
        $tail = $match.Groups["tail"].Value
        $prefix = ($tableName -split "_")[0]
        $isSpringBatchTable = $tableName -like "BATCH_*"

        # Spring Batch 표준 JobRepository 테이블은 프레임워크가 대문자 BATCH_* 이름으로 조회하므로
        # CPF 주제영역 prefix와 공통 감사 컬럼 규칙의 예외로 둡니다.
        if (-not $isSpringBatchTable -and $allowedPrefixes -notcontains $prefix) {
            $failures.Add("table prefix invalid: $relativePath -> $tableName")
        }
        if (-not $isSpringBatchTable) {
            [void] $schemaTableNames.Add($tableName)
        }
        if (-not $isSpringBatchTable -and $tableName.EndsWith("_table")) {
            $failures.Add("table suffix _table is not allowed: $relativePath -> $tableName")
        }
        if ($tail -notmatch "(?i)\bCOMMENT\s*=\s*'[^']+'") {
            $failures.Add("table comment missing: $relativePath -> $tableName")
        }

        if (-not $isSpringBatchTable) {
            foreach ($column in $commonColumns) {
                if ($body -notmatch "(?im)^\s*$column\s+") {
                    $failures.Add("common audit column missing: $relativePath -> $tableName.$column")
                }
            }
        }

        $columnLines = $body -split "`r?`n"
        foreach ($line in $columnLines) {
            $trimmed = $line.Trim()
            if (-not $trimmed) {
                continue
            }
            if ($trimmed -match "^(PRIMARY|CONSTRAINT|FOREIGN|UNIQUE|INDEX|KEY|ON)\b") {
                continue
            }
            if ($trimmed -notmatch "^[A-Za-z_][A-Za-z0-9_]*\s+") {
                continue
            }
            if ($trimmed -notmatch "(?i)\bCOMMENT\s+'[^']+'") {
                $columnName = (($trimmed -split "\s+")[0] -replace '[`",]', '')
                $failures.Add("column comment missing: $relativePath -> $tableName.$columnName")
            }
        }
    }

    $textForNameCheck = [regex]::Replace($text, "'[^']*'", "''")
    $textForNameCheck = [regex]::Replace(
        $textForNameCheck,
        "CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+BATCH_[A-Z0-9_]+\s*\(.*?\)\s*ENGINE\s*=\s*InnoDB.*?;",
        "",
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor [System.Text.RegularExpressions.RegexOptions]::Singleline
    )
    if ($textForNameCheck -cmatch "(?m)\b(CONSTRAINT|INDEX|UNIQUE\s+KEY)\s+(?!IF\b)[A-Z][A-Z0-9_]*\b") {
        $failures.Add("constraint/index name must be lower snake case: $relativePath")
    }
}

# split schema에 있는 테이블이 전체 설치본에 빠지면 신규 DB 재현성이 깨집니다.
# 이 검사는 Flyway에는 있는데 all_install에는 없는 테이블 누락을 조기에 잡기 위한 안전장치입니다.
function Test-AllInstallContainsSchemaTables {
    param(
        [string] $RelativePath
    )

    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path)) {
        $failures.Add("all install file missing: $RelativePath")
        return
    }

    $content = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
    foreach ($tableName in $schemaTableNames) {
        $escapedTableName = [regex]::Escape($tableName)
        if ($content -notmatch "(?is)CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+(?:[a-zA-Z0-9_]+\.)?$escapedTableName\s*\(") {
            $failures.Add("all install create missing: $RelativePath -> $tableName")
        }
    }

    # EDU 조회 샘플은 SQL/Flyway/문서/테스트가 같이 움직이는 대표 샘플이라 seed 누락도 별도 검출합니다.
    if ($content -notmatch "(?is)INSERT\s+INTO\s+(?:[a-zA-Z0-9_]+\.)?cmn_edu_query_item\s*\(") {
        $failures.Add("all install seed missing: $RelativePath -> cmn_edu_query_item")
    }
}

Test-AllInstallContainsSchemaTables "specs/sql/00_all_install.sql"
Test-AllInstallContainsSchemaTables "specs/sql/00_all_install_and_smoke.sql"
Test-AllInstallContainsSchemaTables "specs/sql/migration/flyway/V1__cpf_baseline_install.sql"

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host "SQL standard check passed."
