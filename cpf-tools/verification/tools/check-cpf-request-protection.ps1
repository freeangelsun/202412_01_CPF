param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = $env:CPF_RESULT_DIR
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path

$RequestFileName = "cpf-docs/work/current/CODEX_FINAL_RUNTIME_VALIDATION_INSTRUCTION.md"
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

$baselineHead = (& git -C $Root rev-parse HEAD).Trim().ToLowerInvariant()
if ($LASTEXITCODE -ne 0 -or $baselineHead -notmatch "^[0-9a-f]{40}$") {
    throw "Repository HEAD could not be resolved for request protection."
}
$expectedBlob = (& git -C $Root rev-parse "$baselineHead`:$RequestFileName").Trim().ToLowerInvariant()
if ($LASTEXITCODE -ne 0 -or $expectedBlob -notmatch "^[0-9a-f]{40}$") {
    throw "Protected request is not tracked at HEAD: $RequestFileName"
}

function Get-GitBlobSha256([string] $BlobId) {
    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = "git"
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    foreach ($argument in @("-C", $Root, "cat-file", "blob", $BlobId)) {
        [void] $startInfo.ArgumentList.Add($argument)
    }
    $process = [System.Diagnostics.Process]::Start($startInfo)
    $sha256 = [System.Security.Cryptography.SHA256]::Create()
    try {
        $hashBytes = $sha256.ComputeHash($process.StandardOutput.BaseStream)
        $process.WaitForExit()
        if ($process.ExitCode -ne 0) {
            $stderr = $process.StandardError.ReadToEnd()
            throw "Protected request Git blob could not be read: $stderr"
        }
        return [Convert]::ToHexString($hashBytes).ToLowerInvariant()
    } finally {
        $sha256.Dispose()
        $process.Dispose()
    }
}

$currentHash = (Get-FileHash -LiteralPath $RequestPath -Algorithm SHA256).Hash.ToLowerInvariant()
$currentBlob = (& git -C $Root hash-object -- $RequestFileName).Trim().ToLowerInvariant()
if ($LASTEXITCODE -ne 0 -or $currentBlob -notmatch "^[0-9a-f]{40}$") {
    throw "Protected request Git blob hash could not be calculated."
}
$expectedHash = Get-GitBlobSha256 $expectedBlob
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
    baselineFile = "git:$baselineHead`:$RequestFileName"
    baselineHead = $baselineHead
    expectedContentSha256 = $expectedHash
    currentContentSha256 = $currentHash
    expectedGitBlobSha1 = $expectedBlob
    currentGitBlobSha1 = $currentBlob
    policy = "The tracked final Runtime validation instruction is immutable during validation; Git HEAD is the repository-controlled baseline."
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
