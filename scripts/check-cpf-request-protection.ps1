param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = $env:CPF_RESULT_DIR,
    [switch] $InitializeBaseline
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$RequestFileName = "CPF_CURRENT_WORK_REQUEST.md"
$RequestPath = Join-Path $Root $RequestFileName

# 회차별 증적 경로를 명시해 과거 기준 해시를 현재 요청서 검증에 재사용하지 않도록 차단합니다.
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    throw "ResultDir is required. Use Gradle task with -PcpfResultDir=<evidence-directory>."
}

$BaselinePath = Join-Path $ResultDir "cpf-current-work-request.baseline.sha256"
$ResultPath = Join-Path $ResultDir "cpf-current-work-request-protection.sanitized.json"

if (-not (Test-Path -LiteralPath $RequestPath)) {
    throw "$RequestFileName file is missing."
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$currentHash = (Get-FileHash -LiteralPath $RequestPath -Algorithm SHA256).Hash.ToLowerInvariant()

# 요청서 보호 기준은 작업 착수 시 명시적으로 초기화하고, qualityGate에서는 검사만 수행합니다.
if ($InitializeBaseline) {
    [System.IO.File]::WriteAllText($BaselinePath, $currentHash, $Utf8NoBom)
    $result = [ordered]@{
        generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
        status = "DONE"
        mode = "INITIALIZE_BASELINE"
        requestFile = $RequestFileName
        baselineHash = $currentHash
        currentHash = $currentHash
        policy = "Baseline is initialized only by explicit command. qualityGate runs check-only mode."
    }
    $json = $result | ConvertTo-Json -Depth 5
    [System.IO.File]::WriteAllText($ResultPath, $json, $Utf8NoBom)
    Write-Host "CPF request protection baseline initialized."
    return
}

if (-not (Test-Path -LiteralPath $BaselinePath)) {
    $result = [ordered]@{
        generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
        status = "FAILED"
        mode = "CHECK_ONLY"
        requestFile = $RequestFileName
        baselineHash = ""
        currentHash = $currentHash
        policy = "Baseline is missing. Run explicit initialization before protected work starts."
    }
    $json = $result | ConvertTo-Json -Depth 5
    [System.IO.File]::WriteAllText($ResultPath, $json, $Utf8NoBom)
    Write-Error "$RequestFileName baseline is missing. Run initializeCpfRequestProtection with the same cpfResultDir before protected work starts."
    exit 1
}

$baselineHash = ([System.IO.File]::ReadAllText($BaselinePath, [System.Text.Encoding]::UTF8)).Trim().ToLowerInvariant()
$status = if ($baselineHash -eq $currentHash) { "DONE" } else { "FAILED" }
$result = [ordered]@{
    generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
    status = $status
    mode = "CHECK_ONLY"
    requestFile = $RequestFileName
    baselineHash = $baselineHash
    currentHash = $currentHash
    policy = "$RequestFileName is read-only request input for this work."
}

$json = $result | ConvertTo-Json -Depth 5
[System.IO.File]::WriteAllText($ResultPath, $json, $Utf8NoBom)

if ($status -ne "DONE") {
    Write-Error "$RequestFileName changed after baseline. See $ResultPath"
    exit 1
}

Write-Host "CPF request protection check passed."
