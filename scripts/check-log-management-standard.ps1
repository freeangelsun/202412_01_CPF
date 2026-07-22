param(
    [string]$Root = (Split-Path -Parent $PSScriptRoot),
    [string]$ResultDir = "build/quality-gate"
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$checks = New-Object System.Collections.Generic.List[object]

function Add-Check([string]$Id, [bool]$Passed, [string]$Detail) {
    $checks.Add([ordered]@{ id = $Id; passed = $Passed; detail = $Detail }) | Out-Null
    if (-not $Passed) {
        Write-Error "$Id failed: $Detail"
    }
}

function Git-Lines([string[]]$Arguments) {
    $output = & git -C $Root @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "git 명령이 실패했습니다: git $($Arguments -join ' ')"
    }
    return @($output | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
}

function Test-SanitizedEvidence([string]$RelativePath) {
    $absolute = Join-Path $Root $RelativePath
    $lines = [System.IO.File]::ReadAllLines($absolute, [System.Text.Encoding]::UTF8)
    $required = @(
        "CPF_EVIDENCE_VERSION", "EVIDENCE_ID", "STATUS", "EXECUTED_AT", "START_COMMIT",
        "BRANCH", "COMMAND", "EXIT_CODE", "JAVA_VERSION", "GRADLE_VERSION", "PROFILE",
        "SANITIZED", "SECRET_SCAN", "TESTS", "FAILURES", "ERRORS", "SKIPPED", "LOG_SHA256"
    )
    $values = @{}
    foreach ($line in $lines) {
        if ($line -match '^([A-Z0-9_]+)=(.*)$') {
            $values[$Matches[1]] = $Matches[2]
        }
    }
    # 작업 시작 상태와 요청서 baseline은 실행 로그가 아니라 변경 전 스냅샷이므로
    # 테스트 건수와 본문 해시를 요구하는 런타임 로그 계약에서 제외합니다.
    if ($values["EVIDENCE_ID"] -like "CPF-START-WORKTREE-*") {
        foreach ($key in @("CPF_EVIDENCE_VERSION", "EVIDENCE_ID", "STATUS", "EXECUTED_AT", "START_COMMIT",
                "BRANCH", "COMMAND", "EXIT_CODE", "JAVA_VERSION", "GRADLE_VERSION", "PROFILE",
                "SANITIZED", "SECRET_SCAN")) {
            if (-not $values.ContainsKey($key)) {
                throw "$RelativePath 시작 스냅샷 메타데이터 누락: $key"
            }
        }
        return
    }
    foreach ($key in $required) {
        if (-not $values.ContainsKey($key)) {
            throw "$RelativePath 필수 메타데이터 누락: $key"
        }
    }
    if ($values["SANITIZED"] -ne "Y" -or $values["SECRET_SCAN"] -ne "PASS") {
        throw "$RelativePath 정제 또는 secret scan 상태가 올바르지 않습니다."
    }
    if ($values["STATUS"] -eq "완료" -and $values["EXIT_CODE"] -ne "0") {
        throw "$RelativePath 완료 상태의 exit code가 0이 아닙니다."
    }
    $canonicalLines = $lines | Where-Object { $_ -notmatch '^LOG_SHA256=' }
    $canonical = ($canonicalLines -join "`n") + "`n"
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    $sha256 = [System.Security.Cryptography.SHA256]::Create()
    try {
        $actual = ([System.BitConverter]::ToString($sha256.ComputeHash($utf8NoBom.GetBytes($canonical)))).Replace("-", "").ToLowerInvariant()
    } finally {
        $sha256.Dispose()
    }
    if ($actual -ne $values["LOG_SHA256"]) {
        throw "$RelativePath LOG_SHA256가 정제 본문과 일치하지 않습니다."
    }
}

$gitignore = Get-Content -LiteralPath (Join-Path $Root ".gitignore") -Encoding UTF8
foreach ($rule in @("logs/", "**/logs/", "*.log", "*.log.*", "!cpf-docs/evidence/**/*.sanitized.log")) {
    Add-Check "GITIGNORE_$rule" ($gitignore -contains $rule) $rule
}

$rawLogs = @(Git-Lines @(
    "ls-files", "--",
    ":(glob)logs/**", ":(glob)*/logs/**", ":(glob)**/*.log", ":(glob)**/*.log.*",
    ":(exclude,glob)cpf-docs/evidence/**/*.sanitized.log"
) | Where-Object { Test-Path -LiteralPath (Join-Path $Root $_) })
Add-Check "TRACKED_RAW_LOGS" ($rawLogs.Count -eq 0) "count=$($rawLogs.Count)"

$activeLogbackReferences = @(& rg -n --glob "application*.yml" --glob "application*.yaml" `
        'logback-(?:local|dev|test|prod)\.xml' $Root 2>$null)
if ($LASTEXITCODE -notin @(0, 1)) {
    throw "레거시 Logback 설정 참조 검색에 실패했습니다."
}
Add-Check "LEGACY_LOGBACK_REFERENCE" ($activeLogbackReferences.Count -eq 0) "count=$($activeLogbackReferences.Count)"

$legacyBatchLogReferences = @(& rg -n 'cpf-bat-batch\.log' `
        (Join-Path $Root "scripts") (Join-Path $Root "cpf-core") (Join-Path $Root "cpf-batch") (Join-Path $Root "cpf-admin") 2>$null)
if ($LASTEXITCODE -notin @(0, 1)) {
    throw "레거시 BAT 단일 로그 참조 검색에 실패했습니다."
}
Add-Check "LEGACY_BATCH_SINGLE_FILE" ($legacyBatchLogReferences.Count -eq 0) "count=$($legacyBatchLogReferences.Count)"

$duplicateLogRoots = @(& rg -n --glob "application*.yml" --glob "application*.yaml" `
        '(?:\./)?(?:cpf-account|cpf-admin|cpf-batch|cpf-biz-admin|cpf-external|cpf-member|cpf-reference)[\\/]logs' $Root 2>$null)
if ($LASTEXITCODE -notin @(0, 1)) {
    throw "중복 로그 root 검색에 실패했습니다."
}
Add-Check "DUPLICATE_MODULE_LOG_ROOT" ($duplicateLogRoots.Count -eq 0) "count=$($duplicateLogRoots.Count)"

$logbackPath = Join-Path $Root "cpf-core/src/main/resources/log/cpf-logback-spring.xml"
$logback = Get-Content -LiteralPath $logbackPath -Raw -Encoding UTF8
[xml]$logbackXml = $logback
$siftingAppenders = @($logbackXml.SelectNodes('//appender[contains(@class, "SiftingAppender")]'))
$logPathText = @($logbackXml.SelectNodes('//file|//fileNamePattern') | ForEach-Object { $_.InnerText }) -join "`n"
Add-Check "NO_TRANSACTION_APPENDER" `
    ($siftingAppenders.Count -eq 0 -and $logPathText -notmatch 'NO_TRANSACTION') "shared logback"
Add-Check "DAILY_INSTANCE_FILENAME" ($logback -match 'cpf-\$\{MODULE_CODE\}-application-\$\{INSTANCE_ID\}.%d\{yyyy-MM-dd') "shared logback"
Add-Check "RETENTION" ($logback -match '<maxHistory>\$\{MAX_HISTORY\}</maxHistory>') "shared logback"
Add-Check "ARCHIVE_COMPRESSION" ($logback -match '\.log\.gz</fileNamePattern>') "shared logback"

$cpfConfig = Get-Content -LiteralPath (Join-Path $Root "cpf-core/src/main/resources/application-cpf.yml") -Raw -Encoding UTF8
foreach ($marker in @("CPF_LOG_ROOT", "CPF_LOG_TIMEZONE", "CPF_LOG_MAX_HISTORY_DAYS", "CPF_INSTANCE_ID", "CPF_ENV")) {
    Add-Check "CPF_LOG_CONFIG_$marker" ($cpfConfig.Contains($marker)) $marker
}

$newEvidenceRoot = if ([System.IO.Path]::IsPathRooted($ResultDir)) {
    [System.IO.Path]::GetFullPath($ResultDir)
} else {
    [System.IO.Path]::GetFullPath((Join-Path $Root $ResultDir))
}
$newEvidence = @()
if (Test-Path -LiteralPath $newEvidenceRoot) {
    $newEvidence = Get-ChildItem -LiteralPath $newEvidenceRoot -Recurse -File -Filter "*.sanitized.log"
}
foreach ($file in $newEvidence) {
    Test-SanitizedEvidence ($file.FullName.Substring($Root.Length + 1).Replace('\\', '/'))
}
Add-Check "NEW_SANITIZED_EVIDENCE" ($newEvidence.Count -gt 0) "count=$($newEvidence.Count)"

$result = [ordered]@{
    status = "완료"
    checkedAt = [DateTimeOffset]::Now.ToString("o")
    trackedRawLogCount = $rawLogs.Count
    sanitizedEvidenceCount = $newEvidence.Count
    checks = $checks
}
$resultPath = Join-Path $newEvidenceRoot "log-management-standard.sanitized.json"
[System.IO.Directory]::CreateDirectory((Split-Path -Parent $resultPath)) | Out-Null
[System.IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 8),
    (New-Object System.Text.UTF8Encoding($false)))
Write-Host "Log management standard check passed."
