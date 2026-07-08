param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"

# 트랜잭션 글로벌 ID 표준이 코드, 설정, 문서에 동시에 반영되어 있는지 정적으로 검사합니다.
# 공식 규격: yyyyMMddHHmmssSSS(17) + moduleId(3) + wasId(7) + sequence(7) = 총 34자리.
$failures = New-Object System.Collections.Generic.List[string]

function Read-Text([string] $RelativePath) {
    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path)) {
        $failures.Add("파일 없음: $RelativePath")
        return ""
    }

    return [System.IO.File]::ReadAllText($path, [System.Text.UTF8Encoding]::new($false, $true))
}

function Assert-Contains([string] $RelativePath, [string] $Pattern, [string] $Message) {
    $text = Read-Text $RelativePath
    if ($text -and $text -notmatch $Pattern) {
        $failures.Add("$RelativePath - $Message")
    }
}

Assert-Contains `
    "pfw/src/main/java/cpf/pfw/common/logging/TransactionIdGenerator.java" `
    "MODULE_ID_LENGTH\s*=\s*3" `
    "moduleId 3자리 상수가 필요합니다."

Assert-Contains `
    "pfw/src/main/java/cpf/pfw/common/logging/TransactionIdGenerator.java" `
    "WAS_ID_LENGTH\s*=\s*7" `
    "wasId 7자리 상수가 필요합니다."

Assert-Contains `
    "pfw/src/main/java/cpf/pfw/common/logging/TransactionIdGenerator.java" `
    "DEFAULT_SEQUENCE_DIGITS\s*=\s*7" `
    "sequence 7자리 기본값이 필요합니다."

Assert-Contains `
    "pfw/src/main/java/cpf/pfw/common/web/TransactionHeaderValidationInterceptor.java" `
    "inboundHeaderValidator\.missingRequiredHeaders" `
    "온라인 API 필수 헤더 검사가 필요합니다."

Assert-Contains `
    "pfw/src/main/java/cpf/pfw/common/header/CpfInboundHeaderValidator.java" `
    "TransactionIdGenerator\.isValid" `
    "X-Transaction-Id 형식 검사가 필요합니다."

Assert-Contains `
    "pfw/src/main/java/cpf/pfw/config/PfwOpenApiAutoConfiguration.java" `
    '"X-Transaction-Id"[\s\S]*true[\s\S]*yyyyMMddHHmmssSSS' `
    "OpenAPI 헤더 설명에서 X-Transaction-Id 필수 표시가 필요합니다."

Assert-Contains `
    "pfw/src/main/resources/application-pfw.yml" `
    "module-id:\s*\$\{CPF_APP_MODULE_ID:\$\{CPF_MODULE_ID:[A-Z0-9]{3}\}\}" `
    "PFW module-id 기본값이 3자리여야 합니다."

Assert-Contains `
    "pfw/src/main/resources/application-pfw.yml" `
    "was-id:\s*\$\{CPF_APP_WAS_ID:\$\{WAS_ID:[A-Za-z0-9]{7}\}\}" `
    "PFW was-id 기본값이 7자리여야 합니다."

Assert-Contains `
    "adm/src/main/resources/static/adm/adm.js" `
    "createTransactionGlobalId" `
    "ADM 정적 화면 API 호출용 트랜잭션 ID 생성기가 필요합니다."

Assert-Contains `
    "specs/sql/10_pfw_schema.sql" `
    "SERVER_INSTANCE_ID\s+VARCHAR\(160\)" `
    "거래 로그 서버 인스턴스 컬럼이 필요합니다."

Assert-Contains `
    "specs/sql/70_test_data.sql" `
    "DATE\(@sample_start_time\)" `
    "테스트 거래 로그의 LOG_DATE는 START_TIME 기준이어야 합니다."

Assert-Contains `
    "specs/index.html" `
    "X-Transaction-Id[\s\S]*34" `
    "개발 가이드에 34자리 트랜잭션 ID 규격 설명이 필요합니다."

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Transaction ID standard check passed."
