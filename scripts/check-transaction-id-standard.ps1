param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

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
    "cpf-core/src/main/java/com/cpf/core/common/logging/TransactionIdGenerator.java" `
    "MODULE_ID_LENGTH\s*=\s*3" `
    "moduleId 3자리 상수가 필요합니다."

Assert-Contains `
    "cpf-core/src/main/java/com/cpf/core/common/logging/TransactionIdGenerator.java" `
    "WAS_ID_LENGTH\s*=\s*7" `
    "wasId 7자리 상수가 필요합니다."

Assert-Contains `
    "cpf-core/src/main/java/com/cpf/core/common/logging/TransactionIdGenerator.java" `
    "DEFAULT_SEQUENCE_DIGITS\s*=\s*7" `
    "sequence 7자리 기본값이 필요합니다."

Assert-Contains `
    "cpf-core/src/main/java/com/cpf/core/common/web/TransactionHeaderValidationInterceptor.java" `
    "inboundHeaderValidator\.missingRequiredHeaders" `
    "온라인 API 필수 헤더 검사가 필요합니다."

Assert-Contains `
    "cpf-core/src/main/java/com/cpf/core/common/header/CpfInboundHeaderValidator.java" `
    "TransactionIdGenerator\.isValid" `
    "X-Transaction-Id 형식 검사가 필요합니다."

Assert-Contains `
    "cpf-core/src/main/java/com/cpf/core/config/CpfOpenApiAutoConfiguration.java" `
    '"X-Transaction-Id"[\s\S]*true[\s\S]*yyyyMMddHHmmssSSS' `
    "OpenAPI 헤더 설명에서 X-Transaction-Id 필수 표시가 필요합니다."

Assert-Contains `
    "cpf-core/src/main/resources/application-cpf.yml" `
    "module-id:\s*\$\{[^\r\n]*:CPF\}\}\}" `
    "cpf-core module-id 기본값은 CPF여야 합니다."

Assert-Contains `
    "cpf-core/src/main/java/com/cpf/core/common/system/CpfSystemCodes.java" `
    'public\s+static\s+final\s+String\s+CORE\s*=\s*"CPF"' `
    "cpf-core 공식 시스템 코드 상수가 CPF여야 합니다."

Assert-Contains `
    "cpf-core/src/main/resources/application-cpf.yml" `
    "was-id:\s*\$\{[^\r\n]*:[A-Za-z0-9]{7}\}\}\}" `
    "CPF was-id 기본값이 7자리여야 합니다."

Assert-Contains `
    "cpf-admin/frontend/src/shared/transaction.ts" `
    "createTransactionGlobalId" `
    "ADM 정적 화면 API 호출용 트랜잭션 ID 생성기가 필요합니다."

Assert-Contains `
    "specs/sql/10_cpf_schema.sql" `
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
$executionIdPattern = '^[OSB][A-Z]{3}[A-Z0-9]{2}(?!0000)[0-9]{4}$'
$legacyExecutionIdPattern = '[OB][A-Z]{3}-[A-Z0-9]{3}-[A-Z0-9]{2}-[0-9]{4}'
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
            '@Cpf(?<type>OnlineTransaction|SharedApi|BatchJob)\s*\((?<body>[\s\S]*?)\)',
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

            $expectedPrefix = switch ($annotation.Groups['type'].Value) {
                'OnlineTransaction' { 'O' }
                'SharedApi' { 'S' }
                'BatchJob' { 'B' }
            }
            if (-not $executionId.StartsWith($expectedPrefix, [StringComparison]::Ordinal)) {
                $failures.Add("$relativePath - 애노테이션 유형과 실행 ID prefix가 다릅니다: $executionId")
            }

            $executionAnnotations.Add([pscustomobject]@{
                Id = $executionId
                Path = $relativePath
            })
        }
    }
}

# 신규 소스와 SQL에 구형 하이픈 ID가 다시 들어오면 alias 정책과 관계없이 빌드를 차단합니다.
$legacyScanRoots = @(
    'cpf-core', 'cpf-common', 'cpf-member', 'cpf-reference', 'cpf-admin',
    'cpf-biz-admin', 'cpf-batch', 'cpf-account', 'cpf-external', 'cpf-gateway',
    'specs/sql'
)
foreach ($relativeRoot in $legacyScanRoots) {
    $scanRoot = Join-Path $Root $relativeRoot
    if (-not (Test-Path -LiteralPath $scanRoot)) {
        continue
    }
    foreach ($file in Get-ChildItem -LiteralPath $scanRoot -Recurse -File | Where-Object {
            $_.Extension -in @('.java', '.xml', '.yml', '.yaml', '.sql', '.json', '.gradle') -and
            $_.FullName -notmatch '[\\/]build[\\/]' -and
            $_.FullName -notmatch '[\\/]src[\\/]test[\\/]' -and
            $_.FullName -notmatch '[\\/]evidence[\\/]' -and
            $_.Name -notin @('V32__standard_execution_id_v2.sql', '52_standard_execution_alias_seed.sql')
        }) {
        $content = [IO.File]::ReadAllText($file.FullName, [Text.Encoding]::UTF8)
        if ($file.Extension -eq '.sql') {
            # 합본 설치 SQL에는 조회 호환을 위한 구형 ID alias seed가 의도적으로 포함됩니다.
            # alias INSERT 구간만 검사 대상에서 제거하고 그 밖의 SQL에서 구형 ID가 재사용되면 계속 실패시킵니다.
            $content = [regex]::Replace(
                $content,
                'INSERT\s+INTO\s+cpf_standard_execution_alias\s*\([\s\S]*?updated_at\s*=\s*CURRENT_TIMESTAMP\s*;',
                '',
                [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
        }
        $legacyMatches = [regex]::Matches($content, $legacyExecutionIdPattern)
        if ($legacyMatches.Count -gt 0) {
            $relativePath = $file.FullName.Substring($Root.Length).TrimStart('\', '/') -replace '\\', '/'
            $ids = @($legacyMatches | ForEach-Object Value | Sort-Object -Unique) -join ', '
            $failures.Add("$relativePath - 구형 표준 실행 ID 사용: $ids")
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
