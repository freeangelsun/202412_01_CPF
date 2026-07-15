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
    "module-id:\s*\$\{[^\r\n]*:[A-Z0-9]{3}\}\}\}" `
    "PFW module-id 기본값이 3자리여야 합니다."

Assert-Contains `
    "pfw/src/main/resources/application-pfw.yml" `
    "was-id:\s*\$\{[^\r\n]*:[A-Za-z0-9]{7}\}\}\}" `
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
    "README.md" `
    "X-Transaction-Id[\s\S]*34" `
    "개발 가이드에 34자리 트랜잭션 ID 규격 설명이 필요합니다."

# 런타임 기동 시 표준 실행 카탈로그가 중복 ID를 거부하므로 빌드 단계에서도 같은 결함을 차단합니다.
$executionIdPattern = '^[OB][A-Z]{3}-[A-Z0-9]{3}-[A-Z0-9]{2}-(?!0000)[0-9]{4}$'
$executionAnnotations = New-Object System.Collections.Generic.List[object]
$moduleDirectories = Get-ChildItem -LiteralPath $Root -Directory | Where-Object {
    Test-Path -LiteralPath (Join-Path $_.FullName "src/main/java")
}

foreach ($moduleDirectory in $moduleDirectories) {
    $javaRoot = Join-Path $moduleDirectory.FullName "src/main/java"
    foreach ($javaFile in Get-ChildItem -LiteralPath $javaRoot -Recurse -File -Filter "*.java") {
        $source = [System.IO.File]::ReadAllText($javaFile.FullName, [System.Text.UTF8Encoding]::new($false, $true))
        $annotations = [regex]::Matches(
            $source,
            '@Cpf(?:OnlineTransaction|BatchJob)\s*\((?<body>[\s\S]*?)\)',
            [System.Text.RegularExpressions.RegexOptions]::CultureInvariant
        )

        foreach ($annotation in $annotations) {
            $idMatch = [regex]::Match($annotation.Groups['body'].Value, '\bid\s*=\s*"(?<id>[^"]+)"')
            $relativePath = $javaFile.FullName.Substring($Root.Length) -replace '^[\\/]+', '' -replace '\\', '/'
            if (-not $idMatch.Success) {
                $failures.Add("$relativePath - 표준 실행 애노테이션의 id는 문자열 리터럴로 선언해야 합니다.")
                continue
            }

            $executionId = $idMatch.Groups['id'].Value
            if ($executionId -notmatch $executionIdPattern) {
                $failures.Add("$relativePath - 표준 실행 ID 형식 오류: $executionId")
            }

            $executionAnnotations.Add([pscustomobject]@{
                Id = $executionId
                Path = $relativePath
            })
        }
    }
}

foreach ($duplicate in $executionAnnotations | Group-Object -Property Id | Where-Object Count -gt 1) {
    $locations = ($duplicate.Group | ForEach-Object Path | Sort-Object -Unique) -join ', '
    $failures.Add("표준 실행 ID 중복: $($duplicate.Name) ($locations)")
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Transaction ID standard check passed."
