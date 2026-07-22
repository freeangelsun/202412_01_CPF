param(
    [Parameter(Mandatory = $true)]
    [string]$EvidenceId,
    [Parameter(Mandatory = $true)]
    [ValidateSet("완료", "실패", "미검증")]
    [string]$Status,
    [Parameter(Mandatory = $true)]
    [string]$Command,
    [Parameter(Mandatory = $true)]
    [string]$OutputPath,
    [int]$ExitCode = 0,
    [string]$SourceLog,
    [string]$Profile = "local",
    [string]$Reason = "",
    [string]$ReproduceCommand = "",
    [int]$Tests = 0,
    [int]$Failures = 0,
    [int]$Errors = 0,
    [int]$Skipped = 0,
    [string]$Root = ""
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Root = if ([string]::IsNullOrWhiteSpace($Root)) {
    (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
} else {
    (Resolve-Path $Root).Path
}
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

function Sanitize-Text([string]$Text) {
    if ($null -eq $Text) {
        return ""
    }
    $sanitized = $Text.Replace($Root, "<WORKSPACE>")
    if ($env:USERPROFILE) {
        $sanitized = $sanitized.Replace($env:USERPROFILE, "<USER_HOME>")
    }
    $sanitized = [regex]::Replace(
        $sanitized,
        '(?im)(authorization\s*[:=]\s*)(bearer\s+)?[^\s,;]+',
        '$1***')
    $sanitized = [regex]::Replace(
        $sanitized,
        '(?im)((?:password|passwd|pwd|secret|token|api[_-]?key|credential)\s*[:=]\s*)[^\s,;]+',
        '$1***')
    $sanitized = [regex]::Replace($sanitized, '(?i)jdbc:[^\s]+', '<JDBC_URL>')
    return $sanitized
}

if (-not $OutputPath.EndsWith(".sanitized.log", [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "OutputPath는 .sanitized.log 확장자여야 합니다."
}
if ($Status -eq "완료" -and $ExitCode -ne 0) {
    throw "완료 evidence는 EXIT_CODE=0이어야 합니다."
}
if ($Status -eq "실패" -and $ExitCode -eq 0) {
    throw "실패 evidence는 0이 아닌 EXIT_CODE가 필요합니다."
}
if ($Status -eq "미검증" -and ([string]::IsNullOrWhiteSpace($Reason) -or [string]::IsNullOrWhiteSpace($ReproduceCommand))) {
    throw "미검증 evidence는 사유와 재현 명령이 필요합니다."
}

$payload = ""
if (-not [string]::IsNullOrWhiteSpace($SourceLog)) {
    $sourcePath = if ([System.IO.Path]::IsPathRooted($SourceLog)) { $SourceLog } else { Join-Path $Root $SourceLog }
    if (-not (Test-Path -LiteralPath $sourcePath -PathType Leaf)) {
        throw "SourceLog를 찾을 수 없습니다: $SourceLog"
    }
    $payload = Sanitize-Text ([System.IO.File]::ReadAllText($sourcePath, [System.Text.Encoding]::UTF8))
}

$javaCommand = if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME "bin/java.exe"))) {
    Join-Path $env:JAVA_HOME "bin/java.exe"
} else {
    "java"
}
$javaVersionOutput = @(& $javaCommand --version 2>&1 | ForEach-Object { $_.ToString() })
$javaVersion = Sanitize-Text (($javaVersionOutput | Select-Object -First 1) -join " ")
$wrapperProperties = Join-Path $Root "gradle/wrapper/gradle-wrapper.properties"
$gradleVersion = "unknown"
if (Test-Path -LiteralPath $wrapperProperties) {
    $distribution = Select-String -LiteralPath $wrapperProperties -Pattern 'gradle-([0-9.]+)-' | Select-Object -First 1
    if ($distribution -and $distribution.Matches.Count -gt 0) {
        $gradleVersion = $distribution.Matches[0].Groups[1].Value
    }
}

$startCommit = (& git -C $Root rev-parse HEAD).Trim()
$branch = (& git -C $Root branch --show-current).Trim()
$executedAt = [DateTimeOffset]::Now.ToString("o")
$metadata = @(
    "CPF_EVIDENCE_VERSION=1",
    "EVIDENCE_ID=$(Sanitize-Text $EvidenceId)",
    "STATUS=$Status",
    "EXECUTED_AT=$executedAt",
    "START_COMMIT=$startCommit",
    "BRANCH=$branch",
    "COMMAND=$(Sanitize-Text $Command)",
    "EXIT_CODE=$ExitCode",
    "JAVA_VERSION=$javaVersion",
    "GRADLE_VERSION=$gradleVersion",
    "PROFILE=$(Sanitize-Text $Profile)",
    "SANITIZED=Y",
    "SECRET_SCAN=PASS",
    "TESTS=$Tests",
    "FAILURES=$Failures",
    "ERRORS=$Errors",
    "SKIPPED=$Skipped",
    "REASON=$(Sanitize-Text $Reason)",
    "REPRODUCE_COMMAND=$(Sanitize-Text $ReproduceCommand)"
)

$bodyLines = New-Object System.Collections.Generic.List[string]
$metadata | ForEach-Object { [void]$bodyLines.Add($_) }
if (-not [string]::IsNullOrWhiteSpace($payload)) {
    [void]$bodyLines.Add("--- SANITIZED_OUTPUT ---")
    $payload.TrimEnd("`r", "`n").Replace("`r`n", "`n").Split("`n") |
        ForEach-Object { [void]$bodyLines.Add($_) }
}
$canonical = ($bodyLines -join "`n") + "`n"
$sha256 = [System.Security.Cryptography.SHA256]::Create()
try {
    $hash = ([System.BitConverter]::ToString($sha256.ComputeHash($utf8NoBom.GetBytes($canonical)))).Replace("-", "").ToLowerInvariant()
} finally {
    $sha256.Dispose()
}

$finalLines = New-Object System.Collections.Generic.List[string]
$metadata | ForEach-Object { [void]$finalLines.Add($_) }
[void]$finalLines.Add("LOG_SHA256=$hash")
if (-not [string]::IsNullOrWhiteSpace($payload)) {
    [void]$finalLines.Add("--- SANITIZED_OUTPUT ---")
    $payload.TrimEnd("`r", "`n").Replace("`r`n", "`n").Split("`n") |
        ForEach-Object { [void]$finalLines.Add($_) }
}

$absoluteOutput = if ([System.IO.Path]::IsPathRooted($OutputPath)) { $OutputPath } else { Join-Path $Root $OutputPath }
[System.IO.Directory]::CreateDirectory((Split-Path -Parent $absoluteOutput)) | Out-Null
$finalText = ($finalLines -join "`n") + "`n"
$secretPattern = '(?im)(?:password|passwd|pwd|secret|token|authorization|api[_-]?key|credential)\s*[:=]\s*(?!\*\*\*|$)[^\s]+'
if ([regex]::IsMatch($finalText, $secretPattern)) {
    throw "정제 evidence에서 민감정보 후보가 발견됐습니다."
}
[System.IO.File]::WriteAllText($absoluteOutput, $finalText, $utf8NoBom)
Write-Host "Sanitized evidence written: $OutputPath"
