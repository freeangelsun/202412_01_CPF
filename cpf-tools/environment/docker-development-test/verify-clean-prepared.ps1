param([string]$DockerRoot = "C:\dev\Docker")

$ErrorActionPreference = "Stop"
$scriptPath = Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Path) "verify-complete-environment.ps1"
if (-not (Test-Path -LiteralPath $scriptPath -PathType Leaf)) { throw "전체 환경 확인 Script가 없습니다: $scriptPath" }
& pwsh -NoProfile -File $scriptPath -DockerRoot $DockerRoot -RequireStopped
if ($LASTEXITCODE -ne 0) { throw "Clean Prepared 상태 확인 실패(exit=$LASTEXITCODE)" }
Write-Host "CPF Clean Prepared 전체 환경 확인 완료" -ForegroundColor Green
