[CmdletBinding()]
param(
    [string]$DockerRoot = 'C:\dev\Docker',
    [string]$RepoRoot = 'C:\dev\projects\jck\202412_01_CPF',
    [switch]$SkipPull,
    [switch]$SkipToolchainBuild
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
[Console]::InputEncoding = [Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = [Text.UTF8Encoding]::new($false)
$OutputEncoding = [Text.UTF8Encoding]::new($false)
$env:DOTNET_CLI_UI_LANGUAGE = 'en-US'
$env:JAVA_TOOL_OPTIONS = (($env:JAVA_TOOL_OPTIONS, '-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8') -join ' ').Trim()
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'
$env:NODE_OPTIONS = (($env:NODE_OPTIONS, '--no-warnings') -join ' ').Trim()

function Invoke-DockerChecked {
    param([Parameter(Mandatory)][string[]]$Arguments, [switch]$Quiet)
    if ($Quiet) { & docker @Arguments *> $null } else { & docker @Arguments }
    if ($LASTEXITCODE -ne 0) { throw "Docker 단계 실패(exit=$LASTEXITCODE). Secret 보호를 위해 인자는 출력하지 않습니다." }
}

function New-CpfSecretValue {
    param([int]$Bytes = 32)
    $buffer = New-Object byte[] $Bytes
    [Security.Cryptography.RandomNumberGenerator]::Fill($buffer)
    return [Convert]::ToHexString($buffer).ToLowerInvariant()
}

function Write-SecretIfMissing {
    param([Parameter(Mandatory)][string]$Path)
    if (Test-Path -LiteralPath $Path -PathType Leaf) {
        if ((Get-Item -LiteralPath $Path).Length -le 0) { throw "비어 있는 기존 Secret 파일: $Path" }
        return
    }
    [IO.File]::WriteAllText($Path, ((New-CpfSecretValue) + "`n"), [Text.UTF8Encoding]::new($false))
}

function Read-EnvMap {
    param([Parameter(Mandatory)][string]$Path)
    $map = [ordered]@{}
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) { return $map }
    foreach ($line in [IO.File]::ReadAllLines($Path, [Text.UTF8Encoding]::new($false))) {
        if ($line -match '^\s*([A-Za-z_][A-Za-z0-9_]*)=(.*)$') { $map[$Matches[1]] = $Matches[2] }
    }
    return $map
}

function Write-EnvMap {
    param([Parameter(Mandatory)][string]$Path, [Parameter(Mandatory)][System.Collections.IDictionary]$Map)
    $lines = @($Map.Keys | ForEach-Object { "$_=$($Map[$_])" })
    [IO.File]::WriteAllLines($Path, $lines, [Text.UTF8Encoding]::new($false))
}

if (-not (Test-Path -LiteralPath $RepoRoot -PathType Container)) { throw "Repository가 없습니다: $RepoRoot" }
Invoke-DockerChecked -Arguments @('version') -Quiet

$sourceRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$cpfRoot = Join-Path $DockerRoot 'CPF'
$secretRoot = Join-Path $DockerRoot 'Secrets'
New-Item -ItemType Directory -Force -Path $cpfRoot, $secretRoot | Out-Null

$managedContainers = @('cpf-mariadb','cpf-postgresql','cpf-oracle','cpf-redis','cpf-kafka','cpf-wiremock','cpf-sftp','cpf-vault','cpf-keycloak','cpf-toxiproxy','cpf-otel-collector')
$running = @(docker ps --format '{{.Names}}' | Where-Object { $_ -in $managedContainers })
if ($running.Count -gt 0) { throw "설치 전 CPF Container를 중지하세요: $($running -join ', ')" }

# Repository의 현재 Runtime Source를 매 설치마다 강제로 동기화한다. Destination 존재 여부로 skip하지 않는다.
$baseRuntimeFiles = @(
    'compose.yml','compose.redis.yml','compose.kafka.yml','compose.integration.yml','compose.tooling.yml',
    'cpf-env.ps1','cpf-tooling.ps1','initialize-integration-fixtures.ps1','ensure-cpf-runtime-secrets.ps1',
    'verify-complete-environment.ps1','run-full-toolchain.ps1','run-trivy.ps1','run-ort.ps1',
    'Dockerfile.full-toolchain','Dockerfile.sftp-fixture','sftp-entrypoint.sh','toxiproxy.json','otel-collector-config.yml',
    'CPF_도커_개발테스트환경_전체설치.ps1','CPF_도커_확장연동환경_증분설치.ps1',
    'compose.qa39-runtime.yml','CPF_도커_QA39_Runtime_증분설치.ps1','verify-qa39-runtime.ps1',
    'start-qa39-runtime.ps1','stop-qa39-runtime.ps1','cleanup-qa39-runtime.ps1','run-qa39-runtime-validation.ps1',
    'run-qa39-runtime-fault-smoke.ps1','repair-qa39-runtime-r3.ps1','CPF_QA39_DOCKER_RUNTIME_MANIFEST.json'
)
foreach ($name in $baseRuntimeFiles) {
    $source = Join-Path $sourceRoot $name
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) { throw "필수 Runtime Source가 없습니다: $source" }
    $destination = Join-Path $cpfRoot $name
    Copy-Item -LiteralPath $source -Destination $destination -Force
}
# Secrets and persistent volumes are intentionally preserved across idempotent reinstall.
$ownedFiles = @('fixtures')
foreach ($name in $ownedFiles) {
    $source = Join-Path $sourceRoot $name
    $destination = Join-Path $cpfRoot $name
    if (Test-Path -LiteralPath $destination) { Remove-Item -LiteralPath $destination -Recurse -Force }
    Copy-Item -LiteralPath $source -Destination $destination -Recurse -Force
}
New-Item -ItemType Directory -Force -Path (Join-Path $cpfRoot 'output\otel') | Out-Null

$runtimeEnv = Join-Path $secretRoot 'cpf-runtime.env'
$runtimeMap = Read-EnvMap -Path $runtimeEnv
$defaults = [ordered]@{
    CPF_SFTP_USER='cpfuser'
    CPF_KEYCLOAK_ADMIN_USER='cpfadmin'
    CPF_KEYCLOAK_TEST_USER='cpf-reviewer'
    CPF_KEYCLOAK_REALM='cpf-test'
    CPF_KEYCLOAK_PUBLIC_CLIENT='cpf-browser'
    CPF_KEYCLOAK_SERVICE_CLIENT='cpf-service'
    CPF_RABBITMQ_USER='cpf'
    CPF_RABBITMQ_VHOST='cpf'
    CPF_ARTEMIS_USER='cpf'
    CPF_IBM_MQ_QMGR='CPFQM1'
}
foreach ($key in $defaults.Keys) { if (-not $runtimeMap.Contains($key)) { $runtimeMap[$key] = $defaults[$key] } }
if (-not $runtimeMap.Contains('CPF_ADMIN_PASSWORD')) { $runtimeMap['CPF_ADMIN_PASSWORD'] = New-CpfSecretValue }
Write-EnvMap -Path $runtimeEnv -Map $runtimeMap
& (Join-Path $sourceRoot 'ensure-cpf-runtime-secrets.ps1') -SecretFile $runtimeEnv | Out-Null

foreach ($name in @('redis-password.txt','sftp-password.txt','vault-token.txt','keycloak-admin-password.txt','keycloak-test-password.txt','keycloak-service-client-secret.txt')) {
    Write-SecretIfMissing -Path (Join-Path $secretRoot $name)
}

$toolEnv = [ordered]@{
    TOXIPROXY_IMAGE='ghcr.io/shopify/toxiproxy:2.12.0'
    OTEL_COLLECTOR_IMAGE='otel/opentelemetry-collector-contrib:0.157.0'
    TRIVY_IMAGE='aquasec/trivy:0.67.2'
    ORT_IMAGE='ghcr.io/oss-review-toolkit/ort:69.0.0'
    FULL_TOOLCHAIN_IMAGE='cpf-full-development-test-runner:java25-node22.18-pwsh7.6.5-playwright1.62.0'
    WIREMOCK_IMAGE='wiremock/wiremock:3.13.1'
    VAULT_IMAGE='hashicorp/vault:1.20.4'
    KEYCLOAK_IMAGE='quay.io/keycloak/keycloak:26.3.3'
    ALPINE_IMAGE='alpine:3.22.1'
    SFTP_FIXTURE_IMAGE='cpf-sftp-fixture:current'
}
Write-EnvMap -Path (Join-Path $cpfRoot 'tool-images.env') -Map $toolEnv

if (-not $SkipPull) {
    $images = @(
        'mariadb:12.3.2','postgres:18.4-trixie','container-registry.oracle.com/database/free:26ai-free-23.26.2.0.0',
        'redis:8.8.1-trixie','apache/kafka:4.3.1','eclipse-temurin:25.0.3_9-jdk','node:22.18.0-bookworm',
        'mcr.microsoft.com/playwright:v1.62.0-noble',$toolEnv.TOXIPROXY_IMAGE,$toolEnv.OTEL_COLLECTOR_IMAGE,
        $toolEnv.TRIVY_IMAGE,$toolEnv.ORT_IMAGE,$toolEnv.WIREMOCK_IMAGE,$toolEnv.VAULT_IMAGE,$toolEnv.KEYCLOAK_IMAGE,$toolEnv.ALPINE_IMAGE
    )
    foreach ($image in $images) { Write-Host "Image 준비: $image"; Invoke-DockerChecked -Arguments @('pull',$image) }
}

Invoke-DockerChecked -Arguments @('build','-t',$toolEnv.SFTP_FIXTURE_IMAGE,'-f',(Join-Path $cpfRoot 'Dockerfile.sftp-fixture'),$cpfRoot)
if (-not $SkipToolchainBuild) {
    Invoke-DockerChecked -Arguments @('build','-t',$toolEnv.FULL_TOOLCHAIN_IMAGE,'-f',(Join-Path $cpfRoot 'Dockerfile.full-toolchain'),$cpfRoot)
}

& (Join-Path $sourceRoot 'CPF_도커_확장연동환경_증분설치.ps1') -DockerRoot $DockerRoot -RepoRoot $RepoRoot -SkipPull:$SkipPull
if ($LASTEXITCODE -ne 0) { throw "확장 연동 환경 설치 실패(exit=$LASTEXITCODE)" }
& (Join-Path $sourceRoot 'CPF_도커_QA39_Runtime_증분설치.ps1') -DockerRoot $DockerRoot -RepoRoot $RepoRoot -SkipPull:$SkipPull
if ($LASTEXITCODE -ne 0) { throw "QA39 Runtime 환경 설치 실패(exit=$LASTEXITCODE)" }

$compose = @('compose','--project-name','cpf','--env-file',$runtimeEnv,'--env-file',(Join-Path $cpfRoot 'tool-images.env'),'-f',(Join-Path $cpfRoot 'compose.yml'),'-f',(Join-Path $cpfRoot 'compose.redis.yml'),'-f',(Join-Path $cpfRoot 'compose.kafka.yml'),'-f',(Join-Path $cpfRoot 'compose.integration.yml'),'-f',(Join-Path $cpfRoot 'compose.tooling.yml'))
Invoke-DockerChecked -Arguments ($compose + @('config','--quiet'))
Invoke-DockerChecked -Arguments ($compose + @('create','--force-recreate','mariadb','postgresql','oracle','redis','kafka','wiremock','sftp','vault','keycloak','toxiproxy','otel-collector'))
foreach ($name in $managedContainers) { docker update --restart=no $name *> $null; if ($LASTEXITCODE -ne 0) { throw "Restart Policy 설정 실패: $name" } }

& (Join-Path $cpfRoot 'verify-complete-environment.ps1') -DockerRoot $DockerRoot -RequireStopped
if ($LASTEXITCODE -ne 0) { throw "CPF Docker 전체 환경 검증 실패(exit=$LASTEXITCODE)" }
Write-Host 'CPF Docker 개발·테스트 전체 환경 설치 완료 / Container Created-Stopped / restart=no' -ForegroundColor Green
