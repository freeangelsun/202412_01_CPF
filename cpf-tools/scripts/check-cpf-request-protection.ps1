param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ResultDir = $env:CPF_RESULT_DIR
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path

$BaselineRelativePath = "cpf-docs/work/state/CPF_CODEX_REQUEST_PROTECTION_BASELINE_20260729.json"
$BaselinePath = Join-Path $Root $BaselineRelativePath
if (-not (Test-Path -LiteralPath $BaselinePath -PathType Leaf)) {
    throw "Repository-controlled request baseline is missing: $BaselineRelativePath"
}
$baseline = Get-Content -LiteralPath $BaselinePath -Raw -Encoding UTF8 | ConvertFrom-Json
if ($baseline.schemaVersion -ne 1) {
    throw "Unsupported request protection baseline schemaVersion: $($baseline.schemaVersion)"
}
$RequestFileName = [string] $baseline.requestFile
$RequestPath = Join-Path $Root $RequestFileName
if (-not (Test-Path -LiteralPath $RequestPath -PathType Leaf)) {
    throw "Protected request file is missing: $RequestFileName"
}
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    throw "ResultDir is required. Use Gradle task with -PcpfResultDir=<evidence-directory>."
}
$ResultDir = [System.IO.Path]::GetFullPath($ResultDir)
$ResultPath = Join-Path $ResultDir "cpf-final-validation-request-protection.sanitized.json"
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$currentHash = (Get-FileHash -LiteralPath $RequestPath -Algorithm SHA256).Hash.ToLowerInvariant()
$currentBlob = (& git -C $Root hash-object -- $RequestFileName).Trim().ToLowerInvariant()
if ($LASTEXITCODE -ne 0 -or $currentBlob -notmatch "^[0-9a-f]{40}$") {
    throw "Protected request Git blob hash could not be calculated."
}
$expectedHash = ([string] $baseline.contentSha256).Trim().ToLowerInvariant()
$expectedBlob = ([string] $baseline.gitBlobSha1).Trim().ToLowerInvariant()
$status = if ($currentHash -ceq $expectedHash -and $currentBlob -ceq $expectedBlob) {
    "완료"
} else {
    "실패"
}
$result = [ordered]@{
    generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
    status = $status
    mode = "CHECK_ONLY"
    requestFile = $RequestFileName
    baselineFile = $BaselineRelativePath
    baselineHead = [string] $baseline.baselineHead
    expectedContentSha256 = $expectedHash
    currentContentSha256 = $currentHash
    expectedGitBlobSha1 = $expectedBlob
    currentGitBlobSha1 = $currentBlob
    policy = [string] $baseline.policy
}
[System.IO.File]::WriteAllText(
    $ResultPath,
    ($result | ConvertTo-Json -Depth 8) + [Environment]::NewLine,
    $Utf8NoBom)

if ($status -ne "완료") {
    Write-Error "Protected final validation request changed. See $ResultPath"
    exit 1
}
Write-Host "CPF final validation request protection check passed."
