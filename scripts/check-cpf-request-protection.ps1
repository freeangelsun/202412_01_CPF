param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260714_02"),
    [switch] $InitializeBaseline
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$RequestPath = Join-Path $Root "CPF_NEW_REQUEST.md"
$BaselinePath = Join-Path $ResultDir "cpf-new-request.baseline.sha256"
$ResultPath = Join-Path $ResultDir "cpf-new-request-protection.sanitized.json"

if (-not (Test-Path -LiteralPath $RequestPath)) {
    throw "CPF_NEW_REQUEST.md file is missing."
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
        requestFile = "CPF_NEW_REQUEST.md"
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
        requestFile = "CPF_NEW_REQUEST.md"
        baselineHash = ""
        currentHash = $currentHash
        policy = "Baseline is missing. Run explicit initialization before protected work starts."
    }
    $json = $result | ConvertTo-Json -Depth 5
    [System.IO.File]::WriteAllText($ResultPath, $json, $Utf8NoBom)
    Write-Error "CPF_NEW_REQUEST.md baseline is missing. Run scripts/check-cpf-request-protection.ps1 -InitializeBaseline before protected work starts."
    exit 1
}

$baselineHash = ([System.IO.File]::ReadAllText($BaselinePath, [System.Text.Encoding]::UTF8)).Trim().ToLowerInvariant()
$status = if ($baselineHash -eq $currentHash) { "DONE" } else { "FAILED" }
$result = [ordered]@{
    generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
    status = $status
    mode = "CHECK_ONLY"
    requestFile = "CPF_NEW_REQUEST.md"
    baselineHash = $baselineHash
    currentHash = $currentHash
    policy = "CPF_NEW_REQUEST.md is read-only request input for this work."
}

$json = $result | ConvertTo-Json -Depth 5
[System.IO.File]::WriteAllText($ResultPath, $json, $Utf8NoBom)

if ($status -ne "DONE") {
    Write-Error "CPF_NEW_REQUEST.md changed after baseline. See $ResultPath"
    exit 1
}

Write-Host "CPF request protection check passed."
