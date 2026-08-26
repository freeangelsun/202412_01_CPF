[CmdletBinding()]
param(
    [string]$DockerRoot = 'C:\dev\Docker'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Invoke-Docker {
    param([Parameter(Mandatory)][string[]]$Arguments)
    & docker @Arguments
    if ($LASTEXITCODE -ne 0) { throw "docker $($Arguments -join ' ') 실패(exit=$LASTEXITCODE)" }
}

function New-RandomSecret {
    param([int]$Bytes = 32)
    $buffer = New-Object byte[] $Bytes
    [Security.Cryptography.RandomNumberGenerator]::Fill($buffer)
    return [Convert]::ToBase64String($buffer).TrimEnd('=').Replace('+','A').Replace('/','B')
}

function Get-AdminPassword {
    $value = [Environment]::GetEnvironmentVariable('CPF_ADMIN_PASSWORD', 'Process')
    if (-not [string]::IsNullOrWhiteSpace($value)) { return $value }
    $secure = Read-Host 'CPF local Docker 관리자 비밀번호' -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}

$sourceRoot = $PSScriptRoot
$cpfRoot = Join-Path $DockerRoot 'CPF'
$secretRoot = Join-Path $DockerRoot 'Secrets'
$envPath = Join-Path $secretRoot 'cpf-runtime.env'

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw 'Docker CLI를 찾을 수 없습니다.' }
Invoke-Docker @('version')

$runningCpf = @(& docker ps --format '{{.Names}}' | Where-Object { $_ -like 'cpf-*' })
if ($runningCpf.Count -gt 0) {
    throw "설치 전 CPF Container를 정지해야 합니다. running=$($runningCpf -join ', ')"
}
New-Item -ItemType Directory -Path $cpfRoot -Force | Out-Null
New-Item -ItemType Directory -Path $secretRoot -Force | Out-Null

$baseRuntimeFiles = @(
    'compose.yml','compose.redis.yml','compose.kafka.yml','compose.integration.yml','compose.tooling.yml',
    'cpf-env.ps1','cpf-tooling.ps1','initialize-integration-fixtures.ps1','verify-complete-environment.ps1',
    'ensure-cpf-runtime-secrets.ps1','Dockerfile.full-toolchain','Dockerfile.sftp-fixture','sftp-entrypoint.sh',
    'toxiproxy.json','otel-collector-config.yml','CPF_도커_개발테스트환경_전체설치.ps1',
    'CPF_도커_확장연동환경_증분설치.ps1','CPF_도커_QA39_Runtime_증분설치.ps1'
)
foreach ($name in $baseRuntimeFiles) {
    $source = Join-Path $sourceRoot $name
    $destination = Join-Path $cpfRoot $name
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) { throw "필수 설치 Source가 없습니다: $source" }
    Copy-Item -LiteralPath $source -Destination $destination -Force
}
# Secrets and persistent volumes are operator-owned state. Installer reruns refresh executable/config files only.
# Secret files and named volumes are never deleted or overwritten by this installer.
$ownedFiles = @($baseRuntimeFiles)

if (-not (Test-Path -LiteralPath $envPath -PathType Leaf)) {
    $adminPassword = Get-AdminPassword
    if ([string]::IsNullOrWhiteSpace($adminPassword)) { throw 'CPF_ADMIN_PASSWORD가 비어 있습니다.' }
    $runtimeLines = @(
        "CPF_ADMIN_PASSWORD=$adminPassword",
        'CPF_SFTP_USER=cpf',
        'CPF_KEYCLOAK_ADMIN_USER=admin',
        'CPF_RABBITMQ_USER=cpf',
        'CPF_RABBITMQ_VHOST=/',
        'CPF_ARTEMIS_USER=cpf',
        'CPF_IBM_MQ_QMGR=QM1'
    )
    [IO.File]::WriteAllLines($envPath, $runtimeLines, [Text.UTF8Encoding]::new($false))
}
& (Join-Path $cpfRoot 'ensure-cpf-runtime-secrets.ps1') -SecretFile $envPath | Out-Null

$secretFiles = [ordered]@{
    'redis-password.txt' = $null
    'sftp-password.txt' = $null
    'vault-token.txt' = $null
    'keycloak-admin-password.txt' = $null
    'keycloak-test-password.txt' = $null
    'keycloak-service-client-secret.txt' = $null
}
foreach ($name in $secretFiles.Keys) {
    $path = Join-Path $secretRoot $name
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        [IO.File]::WriteAllText($path, (New-RandomSecret), [Text.UTF8Encoding]::new($false))
    }
    if ((Get-Item -LiteralPath $path).Length -le 0) { throw "비어 있는 Secret 파일: $path" }
}

$toolEnv = Join-Path $cpfRoot 'tool-images.env'
if (-not (Test-Path -LiteralPath $toolEnv -PathType Leaf)) {
    $trivy = [Environment]::GetEnvironmentVariable('CPF_TRIVY_IMAGE','Process')
    $ort = [Environment]::GetEnvironmentVariable('CPF_ORT_IMAGE','Process')
    if ([string]::IsNullOrWhiteSpace($trivy) -or [string]::IsNullOrWhiteSpace($ort)) {
        throw 'Fresh 설치에서 CPF_TRIVY_IMAGE와 CPF_ORT_IMAGE를 명시해야 합니다. 임의 latest tag는 허용하지 않습니다.'
    }
    $toolLines = @(
        'TOXIPROXY_IMAGE=ghcr.io/shopify/toxiproxy:2.12.0',
        'OTEL_COLLECTOR_IMAGE=otel/opentelemetry-collector-contrib:0.157.0',
        "TRIVY_IMAGE=$trivy",
        "ORT_IMAGE=$ort",
        'FULL_TOOLCHAIN_IMAGE=cpf-full-toolchain:java25-node22-pwsh7.6.4-playwright1.62.0',
        'WIREMOCK_IMAGE=wiremock/wiremock:3.13.2',
        'VAULT_IMAGE=hashicorp/vault:1.21.4',
        'KEYCLOAK_IMAGE=quay.io/keycloak/keycloak:26.6.1',
        'ALPINE_IMAGE=alpine:3.23.5',
        'SFTP_FIXTURE_IMAGE=cpf-sftp-fixture:alpine3.23'
    )
    [IO.File]::WriteAllLines($toolEnv, $toolLines, [Text.UTF8Encoding]::new($false))
}

$compose = @('compose','--project-name','cpf','--env-file',$envPath,'--env-file',$toolEnv)
foreach ($file in @('compose.yml','compose.redis.yml','compose.kafka.yml','compose.integration.yml','compose.tooling.yml')) {
    $compose += @('-f',(Join-Path $cpfRoot $file))
}
Invoke-Docker ($compose + @('config','--quiet'))

# 로컬 전용 Toolchain/SFTP 이미지는 현재 Workspace Dockerfile에서 Fresh build한다.
Invoke-Docker @('build','-f',(Join-Path $cpfRoot 'Dockerfile.full-toolchain'),'-t','cpf-full-toolchain:java25-node22-pwsh7.6.4-playwright1.62.0',$cpfRoot)
Invoke-Docker @('build','-f',(Join-Path $cpfRoot 'Dockerfile.sftp-fixture'),'--build-arg','ALPINE_IMAGE=alpine:3.23.5','-t','cpf-sftp-fixture:alpine3.23',$cpfRoot)

# 업무 Schema/Seed/Topic은 설치 단계에서 만들지 않는다. Container/Volume만 Created/Stopped로 준비한다.
Invoke-Docker ($compose + @('create'))
$managedContainers = @('cpf-mariadb','cpf-postgresql','cpf-oracle','cpf-redis','cpf-kafka','cpf-wiremock','cpf-sftp','cpf-vault','cpf-keycloak','cpf-toxiproxy','cpf-otel-collector')
foreach ($name in $managedContainers) {
    docker container inspect $name *> $null
    if ($LASTEXITCODE -eq 0) { Invoke-Docker @('update','--restart=no',$name) }
}

& (Join-Path $cpfRoot 'verify-complete-environment.ps1') -DockerRoot $DockerRoot -RequireStopped
Write-Host "CPF Docker 개발·테스트 전체 설치 완료. root=$DockerRoot copied=$($ownedFiles.Count)" -ForegroundColor Green
