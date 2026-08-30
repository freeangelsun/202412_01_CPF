[CmdletBinding()]
param(
    [string]$DockerRoot = 'C:\dev\Docker',
    [string]$AdminPassword,
    [switch]$SkipImagePull
)

<#
.SYNOPSIS
현재 CPF Working Tree를 기준으로 Docker 개발·테스트 전체 환경을 Created/Stopped 상태로 준비한다.

.DESCRIPTION
- Repository 안의 Compose/Runtime 파일은 매 실행마다 C:\dev\Docker\CPF로 강제 동기화한다.
- Repository 밖 Secret과 named volume은 기존 값을 보존한다.
- 실행 중인 CPF Container가 있으면 작업을 중단해 부분 덮어쓰기와 데이터 손상을 막는다.
- Host Docker/Compose는 특정 patch 버전을 요구하지 않고 실제 명령 capability로 판정한다.
- 업무 Schema/Seed/Topic은 생성하지 않는다. 설치의 완료 상태는 Container Created/Stopped이다.
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
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'
$javaUtf8Options = '-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8'
if ([string]::IsNullOrWhiteSpace($env:JAVA_TOOL_OPTIONS)) {
    $env:JAVA_TOOL_OPTIONS = $javaUtf8Options
} elseif ($env:JAVA_TOOL_OPTIONS -notmatch '-Dfile\.encoding=UTF-8') {
    $env:JAVA_TOOL_OPTIONS = "$($env:JAVA_TOOL_OPTIONS) $javaUtf8Options"
}

$sourceRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$cpfRoot = Join-Path $DockerRoot 'CPF'
$secretRoot = Join-Path $DockerRoot 'Secrets'
$toolEnvPath = Join-Path $cpfRoot 'tool-images.env'
$secretEnvPath = Join-Path $secretRoot 'cpf-runtime.env'

function Write-Step([string]$Message) { Write-Host "[CPF Docker] $Message" -ForegroundColor Cyan }
function Invoke-Docker([string[]]$Arguments) {
    & docker @Arguments
    if ($LASTEXITCODE -ne 0) { throw "docker $($Arguments -join ' ') 실패(exit=$LASTEXITCODE)" }
}
function New-CpfSecret([int]$Bytes = 32) {
    $buffer = New-Object byte[] $Bytes
    [Security.Cryptography.RandomNumberGenerator]::Fill($buffer)
    return ([Convert]::ToBase64String($buffer)).TrimEnd('=').Replace('+','A').Replace('/','B')
}
function Read-EnvMap([string]$Path) {
    $map = [ordered]@{}
    if (Test-Path -LiteralPath $Path -PathType Leaf) {
        foreach ($line in [IO.File]::ReadAllLines($Path, $utf8)) {
            if ($line -match '^\s*([A-Za-z_][A-Za-z0-9_]*)=(.*)$') { $map[$Matches[1]] = $Matches[2] }
        }
    }
    return $map
}
function Write-EnvMap([string]$Path, [System.Collections.IDictionary]$Map) {
    $lines = foreach ($key in $Map.Keys) { "$key=$($Map[$key])" }
    [IO.File]::WriteAllText($Path, (($lines -join "`n") + "`n"), $utf8)
}
function Ensure-SecretFile([string]$Name, [string]$Value) {
    $path = Join-Path $secretRoot $Name
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        [IO.File]::WriteAllText($path, ($Value + "`n"), $utf8)
    }
}

Write-Step '사전조건 확인'
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw 'Docker CLI가 설치되어 있지 않습니다.' }
& docker version --format '{{.Server.Version}}' *> $null
if ($LASTEXITCODE -ne 0) { throw 'Docker Engine에 연결할 수 없습니다. Docker Desktop/Engine 기동 상태를 확인하세요.' }
& docker compose version *> $null
if ($LASTEXITCODE -ne 0) { throw 'docker compose 기능을 사용할 수 없습니다.' }

$expectedContainers = @(
    'cpf-mariadb','cpf-postgresql','cpf-oracle','cpf-redis','cpf-kafka',
    'cpf-wiremock','cpf-sftp','cpf-vault','cpf-keycloak','cpf-toxiproxy','cpf-otel-collector'
)
$runningNames = @(docker ps --format '{{.Names}}')
$runningCpf = @($expectedContainers | Where-Object { $runningNames -contains $_ })
if ($runningCpf.Count -gt 0) {
    throw "기존 CPF Container가 실행 중입니다. 먼저 정지하세요: $($runningCpf -join ', ')"
}

New-Item -ItemType Directory -Force -Path $cpfRoot, $secretRoot | Out-Null

Write-Step '현재 Workspace Runtime 파일 동기화'
$baseRuntimeFiles = @(
    'compose.yml','compose.redis.yml','compose.kafka.yml','compose.integration.yml','compose.tooling.yml',
    'cpf-env.ps1','cpf-tooling.ps1','initialize-integration-fixtures.ps1','ensure-cpf-runtime-secrets.ps1',
    'verify-complete-environment.ps1','verify-clean-prepared.ps1',
    'Dockerfile.full-toolchain','Dockerfile.sftp-fixture','sftp-entrypoint.sh',
    'toxiproxy.json','otel-collector-config.yml'
)
foreach ($name in $baseRuntimeFiles) {
    $source = Join-Path $sourceRoot $name
    $destination = Join-Path $cpfRoot $name
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) { throw "Workspace Runtime 파일이 없습니다: $source" }
    Copy-Item -LiteralPath $source -Destination $destination -Force
}
# Secrets and persistent volumes are deliberately outside this owned-file refresh and are preserved.
$ownedFiles = @('fixtures')
foreach ($name in $ownedFiles) {
    $source = Join-Path $sourceRoot $name
    $destination = Join-Path $cpfRoot $name
    if (Test-Path -LiteralPath $destination) { Remove-Item -LiteralPath $destination -Recurse -Force }
    Copy-Item -LiteralPath $source -Destination $destination -Recurse -Force
}
New-Item -ItemType Directory -Force -Path (Join-Path $cpfRoot 'output\otel') | Out-Null

Write-Step 'Tool Image 계약 준비'
# Image version은 재현 가능한 개발환경을 위한 Container-side 계약이며 Host Docker/PowerShell 버전 고정과 별개다.
# 이미 tool-images.env가 있으면 운영자가 선택한 호환 Image override를 보존하고, 누락 Key만 canonical default로 채운다.
$toolImages = Read-EnvMap $toolEnvPath
$defaults = [ordered]@{
    TOXIPROXY_IMAGE = 'ghcr.io/shopify/toxiproxy:2.12.0'
    OTEL_COLLECTOR_IMAGE = 'otel/opentelemetry-collector-contrib:0.132.0'
    TRIVY_IMAGE = 'aquasec/trivy:0.66.0'
    ORT_IMAGE = 'ghcr.io/oss-review-toolkit/ort:latest'
    FULL_TOOLCHAIN_IMAGE = 'cpf-full-development-test-runner:current'
    WIREMOCK_IMAGE = 'wiremock/wiremock:3.13.1'
    VAULT_IMAGE = 'hashicorp/vault:1.20'
    KEYCLOAK_IMAGE = 'quay.io/keycloak/keycloak:26.3'
    ALPINE_IMAGE = 'alpine:3.23.5'
    SFTP_FIXTURE_IMAGE = 'cpf-sftp-fixture:current'
}
foreach ($key in $defaults.Keys) {
    if (-not $toolImages.Contains($key) -or [string]::IsNullOrWhiteSpace([string]$toolImages[$key])) { $toolImages[$key] = $defaults[$key] }
}
Write-EnvMap $toolEnvPath $toolImages

Write-Step 'Secret 준비(기존 값 보존)'
$envMap = Read-EnvMap $secretEnvPath
if (-not $envMap.Contains('CPF_ADMIN_PASSWORD') -or [string]::IsNullOrWhiteSpace([string]$envMap['CPF_ADMIN_PASSWORD'])) {
    $envMap['CPF_ADMIN_PASSWORD'] = $(if ([string]::IsNullOrWhiteSpace($AdminPassword)) { New-CpfSecret 32 } else { $AdminPassword })
}
$secretDefaults = [ordered]@{
    CPF_SFTP_USER = 'cpf'
    CPF_KEYCLOAK_ADMIN_USER = 'admin'
    CPF_KEYCLOAK_TEST_USER = 'cpf-test'
    CPF_KEYCLOAK_REALM = 'cpf-test'
    CPF_KEYCLOAK_PUBLIC_CLIENT = 'cpf-public'
    CPF_KEYCLOAK_SERVICE_CLIENT = 'cpf-service'
}
foreach ($key in $secretDefaults.Keys) {
    if (-not $envMap.Contains($key) -or [string]::IsNullOrWhiteSpace([string]$envMap[$key])) { $envMap[$key] = $secretDefaults[$key] }
}
Write-EnvMap $secretEnvPath $envMap
Ensure-SecretFile 'redis-password.txt' (New-CpfSecret 32)
Ensure-SecretFile 'sftp-password.txt' (New-CpfSecret 24)
Ensure-SecretFile 'vault-token.txt' (New-CpfSecret 32)
Ensure-SecretFile 'keycloak-admin-password.txt' (New-CpfSecret 32)
Ensure-SecretFile 'keycloak-test-password.txt' (New-CpfSecret 24)
Ensure-SecretFile 'keycloak-service-client-secret.txt' (New-CpfSecret 32)
& (Join-Path $cpfRoot 'ensure-cpf-runtime-secrets.ps1') -SecretFile $secretEnvPath | Out-Null

if (-not $SkipImagePull) {
    Write-Step '필수 Image 준비'
    $images = @(
        'mariadb:12.3.2','postgres:18.4-trixie','container-registry.oracle.com/database/free:26ai-free-23.26.2.0.0',
        'redis:8.8.1-trixie','apache/kafka:4.3.1','eclipse-temurin:25.0.3_9-jdk','node:22.18.0-bookworm',
        'mcr.microsoft.com/playwright:v1.62.0-noble',
        $toolImages['TOXIPROXY_IMAGE'],$toolImages['OTEL_COLLECTOR_IMAGE'],$toolImages['TRIVY_IMAGE'],$toolImages['ORT_IMAGE'],
        $toolImages['WIREMOCK_IMAGE'],$toolImages['VAULT_IMAGE'],$toolImages['KEYCLOAK_IMAGE'],$toolImages['ALPINE_IMAGE']
    )
    foreach ($image in $images) { Invoke-Docker @('pull',[string]$image) }
}

Write-Step 'CPF Local Tool Image 생성'
Invoke-Docker @('build','-f',(Join-Path $cpfRoot 'Dockerfile.sftp-fixture'),'--build-arg',"ALPINE_IMAGE=$($toolImages['ALPINE_IMAGE'])",'-t',[string]$toolImages['SFTP_FIXTURE_IMAGE'],$cpfRoot)
Invoke-Docker @('build','-f',(Join-Path $cpfRoot 'Dockerfile.full-toolchain'),'-t',[string]$toolImages['FULL_TOOLCHAIN_IMAGE'],$cpfRoot)

Write-Step 'Base·Integration·Tooling Container Created/Stopped 준비'
$compose = @('compose','--env-file',$secretEnvPath,'--env-file',$toolEnvPath)
foreach ($file in @('compose.yml','compose.redis.yml','compose.kafka.yml','compose.integration.yml','compose.tooling.yml')) {
    $compose += @('-f',(Join-Path $cpfRoot $file))
}
Invoke-Docker ($compose + @('create'))

Write-Step '설치 결과 최대 검증'
& (Join-Path $cpfRoot 'verify-complete-environment.ps1') -DockerRoot $DockerRoot -RequireStopped
if ($LASTEXITCODE -ne 0) { throw "전체환경 검증 실패(exit=$LASTEXITCODE)" }

Write-Host 'CPF Docker 개발·테스트 전체 환경 설치 완료 (Created/Stopped)' -ForegroundColor Green
Write-Host "Runtime Root: $cpfRoot"
Write-Host "Secret Root: $secretRoot"
Write-Host '업무 Schema/Seed/Topic은 생성하지 않았습니다.'
