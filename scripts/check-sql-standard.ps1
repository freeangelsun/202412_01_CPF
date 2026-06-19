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
    "specs/sql/40_business_sample_schema.sql"
)
$allowedPrefixes = @("pfw", "cmn", "adm", "acc", "mbr", "bizadm", "exs")
$commonColumns = @("created_by", "created_at", "updated_by", "updated_at")
$failures = New-Object System.Collections.Generic.List[string]

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

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host "SQL standard check passed."
