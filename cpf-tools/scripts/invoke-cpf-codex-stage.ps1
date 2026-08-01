[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][ValidatePattern("^[A-Za-z0-9_.-]+$")][string]$StageId,
    [Parameter(Mandatory = $true)][string]$Command,
    [string]$WorkingDirectory = "C:\dev\projects\jck\202412_01_CPF",
    [string]$LedgerRoot = "C:\dev\Docker\CPF\output\codex\qa37",
    [switch]$AllowRerun
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $WorkingDirectory -PathType Container)) {
    throw "Working directory not found: $WorkingDirectory"
}

New-Item -ItemType Directory -Path $LedgerRoot -Force | Out-Null
$logRoot = Join-Path $LedgerRoot "logs"
New-Item -ItemType Directory -Path $logRoot -Force | Out-Null

$ledgerPath = Join-Path $LedgerRoot "execution-ledger.csv"
$previous = @()
if (Test-Path -LiteralPath $ledgerPath -PathType Leaf) {
    $previous = @(Import-Csv -LiteralPath $ledgerPath)
}

$lastStageResult = @(
    $previous |
    Where-Object { $_.stageId -eq $StageId } |
    Select-Object -Last 1
)

if ($lastStageResult.Count -gt 0 -and $lastStageResult[0].status -eq "PASS" -and -not $AllowRerun) {
    Write-Host "SKIP: Stage '$StageId' already passed."
    Write-Host "Ledger: $ledgerPath"
    exit 0
}

if ($lastStageResult.Count -gt 0 -and $lastStageResult[0].status -eq "FAIL" -and -not $AllowRerun) {
    throw "Stage '$StageId' previously failed. Fix the root cause and rerun with -AllowRerun."
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logPath = Join-Path $logRoot "$StageId-$timestamp.log"

$commandBytes = [Text.Encoding]::UTF8.GetBytes($Command)
$sha = [Security.Cryptography.SHA256]::Create()
$commandHash = ([BitConverter]::ToString($sha.ComputeHash($commandBytes))).Replace("-", "").ToLowerInvariant()

$gitHead = ""
try {
    $gitHead = (& git -C $WorkingDirectory rev-parse HEAD 2>$null).Trim()
} catch {
    $gitHead = ""
}

$startedAt = Get-Date
Write-Host "START: $StageId"
Write-Host "WORKDIR: $WorkingDirectory"
Write-Host "LOG: $logPath"

Push-Location -LiteralPath $WorkingDirectory
try {
    & pwsh -NoProfile -Command $Command 2>&1 |
        Tee-Object -FilePath $logPath
    $exitCode = $LASTEXITCODE
} finally {
    Pop-Location
}
$endedAt = Get-Date

if (-not (Test-Path -LiteralPath $logPath -PathType Leaf)) {
    New-Item -ItemType File -Path $logPath -Force | Out-Null
}

$logHash = (Get-FileHash -LiteralPath $logPath -Algorithm SHA256).Hash.ToLowerInvariant()
$status = if ($exitCode -eq 0) { "PASS" } else { "FAIL" }

$row = [pscustomobject]@{
    stageId = $StageId
    status = $status
    startedAt = $startedAt.ToString("o")
    endedAt = $endedAt.ToString("o")
    durationSeconds = [Math]::Round(($endedAt - $startedAt).TotalSeconds, 3)
    exitCode = $exitCode
    gitHead = $gitHead
    workingDirectory = $WorkingDirectory
    commandHash = $commandHash
    logPath = $logPath
    logSha256 = $logHash
}

if (Test-Path -LiteralPath $ledgerPath -PathType Leaf) {
    $row | Export-Csv -LiteralPath $ledgerPath -NoTypeInformation -Append -Encoding utf8
} else {
    $row | Export-Csv -LiteralPath $ledgerPath -NoTypeInformation -Encoding utf8
}

Write-Host "END: $StageId status=$status exit=$exitCode"
Write-Host "LEDGER: $ledgerPath"
Write-Host "LOG SHA-256: $logHash"

exit $exitCode
