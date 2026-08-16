param(
    [string]$DockerRoot = "C:\dev\Docker",
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [string]$CommonPassword = "",
    [switch]$AcceptIbmMqDeveloperLicense
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Write-Utf8NoBom {
    param([Parameter(Mandatory)][string]$Path, [Parameter(Mandatory)][string]$Content)
    $parent = Split-Path -Parent $Path
    if ($parent) { New-Item -ItemType Directory -Force -Path $parent | Out-Null }
    [System.IO.File]::WriteAllText($Path, $Content, [System.Text.UTF8Encoding]::new($false))
}

function Read-Password {
    if (-not [string]::IsNullOrWhiteSpace($CommonPassword)) { return $CommonPassword }
    $secure = Read-Host -Prompt "CPF 로컬 Docker 공통 비밀번호" -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) } finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}

function Assert-Password {
    param([Parameter(Mandatory)][string]$Value)
    if ([string]::IsNullOrWhiteSpace($Value)) { throw "공통 비밀번호를 비워 둘 수 없습니다." }
    if ($Value.Contains("`r") -or $Value.Contains("`n")) { throw "공통 비밀번호에 줄바꿈을 사용할 수 없습니다." }
}

function Read-EnvMap {
    param([Parameter(Mandatory)][string]$Path)
    $map = [ordered]@{}
    if (Test-Path -LiteralPath $Path -PathType Leaf) {
        foreach ($line in Get-Content -LiteralPath $Path -Encoding UTF8) {
            if ($line -match '^(?<key>[A-Za-z_][A-Za-z0-9_]*)=(?<value>.*)$') { $map[$Matches.key] = $Matches.value }
        }
    }
    return $map
}

function Write-EnvMap {
    param([Parameter(Mandatory)][string]$Path, [Parameter(Mandatory)]$Map)
    $lines = foreach ($key in $Map.Keys) { "$key=$($Map[$key])" }
    Write-Utf8NoBom -Path $Path -Content (($lines -join "`n") + "`n")
}

function Invoke-Docker {
    param([Parameter(Mandatory)][string[]]$Arguments)
    & docker @Arguments
    if ($LASTEXITCODE -ne 0) { throw "docker $($Arguments -join ' ') 실패(exit=$LASTEXITCODE)" }
}

function Test-Image {
    param([Parameter(Mandatory)][string]$Reference)
    & docker image inspect $Reference *> $null
    return $LASTEXITCODE -eq 0
}

function Copy-RequiredFile {
    param([Parameter(Mandatory)][string]$Source, [Parameter(Mandatory)][string]$Destination)
    if (-not (Test-Path -LiteralPath $Source -PathType Leaf)) { throw "설치 파일이 없습니다: $Source" }
    $parent = Split-Path -Parent $Destination
    if ($parent) { New-Item -ItemType Directory -Path $parent -Force | Out-Null }
    Copy-Item -LiteralPath $Source -Destination $Destination -Force
}

if (-not (Test-Path -LiteralPath $RepoRoot -PathType Container)) { throw "Repository가 없습니다: $RepoRoot" }
docker version *> $null
if ($LASTEXITCODE -ne 0) { throw "Docker Desktop이 실행 중이 아닙니다." }
if ((& docker info --format "{{.OSType}}").Trim() -ne "linux") { throw "Linux Container Backend가 필요합니다." }
if ((& docker info --format "{{.Architecture}}").Trim() -notin @("x86_64", "amd64")) { throw "linux/amd64 환경이 필요합니다." }

$password = Read-Password
Assert-Password -Value $password
$sourceRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$cpfRoot = Join-Path $DockerRoot "CPF"
$secretRoot = Join-Path $DockerRoot "Secrets"
$runtimeEnvPath = Join-Path $secretRoot "cpf-runtime.env"
$toolEnvPath = Join-Path $cpfRoot "tool-images.env"
$providerEnvPath = Join-Path $cpfRoot "qa39-provider-images.env"

$requiredBaseFiles = @("compose.yml", "compose.redis.yml", "compose.kafka.yml", "compose.integration.yml", "compose.tooling.yml", "tool-images.env", "verify-complete-environment.ps1")
foreach ($name in $requiredBaseFiles) { if (-not (Test-Path -LiteralPath (Join-Path $cpfRoot $name) -PathType Leaf)) { throw "Base Docker 환경이 먼저 필요합니다: $name" } }
if (-not (Test-Path -LiteralPath $runtimeEnvPath -PathType Leaf)) { throw "Base Runtime 환경파일이 없습니다: $runtimeEnvPath" }

$ownedFiles = @(
    "compose.qa39-runtime.yml", "compose.integration.yml", "otel-collector-config.yml", "CPF_도커_QA39_Runtime_증분설치.ps1", "repair-qa39-runtime-r3.ps1",
    "start-qa39-runtime.ps1", "verify-qa39-runtime.ps1", "run-qa39-runtime-fault-smoke.ps1",
    "run-qa39-runtime-validation.ps1", "stop-qa39-runtime.ps1", "cleanup-qa39-runtime.ps1",
    "CPF_QA39_DOCKER_RUNTIME_MANIFEST.json"
)
foreach ($name in $ownedFiles) { Copy-RequiredFile -Source (Join-Path $sourceRoot $name) -Destination (Join-Path $cpfRoot $name) }

$allCpfContainers = @("cpf-mariadb", "cpf-postgresql", "cpf-oracle", "cpf-redis", "cpf-kafka", "cpf-wiremock", "cpf-sftp", "cpf-vault", "cpf-keycloak", "cpf-toxiproxy", "cpf-otel-collector", "cpf-rabbitmq", "cpf-artemis", "cpf-ibm-mq", "cpf-tcp-simulator", "cpf-mailpit")
$runningCpf = @(docker ps --format "{{.Names}}" | Where-Object { $_ -in $allCpfContainers })
if ($runningCpf.Count -gt 0) { throw "증분 설치 중에는 CPF Container가 정지 상태여야 합니다: $($runningCpf -join ', ')" }
foreach ($name in $allCpfContainers) { docker container inspect $name *> $null; if ($LASTEXITCODE -eq 0) { docker update --restart=no $name *> $null; if ($LASTEXITCODE -ne 0) { throw "Restart Policy 설정 실패: $name" } } }

New-Item -ItemType Directory -Path $secretRoot, (Join-Path $cpfRoot "output\qa39-runtime") -Force | Out-Null
foreach ($name in @("rabbitmq-password.txt", "artemis-password.txt", "mqAdminPassword", "mqAppPassword")) { Write-Utf8NoBom -Path (Join-Path $secretRoot $name) -Content "$password`n" }

$runtimeMap = Read-EnvMap -Path $runtimeEnvPath
$defaults = [ordered]@{ CPF_RABBITMQ_USER = "cpf-rabbit"; CPF_RABBITMQ_VHOST = "/cpf"; CPF_ARTEMIS_USER = "cpf-jms"; CPF_IBM_MQ_QMGR = "CPFQM1" }
foreach ($entry in $defaults.GetEnumerator()) { $runtimeMap[$entry.Key] = $entry.Value }
Write-EnvMap -Path $runtimeEnvPath -Map $runtimeMap

$images = [ordered]@{
    RABBITMQ_IMAGE = "rabbitmq:4.1.8-management"
    ARTEMIS_IMAGE = "apache/artemis:2.55.0"
    IBM_MQ_IMAGE = "icr.io/ibm-messaging/mq:9.4.5.1-r1"
    MAILPIT_IMAGE = "axllent/mailpit:v1.30.0"
    PYTHON_FIXTURE_IMAGE = "python:3.13.14-alpine3.24"
    IBM_MQ_ENABLED = $(if ($AcceptIbmMqDeveloperLicense) { "true" } else { "false" })
}
Write-EnvMap -Path $providerEnvPath -Map $images
foreach ($key in @("RABBITMQ_IMAGE", "ARTEMIS_IMAGE", "MAILPIT_IMAGE", "PYTHON_FIXTURE_IMAGE")) { $image = [string]$images[$key]; if (-not (Test-Image $image)) { Invoke-Docker @("pull", $image) } }
if ($AcceptIbmMqDeveloperLicense) { $image = [string]$images["IBM_MQ_IMAGE"]; if (-not (Test-Image $image)) { Invoke-Docker @("pull", $image) } }

$composeArgs = @("compose", "--project-name", "cpf", "--env-file", $runtimeEnvPath, "--env-file", $toolEnvPath, "--env-file", $providerEnvPath, "-f", (Join-Path $cpfRoot "compose.yml"), "-f", (Join-Path $cpfRoot "compose.redis.yml"), "-f", (Join-Path $cpfRoot "compose.kafka.yml"), "-f", (Join-Path $cpfRoot "compose.integration.yml"), "-f", (Join-Path $cpfRoot "compose.tooling.yml"), "-f", (Join-Path $cpfRoot "compose.qa39-runtime.yml"))
$profileArgs = if ($AcceptIbmMqDeveloperLicense) { @("--profile", "ibm-mq") } else { @() }
Invoke-Docker ($composeArgs + $profileArgs + @("config", "--quiet"))
Invoke-Docker ($composeArgs + $profileArgs + @("create", "rabbitmq", "artemis", "tcp-simulator", "mailpit"))
if ($AcceptIbmMqDeveloperLicense) { Invoke-Docker ($composeArgs + $profileArgs + @("create", "ibm-mq")) }
Invoke-Docker ($composeArgs + $profileArgs + @("create", "--force-recreate", "toxiproxy"))

$prepared = @("cpf-rabbitmq", "cpf-artemis", "cpf-tcp-simulator", "cpf-mailpit", "cpf-toxiproxy")
if ($AcceptIbmMqDeveloperLicense) { $prepared += "cpf-ibm-mq" }
foreach ($name in $prepared) { docker update --restart=no $name *> $null; if ($LASTEXITCODE -ne 0) { throw "Restart Policy 설정 실패: $name" } }

$verifyArgs = @("-NoProfile", "-File", (Join-Path $cpfRoot "verify-qa39-runtime.ps1"), "-DockerRoot", $DockerRoot, "-RepoRoot", $RepoRoot, "-RequireStopped")
if ($AcceptIbmMqDeveloperLicense) { $verifyArgs += "-IncludeIbmMq" }
& pwsh @verifyArgs
if ($LASTEXITCODE -ne 0) { throw "QA39 Runtime 준비 상태 검증 실패(exit=$LASTEXITCODE)" }
Write-Host "CPF QA39 Provider Runtime 증분 설치 완료 / Running: 0 / Restart Policy: no" -ForegroundColor Green
