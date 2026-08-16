[CmdletBinding()]
param(
    [string]$DockerRoot = "C:\dev\Docker",
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [string]$SourceIdentity = $env:CPF_SOURCE_SHA,
    [string]$EvidenceDirectory = "",
    [switch]$IncludeIbmMq
)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$cpfRoot = Join-Path $DockerRoot "CPF"
$sourceRuntimeRoot = Join-Path $RepoRoot "cpf-tools/environment/docker-development-test"
if (-not (Test-Path -LiteralPath $sourceRuntimeRoot -PathType Container)) { throw "Canonical QA39 source runtime root is missing: $sourceRuntimeRoot" }
if ([string]::IsNullOrWhiteSpace($EvidenceDirectory)) { $EvidenceDirectory = Join-Path $cpfRoot "output/qa39-runtime" } else { $EvidenceDirectory = [IO.Path]::GetFullPath($EvidenceDirectory) }
New-Item -ItemType Directory -Path $EvidenceDirectory -Force | Out-Null
$failed = $false
try {
    $startArgs = @(
        "-NoProfile", "-File", (Join-Path $sourceRuntimeRoot "start-qa39-runtime.ps1"),
        "-DockerRoot", $DockerRoot, "-RepoRoot", $RepoRoot, "-EvidenceDirectory", $EvidenceDirectory
    )
    if ($IncludeIbmMq) { $startArgs += "-IncludeIbmMq" }
    & pwsh @startArgs
    if ($LASTEXITCODE -ne 0) { throw "기동 검증 실패(exit=$LASTEXITCODE)" }

    $faultSmoke = Join-Path $sourceRuntimeRoot "run-qa39-runtime-fault-smoke.ps1"
    & pwsh -NoProfile -File $faultSmoke -DockerRoot $DockerRoot -RepoRoot $RepoRoot -SourceIdentity $SourceIdentity -EvidenceDirectory $EvidenceDirectory
    if ($LASTEXITCODE -ne 0) { throw "장애·복구 Smoke 실패(exit=$LASTEXITCODE)" }
    Write-Host "CPF QA39 일회성 Runtime 검증 통과" -ForegroundColor Green
} catch {
    $failed = $true
    throw
} finally {
    $stopArgs = @(
        "-NoProfile", "-File", (Join-Path $sourceRuntimeRoot "stop-qa39-runtime.ps1"),
        "-DockerRoot", $DockerRoot
    )
    if ($IncludeIbmMq) { $stopArgs += "-IncludeIbmMq" }
    & pwsh @stopArgs
    if ($LASTEXITCODE -ne 0 -and -not $failed) { throw "검증 후 중지 실패(exit=$LASTEXITCODE)" }
}
