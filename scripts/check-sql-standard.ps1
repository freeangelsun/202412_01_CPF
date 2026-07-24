param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

# 공식 설치 기준이 되는 현행 스키마 SQL만 검사합니다.
# archive 폴더는 과거 전환용 자료이므로 공식 naming/comment gate 대상에서 제외합니다.
$schemaFiles = @(
    "specs/sql/10_cpf_schema.sql",
    "specs/sql/20_cmn_schema.sql",
    "specs/sql/30_adm_schema.sql",
    "specs/sql/35_bat_schema.sql",
    "specs/sql/40_business_modules_schema.sql",
    "specs/sql/45_external_schema.sql"
)
$allowedPrefixes = @("cpf", "cmn", "adm", "bat", "ref", "mbr", "bza", "acc", "exs")
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
    $tableMatches = [regex]::Matches(
        $text,
        "CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+([a-z0-9_]+)\s*\((?<body>.*?)\)\s*ENGINE\s*=\s*InnoDB(?<tail>.*?);",
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor [System.Text.RegularExpressions.RegexOptions]::Singleline
    )

    foreach ($tableMatch in $tableMatches) {
        $tableName = $tableMatch.Groups[1].Value
        $body = $tableMatch.Groups["body"].Value
        $tail = $tableMatch.Groups["tail"].Value
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
            if ($trimmed -match "^(PRIMARY|CONSTRAINT|FOREIGN|UNIQUE|INDEX|KEY|ON|AND|OR|CHECK)\b") {
                continue
            }
            if ($trimmed -notmatch "(?i)^[A-Za-z_][A-Za-z0-9_]*\s+(BIGINT|INT|SMALLINT|TINYINT|VARCHAR|CHAR|TEXT|LONGTEXT|DATETIME|TIMESTAMP|DATE|TIME|DECIMAL|NUMERIC|BOOLEAN|BLOB|JSON)\b") {
                continue
            }
            if ($trimmed.IndexOf(" COMMENT '", [System.StringComparison]::OrdinalIgnoreCase) -lt 0) {
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

}

Test-AllInstallContainsSchemaTables "specs/sql/00_all_install.sql"
Test-AllInstallContainsSchemaTables "specs/sql/00_all_install_and_smoke.sql"

# V1은 정식 출시 전 CPF 명명 표준으로 다시 확정한 기준선입니다.
# 공식 REF/EXS 확장은 V37 expand-contract migration에도 존재해야 합니다.
$v37Path = Join-Path $Root "specs/sql/migration/flyway/V37__official_ref_external_expansion.sql"
if (-not (Test-Path -LiteralPath $v37Path)) {
    $failures.Add("official REF/EXS migration missing: specs/sql/migration/flyway/V37__official_ref_external_expansion.sql")
} else {
    $v37 = [System.IO.File]::ReadAllText($v37Path, [System.Text.Encoding]::UTF8)
    foreach ($marker in @("ref_center_cut_sample_target", "ref_center_cut_sample_result", "exs_execution", "exs_reconciliation_log", "REF", "EXS")) {
        if ($v37 -notmatch [regex]::Escape($marker)) {
            $failures.Add("official REF/EXS migration marker missing: $marker")
        }
    }
}

# 현행 설치 스키마와 시드는 cpf-core 시스템 코드와 DB 객체명을 직접 사용해야 합니다.
$cpfSchema = [System.IO.File]::ReadAllText((Join-Path $Root "specs/sql/10_cpf_schema.sql"), [System.Text.Encoding]::UTF8)
$cmnSchema = [System.IO.File]::ReadAllText((Join-Path $Root "specs/sql/20_cmn_schema.sql"), [System.Text.Encoding]::UTF8)
$cpfSeed = [System.IO.File]::ReadAllText((Join-Path $Root "specs/sql/50_framework_seed_data.sql"), [System.Text.Encoding]::UTF8)
foreach ($marker in @("cpfDB", "cpf_transaction_log", "cpf_message", "cpf_response_code")) {
    if ($cpfSchema -notmatch [regex]::Escape($marker)) {
        $failures.Add("cpf-core schema marker missing: $marker")
    }
}
foreach ($marker in @("'CPF'", "MCPF", "SCPF", "ECPF")) {
    if ($cpfSeed -notmatch [regex]::Escape($marker)) {
        $failures.Add("cpf-core seed marker missing: $marker")
    }
}

# cpf-common은 DB-less가 기본이고, 선택형 교육 DB에는 공통 Template인 cmn_sample_item 하나만 허용합니다.
$cmnTableMatches = [regex]::Matches(
    $cmnSchema,
    "CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+([a-z0-9_]+)\s*\(",
    [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
)
if ($cmnTableMatches.Count -ne 1 -or $cmnTableMatches[0].Groups[1].Value -ine "cmn_sample_item") {
    $cmnTables = @($cmnTableMatches | ForEach-Object { $_.Groups[1].Value }) -join ", "
    $failures.Add("cmnDB must contain exactly one optional table cmn_sample_item; actual=[$cmnTables]")
}
foreach ($forbiddenMarker in @("cmn_sequence", "cmn_edu_query_item", "cmn_fixed_length_", "cmn_notification_log", "cmn_business_log")) {
    if ($cmnSchema -match [regex]::Escape($forbiddenMarker)) {
        $failures.Add("cmnDB stale object remains: $forbiddenMarker")
    }
}

$batSchema = [System.IO.File]::ReadAllText((Join-Path $Root "specs/sql/35_bat_schema.sql"), [System.Text.Encoding]::UTF8)
$databaseProvisioning = [System.IO.File]::ReadAllText((Join-Path $Root "specs/sql/01_create_databases.sql"), [System.Text.Encoding]::UTF8)
$userProvisioning = [System.IO.File]::ReadAllText((Join-Path $Root "specs/sql/02_create_service_users.sql"), [System.Text.Encoding]::UTF8)
if ($databaseProvisioning -notmatch "(?i)CREATE\s+DATABASE\s+IF\s+NOT\s+EXISTS\s+batDB") {
    $failures.Add("batDB provisioning is missing")
}
foreach ($marker in @(
    "cpf_bat_migration",
    "cpf_bat_app",
    "ON batDB.* TO 'cpf_bat_migration'@'localhost'",
    "ON batDB.* TO 'cpf_bat_app'@'localhost'"
)) {
    if ($userProvisioning -notmatch [regex]::Escape($marker)) {
        $failures.Add("BAT service-account provisioning marker missing: $marker")
    }
}
if ($batSchema -notmatch "(?im)^\s*USE\s+batDB\s*;") {
    $failures.Add("BAT schema must target batDB")
}
foreach ($marker in @(
    "BATCH_JOB_INSTANCE",
    "BATCH_JOB_EXECUTION",
    "bat_job",
    "bat_schedule",
    "bat_worker",
    "bat_execution",
    "bat_execution_lease",
    "bat_business_day_calendar",
    "bat_center_cut_job",
    "bat_on_demand_request"
)) {
    if ($batSchema -notmatch [regex]::Escape($marker)) {
        $failures.Add("BAT schema marker missing: $marker")
    }
}
if ($cpfSchema -match "(?i)CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+(?:BATCH_|cpf_batch_|bat_center_cut_|cpf_business_day_calendar)") {
    $failures.Add("BAT runtime table remains in cpf-core schema")
}
foreach ($sequenceName in @("BATCH_STEP_EXECUTION_SEQ", "BATCH_JOB_EXECUTION_SEQ", "BATCH_JOB_SEQ")) {
    if ($batSchema -match "(?is)CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+$sequenceName\s*\(") {
        $failures.Add("Spring Batch MariaDB sequence must not be implemented as a table: $sequenceName")
    }
    if ($batSchema -notmatch "(?is)CREATE\s+SEQUENCE\s+IF\s+NOT\s+EXISTS\s+$sequenceName\s+START\s+WITH\s+1\s+MINVALUE\s+1\s+MAXVALUE\s+9223372036854775806\s+INCREMENT\s+BY\s+1\s+NOCACHE\s+NOCYCLE\s+ENGINE\s*=\s*InnoDB\s*;") {
        $failures.Add("Spring Batch 5.2.4 MariaDB sequence contract missing: $sequenceName")
    }
}
$productSeed = [System.IO.File]::ReadAllText(
    (Join-Path $Root "specs/sql/50_framework_seed_data.sql"),
    [System.Text.Encoding]::UTF8
)
foreach ($sequenceName in @("BATCH_STEP_EXECUTION_SEQ", "BATCH_JOB_EXECUTION_SEQ", "BATCH_JOB_SEQ")) {
    if ($productSeed -match "(?i)INSERT\s+INTO\s+(?:batDB\.)?$sequenceName") {
        $failures.Add("MariaDB sequence must not be row-seeded: $sequenceName")
    }
}

# 실제 Runtime consumer가 없거나 현행 정본 원장과 중복되어 Empty Install에서 제외한
# object가 다시 추가되지 않도록 active schema만 검사합니다. Historical migration은 불변입니다.
$forbiddenActiveTables = [ordered]@{
    "specs/sql/10_cpf_schema.sql" = @("cpf_file_exchange_log")
    "specs/sql/30_adm_schema.sql" = @("adm_operation_log")
    "specs/sql/40_business_modules_schema.sql" = @("bza_user_role")
    "specs/sql/45_external_schema.sql" = @(
        "exs_token_store",
        "exs_token_event_history",
        "exs_route_rule",
        "exs_transaction_log",
        "exs_message_log",
        "exs_retry_log"
    )
}
foreach ($entry in $forbiddenActiveTables.GetEnumerator()) {
    $activeSchema = [System.IO.File]::ReadAllText(
        (Join-Path $Root $entry.Key),
        [System.Text.Encoding]::UTF8
    )
    foreach ($tableName in $entry.Value) {
        if ($activeSchema -match "(?i)CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+$tableName\s*\(") {
            $failures.Add("consumer-less or duplicate table returned to Empty Install: $tableName")
        }
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host "SQL standard check passed."
