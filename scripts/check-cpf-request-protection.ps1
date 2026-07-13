param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260710_01")
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
if (-not (Test-Path -LiteralPath $BaselinePath)) {
    [System.IO.File]::WriteAllText($BaselinePath, $currentHash, $Utf8NoBom)
}

$baselineHash = ([System.IO.File]::ReadAllText($BaselinePath, [System.Text.Encoding]::UTF8)).Trim().ToLowerInvariant()
$status = if ($baselineHash -eq $currentHash) { "DONE" } else { "FAILED" }
$result = [ordered]@{
    generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
    status = $status
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
