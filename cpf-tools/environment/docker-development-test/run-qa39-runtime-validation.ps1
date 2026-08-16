[CmdletBinding()]
param(
    [string]$DockerRoot = "C:\dev\Docker",
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [string]$SourceIdentity = $env:CPF_SOURCE_SHA,
    [switch]$IncludeIbmMq
)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$cpfRoot = Join-Path $DockerRoot "CPF"
$failed = $false
try {
    $startArgs = @(
        "-NoProfile", "-File", (Join-Path $cpfRoot "start-qa39-runtime.ps1"),
        "-DockerRoot", $DockerRoot, "-RepoRoot", $RepoRoot
    )
    if ($IncludeIbmMq) { $startArgs += "-IncludeIbmMq" }
    & pwsh @startArgs
    if ($LASTEXITCODE -ne 0) { throw "기동 검증 실패(exit=$LASTEXITCODE)" }

    $faultSmoke = Join-Path $RepoRoot "cpf-tools/environment/docker-development-test/run-qa39-runtime-fault-smoke.ps1"
    & pwsh -NoProfile -File $faultSmoke -DockerRoot $DockerRoot -RepoRoot $RepoRoot -SourceIdentity $SourceIdentity
    if ($LASTEXITCODE -ne 0) { throw "장애·복구 Smoke 실패(exit=$LASTEXITCODE)" }
    Write-Host "CPF QA39 일회성 Runtime 검증 통과" -ForegroundColor Green
} catch {
    $failed = $true
    throw
} finally {
    $stopArgs = @(
        "-NoProfile", "-File", (Join-Path $cpfRoot "stop-qa39-runtime.ps1"),
        "-DockerRoot", $DockerRoot
    )
    if ($IncludeIbmMq) { $stopArgs += "-IncludeIbmMq" }
    & pwsh @stopArgs
    if ($LASTEXITCODE -ne 0 -and -not $failed) { throw "검증 후 중지 실패(exit=$LASTEXITCODE)" }
}
