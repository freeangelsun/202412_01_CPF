[CmdletBinding()]
param(
    [string]$DockerRoot = 'C:\dev\Docker',
    [switch]$SkipImagePull
)

<#
.SYNOPSIS
이미 준비된 CPF Base Docker 환경에 Integration/Tooling 서비스를 증분으로 추가한다.

.DESCRIPTION
Base DB/Redis/Kafka의 Container, Secret, Volume을 삭제하거나 재생성하지 않는다.
현재 Workspace의 Integration/Tooling-owned 파일만 동기화하고 WireMock/SFTP/Vault/Keycloak/
Toxiproxy/OTel Collector를 Created/Stopped 상태로 준비한다.
#>

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$utf8 = [Text.UTF8Encoding]::new($false)
try {
    [Console]::InputEncoding = $utf8
    [Console]::OutputEncoding = $utf8
    $OutputEncoding = $utf8
    $global:OutputEncoding = $utf8
} catch { }

$sourceRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$cpfRoot = Join-Path $DockerRoot 'CPF'
$secretRoot = Join-Path $DockerRoot 'Secrets'
$secretEnvPath = Join-Path $secretRoot 'cpf-runtime.env'
$toolEnvPath = Join-Path $cpfRoot 'tool-images.env'

function Write-Step([string]$Message) { Write-Host "[CPF Docker 증분] $Message" -ForegroundColor Cyan }
function Invoke-Docker([string[]]$Arguments) {
    & docker @Arguments
    if ($LASTEXITCODE -ne 0) { throw "docker $($Arguments -join ' ') 실패(exit=$LASTEXITCODE)" }
}
function New-CpfSecret([int]$Bytes = 32) {
    $buffer = New-Object byte[] $Bytes
    [Security.Cryptography.RandomNumberGenerator]::Fill($buffer)
    return ([Convert]::ToBase64String($buffer)).TrimEnd('=').Replace('+','A').Replace('/','B')
}
function Ensure-SecretFile([string]$Name, [string]$Value) {
    $path = Join-Path $secretRoot $Name
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { [IO.File]::WriteAllText($path, ($Value + "`n"), $utf8) }
}

Write-Step '사전조건 확인'
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw 'Docker CLI가 설치되어 있지 않습니다.' }
& docker compose version *> $null
if ($LASTEXITCODE -ne 0) { throw 'docker compose 기능을 사용할 수 없습니다.' }
if (-not (Test-Path -LiteralPath $cpfRoot -PathType Container)) { throw "CPF Base Docker 환경이 없습니다: $cpfRoot" }
if (-not (Test-Path -LiteralPath $secretEnvPath -PathType Leaf)) { throw "CPF Base Secret env가 없습니다: $secretEnvPath" }
if (-not (Test-Path -LiteralPath $toolEnvPath -PathType Leaf)) { throw "Tool Image 환경파일이 없습니다: $toolEnvPath" }

$baseContainers = @('cpf-mariadb','cpf-postgresql','cpf-oracle','cpf-redis','cpf-kafka')
$existing = @(docker ps -a --format '{{.Names}}')
$missingBase = @($baseContainers | Where-Object { $existing -notcontains $_ })
if ($missingBase.Count -gt 0) { throw "Base Container가 없습니다. 먼저 전체설치를 실행하세요: $($missingBase -join ', ')" }
$running = @(docker ps --format '{{.Names}}')
$runningCpf = @($baseContainers | Where-Object { $running -contains $_ })
if ($runningCpf.Count -gt 0) { throw "Base CPF Container가 실행 중입니다. 증분설치 전에 정지하세요: $($runningCpf -join ', ')" }

Write-Step 'Integration/Tooling Runtime 파일 동기화'
$incrementalFiles = @(
    'compose.integration.yml','compose.tooling.yml','cpf-tooling.ps1','initialize-integration-fixtures.ps1',
    'Dockerfile.sftp-fixture','sftp-entrypoint.sh','toxiproxy.json','otel-collector-config.yml'
)
foreach ($name in $incrementalFiles) {
    $source = Join-Path $sourceRoot $name
    $destination = Join-Path $cpfRoot $name
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) { throw "Workspace Runtime 파일이 없습니다: $source" }
    Copy-Item -LiteralPath $source -Destination $destination -Force
}
$fixtureSource = Join-Path $sourceRoot 'fixtures'
$fixtureDestination = Join-Path $cpfRoot 'fixtures'
if (Test-Path -LiteralPath $fixtureDestination) { Remove-Item -LiteralPath $fixtureDestination -Recurse -Force }
Copy-Item -LiteralPath $fixtureSource -Destination $fixtureDestination -Recurse -Force
New-Item -ItemType Directory -Force -Path (Join-Path $cpfRoot 'output\otel') | Out-Null

Write-Step '확장 Secret 보완(기존 값 보존)'
Ensure-SecretFile 'sftp-password.txt' (New-CpfSecret 24)
Ensure-SecretFile 'vault-token.txt' (New-CpfSecret 32)
Ensure-SecretFile 'keycloak-admin-password.txt' (New-CpfSecret 32)
Ensure-SecretFile 'keycloak-test-password.txt' (New-CpfSecret 24)
Ensure-SecretFile 'keycloak-service-client-secret.txt' (New-CpfSecret 32)

$toolImages = @{}
foreach ($line in [IO.File]::ReadAllLines($toolEnvPath, $utf8)) {
    if ($line -match '^\s*([A-Z0-9_]+)=(.+)$') { $toolImages[$Matches[1]] = $Matches[2] }
}
foreach ($key in @('WIREMOCK_IMAGE','VAULT_IMAGE','KEYCLOAK_IMAGE','ALPINE_IMAGE','SFTP_FIXTURE_IMAGE','TOXIPROXY_IMAGE','OTEL_COLLECTOR_IMAGE')) {
    if (-not $toolImages.ContainsKey($key) -or [string]::IsNullOrWhiteSpace([string]$toolImages[$key])) { throw "Tool Image 환경값이 없습니다: $key" }
}

if (-not $SkipImagePull) {
    Write-Step '확장 Image 준비'
    foreach ($key in @('WIREMOCK_IMAGE','VAULT_IMAGE','KEYCLOAK_IMAGE','ALPINE_IMAGE','TOXIPROXY_IMAGE','OTEL_COLLECTOR_IMAGE')) {
        Invoke-Docker @('pull',[string]$toolImages[$key])
    }
}
Write-Step 'SFTP Fixture Image 생성'
Invoke-Docker @('build','-f',(Join-Path $cpfRoot 'Dockerfile.sftp-fixture'),'--build-arg',"ALPINE_IMAGE=$($toolImages['ALPINE_IMAGE'])",'-t',[string]$toolImages['SFTP_FIXTURE_IMAGE'],$cpfRoot)

Write-Step 'Integration/Tooling Container Created/Stopped 준비'
$compose = @('compose','--env-file',$secretEnvPath,'--env-file',$toolEnvPath)
foreach ($file in @('compose.yml','compose.redis.yml','compose.kafka.yml','compose.integration.yml','compose.tooling.yml')) {
    $path = Join-Path $cpfRoot $file
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Base/Integration Compose 파일이 없습니다: $path" }
    $compose += @('-f',$path)
}
$services = @('wiremock','sftp','vault','keycloak','toxiproxy','otel-collector')
Invoke-Docker ($compose + @('create') + $services)

$existingAfter = @(docker ps -a --format '{{.Names}}')
$expected = @('cpf-wiremock','cpf-sftp','cpf-vault','cpf-keycloak','cpf-toxiproxy','cpf-otel-collector')
$missing = @($expected | Where-Object { $existingAfter -notcontains $_ })
if ($missing.Count -gt 0) { throw "증분설치 Container 누락: $($missing -join ', ')" }
$runningAfter = @(docker ps --format '{{.Names}}')
$unexpectedRunning = @($expected | Where-Object { $runningAfter -contains $_ })
if ($unexpectedRunning.Count -gt 0) { throw "증분설치 Container가 Created/Stopped가 아닙니다: $($unexpectedRunning -join ', ')" }

Write-Host 'CPF Docker 확장 연동환경 증분설치 완료 (Created/Stopped)' -ForegroundColor Green
